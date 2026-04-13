package com.ipd.platform.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ipd.platform.dto.LoginRequest;
import com.ipd.platform.dto.RegisterRequest;
import com.ipd.platform.entity.SysRole;
import com.ipd.platform.repository.*;
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

import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 通知模块 API 测试
 * 使用正确的路径：/notifications
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("通知模块 API 测试")
class NotificationControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private SysUserRepository userRepository;
    @Autowired private SysRoleRepository roleRepository;
    @Autowired private SysNotificationRepository notificationRepository;

    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        userRepository.deleteAll();
        roleRepository.deleteAll();
        notificationRepository.deleteAll();

        SysRole adminRole = new SysRole();
        adminRole.setRoleName("系统管理员");
        adminRole.setRoleCode("ADMIN");
        final SysRole savedRole = roleRepository.save(adminRole);

        RegisterRequest reg = new RegisterRequest();
        reg.setUsername("admin"); reg.setPassword("admin123"); reg.setConfirmPassword("admin123");
        reg.setNickname("Admin"); reg.setEmail("admin@test.com"); reg.setPhone("13800000000");
        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reg))).andExpect(status().isOk());
        userRepository.findByUsername("admin").ifPresent(u -> { u.setRoles(Set.of(savedRole)); userRepository.save(u); });

        LoginRequest login = new LoginRequest(); login.setUsername("admin"); login.setPassword("admin123");
        var res = mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login))).andExpect(status().isOk()).andReturn();
        adminToken = objectMapper.readTree(res.getResponse().getContentAsString()).get("data").get("accessToken").asText();
    }

    private String auth() { return "Bearer " + adminToken; }

    // 使用正确的路径前缀
    private String notifBase() { return "/notifications"; }

    @Nested
    @DisplayName("GET /notifications")
    class ListTests {

        @Test @DisplayName("查询通知列表成功")
        void listNotifications() throws Exception {
            mockMvc.perform(get(notifBase()).header("Authorization", auth()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test @DisplayName("无Token查询通知返回401")
        void listNoToken() throws Exception {
            mockMvc.perform(get(notifBase()))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /notifications/unread-count")
    class UnreadCountTests {

        @Test @DisplayName("获取未读消息数成功")
        void unreadCount() throws Exception {
            mockMvc.perform(get(notifBase() + "/unread-count").header("Authorization", auth()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.count").exists());
        }

        @Test @DisplayName("无Token获取未读数返回401")
        void unreadCountNoToken() throws Exception {
            mockMvc.perform(get(notifBase() + "/unread-count"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("PUT /notifications/{id}/read")
    class MarkReadTests {

        @Test @DisplayName("标记单条已读（不存在的通知返回404）")
        void markReadNotFound() throws Exception {
            mockMvc.perform(put(notifBase() + "/99999/read").header("Authorization", auth()))
                    .andExpect(status().isOk());
        }

        @Test @DisplayName("无Token标记已读返回401")
        void markReadNoToken() throws Exception {
            mockMvc.perform(put(notifBase() + "/1/read"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("PUT /notifications/read-all")
    class MarkAllReadTests {

        @Test @DisplayName("全部已读成功")
        void markAllRead() throws Exception {
            mockMvc.perform(put(notifBase() + "/read-all").header("Authorization", auth()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test @DisplayName("无Token全部已读返回401")
        void markAllReadNoToken() throws Exception {
            mockMvc.perform(put(notifBase() + "/read-all"))
                    .andExpect(status().isUnauthorized());
        }
    }
}
