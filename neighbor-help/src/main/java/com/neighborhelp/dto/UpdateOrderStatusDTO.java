package com.neighborhelp.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import java.util.List;

/**
 * 更新订单状态DTO
 */
@Data
public class UpdateOrderStatusDTO {
    private String message;

    @Min(value = 0, message = "进度不能小于0")
    @Max(value = 100, message = "进度不能大于100")
    private Integer progress;

    private String reason;
    private List<String> images;
}

