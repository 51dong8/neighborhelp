package com.neighborhelp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neighborhelp.common.ResultCode;
import com.neighborhelp.common.exception.BusinessException;
import com.neighborhelp.dto.CreateRequestDTO;
import com.neighborhelp.entity.Request;
import com.neighborhelp.entity.User;
import com.neighborhelp.mapper.RequestMapper;
import com.neighborhelp.service.AmapService;
import com.neighborhelp.service.RequestService;
import com.neighborhelp.service.UserService;
import com.neighborhelp.service.support.RequestStatusRules;
import com.neighborhelp.vo.PageVO;
import com.neighborhelp.vo.RequestVO;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 需求服务实现
 */
@Service
@RequiredArgsConstructor
public class RequestServiceImpl extends ServiceImpl<RequestMapper, Request> implements RequestService {

    private final UserService userService;
    private final ObjectMapper objectMapper;
    private final AmapService amapService;

    // ================= 新增注入 Redis =================
    private final StringRedisTemplate stringRedisTemplate;

    // 定义 Redis Geo 的 Key
    private static final String GEO_KEY = "neighbor:request:geo";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RequestVO createRequest(Long userId, CreateRequestDTO createRequestDTO) {
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        // ================= 新增：门禁与社区安全校验 =================
        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "用户不存在");
        }
        // 1. 检查是否已经选择了所在小区
        if (user.getCommunityId() == null) {
            throw new BusinessException(ResultCode.FORBIDDEN, "请先选择您所在的小区");
        }
        // 2. 检查是否完成业主认证 (假设 2 代表已认证通过)
        if (user.getAuthStatus() == null || user.getAuthStatus() != 2) {
            throw new BusinessException(ResultCode.FORBIDDEN, "为了社区安全，请先完成本小区的业主身份认证后再发布需求");
        }
        // =======================================================

        Request request = new Request();
        request.setUserId(userId);

        // ================= 新增：数据隔离，绑定需求到当前小区 =================
        request.setCommunityId(user.getCommunityId());
        // ====================================================================

        request.setServiceType(createRequestDTO.getServiceType());
        request.setTitle(createRequestDTO.getTitle());
        request.setDescription(createRequestDTO.getDescription());
        request.setRewardType(createRequestDTO.getRewardType());
        request.setRewardAmount(createRequestDTO.getRewardAmount());

        if (createRequestDTO.getExpectedTime() != null) {
            request.setExpectedTime(createRequestDTO.getExpectedTime());
        } else {
            request.setExpectedTime(java.time.LocalDateTime.now().plusHours(2));
        }
        request.setUrgency(createRequestDTO.getUrgency());
        request.setStatus("active");

        // 处理高德真实位置
        String finalLocation = createRequestDTO.getLocation();

        if (createRequestDTO.getLongitude() != null && createRequestDTO.getLatitude() != null) {
            String realAddress = amapService.getRealAddress(
                    createRequestDTO.getLongitude(),
                    createRequestDTO.getLatitude()
            );

            if (realAddress != null && !realAddress.isEmpty()) {
                finalLocation = realAddress + (finalLocation != null && !finalLocation.isEmpty() ? " " + finalLocation : "");
            }
        }
        request.setLocation(finalLocation);

        if (createRequestDTO.getServiceDetails() != null) {
            try {
                createRequestDTO.getServiceDetails().put("longitude", createRequestDTO.getLongitude());
                createRequestDTO.getServiceDetails().put("latitude", createRequestDTO.getLatitude());

                request.setServiceDetails(objectMapper.writeValueAsString(createRequestDTO.getServiceDetails()));
            } catch (Exception e) {
                throw new BusinessException("服务详情格式错误");
            }
        }

        save(request);

        // ================= 保持原逻辑：将经纬度写入 Redis GEO =================
        if (createRequestDTO.getLongitude() != null && createRequestDTO.getLatitude() != null) {
            // 注意：如果你使用的 Spring Data Redis，确保导入的是 org.springframework.data.geo.Point
            Point point = new Point(createRequestDTO.getLongitude(), createRequestDTO.getLatitude());
            // GEOADD key longitude latitude member (把需求ID作为member存进去)
            stringRedisTemplate.opsForGeo().add(GEO_KEY, point, request.getId().toString());
        }
        // ===============================================================

        // 保持原逻辑：增加积分
        userService.addPoints(userId, 50, "publish_request", "发布需求奖励", request.getId(), "request");

        return toVO(request);
    }

    @Override
    public PageVO<RequestVO> getRequestList(String serviceType, String status, Integer page, Integer limit, String sort) {
        Page<Request> pageParam = new Page<>(page, limit);
        LambdaQueryWrapper<Request> wrapper = new LambdaQueryWrapper<>();

        if (serviceType != null && !serviceType.isEmpty()) {
            wrapper.eq(Request::getServiceType, serviceType);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Request::getStatus, status);
        }

        // 排序
        if ("reward".equals(sort)) {
            wrapper.orderByDesc(Request::getRewardAmount);
        } else if ("urgency".equals(sort)) {
            wrapper.orderByDesc(Request::getUrgency);
        } else {
            // 默认 latest
            wrapper.orderByDesc(Request::getCreatedAt);
        }

        IPage<Request> pageResult = page(pageParam, wrapper);
        List<RequestVO> voList = pageResult.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList());

        return new PageVO<>(voList, pageResult.getTotal(), page, limit);
    }

    @Override
    public RequestVO getRequestDetail(Long requestId) {
        Request request = getById(requestId);
        if (request == null) {
            throw new BusinessException(ResultCode.REQUEST_NOT_FOUND);
        }
        return toVO(request);
    }

    @Override
    public PageVO<RequestVO> getUserRequests(Long userId, String status, Integer page, Integer limit) {
        Page<Request> pageParam = new Page<>(page, limit);
        LambdaQueryWrapper<Request> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Request::getUserId, userId);

        if (status != null && !status.isEmpty()) {
            wrapper.eq(Request::getStatus, status);
        }

        wrapper.orderByDesc(Request::getCreatedAt);

        IPage<Request> pageResult = page(pageParam, wrapper);
        List<RequestVO> voList = pageResult.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList());

        return new PageVO<>(voList, pageResult.getTotal(), page, limit);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRequestStatus(Long requestId, Long operatorUserId, String status, String reason) {
        Request request = getById(requestId);
        if (request == null) {
            throw new BusinessException(ResultCode.REQUEST_NOT_FOUND);
        }

        if (operatorUserId == null || !operatorUserId.equals(request.getUserId())) {
            throw new BusinessException(ResultCode.RESOURCE_FORBIDDEN);
        }

        RequestStatusRules.validateManualStatusTransition(request, status);

        request.setStatus(status.trim().toLowerCase());
        updateById(request);

        // ================= 新增：如果订单取消或完成，从地图上移除 =================
        if ("cancelled".equals(request.getStatus()) || "completed".equals(request.getStatus())) {
            stringRedisTemplate.opsForGeo().remove(GEO_KEY, requestId.toString());
        }
        // ===============================================================

        if ("cancelled".equals(request.getStatus())) {
            userService.deductPoints(request.getUserId(), 10, "cancel_request", "取消需求", requestId, "request");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRequest(Long requestId, Long operatorUserId) {
        Request request = getById(requestId);
        if (request == null) {
            throw new BusinessException(ResultCode.REQUEST_NOT_FOUND);
        }

        if (operatorUserId == null || !operatorUserId.equals(request.getUserId())) {
            throw new BusinessException(ResultCode.RESOURCE_FORBIDDEN);
        }

        RequestStatusRules.assertDeletable(request);

        removeById(requestId);

        // ================= 新增：从 Redis 中同步删除位置信息 =================
        stringRedisTemplate.opsForGeo().remove(GEO_KEY, requestId.toString());
        // ===============================================================
    }

    // ================= 新增：获取附近需求 (融合社区隔离与游客脱敏) =================
    @Override
    public List<RequestVO> getNearbyRequests(Double longitude, Double latitude, Double radiusKm, Long currentUserId) {
        if (longitude == null || latitude == null || currentUserId == null) {
            return Collections.emptyList();
        }

        // 1. 获取当前查询用户的信息（用于社区隔离和脱敏判断）
        User currentUser = userService.getById(currentUserId);
        if (currentUser == null) {
            return Collections.emptyList();
        }
        Long userCommunityId = currentUser.getCommunityId();
        Integer authStatus = currentUser.getAuthStatus();

        // 2. 设置查询中心点和半径
        Point center = new Point(longitude, latitude);
        Distance radius = new Distance(radiusKm, RedisGeoCommands.DistanceUnit.KILOMETERS);
        Circle circle = new Circle(center, radius);

        // 3. 设置 Geo 参数（要求返回距离，且按由近到远排序）
        RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs()
                .includeDistance()
                .sortAscending();

        // 4. 从 Redis 查询附近的 Request ID
        GeoResults<RedisGeoCommands.GeoLocation<String>> results = stringRedisTemplate.opsForGeo()
                .radius(GEO_KEY, circle, args);

        if (results == null || results.getContent().isEmpty()) {
            return Collections.emptyList();
        }

        List<RequestVO> nearbyList = new ArrayList<>();

        // 5. 解析结果，查询 MySQL 并组装 VO，同时应用安全策略
        for (GeoResult<RedisGeoCommands.GeoLocation<String>> result : results) {
            // 获取 Redis 中的 requestId 和 距离
            String requestIdStr = result.getContent().getName();
            Double distance = result.getDistance().getValue();

            // 去数据库查询具体详情
            Request request = getById(Long.valueOf(requestIdStr));

            if (request != null && "active".equals(request.getStatus())) {

                // =============== 核心新增：社区数据隔离 ===============
                // 如果用户已经绑定了小区，则在结果中只展示本小区的需求（强化社区边界感）
                // 如果用户还未绑定小区（纯游客态），则不拦截，允许看到附近的打码需求以吸引注册
                if (userCommunityId != null && !userCommunityId.equals(request.getCommunityId())) {
                    continue; // 跨小区的数据直接跳过，不返回给前端
                }
                // ===============================================

                RequestVO vo = toVO(request);
                // 设置并保留 2 位小数，如 1.25 km
                vo.setDistance(Math.round(distance * 100.0) / 100.0);

                // =============== 游客数据脱敏策略 ===============
                if (authStatus == null || authStatus != 2) {
                    // 修复报错点：通过嵌套的 publisher 对象获取 username 并脱敏
                    if (vo.getPublisher() != null && vo.getPublisher().getUsername() != null && !vo.getPublisher().getUsername().isEmpty()) {
                        String originalName = vo.getPublisher().getUsername();
                        vo.getPublisher().setUsername(originalName.substring(0, 1) + "** (认证后可见)");
                    }

                    // 隐藏具体地址
                    vo.setLocation("社区内部 (认证业主可见详情)");

                    // 如果还要隐藏电话，也可以在这里加（看你 UserVO 里有 phone 字段）
                    if (vo.getPublisher() != null && vo.getPublisher().getPhone() != null) {
                        vo.getPublisher().setPhone("***********");
                    }
                }
                // =======================================================

                nearbyList.add(vo);

            } else if (request == null || !"active".equals(request.getStatus())) {
                // 如果数据库里不存在或不再活跃，说明 Redis 是脏数据，顺手清理
                stringRedisTemplate.opsForGeo().remove(GEO_KEY, requestIdStr);
            }
        }

        return nearbyList;
    }
    // ===============================================================
    @Override
    public RequestVO toVO(Request request) {
        if (request == null) {
            return null;
        }

        RequestVO vo = new RequestVO();
        BeanUtils.copyProperties(request, vo);
        vo.setId(request.getId());

        // 获取发布者信息
        if (request.getUserId() != null) {
            User publisher = userService.getById(request.getUserId());
            if (publisher != null) {
                vo.setPublisher(userService.toVO(publisher));
            }
        }

        // 获取接单者信息
        if (request.getAcceptedBy() != null) {
            User acceptedBy = userService.getById(request.getAcceptedBy());
            if (acceptedBy != null) {
                vo.setAcceptedBy(userService.toVO(acceptedBy));
            }
        }

        // 解析服务详情
        if (request.getServiceDetails() != null && !request.getServiceDetails().isEmpty()) {
            try {
                Map<String, Object> details = objectMapper.readValue(
                        request.getServiceDetails(),
                        new TypeReference<Map<String, Object>>() {}
                );
                vo.setServiceDetails(details);
            } catch (Exception e) {
                vo.setServiceDetails(new HashMap<>());
            }
        }

        return vo;
    }
}