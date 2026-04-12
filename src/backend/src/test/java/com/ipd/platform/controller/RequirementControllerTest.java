package com.ipd.platform.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ipd.platform.dto.LoginRequest;
import com.ipd.platform.dto.RegisterRequest;
import com.ipd.platform.entity.SysRole;
import com.ipd.platform.entity.SysUser;
import com.ipd.platform.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 需求管理模块 API 测试
 * 注意：@Profile("!test") 使 DataInitializer 不在测试环境运行，
 * 因此测试需要手动创建角色并分配给用户
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("需求管理模块 API 测试")
class RequirementControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private SysUserRepository userRepository;
    @Autowired private SysRoleRepository roleRepository;
    @Autowired private ReqRequirementRepository requirementRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    /** 管理员 Token（从真实登录获取） */
    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        // 清理（不带 @Transactional，每个测试方法独立数据）
        userRepository.deleteAll();
        roleRepository.deleteAll();
        requirementRepository.deleteAll();

        // 手动创建 ADMIN 角色（DataInitializer 不在 test profile 运行）
        SysRole adminRole = new SysRole();
        adminRole.setRoleName("系统管理员");
        adminRole.setRoleCode("ADMIN");
        adminRole.setDescription("管理员");
        final SysRole savedAdminRole = roleRepository.save(adminRole);

        // 注册并登录
        RegisterRequest reg = new RegisterRequest();
        reg.setUsername("admin");
        reg.setPassword("admin123");
        reg.setConfirmPassword("admin123");
        reg.setNickname("Admin");
        reg.setEmail("admin@test.com");
        reg.setPhone("13800000000");

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isOk());

        // 给用户分配 ADMIN 角色
        userRepository.findByUsername("admin").ifPresent(user -> {
            user.setRoles(Set.of(savedAdminRole));
            userRepository.save(user);
        });

        LoginRequest login = new LoginRequest();
        login.setUsername("admin");
        login.setPassword("admin123");

        var result = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn();

        adminToken = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("data").get("accessToken").asText();
    }

    private Map req(String title, int priority) {
        Map m = new HashMap<>();
        m.put("title", title);
        m.put("priority", priority);
        m.put("estimateHours", 8);
        return m;
    }

    // ─── 创建需求 ──────────────────────────────────────────

    @Nested
    @DisplayName("POST /requirements")
    class CreateTests {

        @Test
        @DisplayName("创建需求成功返回200")
        void createSuccess() throws Exception {
            mockMvc.perform(post("/requirements")
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req("登录功能", 1))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.title").value("登录功能"));
        }

        @Test
        @DisplayName("无Token创建需求返回401")
        void createNoToken() throws Exception {
            mockMvc.perform(post("/requirements")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req("Test", 1))))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ─── 查询需求 ──────────────────────────────────────────

    @Nested
    @DisplayName("GET /requirements")
    class ListTests {

        @Test
        @DisplayName("分页查询返回3条数据")
        void listPagination() throws Exception {
            for (int i = 1; i <= 3; i++) {
                mockMvc.perform(post("/requirements")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req("需求" + i, i))));
            }
            mockMvc.perform(get("/requirements")
                    .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.totalElements").value(3));
        }

        @Test
        @DisplayName("按优先级筛选")
        void filterByPriority() throws Exception {
            mockMvc.perform(post("/requirements")
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req("P0需求", 0))));

            mockMvc.perform(get("/requirements?priority=0")
                    .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content[0].priority").value(0));
        }
    }

    // ─── 更新需求 ──────────────────────────────────────────

    @Nested
    @DisplayName("PUT /requirements/{id}")
    class UpdateTests {

        @Test
        @DisplayName("更新需求标题成功")
        void updateTitle() throws Exception {
            var cr = mockMvc.perform(post("/requirements")
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req("待更新", 1))))
                    .andExpect(status().isOk())
                    .andReturn();

            Long id = objectMapper.readTree(cr.getResponse().getContentAsString())
                    .get("data").get("id").asLong();

            Map update = new HashMap<>();
            update.put("title", "已更新标题");
            mockMvc.perform(put("/requirements/" + id)
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(update)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.title").value("已更新标题"));
        }

        @Test
        @DisplayName("更新不存在需求返回404")
        void updateNotFound() throws Exception {
            Map update = new HashMap<>();
            update.put("title", "不存在");
            mockMvc.perform(put("/requirements/99999")
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(update)))
                    .andExpect(status().isNotFound());
        }
    }

    // ─── 删除需求 ──────────────────────────────────────────

    @Nested
    @DisplayName("DELETE /requirements/{id}")
    class DeleteTests {

        @Test
        @DisplayName("删除需求成功")
        void deleteSuccess() throws Exception {
            var cr = mockMvc.perform(post("/requirements")
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req("待删除", 1))))
                    .andExpect(status().isOk())
                    .andReturn();

            Long id = objectMapper.readTree(cr.getResponse().getContentAsString())
                    .get("data").get("id").asLong();

            // 删除请求返回 200
            mockMvc.perform(delete("/requirements/" + id)
                    .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk());

            // 删除后列表中不应再出现该需求（软删除验证）
            var listRes = mockMvc.perform(get("/requirements")
                    .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andReturn();
            int total = objectMapper.readTree(listRes.getResponse().getContentAsString())
                    .get("data").get("totalElements").asInt();
            assert total == 0 : "删除后列表应为空，实际：" + total;
        }
    }
}
