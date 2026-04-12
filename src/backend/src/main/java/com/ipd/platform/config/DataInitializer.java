package com.ipd.platform.config;

import com.ipd.platform.entity.SysPermission;
import com.ipd.platform.entity.SysRole;
import com.ipd.platform.entity.SysUser;
import com.ipd.platform.repository.SysPermissionRepository;
import com.ipd.platform.repository.SysRoleRepository;
import com.ipd.platform.repository.SysUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 数据初始化组件
 * 启动时创建默认角色、权限和超级管理员
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final SysRoleRepository roleRepository;
    private final SysUserRepository userRepository;
    private final SysPermissionRepository permissionRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        initRoles();
        initPermissions();
        initAdminUser();
        log.info("数据初始化完成");
    }

    /**
     * 初始化角色
     */
    private void initRoles() {
        List<SysRole> defaultRoles = Arrays.asList(
            createRole(SysRole.RoleCode.ADMIN, "超级管理员", "系统超级管理员，拥有所有权限"),
            createRole(SysRole.RoleCode.PRODUCT_MANAGER, "产品经理", "负责产品规划和需求管理"),
            createRole(SysRole.RoleCode.PROJECT_MANAGER, "项目经理", "负责项目管理和进度控制"),
            createRole(SysRole.RoleCode.TECH_LEAD, "开发负责人", "负责技术方案和代码评审"),
            createRole(SysRole.RoleCode.DEVELOPER, "开发者", "负责功能开发和实现"),
            createRole(SysRole.RoleCode.QA_MANAGER, "测试经理", "负责测试策略和质量把关"),
            createRole(SysRole.RoleCode.QA, "测试人员", "负责测试用例执行和缺陷报告"),
            createRole(SysRole.RoleCode.OPS, "运维人员", "负责部署和运维支持"),
            createRole(SysRole.RoleCode.EXECUTIVE, "高层管理", "查看数据报表和决策支持"),
            createRole(SysRole.RoleCode.VIEWER, "访客", "只读访问权限")
        );

        for (SysRole role : defaultRoles) {
            if (!roleRepository.existsByRoleCode(role.getRoleCode())) {
                roleRepository.save(role);
                log.info("创建角色: {}", role.getRoleName());
            }
        }
    }

    private SysRole createRole(String code, String name, String desc) {
        return SysRole.builder()
                .roleCode(code)
                .roleName(name)
                .description(desc)
                .status(1)
                .sortOrder(getRoleSortOrder(code))
                .build();
    }

    private int getRoleSortOrder(String code) {
        return switch (code) {
            case SysRole.RoleCode.ADMIN -> 1;
            case SysRole.RoleCode.PRODUCT_MANAGER -> 2;
            case SysRole.RoleCode.PROJECT_MANAGER -> 3;
            case SysRole.RoleCode.TECH_LEAD -> 4;
            case SysRole.RoleCode.DEVELOPER -> 5;
            case SysRole.RoleCode.QA_MANAGER -> 6;
            case SysRole.RoleCode.QA -> 7;
            case SysRole.RoleCode.OPS -> 8;
            case SysRole.RoleCode.EXECUTIVE -> 9;
            case SysRole.RoleCode.VIEWER -> 10;
            default -> 99;
        };
    }

    /**
     * 初始化权限
     */
    private void initPermissions() {
        List<SysPermission> permissions = Arrays.asList(
            // 系统管理权限
            createPermission(SysPermission.Code.SYSTEM_USER, "用户管理", 1, "system/user", "user"),
            createPermission(SysPermission.Code.SYSTEM_ROLE, "角色管理", 1, "system/role", "team"),
            createPermission(SysPermission.Code.SYSTEM_PERMISSION, "权限管理", 1, "system/permission", "lock"),
            createPermission(SysPermission.Code.SYSTEM_CONFIG, "系统配置", 1, "system/config", "setting"),
            
            // 产品管理权限
            createPermission(SysPermission.Code.PRODUCT_LIST, "产品列表", 1, "product/list", "app"),
            createPermission(SysPermission.Code.PRODUCT_CREATE, "创建产品", 2, null, null),
            createPermission(SysPermission.Code.PRODUCT_EDIT, "编辑产品", 2, null, null),
            createPermission(SysPermission.Code.PRODUCT_DELETE, "删除产品", 2, null, null),
            
            // 需求管理权限
            createPermission(SysPermission.Code.REQ_LIST, "需求列表", 1, "requirement/list", "document"),
            createPermission(SysPermission.Code.REQ_CREATE, "创建需求", 2, null, null),
            createPermission(SysPermission.Code.REQ_EDIT, "编辑需求", 2, null, null),
            createPermission(SysPermission.Code.REQ_DELETE, "删除需求", 2, null, null),
            createPermission(SysPermission.Code.REQ_ASSIGN, "分配需求", 2, null, null),
            
            // 项目管理权限
            createPermission(SysPermission.Code.PROJECT_LIST, "项目列表", 1, "project/list", "folder"),
            createPermission(SysPermission.Code.PROJECT_CREATE, "创建项目", 2, null, null),
            createPermission(SysPermission.Code.PROJECT_EDIT, "编辑项目", 2, null, null),
            createPermission(SysPermission.Code.PROJECT_DELETE, "删除项目", 2, null, null),
            createPermission(SysPermission.Code.PROJECT_VIEW, "查看项目", 1, null, null),
            
            // 任务管理权限
            createPermission(SysPermission.Code.TASK_LIST, "任务列表", 1, "task/list", "list"),
            createPermission(SysPermission.Code.TASK_CREATE, "创建任务", 2, null, null),
            createPermission(SysPermission.Code.TASK_EDIT, "编辑任务", 2, null, null),
            createPermission(SysPermission.Code.TASK_DELETE, "删除任务", 2, null, null),
            createPermission(SysPermission.Code.TASK_ASSIGN, "分配任务", 2, null, null),
            createPermission(SysPermission.Code.TASK_STATUS, "更新状态", 2, null, null),
            
            // 评审管理权限
            createPermission(SysPermission.Code.REVIEW_LIST, "评审列表", 1, "review/list", "audit"),
            createPermission(SysPermission.Code.REVIEW_CREATE, "创建评审", 2, null, null),
            createPermission(SysPermission.Code.REVIEW_EDIT, "编辑评审", 2, null, null),
            createPermission(SysPermission.Code.REVIEW_VOTE, "评审投票", 2, null, null),
            createPermission(SysPermission.Code.REVIEW_DECISION, "评审决策", 2, null, null),
            
            // 测试管理权限
            createPermission(SysPermission.Code.TEST_LIST, "测试列表", 1, "test/list", "bug"),
            createPermission(SysPermission.Code.TEST_CREATE, "创建测试", 2, null, null),
            createPermission(SysPermission.Code.TEST_EDIT, "编辑测试", 2, null, null),
            createPermission(SysPermission.Code.TEST_EXECUTE, "执行测试", 2, null, null),
            
            // 文档管理权限
            createPermission(SysPermission.Code.DOC_LIST, "文档列表", 1, "doc/list", "file-text"),
            createPermission(SysPermission.Code.DOC_CREATE, "创建文档", 2, null, null),
            createPermission(SysPermission.Code.DOC_EDIT, "编辑文档", 2, null, null),
            createPermission(SysPermission.Code.DOC_DELETE, "删除文档", 2, null, null),
            
            // 报表权限
            createPermission(SysPermission.Code.REPORT_VIEW, "查看报表", 1, "report/view", "bar-chart"),
            createPermission(SysPermission.Code.REPORT_EXPORT, "导出报表", 2, null, null)
        );

        for (SysPermission permission : permissions) {
            if (permissionRepository.findByCode(permission.getCode()).isEmpty()) {
                permissionRepository.save(permission);
                log.info("创建权限: {}", permission.getName());
            }
        }

        // 为管理员角色分配所有权限
        roleRepository.findByRoleCode(SysRole.RoleCode.ADMIN).ifPresent(adminRole -> {
            adminRole.setPermissions(new HashSet<>(permissionRepository.findAll()));
            roleRepository.save(adminRole);
            log.info("为管理员分配所有权限");
        });
    }

    private SysPermission createPermission(String code, String name, int type, String path, String icon) {
        return SysPermission.builder()
                .code(code)
                .name(name)
                .type(type)
                .path(path)
                .icon(icon)
                .sortOrder(0)
                .status(1)
                .isPublic(false)
                .build();
    }

    /**
     * 初始化超级管理员
     */
    private void initAdminUser() {
        if (!userRepository.existsByUsername("admin")) {
            SysUser admin = SysUser.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .nickname("系统管理员")
                    .email("admin@ipd-platform.com")
                    .phone("13800138000")
                    .status(1)
                    .userType(1)
                    .build();

            roleRepository.findByRoleCode(SysRole.RoleCode.ADMIN)
                    .ifPresent(adminRole -> admin.getRoles().add(adminRole));

            userRepository.save(admin);
            log.info("创建超级管理员: admin / admin123");
        }
    }
}
