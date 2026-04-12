package com.ipd.platform.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.HashSet;
import java.util.Set;

/**
 * 系统角色实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "sys_role")
public class SysRole extends BaseEntity {

    /**
     * 角色代码（唯一）
     */
    @Column(name = "role_code", nullable = false, unique = true, length = 50)
    private String roleCode;

    /**
     * 角色名称
     */
    @Column(name = "role_name", nullable = false, length = 100)
    private String roleName;

    /**
     * 角色描述
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * 状态：0-禁用，1-正常
     */
    @Column(name = "status", nullable = false)
    @Builder.Default
    private Integer status = 1;

    /**
     * 排序号
     */
    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 0;

    /**
     * 角色权限关联（多对多）
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "sys_role_permission",
        joinColumns = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    @Builder.Default
    private Set<SysPermission> permissions = new HashSet<>();

    /**
     * 预定义角色代码
     */
    public static final class RoleCode {
        public static final String ADMIN = "ADMIN";           // 超级管理员
        public static final String PRODUCT_MANAGER = "PM";    // 产品经理
        public static final String PROJECT_MANAGER = "PGM";   // 项目经理
        public static final String TECH_LEAD = "TL";          // 开发负责人
        public static final String DEVELOPER = "DEV";          // 开发者
        public static final String QA_MANAGER = "QAM";        // 测试经理
        public static final String QA = "QA";                  // 测试人员
        public static final String OPS = "OPS";               // 运维人员
        public static final String EXECUTIVE = "EXEC";        // 高层管理
        public static final String VIEWER = "VIEWER";          // 访客/查看者

        private RoleCode() {}
    }
}
