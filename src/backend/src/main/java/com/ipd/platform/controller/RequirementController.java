package com.ipd.platform.controller;

import com.ipd.platform.dto.ApiResponse;
import com.ipd.platform.entity.ReqRequirement;
import com.ipd.platform.service.RequirementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 需求管理控制器
 */
@Tag(name = "需求管理", description = "需求CRUD操作")
@RestController
@RequestMapping("/requirements")
@RequiredArgsConstructor
public class RequirementController {

    private final RequirementService requirementService;

    @Operation(summary = "获取需求列表")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PM', 'PGM', 'DEV', 'QA', 'VIEWER')")
    public ResponseEntity<ApiResponse<Page<ReqRequirement>>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer priority,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<ReqRequirement> list = requirementService.list(keyword, productId, status, priority, page, size);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @Operation(summary = "获取需求详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PM', 'PGM', 'DEV', 'QA', 'VIEWER')")
    public ResponseEntity<ApiResponse<ReqRequirement>> getById(@PathVariable Long id) {
        try {
            ReqRequirement requirement = requirementService.getById(id);
            return ResponseEntity.ok(ApiResponse.success(requirement));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(ApiResponse.notFound(e.getMessage()));
        }
    }

    @Operation(summary = "创建需求")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PM', 'PGM', 'DEV')")
    public ResponseEntity<ApiResponse<ReqRequirement>> create(@RequestBody ReqRequirement requirement) {
        try {
            ReqRequirement created = requirementService.create(requirement);
            return ResponseEntity.ok(ApiResponse.success("创建成功", created));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @Operation(summary = "更新需求")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PM', 'PGM')")
    public ResponseEntity<ApiResponse<ReqRequirement>> update(@PathVariable Long id, @RequestBody ReqRequirement requirement) {
        try {
            ReqRequirement updated = requirementService.update(id, requirement);
            return ResponseEntity.ok(ApiResponse.success("更新成功", updated));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(ApiResponse.notFound(e.getMessage()));
        }
    }

    @Operation(summary = "更新需求状态")
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'PM', 'PGM', 'DEV', 'QA')")
    public ResponseEntity<ApiResponse<ReqRequirement>> updateStatus(@PathVariable Long id, @RequestParam String status) {
        try {
            ReqRequirement updated = requirementService.updateStatus(id, status);
            return ResponseEntity.ok(ApiResponse.success("状态更新成功", updated));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(ApiResponse.notFound(e.getMessage()));
        }
    }

    @Operation(summary = "删除需求")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PM')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        try {
            requirementService.delete(id);
            return ResponseEntity.ok(ApiResponse.success("删除成功", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(ApiResponse.notFound(e.getMessage()));
        }
    }

    @Operation(summary = "按产品线获取需求")
    @GetMapping("/product/{productId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PM', 'PGM', 'VIEWER')")
    public ResponseEntity<ApiResponse<List<ReqRequirement>>> getByProduct(@PathVariable Long productId) {
        List<ReqRequirement> list = requirementService.getByProductId(productId);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @Operation(summary = "按项目获取需求")
    @GetMapping("/project/{projectId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PM', 'PGM', 'DEV', 'QA', 'VIEWER')")
    public ResponseEntity<ApiResponse<List<ReqRequirement>>> getByProject(@PathVariable Long projectId) {
        List<ReqRequirement> list = requirementService.getByProjectId(projectId);
        return ResponseEntity.ok(ApiResponse.success(list));
    }
}
