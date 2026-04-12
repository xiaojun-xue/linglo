package com.ipd.platform.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ipd.platform.dto.LoginRequest;
import com.ipd.platform.dto.RegisterRequest;
import com.ipd.platform.repository.SysUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 认证模块 API 测试
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@DisplayName("认证模块 API 测试")
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private SysUserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    private String registerAndLogin(String username, String password) throws Exception {
        RegisterRequest reg = new RegisterRequest();
        reg.setUsername(username);
        reg.setPassword(password);
        reg.setConfirmPassword(password);
        reg.setNickname("tester");
        reg.setEmail(username + "@test.com");
        reg.setPhone("13800000000");

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isOk());

        LoginRequest login = new LoginRequest();
        login.setUsername(username);
        login.setPassword(password);

        var result = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("data").get("accessToken").asText();
    }

    // ─── 登录 ────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /auth/login")
    class LoginTests {

        @Test @DisplayName("成功登录返回token")
        void loginSuccess() throws Exception {
            String token = registerAndLogin("user_ok", "Pass1234");
            assert token != null && !token.isEmpty();
        }

        @Test @DisplayName("密码错误返回401")
        void loginWrongPassword() throws Exception {
            registerAndLogin("user_wp", "Pass1234");
            LoginRequest req = new LoginRequest();
            req.setUsername("user_wp");
            req.setPassword("wrongpass");
            mockMvc.perform(post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isUnauthorized());
        }

        @Test @DisplayName("用户不存在返回401")
        void loginUserNotFound() throws Exception {
            LoginRequest req = new LoginRequest();
            req.setUsername("ghost");
            req.setPassword("anypass");
            mockMvc.perform(post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isUnauthorized());
        }

        @Test @DisplayName("空用户名返回400")
        void loginEmptyUsername() throws Exception {
            LoginRequest req = new LoginRequest();
            req.setUsername("");
            req.setPassword("pass");
            mockMvc.perform(post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }

        @Test @DisplayName("空密码返回400")
        void loginEmptyPassword() throws Exception {
            LoginRequest req = new LoginRequest();
            req.setUsername("someuser");
            req.setPassword("");
            mockMvc.perform(post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }
    }

    // ─── 注册 ────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /auth/register")
    class RegisterTests {

        @Test @DisplayName("成功注册返回200")
        void registerSuccess() throws Exception {
            RegisterRequest req = new RegisterRequest();
            req.setUsername("newguy");
            req.setPassword("Pass5678");
            req.setConfirmPassword("Pass5678");
            req.setNickname("New Guy");
            req.setEmail("newguy@test.com");
            req.setPhone("13900000010");

            mockMvc.perform(post("/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.username").value("newguy"));
        }

        @Test @DisplayName("重复用户名返回400")
        void registerDuplicate() throws Exception {
            registerAndLogin("dupuser", "Pass1234");
            RegisterRequest req = new RegisterRequest();
            req.setUsername("dupuser");
            req.setPassword("Pass9999");
            req.setConfirmPassword("Pass9999");
            req.setNickname("Dup");
            req.setEmail("dup@test.com");
            req.setPhone("13900000011");
            mockMvc.perform(post("/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }

        @Test @DisplayName("空用户名返回400")
        void registerEmptyUsername() throws Exception {
            RegisterRequest req = new RegisterRequest();
            req.setUsername("");
            req.setPassword("Pass1234");
            req.setConfirmPassword("Pass1234");
            mockMvc.perform(post("/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }

        @Test @DisplayName("密码不一致返回400")
        void registerPasswordMismatch() throws Exception {
            RegisterRequest req = new RegisterRequest();
            req.setUsername("mismatch");
            req.setPassword("Pass1234");
            req.setConfirmPassword("Pass9999");
            mockMvc.perform(post("/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }
    }

    // ─── 权限控制 ────────────────────────────────────────────

    @Nested
    @DisplayName("权限控制")
    class ProtectedTests {

        @Test @DisplayName("无Token访问受保护接口返回401")
        void noTokenReturns401() throws Exception {
            mockMvc.perform(get("/auth/current"))
                    .andExpect(status().isUnauthorized());
        }

        @Test @DisplayName("无效Token返回401")
        void invalidTokenReturns401() throws Exception {
            mockMvc.perform(get("/auth/current")
                    .header("Authorization", "Bearer bad_token_xyz"))
                    .andExpect(status().isUnauthorized());
        }

        @Test @DisplayName("有效Token可访问受保护接口")
        void validTokenAccessOk() throws Exception {
            String token = registerAndLogin("tokentest", "Pass1234");
            mockMvc.perform(get("/auth/current")
                    .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.username").value("tokentest"));
        }
    }
}
