package com.ipd.platform.controller;

import com.ipd.platform.dto.ApiResponse;
import com.ipd.platform.entity.RevReview;
import com.ipd.platform.service.ReviewService;
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
 * 评审管理控制器
 */
@Tag(name = "评审管理", description = "IPD评审管理 - CDCP/PDCP/TR4/ADCP")
@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "获取评审列表")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PM', 'PGM', 'TL', 'DEV', 'QA', 'VIEWER')")
    public ResponseEntity<ApiResponse<Page<RevReview>>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<RevReview> list = reviewService.list(keyword, projectId, type, status, page, size);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @Operation(summary = "获取评审详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PM', 'PGM', 'TL', 'DEV', 'QA', 'VIEWER')")
    public ResponseEntity<ApiResponse<RevReview>> getById(@PathVariable Long id) {
        try {
            RevReview review = reviewService.getById(id);
            return ResponseEntity.ok(ApiResponse.success(review));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(ApiResponse.notFound(e.getMessage()));
        }
    }

    @Operation(summary = "创建评审")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PM', 'PGM', 'TL')")
    public ResponseEntity<ApiResponse<RevReview>> create(@RequestBody RevReview review) {
        try {
            RevReview created = reviewService.create(review);
            return ResponseEntity.ok(ApiResponse.success("创建成功", created));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @Operation(summary = "更新评审")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PM', 'PGM', 'TL')")
    public ResponseEntity<ApiResponse<RevReview>> update(@PathVariable Long id, @RequestBody RevReview review) {
        try {
            RevReview updated = reviewService.update(id, review);
            return ResponseEntity.ok(ApiResponse.success("更新成功", updated));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(ApiResponse.notFound(e.getMessage()));
        }
    }

    @Operation(summary = "提交评审")
    @PutMapping("/{id}/submit")
    @PreAuthorize("hasAnyRole('ADMIN', 'PM', 'PGM', 'TL')")
    public ResponseEntity<ApiResponse<RevReview>> submit(@PathVariable Long id) {
        try {
            RevReview updated = reviewService.submit(id);
            return ResponseEntity.ok(ApiResponse.success("提交成功", updated));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(ApiResponse.badRequest(e.getMessage()));
        }
    }

    @Operation(summary = "开始评审")
    @PutMapping("/{id}/start")
    @PreAuthorize("hasAnyRole('ADMIN', 'PM', 'PGM', 'TL', 'DEV', 'QA')")
    public ResponseEntity<ApiResponse<RevReview>> startReview(@PathVariable Long id) {
        try {
            RevReview updated = reviewService.startReview(id);
            return ResponseEntity.ok(ApiResponse.success("评审已开始", updated));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(ApiResponse.badRequest(e.getMessage()));
        }
    }

    @Operation(summary = "评审决策")
    @PutMapping("/{id}/decide")
    @PreAuthorize("hasAnyRole('ADMIN', 'PM', 'PGM', 'TL')")
    public ResponseEntity<ApiResponse<RevReview>> decide(
            @PathVariable Long id,
            @RequestParam Integer decision,
            @RequestParam(required = false) String conclusion) {
        try {
            RevReview updated = reviewService.decide(id, decision, conclusion);
            return ResponseEntity.ok(ApiResponse.success("决策完成", updated));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(ApiResponse.badRequest(e.getMessage()));
        }
    }

    @Operation(summary = "撤回评审")
    @PutMapping("/{id}/withdraw")
    @PreAuthorize("hasAnyRole('ADMIN', 'PM', 'PGM', 'TL')")
    public ResponseEntity<ApiResponse<RevReview>> withdraw(@PathVariable Long id) {
        try {
            RevReview updated = reviewService.withdraw(id);
            return ResponseEntity.ok(ApiResponse.success("已撤回", updated));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(ApiResponse.badRequest(e.getMessage()));
        }
    }

    @Operation(summary = "获取待我评审的列表")
    @GetMapping("/my/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'PM', 'PGM', 'TL', 'DEV', 'QA')")
    public ResponseEntity<ApiResponse<Page<RevReview>>> getMyPending(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<RevReview> list = reviewService.getMyPendingReviews(page, size);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @Operation(summary = "获取评审统计")
    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'PM', 'PGM', 'VIEWER')")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getStatistics() {
        Map<String, Long> stats = reviewService.getStatistics();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
