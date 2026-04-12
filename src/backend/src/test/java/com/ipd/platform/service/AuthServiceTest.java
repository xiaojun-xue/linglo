package com.ipd.platform.service;

import com.ipd.platform.dto.LoginRequest;
import com.ipd.platform.dto.LoginResponse;
import com.ipd.platform.entity.SysRole;
import com.ipd.platform.entity.SysUser;
import com.ipd.platform.repository.SysRoleRepository;
import com.ipd.platform.repository.SysUserRepository;
import com.ipd.platform.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 认证服务测试
 */
@SpringBootTest
@Transactional
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private SysUserRepository userRepository;

    @Autowired
    private SysRoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        // 清理数据
        userRepository.deleteAll();
        
        // 创建测试用户
        SysUser testUser = SysUser.builder()
                .username("testuser")
                .password(passwordEncoder.encode("password123"))
                .nickname("测试用户")
                .email("test@example.com")
                .status(1)
                .userType(0)
                .roles(new HashSet<>())
                .build();
        
        // 分配角色
        roleRepository.findByRoleCode(SysRole.RoleCode.DEVELOPER)
                .ifPresent(role -> testUser.getRoles().add(role));
        
        userRepository.save(testUser);
    }

    @Test
    void testLoginSuccess() {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password123");

        LoginResponse response = authService.login(request);

        assertNotNull(response);
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        assertEquals("testuser", response.getUsername());
        assertTrue(response.getRoles().contains("DEV"));
    }

    @Test
    void testLoginWithWrongPassword() {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("wrongpassword");

        assertThrows(Exception.class, () -> authService.login(request));
    }

    @Test
    void testJwtTokenGeneration() {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password123");

        LoginResponse response = authService.login(request);
        String token = response.getAccessToken();

        assertTrue(jwtTokenProvider.validateToken(token));
        assertEquals("testuser", jwtTokenProvider.getUsernameFromToken(token));
    }
}
