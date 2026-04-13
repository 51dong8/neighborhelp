package com.neighborhelp.dto;

import lombok.Data;

@Data
public class CreateFeedbackDTO {
    private String type;
    private String content;
    private String contact;
}