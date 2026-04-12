package com.ipd.platform.controller;

import com.ipd.platform.dto.ApiResponse;
import com.ipd.platform.entity.DocDocument;
import com.ipd.platform.service.DocService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 文档管理控制器
 */
@Tag(name = "文档管理", description = "项目文档管理")
@RestController
@RequestMapping("/docs")
@RequiredArgsConstructor
public class DocController {

    private final DocService docService;

    @Operation(summary = "获取文档列表")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PM', 'PGM', 'DEV', 'QA', 'VIEWER')")
    public ResponseEntity<ApiResponse<Page<DocDocument>>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Integer category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<DocDocument> list = docService.listDocs(keyword, projectId, category, page, size);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @Operation(summary = "获取文档树")
    @GetMapping("/tree")
    @PreAuthorize("hasAnyRole('ADMIN', 'PM', 'PGM', 'DEV', 'QA', 'VIEWER')")
    public ResponseEntity<ApiResponse<List<DocDocument>>> getTree(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Long parentId) {
        List<DocDocument> tree = docService.getDocTree(projectId, parentId);
        return ResponseEntity.ok(ApiResponse.success(tree));
    }

    @Operation(summary = "获取文档详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PM', 'PGM', 'DEV', 'QA', 'VIEWER')")
    public ResponseEntity<ApiResponse<DocDocument>> getById(@PathVariable Long id) {
        try {
            DocDocument doc = docService.getById(id);
            return ResponseEntity.ok(ApiResponse.success(doc));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(ApiResponse.notFound(e.getMessage()));
        }
    }

    @Operation(summary = "创建文档")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PM', 'PGM', 'DEV', 'QA')")
    public ResponseEntity<ApiResponse<DocDocument>> create(@RequestBody DocDocument doc) {
        try {
            DocDocument created = docService.create(doc);
            return ResponseEntity.ok(ApiResponse.success("创建成功", created));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }

    @Operation(summary = "创建文件夹")
    @PostMapping("/folder")
    @PreAuthorize("hasAnyRole('ADMIN', 'PM', 'PGM', 'DEV', 'QA')")
    public ResponseEntity<ApiResponse<DocDocument>> createFolder(@RequestBody DocDocument folder) {
        try {
            DocDocument created = docService.createFolder(folder);
            return ResponseEntity.ok(ApiResponse.success("创建成功", created));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }

    @Operation(summary = "更新文档")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PM', 'PGM', 'DEV', 'QA')")
    public ResponseEntity<ApiResponse<DocDocument>> update(@PathVariable Long id, @RequestBody DocDocument doc) {
        try {
            DocDocument updated = docService.update(id, doc);
            return ResponseEntity.ok(ApiResponse.success("更新成功", updated));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(ApiResponse.notFound(e.getMessage()));
        }
    }

    @Operation(summary = "删除文档")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PM', 'PGM')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        try {
            docService.delete(id);
            return ResponseEntity.ok(ApiResponse.success("删除成功", null));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(ApiResponse.notFound(e.getMessage()));
        }
    }
}
