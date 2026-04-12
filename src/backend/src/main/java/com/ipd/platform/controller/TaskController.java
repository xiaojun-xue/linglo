package com.ipd.platform.controller;

import com.ipd.platform.dto.ApiResponse;
import com.ipd.platform.entity.PrjTask;
import com.ipd.platform.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 任务管理控制器
 */
@Tag(name = "任务管理", description = "任务CRUD操作")
@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @Operation(summary = "获取任务列表")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PM', 'PGM', 'DEV', 'QA', 'VIEWER')")
    public ResponseEntity<ApiResponse<Page<PrjTask>>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Long sprintId,
            @RequestParam(required = false) Long assigneeId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<PrjTask> list = taskService.list(keyword, projectId, sprintId, assigneeId, status, page, size);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @Operation(summary = "获取任务详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PM', 'PGM', 'DEV', 'QA', 'VIEWER')")
    public ResponseEntity<ApiResponse<PrjTask>> getById(@PathVariable Long id) {
        try {
            PrjTask task = taskService.getById(id);
            return ResponseEntity.ok(ApiResponse.success(task));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(ApiResponse.notFound(e.getMessage()));
        }
    }

    @Operation(summary = "创建任务")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PM', 'PGM', 'DEV', 'QA')")
    public ResponseEntity<ApiResponse<PrjTask>> create(@RequestBody PrjTask task) {
        try {
            PrjTask created = taskService.create(task);
            return ResponseEntity.ok(ApiResponse.success("创建成功", created));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }

    @Operation(summary = "更新任务")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PGM', 'DEV', 'QA')")
    public ResponseEntity<ApiResponse<PrjTask>> update(@PathVariable Long id, @RequestBody PrjTask task) {
        try {
            PrjTask updated = taskService.update(id, task);
            return ResponseEntity.ok(ApiResponse.success("更新成功", updated));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(ApiResponse.notFound(e.getMessage()));
        }
    }

    @Operation(summary = "更新任务状态")
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'PGM', 'DEV', 'QA')")
    public ResponseEntity<ApiResponse<PrjTask>> updateStatus(@PathVariable Long id, @RequestParam String status) {
        try {
            PrjTask updated = taskService.updateStatus(id, status);
            return ResponseEntity.ok(ApiResponse.success("状态更新成功", updated));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(ApiResponse.notFound(e.getMessage()));
        }
    }

    @Operation(summary = "分配任务")
    @PutMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('ADMIN', 'PGM', 'PM')")
    public ResponseEntity<ApiResponse<PrjTask>> assign(@PathVariable Long id, @RequestParam Long assigneeId) {
        try {
            PrjTask updated = taskService.assign(id, assigneeId);
            return ResponseEntity.ok(ApiResponse.success("分配成功", updated));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(ApiResponse.notFound(e.getMessage()));
        }
    }

    @Operation(summary = "删除任务")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PGM')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        try {
            taskService.delete(id);
            return ResponseEntity.ok(ApiResponse.success("删除成功", null));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(ApiResponse.notFound(e.getMessage()));
        }
    }

    @Operation(summary = "获取看板视图")
    @GetMapping("/board")
    @PreAuthorize("hasAnyRole('ADMIN', 'PM', 'PGM', 'DEV', 'QA', 'VIEWER')")
    public ResponseEntity<ApiResponse<Map<String, List<PrjTask>>>> getBoardView(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Long sprintId) {
        Map<String, List<PrjTask>> board = taskService.getBoardView(projectId, sprintId);
        return ResponseEntity.ok(ApiResponse.success(board));
    }

    @Operation(summary = "获取燃尽图数据")
    @GetMapping("/{sprintId}/burndown")
    @PreAuthorize("hasAnyRole('ADMIN', 'PM', 'PGM', 'VIEWER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getBurndownData(@PathVariable Long sprintId) {
        Map<String, Object> data = taskService.getBurndownData(sprintId);
        return ResponseEntity.ok(ApiResponse.success(data));
    }
}
