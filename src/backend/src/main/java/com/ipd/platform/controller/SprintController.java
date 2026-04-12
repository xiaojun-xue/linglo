package com.ipd.platform.controller;

import com.ipd.platform.dto.ApiResponse;
import com.ipd.platform.entity.PrjSprint;
import com.ipd.platform.service.SprintService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Sprint 管理控制器
 */
@Tag(name = "Sprint管理", description = "Sprint迭代管理")
@RestController
@RequestMapping("/sprints")
@RequiredArgsConstructor
public class SprintController {

    private final SprintService sprintService;

    @Operation(summary = "获取项目的Sprint列表")
    @GetMapping("/project/{projectId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PM', 'PGM', 'DEV', 'QA', 'VIEWER')")
    public ResponseEntity<ApiResponse<List<PrjSprint>>> getByProject(@PathVariable Long projectId) {
        List<PrjSprint> list = sprintService.getByProjectId(projectId);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @Operation(summary = "获取Sprint详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PM', 'PGM', 'DEV', 'QA', 'VIEWER')")
    public ResponseEntity<ApiResponse<PrjSprint>> getById(@PathVariable Long id) {
        try {
            PrjSprint sprint = sprintService.getById(id);
            return ResponseEntity.ok(ApiResponse.success(sprint));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(ApiResponse.notFound(e.getMessage()));
        }
    }

    @Operation(summary = "创建Sprint")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PGM')")
    public ResponseEntity<ApiResponse<PrjSprint>> create(@RequestBody PrjSprint sprint) {
        try {
            PrjSprint created = sprintService.create(sprint);
            return ResponseEntity.ok(ApiResponse.success("创建成功", created));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }

    @Operation(summary = "更新Sprint")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PGM')")
    public ResponseEntity<ApiResponse<PrjSprint>> update(@PathVariable Long id, @RequestBody PrjSprint sprint) {
        try {
            PrjSprint updated = sprintService.update(id, sprint);
            return ResponseEntity.ok(ApiResponse.success("更新成功", updated));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(ApiResponse.notFound(e.getMessage()));
        }
    }

    @Operation(summary = "开始Sprint")
    @PutMapping("/{id}/start")
    @PreAuthorize("hasAnyRole('ADMIN', 'PGM')")
    public ResponseEntity<ApiResponse<PrjSprint>> start(@PathVariable Long id) {
        try {
            PrjSprint updated = sprintService.start(id);
            return ResponseEntity.ok(ApiResponse.success("Sprint已开始", updated));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(ApiResponse.badRequest(e.getMessage()));
        }
    }

    @Operation(summary = "完成Sprint")
    @PutMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('ADMIN', 'PGM')")
    public ResponseEntity<ApiResponse<PrjSprint>> complete(@PathVariable Long id) {
        try {
            PrjSprint updated = sprintService.complete(id);
            return ResponseEntity.ok(ApiResponse.success("Sprint已完成", updated));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(ApiResponse.badRequest(e.getMessage()));
        }
    }

    @Operation(summary = "关闭Sprint")
    @PutMapping("/{id}/close")
    @PreAuthorize("hasAnyRole('ADMIN', 'PGM')")
    public ResponseEntity<ApiResponse<PrjSprint>> close(@PathVariable Long id) {
        try {
            PrjSprint updated = sprintService.close(id);
            return ResponseEntity.ok(ApiResponse.success("Sprint已关闭", updated));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(ApiResponse.badRequest(e.getMessage()));
        }
    }

    @Operation(summary = "获取燃尽图数据")
    @GetMapping("/{id}/burndown")
    @PreAuthorize("hasAnyRole('ADMIN', 'PM', 'PGM', 'VIEWER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getBurndownData(@PathVariable Long id) {
        Map<String, Object> data = sprintService.getBurndownData(id);
        return ResponseEntity.ok(ApiResponse.success(data));
    }
}
