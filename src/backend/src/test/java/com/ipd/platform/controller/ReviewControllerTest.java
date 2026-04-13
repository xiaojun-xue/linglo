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
        adminRole.setRoleName("管理员");
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

        // Create project first
        Map<String, Object> proj = new HashMap<>();
        proj.put("name", "测试项目");
        proj.put("description", "测试");
        proj.put("stage", 1);
        var projRes = mockMvc.perform(post("/projects").header("Authorization", auth())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(proj))).andExpect(status().isOk()).andReturn();
        testProjectId = objectMapper.readTree(projRes.getResponse().getContentAsString()).get("data").get("id").asLong();
    }

    private String auth() { return "Bearer " + adminToken; }

    private Map<String, Object> review(String title) {
        Map<String, Object> m = new HashMap<>();
        m.put("title", title);
        m.put("projectId", testProjectId);
        m.put("type", 1);
        m.put("status", 1);
        return m;
    }

    @Nested
    @DisplayName("POST /reviews")
    class CreateTests {
        @Test void createSuccess() throws Exception {
            mockMvc.perform(post("/reviews").header("Authorization", auth())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(review("需求评审"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.title").value("需求评审"));
        }
        @Test void createNoToken() throws Exception {
            mockMvc.perform(post("/reviews").contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(review("Test"))))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /reviews")
    class ListTests {
        @Test void listPagination() throws Exception {
            mockMvc.perform(post("/reviews").header("Authorization", auth())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(review("评审A"))));
            mockMvc.perform(post("/reviews").header("Authorization", auth())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(review("评审B"))));
            mockMvc.perform(get("/reviews").header("Authorization", auth()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.totalElements").value(2));
        }
    }

    @Nested
    @DisplayName("PUT /reviews/{id}")
    class UpdateTests {
        @Test void updateTitle() throws Exception {
            MvcResult cr = mockMvc.perform(post("/reviews").header("Authorization", auth())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(review("待更新评审"))))
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
        @Test void updateNotFound() throws Exception {
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
        @Test void deleteNotSupported() throws Exception {
            mockMvc.perform(delete("/reviews/1").header("Authorization", auth()))
                    .andExpect(status().isMethodNotAllowed());
        }
    }
}
