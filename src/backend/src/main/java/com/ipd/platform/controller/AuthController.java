package com.ipd.platform.controller;

import com.ipd.platform.dto.*;
import com.ipd.platform.entity.SysUser;
import com.ipd.platform.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@Tag(name = "认证管理", description = "用户登录、注册、Token刷新")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        try {
            LoginResponse response = authService.login(request);
            return ResponseEntity.ok(ApiResponse.success("登录成功", response));
        } catch (BadCredentialsException | UsernameNotFoundException e) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.<LoginResponse>builder().code(401).message("用户名或密码错误").build());
        }
    }

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<SysUser>> register(@Valid @RequestBody RegisterRequest request) {
        try {
            SysUser user = authService.register(request);
            return ResponseEntity.ok(ApiResponse.success("注册成功", user));
        } catch (RuntimeException e) {
            String msg = e.getMessage();
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<SysUser>builder().code(400).message(msg != null ? msg : "注册失败").build());
        }
    }

    @Operation(summary = "刷新Token")
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refresh(@RequestHeader("Refresh-Token") String refreshToken) {
        try {
            LoginResponse response = authService.refreshToken(refreshToken);
            return ResponseEntity.ok(ApiResponse.success("刷新成功", response));
        } catch (RuntimeException e) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.<LoginResponse>builder().code(401).message("无效的刷新令牌").build());
        }
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/current")
    public ResponseEntity<ApiResponse<SysUser>> getCurrentUser() {
        SysUser user = authService.getCurrentUser(
                org.springframework.security.core.context.SecurityContextHolder.getContext()
                        .getAuthentication().getName());
        return ResponseEntity.ok(ApiResponse.success(user));
    }
}
