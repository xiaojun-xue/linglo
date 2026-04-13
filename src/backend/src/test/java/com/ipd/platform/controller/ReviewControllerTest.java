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
 * 评审管理模块 API 测试
 * 策略：先创建项目，再创建评审
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("评审管理模块 API 测试")
class ReviewControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private SysUserRepository userRepository;
    @Autowired private SysRoleRepository roleRepository;
    @Autowired private RevReviewRepository reviewRepository;
    @Autowired private PrjProjectRepository projectRepository;

    private String adminToken;
    private Long testProjectId;

    @BeforeEach
    void setUp() throws Exception {
        userRepository.deleteAll();
        roleRepository.deleteAll();
        reviewRepository.deleteAll();
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

        testProjectId = createProject("测试项目", "concept");
    }

    private Long createProject(String name, String stage) throws Exception {
        Map<String, Object> proj = new HashMap<>();
        proj.put("name", name);
        proj.put("description", "测试项目");
        proj.put("stage", stage);
        
        MvcResult result = mockMvc.perform(post("/projects").header("Authorization", auth())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(proj)))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("data").get("id").asLong();
    }

    private Map<String, Object> review(String title, String type, String status) {
        Map<String, Object> m = new HashMap<>();
        m.put("title", title);
        m.put("reviewType", type);
        m.put("status", status);
        m.put("projectId", testProjectId);
        return m;
    }

    private String auth() { return "Bearer " + adminToken; }

    @Nested
    @DisplayName("POST /reviews")
    class CreateTests {

        @Test @DisplayName("创建评审成功")
        void createSuccess() throws Exception {
            mockMvc.perform(post("/reviews").header("Authorization", auth())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(review("需求评审", "REQUIREMENT", "pending"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.title").value("需求评审"));
        }

        @Test @DisplayName("无Token创建评审返回401")
        void createNoToken() throws Exception {
            mockMvc.perform(post("/reviews").contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(review("Test", "REQUIREMENT", "pending"))))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /reviews")
    class ListTests {

        @Test @DisplayName("分页查询返回2条数据")
        void listPagination() throws Exception {
            mockMvc.perform(post("/reviews").header("Authorization", auth())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(review("评审A", "REQUIREMENT", "pending"))));
            mockMvc.perform(post("/reviews").header("Authorization", auth())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(review("评审B", "DESIGN", "pending"))));
            mockMvc.perform(get("/reviews").header("Authorization", auth()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.totalElements").value(2));
        }
    }

    @Nested
    @DisplayName("PUT /reviews/{id}")
    class UpdateTests {

        @Test @DisplayName("更新评审标题成功")
        void updateTitle() throws Exception {
            MvcResult cr = mockMvc.perform(post("/reviews").header("Authorization", auth())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(review("待更新评审", "REQUIREMENT", "pending"))))
                    .andExpect(status().isOk()).andReturn();
            Long id = objectMapper.readTree(cr.getResponse().getContentAsString()).get("data").get("id").asLong();

            Map<String, Object> update = new HashMap<>();
            update.put("title", "已更新标题");
            mockMvc.perform(put("/reviews/" + id).header("Authorization", auth())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(update)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.title").value("已更新标题"));
        }

        @Test @DisplayName("更新不存在评审返回404")
        void updateNotFound() throws Exception {
            Map<String, Object> update = new HashMap<>();
            update.put("title", "不存在");
            mockMvc.perform(put("/reviews/99999").header("Authorization", auth())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(update)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /reviews/{id}")
    class DeleteTests {

        @Test @DisplayName("删除评审成功")
        void deleteSuccess() throws Exception {
            MvcResult cr = mockMvc.perform(post("/reviews").header("Authorization", auth())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(review("待删除评审", "REQUIREMENT", "pending"))))
                    .andExpect(status().isOk()).andReturn();
            Long id = objectMapper.readTree(cr.getResponse().getContentAsString()).get("data").get("id").asLong();

            mockMvc.perform(delete("/reviews/" + id).header("Authorization", auth()))
                    .andExpect(status().isOk());

            mockMvc.perform(delete("/reviews/" + id).header("Authorization", auth()))
                    .andExpect(status().isNotFound());
        }
    }
}
