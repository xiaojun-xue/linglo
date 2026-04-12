package com.ipd.platform.controller;

import com.ipd.platform.dto.ApiResponse;
import com.ipd.platform.entity.SysUser;
import com.ipd.platform.repository.SysUserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户CRUD管理")
public class UserController {

    private final SysUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Operation(summary = "获取用户列表")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PGM')")
    public ResponseEntity<ApiResponse<Page<SysUser>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword) {
        
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<SysUser> users = userRepository.findAll(pageRequest);
        
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @Operation(summary = "获取用户详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PGM') or #id == authentication.principal.id")
    public ResponseEntity<ApiResponse<SysUser>> getById(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(user -> ResponseEntity.ok(ApiResponse.success(user)))
                .orElse(ResponseEntity.status(404).body(ApiResponse.notFound("用户不存在")));
    }

    @Operation(summary = "创建用户")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SysUser>> create(@RequestBody SysUser user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            return ResponseEntity.badRequest().body(ApiResponse.badRequest("用户名已存在"));
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        SysUser saved = userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.success("创建成功", saved));
    }

    @Operation(summary = "更新用户")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SysUser>> update(@PathVariable Long id, @RequestBody SysUser user) {
        return userRepository.findById(id)
                .map(existing -> {
                    if (user.getNickname() != null) existing.setNickname(user.getNickname());
                    if (user.getEmail() != null) existing.setEmail(user.getEmail());
                    if (user.getPhone() != null) existing.setPhone(user.getPhone());
                    if (user.getStatus() != null) existing.setStatus(user.getStatus());
                    SysUser saved = userRepository.save(existing);
                    return ResponseEntity.ok(ApiResponse.success("更新成功", saved));
                })
                .orElse(ResponseEntity.status(404).body(ApiResponse.notFound("用户不存在")));
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity.ok(ApiResponse.notFound("用户不存在"));
        }
        userRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success("删除成功", null));
    }
}
