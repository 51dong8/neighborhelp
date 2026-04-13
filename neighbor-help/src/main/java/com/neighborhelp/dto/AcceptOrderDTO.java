package com.neighborhelp.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 接单DTO
 */
@Data
public class AcceptOrderDTO {
    @NotNull(message = "需求ID不能为空")
    private Long requestId;

    private String message;
}

