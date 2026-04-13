package com.neighborhelp.controller;

import com.neighborhelp.common.Result;
import com.neighborhelp.dto.ChatRequestDTO;
import com.neighborhelp.service.impl.AssistantServiceImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/assistant")
public class AssistantController {

    @Autowired
    private AssistantServiceImpl assistantService;

    @PostMapping("/chat")
    public Result<String> chat(@RequestBody @Valid ChatRequestDTO requestDTO) {
        String reply = assistantService.chat(requestDTO.getMessage());
        return Result.success("获取成功", reply);
    }
}