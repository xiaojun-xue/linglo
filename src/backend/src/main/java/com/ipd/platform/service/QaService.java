package com.ipd.platform.service;

import com.ipd.platform.entity.QaBug;
import com.ipd.platform.entity.QaTestCase;
import com.ipd.platform.repository.QaBugRepository;
import com.ipd.platform.repository.QaTestCaseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * QA 测试服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QaService {

    private final QaTestCaseRepository testCaseRepository;
    private final QaBugRepository bugRepository;
    private final AuthService authService;

    // ==================== 测试用例 ====================

    public Page<QaTestCase> listTestCases(String keyword, Long projectId, String module, Integer status, int page, int size) {
        return testCaseRepository.searchCases(keyword, projectId, module, status, PageRequest.of(page, size));
    }

    public QaTestCase getTestCase(Long id) {
        return testCaseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("测试用例不存在"));
    }

    @Transactional
    public QaTestCase createTestCase(QaTestCase testCase) {
        String caseNo = generateCaseNo();
        testCase.setCaseNo(caseNo);
        testCase.setCreatorId(authService.getCurrentUserId());
        testCase.setStatus(1);
        return testCaseRepository.save(testCase);
    }

    @Transactional
    public QaTestCase updateTestCase(Long id, QaTestCase testCase) {
        QaTestCase existing = getTestCase(id);
        if (testCase.getTitle() != null) existing.setTitle(testCase.getTitle());
        if (testCase.getModule() != null) existing.setModule(testCase.getModule());
        if (testCase.getType() != null) existing.setType(testCase.getType());
        if (testCase.getPriority() != null) existing.setPriority(testCase.getPriority());
        if (testCase.getStatus() != null) existing.setStatus(testCase.getStatus());
        if (testCase.getPrecondition() != null) existing.setPrecondition(testCase.getPrecondition());
        if (testCase.getSteps() != null) existing.setSteps(testCase.getSteps());
        if (testCase.getExpectedResult() != null) existing.setExpectedResult(testCase.getExpectedResult());
        return testCaseRepository.save(existing);
    }

    @Transactional
    public void deleteTestCase(Long id) {
        if (!testCaseRepository.existsById(id)) throw new RuntimeException("测试用例不存在");
        testCaseRepository.deleteById(id);
    }

    // ==================== 缺陷管理 ====================

    public Page<QaBug> listBugs(String keyword, Long projectId, Integer severity, Integer status, int page, int size) {
        return bugRepository.searchBugs(keyword, projectId, severity, status, PageRequest.of(page, size));
    }

    public QaBug getBug(Long id) {
        return bugRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("缺陷不存在"));
    }

    @Transactional
    public QaBug createBug(QaBug bug) {
        String bugNo = generateBugNo();
        bug.setBugNo(bugNo);
        bug.setStatus(QaBug.Status.NEW);
        return bugRepository.save(bug);
    }

    @Transactional
    public QaBug updateBug(Long id, QaBug bug) {
        QaBug existing = getBug(id);
        if (bug.getTitle() != null) existing.setTitle(bug.getTitle());
        if (bug.getSeverity() != null) existing.setSeverity(bug.getSeverity());
        if (bug.getPriority() != null) existing.setPriority(bug.getPriority());
        if (bug.getStatus() != null) existing.setStatus(bug.getStatus());
        if (bug.getAssigneeId() != null) existing.setAssigneeId(bug.getAssigneeId());
        if (bug.getFixedVersion() != null) existing.setFixedVersion(bug.getFixedVersion());
        return bugRepository.save(existing);
    }

    @Transactional
    public QaBug updateBugStatus(Long id, Integer status) {
        QaBug bug = getBug(id);
        bug.setStatus(status);
        return bugRepository.save(bug);
    }

    @Transactional
    public void deleteBug(Long id) {
        if (!bugRepository.existsById(id)) throw new RuntimeException("缺陷不存在");
        bugRepository.deleteById(id);
    }

    // ==================== 质量统计 ====================

    public Map<String, Object> getQualityStats(Long projectId) {
        Map<String, Object> stats = new HashMap<>();

        // 用例统计
        long totalCases = testCaseRepository.countByProjectId(projectId);
        long passedCases = testCaseRepository.countByStatus(3);
        stats.put("totalCases", totalCases);
        stats.put("passedCases", passedCases);
        stats.put("casePassRate", totalCases > 0 ? (passedCases * 100 / totalCases) : 0);

        // 缺陷统计
        long totalBugs = bugRepository.countByProject(projectId);
        long openBugs = bugRepository.countByProjectIdAndStatus(projectId, QaBug.Status.NEW)
                + bugRepository.countByProjectIdAndStatus(projectId, QaBug.Status.CONFIRMED)
                + bugRepository.countByProjectIdAndStatus(projectId, QaBug.Status.ASSIGNED)
                + bugRepository.countByProjectIdAndStatus(projectId, QaBug.Status.FIXING);
        long closedBugs = bugRepository.countByProjectIdAndStatus(projectId, QaBug.Status.CLOSED);

        stats.put("totalBugs", totalBugs);
        stats.put("openBugs", openBugs);
        stats.put("closedBugs", closedBugs);
        stats.put("bugCloseRate", totalBugs > 0 ? (closedBugs * 100 / totalBugs) : 0);

        // 按严重等级统计
        Map<String, Long> severityStats = new HashMap<>();
        severityStats.put("致命", bugRepository.countBySeverity(QaBug.Severity.FATAL));
        severityStats.put("严重", bugRepository.countBySeverity(QaBug.Severity.CRITICAL));
        severityStats.put("一般", bugRepository.countBySeverity(QaBug.Severity.NORMAL));
        severityStats.put("轻微", bugRepository.countBySeverity(QaBug.Severity.MINOR));
        stats.put("severityStats", severityStats);

        return stats;
    }

    private String generateCaseNo() {
        String prefix = "TC";
        long count = testCaseRepository.count() + 1;
        return String.format("%s-%05d", prefix, count);
    }

    private String generateBugNo() {
        String prefix = "BUG";
        long count = bugRepository.count() + 1;
        return String.format("%s-%05d", prefix, count);
    }
}
