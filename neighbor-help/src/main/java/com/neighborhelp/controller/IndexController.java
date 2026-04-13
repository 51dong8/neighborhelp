package com.neighborhelp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 首页控制器
 * 处理根路径访问
 */
@Tag(name = "首页", description = "应用首页和API文档入口")
@Controller
public class IndexController {

    @Operation(summary = "首页重定向到Swagger UI")
    @GetMapping("/")
    public String index() {
        return "redirect:/swagger-ui/index.html";
    }
}

