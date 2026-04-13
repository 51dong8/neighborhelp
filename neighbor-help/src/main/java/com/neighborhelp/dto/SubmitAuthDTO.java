package com.neighborhelp.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class SubmitAuthDTO {
    // 改为接收高德传过来的字符串
    @NotBlank(message = "小区名称不能为空")
    private String communityName;

    @NotBlank(message = "请先上传物业单、租房合同或门禁卡照片")
    private String materialUrl;
}