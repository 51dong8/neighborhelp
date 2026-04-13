package com.neighborhelp.dto;

import lombok.Data;

/**
 * 更新用户资料DTO
 */
@Data
public class UpdateProfileDTO {
    private String username;
    private String email;
    private String avatar;
    private String phone;
    private String wechat;
    private String address;
}

