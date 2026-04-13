package com.neighborhelp.service.impl;

import com.baidu.aip.ocr.AipOcr;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neighborhelp.common.ResultCode;
import com.neighborhelp.common.exception.BusinessException;
import com.neighborhelp.dto.ChangePasswordDTO;
import com.neighborhelp.dto.RegisterDTO;
import com.neighborhelp.dto.UpdateProfileDTO;
import com.neighborhelp.entity.Community;
import com.neighborhelp.entity.PointRecord;
import com.neighborhelp.entity.User;
import com.neighborhelp.mapper.CommunityMapper;
import com.neighborhelp.mapper.PointRecordMapper;
import com.neighborhelp.mapper.UserMapper;
import com.neighborhelp.service.UserService;
import com.neighborhelp.utils.PasswordUtil;
import com.neighborhelp.vo.PageVO;
import com.neighborhelp.vo.PointRecordVO;
import com.neighborhelp.vo.PointsVO;
import com.neighborhelp.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 用户服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final AipOcr aipOcr;
    private final CommunityMapper communityMapper;
    private final PasswordUtil passwordUtil;
    private final PointRecordMapper pointRecordMapper;
    private final StringRedisTemplate stringRedisTemplate;

    @Autowired
    private org.springframework.mail.javamail.JavaMailSender mailSender;

    @org.springframework.beans.factory.annotation.Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO register(RegisterDTO registerDTO) {
        // 检查用户名是否已存在
        if (findByUsername(registerDTO.getUsername()) != null) {
            throw new BusinessException(ResultCode.USERNAME_EXISTS);
        }

        // 检查邮箱是否已存在
        if (findByEmail(registerDTO.getEmail()) != null) {
            throw new BusinessException(ResultCode.EMAIL_EXISTS);
        }

        // 创建用户
        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setEmail(registerDTO.getEmail());
        user.setPasswordHash(passwordUtil.encode(registerDTO.getPassword()));
        user.setLevel(1);
        user.setPoints(0);
        user.setHelpCount(0);
        user.setRating(new BigDecimal("60.0"));
        user.setJoinDays(1);

        // ================== 【核心修复开始】 ==================
        // 明确新注册用户为“无社区”、“未认证”的游客状态。
        // 防止数据库由于默认约束（如 DEFAULT 1）将其自动归入星海社区
        user.setCommunityId(null);
        user.setAuthStatus(0); // 0: 未认证状态
        user.setAuthMaterial(null);
        // ================== 【核心修复结束】 ==================

        save(user);

        // 注册奖励积分
        addPoints(user.getId(), 50, "register", "注册奖励", null, null);

        return toVO(user);
    }

    @Override
    public User findByUsername(String username) {
        return baseMapper.selectByUsername(username);
    }

    @Override
    public User findByEmail(String email) {
        return baseMapper.selectByEmail(email);
    }

    @Override
    public UserVO getUserProfile(Long userId) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 计算加入天数
        if (user.getCreatedAt() != null) {
            long days = ChronoUnit.DAYS.between(user.getCreatedAt().toLocalDate(), LocalDate.now()) + 1;
            user.setJoinDays((int) days);
            if (days != user.getJoinDays()) {
                updateById(user);
            }
        }

        return toVO(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO updateUserProfile(Long userId, UpdateProfileDTO updateProfileDTO) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 如果更新用户名，检查是否重复
        if (updateProfileDTO.getUsername() != null && !updateProfileDTO.getUsername().equals(user.getUsername())) {
            if (findByUsername(updateProfileDTO.getUsername()) != null) {
                throw new BusinessException(ResultCode.USERNAME_EXISTS);
            }
            user.setUsername(updateProfileDTO.getUsername());
        }

        // 如果更新邮箱，检查是否重复
        if (updateProfileDTO.getEmail() != null && !updateProfileDTO.getEmail().equals(user.getEmail())) {
            if (findByEmail(updateProfileDTO.getEmail()) != null) {
                throw new BusinessException(ResultCode.EMAIL_EXISTS);
            }
            user.setEmail(updateProfileDTO.getEmail());
        }

        if (updateProfileDTO.getAvatar() != null) {
            user.setAvatar(updateProfileDTO.getAvatar());
        }
        if (updateProfileDTO.getPhone() != null) {
            user.setPhone(updateProfileDTO.getPhone());
        }
        if (updateProfileDTO.getWechat() != null) {
            user.setWechat(updateProfileDTO.getWechat());
        }
        if (updateProfileDTO.getAddress() != null) {
            user.setAddress(updateProfileDTO.getAddress());
        }

        updateById(user);
        return toVO(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(Long userId, ChangePasswordDTO changePasswordDTO) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 验证原密码
        if (!passwordUtil.matches(changePasswordDTO.getOldPassword(), user.getPasswordHash())) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }

        // 更新密码
        user.setPasswordHash(passwordUtil.encode(changePasswordDTO.getNewPassword()));
        updateById(user);
    }

    @Override
    public PointsVO getUserPoints(Long userId) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        PointsVO pointsVO = new PointsVO();
        pointsVO.setTotalPoints(user.getPoints());
        pointsVO.setAvailablePoints(user.getPoints());
        pointsVO.setFrozenPoints(0);
        pointsVO.setLevel(user.getLevel());
        // 计算下一级所需积分（简单规则：每级需要 level * 200 积分）
        pointsVO.setNextLevelPoints((user.getLevel() + 1) * 200);

        return pointsVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addPoints(Long userId, Integer amount, String type, String description, Long relatedId, String relatedType) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 更新用户积分
        user.setPoints(user.getPoints() + amount);
        // 计算等级（每200积分升一级）
        int newLevel = (user.getPoints() / 200) + 1;
        if (newLevel > user.getLevel()) {
            user.setLevel(newLevel);
        }
        updateById(user);

        // 记录积分变动
        PointRecord record = new PointRecord();
        record.setUserId(userId);
        record.setType(type);
        record.setAmount(amount);
        record.setDescription(description);
        record.setRelatedId(relatedId);
        record.setRelatedType(relatedType);
        pointRecordMapper.insert(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deductPoints(Long userId, Integer amount, String type, String description, Long relatedId, String relatedType) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        if (user.getPoints() < amount) {
            throw new BusinessException(ResultCode.POINTS_INSUFFICIENT);
        }

        // 更新用户积分
        user.setPoints(user.getPoints() - amount);
        updateById(user);

        // 记录积分变动
        PointRecord record = new PointRecord();
        record.setUserId(userId);
        record.setType(type);
        record.setAmount(-amount);
        record.setDescription(description);
        record.setRelatedId(relatedId);
        record.setRelatedType(relatedType);
        pointRecordMapper.insert(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void incrementHelpCount(Long userId) {
        User user = getById(userId);
        if (user != null) {
            user.setHelpCount(user.getHelpCount() + 1);
            updateById(user);
        }
    }

    @Override
    public PageVO<PointRecordVO> getPointRecords(Long userId, Integer page, Integer limit) {
        Page<PointRecord> pageParam = new Page<>(page, limit);
        LambdaQueryWrapper<PointRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PointRecord::getUserId, userId)
                .orderByDesc(PointRecord::getCreatedAt);

        IPage<PointRecord> pageResult = pointRecordMapper.selectPage(pageParam, wrapper);
        List<PointRecordVO> voList = pageResult.getRecords().stream()
                .map(record -> {
                    PointRecordVO vo = new PointRecordVO();
                    BeanUtils.copyProperties(record, vo);
                    return vo;
                })
                .collect(Collectors.toList());

        return new PageVO<>(voList, pageResult.getTotal(), page, limit);
    }

    @Override
    public UserVO toVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO vo = new UserVO();
        // 这一步会自动把 user 里的 authStatus 和 communityId 拷贝给 vo
        BeanUtils.copyProperties(user, vo);
        vo.setUserId(user.getId());

        // ================= 新增：查出小区名字 =================
        if (user.getCommunityId() != null) {
            com.neighborhelp.entity.Community community = communityMapper.selectById(user.getCommunityId());
            if (community != null) {
                vo.setCommunityName(community.getName());
            }
        }
        // =====================================================

        return vo;
    }
    // ================== 新增：手机号相关业务逻辑 ==================

    @Override
    public void sendSmsCode(String phone) {
        // 1. 防止频繁发送（比如 1 分钟内只能发一次）
        String redisKey = "sms:code:" + phone;
        Long expireTime = stringRedisTemplate.getExpire(redisKey, TimeUnit.SECONDS);
        if (expireTime != null && expireTime > 240) { // 如果剩余时间大于4分钟(即发出去不到1分钟)
            throw new BusinessException("验证码发送太频繁，请稍后再试");
        }

        // 2. 生成 6 位随机验证码
        String code = String.valueOf((int) ((Math.random() * 9 + 1) * 100000));

        // 3. 存入 Redis，设置 5 分钟有效 (300秒)
        stringRedisTemplate.opsForValue().set(redisKey, code, 5, TimeUnit.MINUTES);

        // 4. 模拟发送短信（实际项目中这里调用阿里云/腾讯云短信SDK）
        System.out.println("========================================");
        System.out.println("【邻里帮】发送给 " + phone + " 的验证码是：" + code + "，5分钟内有效。");
        System.out.println("========================================");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindPhone(Long userId, String phone, String code) {
        // 1. 校验验证码
        String redisKey = "sms:code:" + phone;
        String cachedCode = stringRedisTemplate.opsForValue().get(redisKey);

        if (cachedCode == null || !cachedCode.equals(code)) {
            throw new BusinessException("验证码错误或已过期");
        }

        // 2. 检查手机号是否已被其他账号绑定
        Long count = baseMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getPhone, phone));
        if (count > 0) {
            throw new BusinessException("该手机号已被其他账号绑定");
        }

        // 3. 更新用户手机号
        User user = getById(userId);
        user.setPhone(phone);
        updateById(user);

        // 4. 绑定成功后，立即让验证码失效，防止二次使用
        stringRedisTemplate.delete(redisKey);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPasswordByPhone(String phone, String code, String newPassword) {
        // 1. 校验验证码
        String redisKey = "sms:code:" + phone;
        String cachedCode = stringRedisTemplate.opsForValue().get(redisKey);

        if (cachedCode == null || !cachedCode.equals(code)) {
            throw new BusinessException("验证码错误或已过期");
        }

        // 2. 根据手机号查询用户
        User user = baseMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getPhone, phone));
        if (user == null) {
            throw new BusinessException("该手机号未注册");
        }

        // 3. 重置密码并加密保存
        user.setPasswordHash(passwordUtil.encode(newPassword));
        updateById(user);

        // 4. 清理验证码
        stringRedisTemplate.delete(redisKey);
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitAuthentication(Long userId, String communityName, String materialUrl) {
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        user.setAuthMaterial(materialUrl);

        // 【核心修改】：因为没有人工审核后台，所以只要提交，不论AI识别结果如何，强制设为通过(2)
        user.setAuthStatus(2);

        // 根据高德返回的名称去数据库查，没有则自动新建一个小区，确保外键不出错
        com.neighborhelp.entity.Community community = communityMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.neighborhelp.entity.Community>()
                        .eq(com.neighborhelp.entity.Community::getName, communityName)
        );

        if (community == null) {
            community = new com.neighborhelp.entity.Community();
            community.setName(communityName);
            communityMapper.insert(community);
        }
        user.setCommunityId(community.getId());

        // 尝试调用百度 AI OCR (保留代码仅作控制台日志记录，不再把状态降级为1)
        try {
            log.info("开始进行 AI 自动化审核，用户ID: {}, 目标小区: {}", userId, communityName);

            byte[] imgData = cn.hutool.http.HttpUtil.downloadBytes(materialUrl);
            org.json.JSONObject res = aipOcr.basicGeneral(imgData, new java.util.HashMap<>());
            org.json.JSONArray wordsResult = res.optJSONArray("words_result");

            boolean isMatch = false;
            if (wordsResult != null) {
                for (int i = 0; i < wordsResult.length(); i++) {
                    String text = wordsResult.getJSONObject(i).getString("words");
                    if (text.contains(communityName)) {
                        isMatch = true;
                        break;
                    }
                }
            }

            if (isMatch) {
                log.info("AI 自动化审核匹配成功！用户ID: {}", userId);
            } else {
                log.info("AI 未能识别出小区名称，但由于无人工后台，直接自动放行。用户ID: {}", userId);
            }
        } catch (Exception e) {
            log.error("AI OCR 识别异常，无人工后台，直接自动放行。原因: {}", e.getMessage());
        }

        // 保存更新
        this.updateById(user);
    }


    @Override
    public void sendEmailCode(String email) {
        // 1. 防止频繁发送（1分钟限流）
        String redisKey = "email:code:" + email;
        Long expireTime = stringRedisTemplate.getExpire(redisKey, TimeUnit.SECONDS);
        if (expireTime != null && expireTime > 240) {
            throw new BusinessException("验证码发送太频繁，请稍后再试");
        }

        // 2. 生成 6 位随机验证码
        String code = String.valueOf((int) ((Math.random() * 9 + 1) * 100000));

        // 3. 存入 Redis，设置 5 分钟有效
        stringRedisTemplate.opsForValue().set(redisKey, code, 5, TimeUnit.MINUTES);

        // ================= 4. 真实发送邮件 =================
        try {
            org.springframework.mail.SimpleMailMessage message = new org.springframework.mail.SimpleMailMessage();
            message.setFrom(fromEmail); // 必须和 yml 里配置的 username 一致
            message.setTo(email);       // 收件人（用户填写的邮箱）
            message.setSubject("【邻里帮】账号注册验证码");
            message.setText("欢迎注册邻里帮社区服务平台！\n\n您本次的注册验证码是：【 " + code + " 】。\n有效期为 5 分钟，请勿泄露给他人。");

            mailSender.send(message);
            log.info("真实邮件已成功发送至: {}", email);
        } catch (Exception e) {
            // 如果邮件发送失败（比如邮箱填错了、授权码不对），必须把 Redis 里的验证码删掉，否则用户要等5分钟才能重试
            stringRedisTemplate.delete(redisKey);
            log.error("邮件发送失败: {}", e.getMessage());
            throw new BusinessException("邮件发送失败，请检查邮箱地址是否正确");
        }
        // ====================================================
    }
}

