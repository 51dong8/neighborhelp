package com.neighborhelp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.neighborhelp.dto.ChangePasswordDTO;
import com.neighborhelp.dto.RegisterDTO;
import com.neighborhelp.dto.UpdateProfileDTO;
import com.neighborhelp.entity.User;
import com.neighborhelp.vo.PageVO;
import com.neighborhelp.vo.PointRecordVO;
import com.neighborhelp.vo.PointsVO;
import com.neighborhelp.vo.UserVO;

/**
 * 用户服务接口
 */
public interface UserService extends IService<User> {
    /**
     * 注册
     */
    UserVO register(RegisterDTO registerDTO);

    /**
     * 根据用户名查找用户
     */
    User findByUsername(String username);

    /**
     * 根据邮箱查找用户
     */
    User findByEmail(String email);

    /**
     * 获取用户信息
     */
    UserVO getUserProfile(Long userId);

    /**
     * 更新用户信息
     */
    UserVO updateUserProfile(Long userId, UpdateProfileDTO updateProfileDTO);

    /**
     * 修改密码
     */
    void changePassword(Long userId, ChangePasswordDTO changePasswordDTO);

    /**
     * 获取用户积分信息
     */
    PointsVO getUserPoints(Long userId);

    /**
     * 增加积分
     */
    void addPoints(Long userId, Integer amount, String type, String description, Long relatedId, String relatedType);

    /**
     * 减少积分
     */
    void deductPoints(Long userId, Integer amount, String type, String description, Long relatedId, String relatedType);

    /**
     * 获取积分记录
     */
    PageVO<PointRecordVO> getPointRecords(Long userId, Integer page, Integer limit);

    /**
     * 增加帮助次数
     */
    void incrementHelpCount(Long userId);

    /**
     * 实体转VO
     */
    UserVO toVO(User user);
    // ... 保留原有的方法 ...

    /**
     * 发送手机验证码
     */
    void sendSmsCode(String phone);

    /**
     * 绑定手机号
     */
    void bindPhone(Long userId, String phone, String code);

    /**
     * 通过手机号和验证码重置密码
     */
    void resetPasswordByPhone(String phone, String code, String newPassword);

    /**
     * 提交业主认证材料（AI 自动化审核）
     * @param userId 用户ID
     * @param materialUrl 认证材料(图片)的OSS地址
     */
// 在 UserService 接口中更新这个方法
    void submitAuthentication(Long userId, String communityName, String materialUrl);

    // 发送邮箱验证码
    void sendEmailCode(String email);
}

