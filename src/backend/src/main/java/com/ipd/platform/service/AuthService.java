package com.ipd.platform.service;

import com.ipd.platform.dto.LoginRequest;
import com.ipd.platform.dto.LoginResponse;
import com.ipd.platform.dto.RegisterRequest;
import com.ipd.platform.entity.SysRole;
import com.ipd.platform.entity.SysUser;
import com.ipd.platform.repository.SysRoleRepository;
import com.ipd.platform.repository.SysUserRepository;
import com.ipd.platform.security.JwtTokenProvider;
import com.ipd.platform.security.JwtUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 认证服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final SysUserRepository userRepository;
    private final SysRoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 用户登录
     */
    public LoginResponse login(LoginRequest request) {
        // 认证
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 生成 Token
        String accessToken = jwtTokenProvider.generateAccessToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);

        JwtUserDetails userDetails = (JwtUserDetails) authentication.getPrincipal();

        // 更新最后登录时间
        SysUser user = userRepository.findById(userDetails.getId()).orElse(null);
        if (user != null) {
            user.setLastLoginTime(LocalDateTime.now());
            userRepository.save(user);
        }

        log.info("用户 {} 登录成功", request.getUsername());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getExpirationSeconds())
                .userId(userDetails.getId())
                .username(userDetails.getUsername())
                .nickname(userDetails.getNickname())
                .avatar(userDetails.getAvatar())
                .roles(userDetails.getAuthorities().stream()
                        .map(a -> a.getAuthority().replace("ROLE_", ""))
                        .collect(Collectors.toSet()))
                .permissions(userDetails.getAuthorities().stream()
                        .map(a -> a.getAuthority())
                        .collect(Collectors.toSet()))
                .build();
    }

    /**
     * 用户注册
     */
    @Transactional
    public SysUser register(RegisterRequest request) {
        // 验证密码
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("两次密码输入不一致");
        }

        // 检查用户名是否存在
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }

        // 检查邮箱是否存在
        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("邮箱已被使用");
        }

        // 创建用户
        SysUser user = SysUser.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname() != null ? request.getNickname() : request.getUsername())
                .email(request.getEmail())
                .phone(request.getPhone())
                .status(1)
                .userType(0)
                .build();

        // 默认分配"查看者"角色
        roleRepository.findByRoleCode(SysRole.RoleCode.VIEWER)
                .ifPresent(role -> user.getRoles().add(role));

        SysUser savedUser = userRepository.save(user);
        log.info("新用户 {} 注册成功", request.getUsername());

        return savedUser;
    }

    /**
     * 刷新 Token
     */
    public LoginResponse refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken) || !jwtTokenProvider.isRefreshToken(refreshToken)) {
            throw new RuntimeException("无效的刷新令牌");
        }

        String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
        SysUser user = userRepository.findByUsernameWithRoles(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        JwtUserDetails userDetails = new JwtUserDetails(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        String newAccessToken = jwtTokenProvider.generateAccessToken(authentication);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(authentication);

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getExpirationSeconds())
                .userId(userDetails.getId())
                .username(userDetails.getUsername())
                .nickname(userDetails.getNickname())
                .roles(userDetails.getAuthorities().stream()
                        .map(a -> a.getAuthority().replace("ROLE_", ""))
                        .collect(Collectors.toSet()))
                .build();
    }

    /**
     * 获取当前用户信息
     */
    public SysUser getCurrentUser(String username) {
        return userRepository.findByUsernameWithRoles(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    /**
     * 获取当前登录用户
     */
    public JwtUserDetails getCurrentUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof JwtUserDetails) {
            return (JwtUserDetails) authentication.getPrincipal();
        }
        throw new RuntimeException("用户未登录");
    }

    /**
     * 获取当前用户ID
     */
    public Long getCurrentUserId() {
        return getCurrentUserDetails().getId();
    }
}
