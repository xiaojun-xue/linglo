package com.ipd.platform.controller;

import com.ipd.platform.dto.ApiResponse;
import com.ipd.platform.entity.SysRole;
import com.ipd.platform.repository.SysRoleRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理控制器
 */
@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
@Tag(name = "角色管理", description = "角色CRUD管理")
public class RoleController {

    private final SysRoleRepository roleRepository;

    @Operation(summary = "获取角色列表")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PGM')")
    public ResponseEntity<ApiResponse<List<SysRole>>> list() {
        List<SysRole> roles = roleRepository.findAll();
        return ResponseEntity.ok(ApiResponse.success(roles));
    }

    @Operation(summary = "获取角色详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SysRole>> getById(@PathVariable Long id) {
        return roleRepository.findById(id)
                .map(role -> ResponseEntity.ok(ApiResponse.success(role)))
                .orElse(ResponseEntity.status(404).body(ApiResponse.notFound("角色不存在")));
    }

    @Operation(summary = "创建角色")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SysRole>> create(@RequestBody SysRole role) {
        if (roleRepository.existsByRoleCode(role.getRoleCode())) {
            return ResponseEntity.badRequest().body(ApiResponse.badRequest("角色代码已存在"));
        }
        SysRole saved = roleRepository.save(role);
        return ResponseEntity.ok(ApiResponse.success("创建成功", saved));
    }

    @Operation(summary = "更新角色")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SysRole>> update(@PathVariable Long id, @RequestBody SysRole role) {
        return roleRepository.findById(id)
                .map(existing -> {
                    if (role.getRoleName() != null) existing.setRoleName(role.getRoleName());
                    if (role.getDescription() != null) existing.setDescription(role.getDescription());
                    if (role.getStatus() != null) existing.setStatus(role.getStatus());
                    SysRole saved = roleRepository.save(existing);
                    return ResponseEntity.<ApiResponse<SysRole>>ok(ApiResponse.success("更新成功", saved));
                })
                .orElse(ResponseEntity.status(404).body(ApiResponse.notFound("角色不存在")));
    }

    @Operation(summary = "删除角色")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        if (!roleRepository.existsById(id)) {
            return ResponseEntity.status(404).body(ApiResponse.notFound("角色不存在"));
        }
        // 防止删除管理员角色
        SysRole role = roleRepository.getReferenceById(id);
        if ("ADMIN".equals(role.getRoleCode())) {
            return ResponseEntity.badRequest().body(ApiResponse.badRequest("不能删除管理员角色"));
        }
        roleRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success("删除成功", null));
    }
}
