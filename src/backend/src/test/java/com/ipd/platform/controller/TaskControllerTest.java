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
 * 任务管理模块 API 测试
 * 策略：先创建项目，再创建任务
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("任务管理模块 API 测试")
class TaskControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private SysUserRepository userRepository;
    @Autowired private SysRoleRepository roleRepository;
    @Autowired private PrjTaskRepository taskRepository;
    @Autowired private PrjProjectRepository projectRepository;

    private String adminToken;
    private Long testProjectId;

    @BeforeEach
    void setUp() throws Exception {
        userRepository.deleteAll();
        roleRepository.deleteAll();
        taskRepository.deleteAll();
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

        // 先创建一个项目，用于后续任务测试
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

    private Map<String, Object> task(String title, String status) {
        Map<String, Object> m = new HashMap<>();
        m.put("title", title);
        m.put("status", status);
        m.put("projectId", testProjectId);
        m.put("estimateHours", 8);
        return m;
    }

    private String auth() { return "Bearer " + adminToken; }

    @Nested
    @DisplayName("POST /tasks")
    class CreateTests {

        @Test @DisplayName("创建任务成功")
        void createSuccess() throws Exception {
            mockMvc.perform(post("/tasks").header("Authorization", auth())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(task("后端开发任务", "todo"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.title").value("后端开发任务"));
        }

        @Test @DisplayName("无Token创建任务返回401")
        void createNoToken() throws Exception {
            mockMvc.perform(post("/tasks").contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(task("Test", "todo"))))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /tasks")
    class ListTests {

        @Test @DisplayName("分页查询返回3条数据")
        void listPagination() throws Exception {
            for (int i = 1; i <= 3; i++) {
                mockMvc.perform(post("/tasks").header("Authorization", auth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(task("任务" + i, "todo"))));
            }
            mockMvc.perform(get("/tasks").header("Authorization", auth()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.totalElements").value(3));
        }

        @Test @DisplayName("按状态筛选")
        void filterByStatus() throws Exception {
            mockMvc.perform(post("/tasks").header("Authorization", auth())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(task("已完成任务", "done"))));
            mockMvc.perform(get("/tasks?status=done").header("Authorization", auth()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content[0].status").value("done"));
        }
    }

    @Nested
    @DisplayName("PUT /tasks/{id}")
    class UpdateTests {

        @Test @DisplayName("更新任务标题成功")
        void updateTitle() throws Exception {
            MvcResult cr = mockMvc.perform(post("/tasks").header("Authorization", auth())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(task("待更新任务", "todo"))))
                    .andExpect(status().isOk()).andReturn();
            Long id = objectMapper.readTree(cr.getResponse().getContentAsString()).get("data").get("id").asLong();

            Map<String, Object> update = new HashMap<>();
            update.put("title", "已更新标题");
            mockMvc.perform(put("/tasks/" + id).header("Authorization", auth())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(update)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.title").value("已更新标题"));
        }

        @Test @DisplayName("更新不存在任务返回404")
        void updateNotFound() throws Exception {
            Map<String, Object> update = new HashMap<>();
            update.put("title", "不存在");
            mockMvc.perform(put("/tasks/99999").header("Authorization", auth())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(update)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /tasks/{id}")
    class DeleteTests {

        @Test @DisplayName("删除任务成功")
        void deleteSuccess() throws Exception {
            MvcResult cr = mockMvc.perform(post("/tasks").header("Authorization", auth())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(task("待删除任务", "todo"))))
                    .andExpect(status().isOk()).andReturn();
            Long id = objectMapper.readTree(cr.getResponse().getContentAsString()).get("data").get("id").asLong();

            mockMvc.perform(delete("/tasks/" + id).header("Authorization", auth()))
                    .andExpect(status().isOk());

            mockMvc.perform(delete("/tasks/" + id).header("Authorization", auth()))
                    .andExpect(status().isNotFound());
        }
    }
}
