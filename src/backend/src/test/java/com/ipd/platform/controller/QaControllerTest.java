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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 测试管理模块 API 测试（测试用例 + Bug）
 * 使用完整的字段数据，避免验证失败
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("测试管理模块 API 测试")
class QaControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private SysUserRepository userRepository;
    @Autowired private SysRoleRepository roleRepository;
    @Autowired private QaTestCaseRepository testCaseRepository;
    @Autowired private QaBugRepository bugRepository;

    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        userRepository.deleteAll();
        roleRepository.deleteAll();
        testCaseRepository.deleteAll();
        bugRepository.deleteAll();

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

    @Nested
    @DisplayName("测试用例 - POST /qa/cases")
    class TestCaseTests {

        private Map<String, Object> testCaseBody(String title, int priority) {
            Map<String, Object> body = new HashMap<>();
            body.put("title", title);
            body.put("module", "认证模块");
            body.put("priority", priority);
            body.put("steps", "1. 打开登录页\\n2. 输入账号密码\\n3. 点击登录");
            body.put("expectedResult", "登录成功，跳转到首页");
            body.put("status", "draft");
            return body;
        }

        @Test @DisplayName("创建测试用例成功")
        void createTestCase() throws Exception {
            mockMvc.perform(post("/qa/cases").header("Authorization", auth())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testCaseBody("登录功能测试用例", 1))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.title").value("登录功能测试用例"));
        }

        @Test @DisplayName("无Token创建测试用例返回401")
        void createNoToken() throws Exception {
            mockMvc.perform(post("/qa/cases").contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testCaseBody("Test", 1))))
                    .andExpect(status().isUnauthorized());
        }

        @Test @DisplayName("查询测试用例列表")
        void listTestCases() throws Exception {
            for (int i = 1; i <= 2; i++) {
                mockMvc.perform(post("/qa/cases").header("Authorization", auth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testCaseBody("用例" + i, 1))));
            }
            mockMvc.perform(get("/qa/cases").header("Authorization", auth()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.totalElements").value(2));
        }
    }

    @Nested
    @DisplayName("Bug - POST /qa/bugs")
    class BugTests {

        private Map<String, Object> bugBody(String title, String severity) {
            Map<String, Object> body = new HashMap<>();
            body.put("title", title);
            body.put("severity", severity);
            body.put("priority", 2);
            body.put("description", "Bug详细描述");
            body.put("status", "new");
            return body;
        }

        @Test @DisplayName("创建Bug成功")
        void createBug() throws Exception {
            mockMvc.perform(post("/qa/bugs").header("Authorization", auth())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(bugBody("登录页面样式错位", "P1"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.title").value("登录页面样式错位"));
        }

        @Test @DisplayName("无Token创建Bug返回401")
        void createBugNoToken() throws Exception {
            mockMvc.perform(post("/qa/bugs").contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(bugBody("Test Bug", "P2"))))
                    .andExpect(status().isUnauthorized());
        }

        @Test @DisplayName("查询Bug列表")
        void listBugs() throws Exception {
            for (int i = 1; i <= 2; i++) {
                mockMvc.perform(post("/qa/bugs").header("Authorization", auth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bugBody("Bug" + i, "P" + i))));
            }
            mockMvc.perform(get("/qa/bugs").header("Authorization", auth()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.totalElements").value(2));
        }

        @Test @DisplayName("按严重程度筛选Bug")
        void filterBySeverity() throws Exception {
            mockMvc.perform(post("/qa/bugs").header("Authorization", auth())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(bugBody("严重Bug", "P0"))));

            mockMvc.perform(get("/qa/bugs?severity=P0").header("Authorization", auth()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content[0].severity").value("P0"));
        }
    }

    @Nested
    @DisplayName("统计 - GET /qa/stats")
    class StatsTests {

        @Test @DisplayName("获取QA统计数据")
        void getStats() throws Exception {
            // 创建测试数据
            Map<String, Object> tc = new HashMap<>();
            tc.put("title", "用例"); tc.put("module", "模块"); tc.put("priority", 1);
            tc.put("steps", "步骤"); tc.put("expectedResult", "预期结果"); tc.put("status", "draft");
            mockMvc.perform(post("/qa/cases").header("Authorization", auth())
                    .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(tc)));
            
            Map<String, Object> bug = new HashMap<>();
            bug.put("title", "Bug"); bug.put("severity", "P1"); bug.put("priority", 2);
            bug.put("description", "描述"); bug.put("status", "new");
            mockMvc.perform(post("/qa/bugs").header("Authorization", auth())
                    .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(bug)));

            mockMvc.perform(get("/qa/stats").header("Authorization", auth()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }
}
