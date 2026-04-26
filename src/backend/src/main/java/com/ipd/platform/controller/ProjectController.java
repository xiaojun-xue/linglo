package com.ipd.platform.controller;

import com.ipd.platform.dto.ApiResponse;
import com.ipd.platform.entity.PrjProject;
import com.ipd.platform.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 项目管理控制器
 */
@Tag(name = "项目管理", description = "项目CRUD操作")
@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @Operation(summary = "获取项目列表")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PM', 'PGM', 'DEV', 'QA', 'VIEWER')")
    public ResponseEntity<ApiResponse<Page<PrjProject>>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer stage,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<PrjProject> list = projectService.list(keyword, productId, status, stage, page, size);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @Operation(summary = "获取项目详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PM', 'PGM', 'DEV', 'QA', 'VIEWER')")
    public ResponseEntity<ApiResponse<PrjProject>> getById(@PathVariable Long id) {
        try {
            PrjProject project = projectService.getById(id);
            return ResponseEntity.ok(ApiResponse.success(project));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(ApiResponse.notFound(e.getMessage()));
        }
    }

    @Operation(summary = "创建项目")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PM', 'PGM')")
    public ResponseEntity<ApiResponse<PrjProject>> create(@RequestBody PrjProject project) {
        try {
            PrjProject created = projectService.create(project);
            return ResponseEntity.ok(ApiResponse.success("创建成功", created));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @Operation(summary = "更新项目")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PM', 'PGM')")
    public ResponseEntity<ApiResponse<PrjProject>> update(@PathVariable Long id, @RequestBody PrjProject project) {
        try {
            PrjProject updated = projectService.update(id, project);
            return ResponseEntity.ok(ApiResponse.success("更新成功", updated));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(ApiResponse.notFound(e.getMessage()));
        }
    }

    @Operation(summary = "更新项目阶段")
    @PutMapping("/{id}/stage")
    @PreAuthorize("hasAnyRole('ADMIN', 'PGM')")
    public ResponseEntity<ApiResponse<PrjProject>> updateStage(@PathVariable Long id, @RequestParam Integer stage) {
        try {
            PrjProject updated = projectService.updateStage(id, stage);
            return ResponseEntity.ok(ApiResponse.success("阶段更新成功", updated));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(ApiResponse.notFound(e.getMessage()));
        }
    }

    @Operation(summary = "推进项目阶段（IPD评审门控）")
    @PutMapping("/{id}/advance-stage")
    @PreAuthorize("hasAnyRole('ADMIN', 'PGM')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> advanceStage(
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean force) {
        try {
            Map<String, Object> result = projectService.advanceStage(id, force);
            boolean advanced = (boolean) result.get("advanced");
            if (advanced) {
                return ResponseEntity.ok(ApiResponse.success("阶段推进成功", result));
            } else {
                return ResponseEntity.ok(ApiResponse.success(result));
            }
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @Operation(summary = "删除项目")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        try {
            projectService.delete(id);
            return ResponseEntity.ok(ApiResponse.success("删除成功", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(ApiResponse.notFound(e.getMessage()));
        }
    }

    @Operation(summary = "获取项目统计")
    @GetMapping("/{id}/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'PM', 'PGM', 'VIEWER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStatistics(@PathVariable Long id) {
        Map<String, Object> stats = projectService.getStatistics(id);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
