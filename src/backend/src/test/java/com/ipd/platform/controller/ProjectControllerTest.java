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
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 项目管理模块 API 测试
 * 策略：先创建项目，再测试其他操作
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("项目管理模块 API 测试")
class ProjectControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private SysUserRepository userRepository;
    @Autowired private SysRoleRepository roleRepository;
    @Autowired private PrjProjectRepository projectRepository;

    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        userRepository.deleteAll();
        roleRepository.deleteAll();
        projectRepository.deleteAll();

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

    private Map<String, Object> proj(String name, String stage) {
        Map<String, Object> m = new HashMap<>();
        m.put("name", name);
        m.put("description", "测试项目描述");
        m.put("stage", stage);
        return m;
    }

    private String auth() { return "Bearer " + adminToken; }

    // 辅助方法：创建项目并返回ID
    private Long createProject(String name, String stage) throws Exception {
        MvcResult result = mockMvc.perform(post("/projects").header("Authorization", auth())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(proj(name, stage))))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("data").get("id").asLong();
    }

    // ─── 创建项目 ─────────────────────────────────────────

    @Nested
    @DisplayName("POST /projects")
    class CreateTests {

        @Test @DisplayName("创建项目成功")
        void createSuccess() throws Exception {
            mockMvc.perform(post("/projects").header("Authorization", auth())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(proj("智慧社区项目", "concept"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.name").value("智慧社区项目"))
                    .andExpect(jsonPath("$.data.stage").value("concept"));
        }

        @Test @DisplayName("无Token创建项目返回401")
        void createNoToken() throws Exception {
            mockMvc.perform(post("/projects").contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(proj("Test", "concept"))))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ─── 查询项目 ─────────────────────────────────────────

    @Nested
    @DisplayName("GET /projects")
    class ListTests {

        @Test @DisplayName("分页查询返回2条数据")
        void listPagination() throws Exception {
            createProject("项目A", "concept");
            createProject("项目B", "plan");
            
            mockMvc.perform(get("/projects").header("Authorization", auth()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.totalElements").value(2));
        }

        @Test @DisplayName("按阶段筛选")
        void filterByStage() throws Exception {
            createProject("概念阶段项目", "concept");
            createProject("计划阶段项目", "plan");
            
            mockMvc.perform(get("/projects?stage=concept").header("Authorization", auth()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content[0].stage").value("concept"));
        }
    }

    // ─── 更新项目 ─────────────────────────────────────────

    @Nested
    @DisplayName("PUT /projects/{id}")
    class UpdateTests {

        @Test @DisplayName("更新项目名称成功")
        void updateName() throws Exception {
            Long id = createProject("待更新项目", "concept");

            Map<String, Object> update = new HashMap<>();
            update.put("name", "已更新项目名称");
            mockMvc.perform(put("/projects/" + id).header("Authorization", auth())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(update)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.name").value("已更新项目名称"));
        }

        @Test @DisplayName("更新不存在项目返回404")
        void updateNotFound() throws Exception {
            Map<String, Object> update = new HashMap<>();
            update.put("name", "不存在");
            mockMvc.perform(put("/projects/99999").header("Authorization", auth())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(update)))
                    .andExpect(status().isNotFound());
        }
    }

    // ─── 删除项目 ─────────────────────────────────────────

    @Nested
    @DisplayName("DELETE /projects/{id}")
    class DeleteTests {

        @Test @DisplayName("删除项目成功")
        void deleteSuccess() throws Exception {
            Long id = createProject("待删除项目", "concept");

            mockMvc.perform(delete("/projects/" + id).header("Authorization", auth()))
                    .andExpect(status().isOk());

            // 再次删除应返回404
            mockMvc.perform(delete("/projects/" + id).header("Authorization", auth()))
                    .andExpect(status().isNotFound());
        }
    }
}
