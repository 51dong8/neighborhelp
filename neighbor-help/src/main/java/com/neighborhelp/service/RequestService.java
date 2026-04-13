package com.neighborhelp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.neighborhelp.dto.CreateRequestDTO;
import com.neighborhelp.entity.Request;
import com.neighborhelp.vo.PageVO;
import com.neighborhelp.vo.RequestVO;

import java.util.List;

/**
 * 需求服务接口
 */
public interface RequestService extends IService<Request> {
    /**
     * 创建需求
     */
    RequestVO createRequest(Long userId, CreateRequestDTO createRequestDTO);

    /**
     * 获取需求列表
     */
    PageVO<RequestVO> getRequestList(String serviceType, String status, Integer page, Integer limit, String sort);

    /**
     * 获取需求详情
     */
    RequestVO getRequestDetail(Long requestId);

    /**
     * 获取用户发布的需求
     */
    PageVO<RequestVO> getUserRequests(Long userId, String status, Integer page, Integer limit);

    /**
     * 更新需求状态
     */
    void updateRequestStatus(Long requestId, Long operatorUserId, String status, String reason);

    /**
     * 删除需求
     */
    void deleteRequest(Long requestId, Long operatorUserId);

    /**
     * 实体转VO
     */
    RequestVO toVO(Request request);

    // 新增方法：搜索附近的需求
    List<RequestVO> getNearbyRequests(Double longitude, Double latitude, Double radiusKm, Long currentUserId);
}

