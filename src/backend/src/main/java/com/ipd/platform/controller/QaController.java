package com.ipd.platform.controller;

import com.ipd.platform.dto.ApiResponse;
import com.ipd.platform.entity.QaBug;
import com.ipd.platform.entity.QaTestCase;
import com.ipd.platform.service.QaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * QA 测试管理控制器
 */
@Tag(name = "测试管理", description = "测试用例和缺陷管理")
@RestController
@RequestMapping("/qa")
@RequiredArgsConstructor
public class QaController {

    private final QaService qaService;

    // ==================== 测试用例 ====================

    @Operation(summary = "获取测试用例列表")
    @GetMapping("/cases")
    @PreAuthorize("hasAnyRole('ADMIN', 'PM', 'PGM', 'QA', 'DEV', 'VIEWER')")
    public ResponseEntity<ApiResponse<Page<QaTestCase>>> listCases(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<QaTestCase> list = qaService.listTestCases(keyword, projectId, module, status, page, size);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @Operation(summary = "获取测试用例详情")
    @GetMapping("/cases/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PM', 'PGM', 'QA', 'DEV', 'VIEWER')")
    public ResponseEntity<ApiResponse<QaTestCase>> getCase(@PathVariable Long id) {
        try {
            QaTestCase testCase = qaService.getTestCase(id);
            return ResponseEntity.ok(ApiResponse.success(testCase));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(ApiResponse.notFound(e.getMessage()));
        }
    }

    @Operation(summary = "创建测试用例")
    @PostMapping("/cases")
    @PreAuthorize("hasAnyRole('ADMIN', 'PM', 'QAM', 'QA')")
    public ResponseEntity<ApiResponse<QaTestCase>> createCase(@RequestBody QaTestCase testCase) {
        try {
            QaTestCase created = qaService.createTestCase(testCase);
            return ResponseEntity.ok(ApiResponse.success("创建成功", created));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @Operation(summary = "更新测试用例")
    @PutMapping("/cases/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PM', 'QAM', 'QA')")
    public ResponseEntity<ApiResponse<QaTestCase>> updateCase(@PathVariable Long id, @RequestBody QaTestCase testCase) {
        try {
            QaTestCase updated = qaService.updateTestCase(id, testCase);
            return ResponseEntity.ok(ApiResponse.success("更新成功", updated));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(ApiResponse.notFound(e.getMessage()));
        }
    }

    @Operation(summary = "删除测试用例")
    @DeleteMapping("/cases/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'QAM')")
    public ResponseEntity<ApiResponse<Void>> deleteCase(@PathVariable Long id) {
        try {
            qaService.deleteTestCase(id);
            return ResponseEntity.ok(ApiResponse.success("删除成功", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(ApiResponse.notFound(e.getMessage()));
        }
    }

    // ==================== 缺陷管理 ====================

    @Operation(summary = "获取缺陷列表")
    @GetMapping("/bugs")
    @PreAuthorize("hasAnyRole('ADMIN', 'PM', 'PGM', 'QA', 'DEV', 'VIEWER')")
    public ResponseEntity<ApiResponse<Page<QaBug>>> listBugs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Integer severity,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<QaBug> list = qaService.listBugs(keyword, projectId, severity, status, page, size);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @Operation(summary = "获取缺陷详情")
    @GetMapping("/bugs/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PM', 'PGM', 'QA', 'DEV', 'VIEWER')")
    public ResponseEntity<ApiResponse<QaBug>> getBug(@PathVariable Long id) {
        try {
            QaBug bug = qaService.getBug(id);
            return ResponseEntity.ok(ApiResponse.success(bug));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(ApiResponse.notFound(e.getMessage()));
        }
    }

    @Operation(summary = "创建缺陷")
    @PostMapping("/bugs")
    @PreAuthorize("hasAnyRole('ADMIN', 'PM', 'PGM', 'QA', 'DEV')")
    public ResponseEntity<ApiResponse<QaBug>> createBug(@RequestBody QaBug bug) {
        try {
            QaBug created = qaService.createBug(bug);
            return ResponseEntity.ok(ApiResponse.success("创建成功", created));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @Operation(summary = "更新缺陷")
    @PutMapping("/bugs/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PM', 'PGM', 'QA', 'DEV')")
    public ResponseEntity<ApiResponse<QaBug>> updateBug(@PathVariable Long id, @RequestBody QaBug bug) {
        try {
            QaBug updated = qaService.updateBug(id, bug);
            return ResponseEntity.ok(ApiResponse.success("更新成功", updated));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(ApiResponse.notFound(e.getMessage()));
        }
    }

    @Operation(summary = "更新缺陷状态")
    @PutMapping("/bugs/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'PGM', 'QA', 'DEV')")
    public ResponseEntity<ApiResponse<QaBug>> updateBugStatus(@PathVariable Long id, @RequestParam Integer status) {
        try {
            QaBug updated = qaService.updateBugStatus(id, status);
            return ResponseEntity.ok(ApiResponse.success("状态更新成功", updated));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(ApiResponse.notFound(e.getMessage()));
        }
    }

    @Operation(summary = "删除缺陷")
    @DeleteMapping("/bugs/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'QAM')")
    public ResponseEntity<ApiResponse<Void>> deleteBug(@PathVariable Long id) {
        try {
            qaService.deleteBug(id);
            return ResponseEntity.ok(ApiResponse.success("删除成功", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(ApiResponse.notFound(e.getMessage()));
        }
    }

    // ==================== 质量统计 ====================

    @Operation(summary = "获取质量统计")
    @GetMapping("/quality/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'PM', 'PGM', 'QAM', 'VIEWER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getQualityStats(@RequestParam(required = false) Long projectId) {
        Map<String, Object> stats = qaService.getQualityStats(projectId);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
