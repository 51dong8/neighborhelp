package com.neighborhelp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 刷新Token DTO
 */
@Data
public class RefreshTokenDTO {
    @NotBlank(message = "刷新Token不能为空")
    private String refreshToken;
}

