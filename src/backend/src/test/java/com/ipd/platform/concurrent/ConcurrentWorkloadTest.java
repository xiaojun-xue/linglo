package com.ipd.platform.concurrent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ipd.platform.dto.LoginRequest;
import com.ipd.platform.dto.RegisterRequest;
import com.ipd.platform.entity.*;
import com.ipd.platform.repository.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 并发工作负载测试
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("并发工作负载测试")
class ConcurrentWorkloadTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private SysUserRepository userRepository;
    @Autowired private SysRoleRepository roleRepository;
    @Autowired private PrjProjectRepository projectRepository;
    @Autowired private PrjTaskRepository taskRepository;
    @Autowired private ReqRequirementRepository requirementRepository;
    @Autowired private PrjSprintRepository sprintRepository;
    @Autowired private QaTestCaseRepository testCaseRepository;
    @Autowired private QaBugRepository bugRepository;
    @Autowired private RevReviewRepository reviewRepository;

    private static final int DEV_COUNT = 20;
    private static final int TESTER_COUNT = 10;
    private static final int TASK_PER_DEV = 3;
    private static final int TEST_PER_TESTER = 2;

    private static final Map<String, AtomicInteger> SUCCESS = new ConcurrentHashMap<>();
    private static final Map<String, AtomicInteger> FAIL = new ConcurrentHashMap<>();

    private String adminToken;
    private String pmToken;
    private final List<String> devTokens = new ArrayList<>();
    private final List<String> testerTokens = new ArrayList<>();
    private Long projectId;
    private Long sprintId;
    private final List<Long> taskIds = new ArrayList<>();

    @BeforeEach
    void setUp() throws Exception {
        bugRepository.deleteAll();
        testCaseRepository.deleteAll();
        reviewRepository.deleteAll();
        taskRepository.deleteAll();
        sprintRepository.deleteAll();
        requirementRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();

        SUCCESS.clear();
        FAIL.clear();
        devTokens.clear();
        testerTokens.clear();
        taskIds.clear();

        initRoles();
        System.out.println("\n================================ BEGIN CONCURRENT TEST =================================");
    }

    @Test
    @DisplayName("完整并发工作负载测试")
    void testConcurrentWorkload() throws Exception {
        System.out.println("=== PHASE 1: Create users ===");
        createUsers();

        System.out.println("=== PHASE 2: Admin creates project ===");
        adminCreatesProject();

        System.out.println("=== PHASE 3: PM assigns tasks ===");
        pmAssignsTasks();

        System.out.println("=== PHASE 4: Developers work concurrently ===");
        developersWorkConcurrently();
        long doneCount = taskIds.stream().map(id -> taskRepository.findById(id).orElse(null))
                .filter(t -> t != null && "done".equals(t.getStatus())).count();

        System.out.println("=== PHASE 5: Testers work concurrently ===");
        testersWorkConcurrently();
        long bugCount = bugRepository.count();

        System.out.println("=== PHASE 6: Verify integrity ===");
        verifyDataIntegrity(bugCount, doneCount);

        System.out.println("\n================================ SUMMARY =================================");
        int totalSuccess = SUCCESS.values().stream().mapToInt(AtomicInteger::get).sum();
        int totalFail = FAIL.values().stream().mapToInt(AtomicInteger::get).sum();
        System.out.println("Total SUCCESS: " + totalSuccess);
        System.out.println("Total FAIL: " + totalFail);
        System.out.println("Users: " + userRepository.count());
        System.out.println("Tasks: " + taskRepository.count() + " (done: " + doneCount + ")");
        System.out.println("Bugs: " + bugCount);
        System.out.println("===============================================================\n");

        Assertions.assertEquals(DEV_COUNT * TASK_PER_DEV, taskIds.size());
        Assertions.assertEquals(DEV_COUNT * TASK_PER_DEV, doneCount);
        Assertions.assertEquals(TESTER_COUNT * TEST_PER_TESTER, bugCount);
        Assertions.assertEquals(1 + 1 + DEV_COUNT + TESTER_COUNT, userRepository.count());
        Assertions.assertEquals(0, totalFail);
    }

    // ========== PHASE 1 ==========

    private void createUsers() throws Exception {
        adminToken = regLogin("ct_admin", "admin123", "管理员", "ADMIN");
        pmToken = regLogin("ct_pm", "pm123456", "项目经理", "PM");

        for (int i = 1; i <= DEV_COUNT; i++) {
            String t = regLogin(String.format("ct_dev_%02d", i), "dev123456", "开发者" + i, "DEV");
            devTokens.add(t);
        }

        for (int i = 1; i <= TESTER_COUNT; i++) {
            String t = regLogin(String.format("ct_tester_%02d", i), "test123456", "测试" + i, "QA");
            testerTokens.add(t);
        }

        for (int i = 1; i <= DEV_COUNT; i++) {
            String t = regLogin(String.format("ct_dev_%02d", i), "dev123456", "开发者" + i, "DEV");
            devTokens.add(t);
        }

        for (int i = 1; i <= TESTER_COUNT; i++) {
            String t = regLogin(String.format("ct_tester_%02d", i), "test123456", "测试" + i, "QA");
            testerTokens.add(t);
        }

        System.out.println("Created: 1 admin + 1 PM + " + DEV_COUNT + " devs + " + TESTER_COUNT + " testers");
    }

    private String regLogin(String user, String pass, String nickname, String roleCode) throws Exception {
        RegisterRequest reg = new RegisterRequest();
        reg.setUsername(user);
        reg.setPassword(pass);
        reg.setConfirmPassword(pass);
        reg.setNickname(nickname);
        reg.setEmail(user + "@test.com");
        reg.setPhone("138" + String.format("%08d", new Random().nextInt(100000000)));

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isOk());

        roleRepository.findByRoleCode(roleCode).ifPresent(r -> {
            userRepository.findByUsername(user).ifPresent(u -> {
                u.getRoles().add(r);
                userRepository.save(u);
            });
        });

        LoginRequest login = new LoginRequest();
        login.setUsername(user);
        login.setPassword(pass);
        MvcResult res = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(res.getResponse().getContentAsString())
                .get("data").get("accessToken").asText();
    }

    // ========== PHASE 2 ==========

    private void adminCreatesProject() throws Exception {
        Map<String, Object> proj = new HashMap<>();
        proj.put("name", "Concurrent Load Test Project");
        proj.put("description", "Project for concurrent load testing");
        proj.put("stage", 1);

        MvcResult res = mockMvc.perform(post("/projects")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(proj)))
                .andExpect(status().isOk())
                .andReturn();
        projectId = objectMapper.readTree(res.getResponse().getContentAsString())
                .get("data").get("id").asLong();
        SUCCESS.computeIfAbsent("proj", k -> new AtomicInteger()).incrementAndGet();

        Map<String, Object> spr = new HashMap<>();
        spr.put("name", "Sprint 1 - Load Test");
        spr.put("projectId", projectId);
        spr.put("goal", "Concurrent load test sprint");
        spr.put("startDate", "2026-04-14");
        spr.put("endDate", "2026-04-28");

        res = mockMvc.perform(post("/sprints")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(spr)))
                .andExpect(status().isOk())
                .andReturn();
        sprintId = objectMapper.readTree(res.getResponse().getContentAsString())
                .get("data").get("id").asLong();
        SUCCESS.computeIfAbsent("sprint", k -> new AtomicInteger()).incrementAndGet();

        for (int i = 1; i <= 5; i++) {
            Map<String, Object> req = new HashMap<>();
            req.put("title", "Requirement " + i);
            req.put("priority", i);
            req.put("projectId", projectId);
            mockMvc.perform(post("/requirements")
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk());
        }

        System.out.println("Project ID=" + projectId + ", Sprint ID=" + sprintId);
    }

    // ========== PHASE 3 ==========

    private void pmAssignsTasks() throws Exception {
        int tid = 0;
        for (int di = 0; di < devTokens.size(); di++) {
            for (int i = 0; i < TASK_PER_DEV; i++) {
                tid++;
                Map<String, Object> task = new HashMap<>();
                task.put("title", "Task #" + tid);
                task.put("projectId", projectId);
                task.put("sprintId", sprintId);
                task.put("type", 1);
                task.put("priority", (tid % 3) + 1);

                MvcResult res = mockMvc.perform(post("/tasks")
                        .header("Authorization", "Bearer " + pmToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(task)))
                        .andExpect(status().isOk())
                        .andReturn();
                Long id = objectMapper.readTree(res.getResponse().getContentAsString())
                        .get("data").get("id").asLong();
                taskIds.add(id);
            }
        }
        System.out.println("Created " + taskIds.size() + " tasks");
    }

    // ========== PHASE 4 ==========

    private void developersWorkConcurrently() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(20);
        CountDownLatch latch = new CountDownLatch(DEV_COUNT * TASK_PER_DEV);
        long start = System.currentTimeMillis();

        for (int di = 0; di < devTokens.size(); di++) {
            String token = devTokens.get(di);
            int devNum = di + 1;

            for (int ti = 0; ti < TASK_PER_DEV; ti++) {
                final int taskIdx = di * TASK_PER_DEV + ti;
                if (taskIdx >= taskIds.size()) break;
                final Long tid = taskIds.get(taskIdx);

                executor.submit(() -> {
                    try {
                        // Mark in progress
                        Map<String, Object> u1 = new HashMap<>();
                        u1.put("status", "in_progress");
                        u1.put("title", "Dev " + devNum + " working on task " + taskIdx);

                        mockMvc.perform(put("/tasks/" + tid)
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(u1)))
                                .andExpect(status().isOk());

                        Thread.sleep(10 + new Random().nextInt(50));

                        // Mark done
                        Map<String, Object> u2 = new HashMap<>();
                        u2.put("status", "done");
                        u2.put("title", "Dev " + devNum + " completed task " + taskIdx);

                        mockMvc.perform(put("/tasks/" + tid)
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(u2)))
                                .andExpect(status().isOk());

                        SUCCESS.computeIfAbsent("dev_done", k -> new AtomicInteger()).incrementAndGet();

                    } catch (Exception e) {
                        FAIL.computeIfAbsent("dev_err", k -> new AtomicInteger()).incrementAndGet();
                    } finally {
                        latch.countDown();
                    }
                });
            }
        }

        boolean ok = latch.await(60, TimeUnit.SECONDS);
        long ms = System.currentTimeMillis() - start;
        executor.shutdown();
        System.out.println("Dev phase done in " + ms + "ms (latch_ok=" + ok + ")");
    }

    // ========== PHASE 5 ==========

    private void testersWorkConcurrently() throws Exception {
        // Admin creates test cases
        for (int i = 1; i <= TESTER_COUNT * TEST_PER_TESTER; i++) {
            Map<String, Object> tc = new HashMap<>();
            tc.put("title", "Test Case #" + i);
            tc.put("module", "Module" + (i % 3));
            tc.put("type", 1);
            tc.put("priority", (i % 3) + 1);
            tc.put("status", "draft");
            tc.put("projectId", projectId);

            mockMvc.perform(post("/qa/cases")
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(tc)))
                    .andExpect(status().isOk());
        }

        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(TESTER_COUNT * TEST_PER_TESTER);
        long start = System.currentTimeMillis();

        for (int ti = 0; ti < testerTokens.size(); ti++) {
            String token = testerTokens.get(ti);
            int testerNum = ti + 1;

            for (int ci = 1; ci <= TEST_PER_TESTER; ci++) {
                final int globalIdx = ti * TEST_PER_TESTER + ci;

                executor.submit(() -> {
                    try {
                        Map<String, Object> bug = new HashMap<>();
                        bug.put("title", "Tester " + testerNum + " Bug #" + globalIdx);
                        bug.put("severity", (globalIdx % 3));
                        bug.put("priority", 2);
                        bug.put("description", "Bug description #" + globalIdx);
                        bug.put("status", "new");
                        bug.put("projectId", projectId);

                        mockMvc.perform(post("/qa/bugs")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(bug)))
                                .andExpect(status().isOk());

                        Thread.sleep(5 + new Random().nextInt(30));
                        SUCCESS.computeIfAbsent("bug_create", k -> new AtomicInteger()).incrementAndGet();

                    } catch (Exception e) {
                        FAIL.computeIfAbsent("bug_err", k -> new AtomicInteger()).incrementAndGet();
                    } finally {
                        latch.countDown();
                    }
                });
            }
        }

        boolean ok = latch.await(60, TimeUnit.SECONDS);
        long ms = System.currentTimeMillis() - start;
        executor.shutdown();
        System.out.println("Tester phase done in " + ms + "ms (latch_ok=" + ok + ")");
    }

    // ========== PHASE 6 ==========

    private void verifyDataIntegrity(long bugCount, long doneCount) throws Exception {
        Assertions.assertTrue(projectRepository.findById(projectId).isPresent(), "project exists");
        SUCCESS.computeIfAbsent("v_proj", k -> new AtomicInteger()).incrementAndGet();

        Assertions.assertTrue(sprintRepository.findById(sprintId).isPresent(), "sprint exists");
        SUCCESS.computeIfAbsent("v_sprint", k -> new AtomicInteger()).incrementAndGet();

        long expectedTasks = (long) DEV_COUNT * TASK_PER_DEV;
        Assertions.assertEquals(expectedTasks, taskRepository.count(), "task count");
        Assertions.assertEquals(expectedTasks, doneCount, "done count");
        SUCCESS.computeIfAbsent("v_tasks", k -> new AtomicInteger()).incrementAndGet();

        long expectedBugs = (long) TESTER_COUNT * TEST_PER_TESTER;
        Assertions.assertEquals(expectedBugs, bugCount, "bug count");
        SUCCESS.computeIfAbsent("v_bugs", k -> new AtomicInteger()).incrementAndGet();

        long expectedUsers = 1 + 1 + DEV_COUNT + TESTER_COUNT;
        Assertions.assertEquals(expectedUsers, userRepository.count(), "user count");
        SUCCESS.computeIfAbsent("v_users", k -> new AtomicInteger()).incrementAndGet();

        System.out.println("All integrity checks PASSED");
    }

    // ========== HELPERS ==========

    private void initRoles() {
        String[][] roles = {
            {"ADMIN", "系统管理员"},
            {"PM", "产品经理"},
            {"PGM", "项目经理"},
            {"TL", "技术负责人"},
            {"DEV", "开发者"},
            {"QA", "测试人员"}
        };
        for (String[] r : roles) {
            if (!roleRepository.existsByRoleCode(r[0])) {
                SysRole role = SysRole.builder()
                        .roleCode(r[0])
                        .roleName(r[1])
                        .status(1)
                        .build();
                roleRepository.save(role);
            }
        }
    }
}
