package com.neighborhelp.controller;

import com.neighborhelp.common.Result;
import com.neighborhelp.dto.LoginDTO;
import com.neighborhelp.dto.RefreshTokenDTO;
import com.neighborhelp.dto.RegisterDTO;
import com.neighborhelp.service.AuthService;
import com.neighborhelp.service.UserService;
import com.neighborhelp.vo.LoginVO;
import com.neighborhelp.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import com.neighborhelp.dto.SendCodeDTO;
import com.neighborhelp.dto.ResetPasswordPhoneDTO;

/**
 * 认证控制器
 */
@Tag(name = "认证管理", description = "用户注册、登录、Token管理")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<UserVO> register(@Valid @RequestBody RegisterDTO registerDTO) {
        UserVO userVO = userService.register(registerDTO);
        return Result.success("注册成功", userVO);
    }

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        LoginVO loginVO = authService.login(loginDTO);
        return Result.success("登录成功", loginVO);
    }

    @Operation(summary = "刷新Token")
    @PostMapping("/refresh")
    public Result<LoginVO> refreshToken(@Valid @RequestBody RefreshTokenDTO refreshTokenDTO) {
        LoginVO loginVO = authService.refreshToken(refreshTokenDTO);
        return Result.success("刷新成功", loginVO);
    }

    @Operation(summary = "用户登出")
    @PostMapping("/logout")
    public Result<Void> logout(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody RefreshTokenDTO refreshTokenDTO) {
        String accessToken = null;
        if (StringUtils.hasText(authorization) && authorization.startsWith("Bearer ")) {
            accessToken = authorization.substring(7);
        }
        authService.logout(accessToken, refreshTokenDTO.getRefreshToken());
        return Result.<Void>success("登出成功");
    }
    @Operation(summary = "发送手机验证码")
    @PostMapping("/send-code")
    public Result<Void> sendCode(@Valid @RequestBody SendCodeDTO dto) {
        userService.sendSmsCode(dto.getPhone());
        return Result.success("验证码已发送，请注意查收");
    }

    @Operation(summary = "通过手机号重置密码")
    @PostMapping("/reset-password-phone")
    public Result<Void> resetPasswordByPhone(@Valid @RequestBody ResetPasswordPhoneDTO dto) {
        userService.resetPasswordByPhone(dto.getPhone(), dto.getCode(), dto.getNewPassword());
        return Result.success("密码重置成功，请重新登录");
    }

    // 新增一个接收邮箱验证码的接口
    @Operation(summary = "发送邮箱验证码")
    @PostMapping("/send-email-code")
    public Result<Void> sendEmailCode(@RequestBody java.util.Map<String, String> body) {
        String email = body.get("email");
        if (!org.springframework.util.StringUtils.hasText(email)) {
            return Result.error("邮箱不能为空");
        }
        userService.sendEmailCode(email);
        return Result.success("验证码已发送至您的邮箱");
    }
}

