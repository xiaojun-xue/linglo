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
 * 系统权限实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "sys_permission")
public class SysPermission extends BaseEntity {

    /**
     * 父权限ID
     */
    @Column(name = "parent_id")
    private Long parentId;

    /**
     * 权限名称
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * 权限代码（唯一）
     */
    @Column(name = "code", nullable = false, unique = true, length = 100)
    private String code;

    /**
     * 权限类型：1-菜单，2-按钮，3-API
     */
    @Column(name = "type", nullable = false)
    private Integer type;

    /**
     * 路由路径
     */
    @Column(name = "path", length = 200)
    private String path;

    /**
     * 组件路径
     */
    @Column(name = "component", length = 200)
    private String component;

    /**
     * 图标
     */
    @Column(name = "icon", length = 100)
    private String icon;

    /**
     * 排序号
     */
    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 0;

    /**
     * 状态：0-禁用，1-正常
     */
    @Column(name = "status", nullable = false)
    @Builder.Default
    private Integer status = 1;

    /**
     * 是否公开（无需授权）
     */
    @Column(name = "is_public")
    @Builder.Default
    private Boolean isPublic = false;

    /**
     * 权限类型枚举
     */
    public static final class Type {
        public static final int MENU = 1;      // 菜单
        public static final int BUTTON = 2;    // 按钮/操作
        public static final int API = 3;       // API接口

        private Type() {}
    }

    /**
     * 预定义权限代码
     */
    public static final class Code {
        // 系统管理
        public static final String SYSTEM_USER = "system:user";
        public static final String SYSTEM_ROLE = "system:role";
        public static final String SYSTEM_PERMISSION = "system:permission";
        public static final String SYSTEM_CONFIG = "system:config";

        // 产品管理
        public static final String PRODUCT_LIST = "product:list";
        public static final String PRODUCT_CREATE = "product:create";
        public static final String PRODUCT_EDIT = "product:edit";
        public static final String PRODUCT_DELETE = "product:delete";

        // 需求管理
        public static final String REQ_LIST = "req:list";
        public static final String REQ_CREATE = "req:create";
        public static final String REQ_EDIT = "req:edit";
        public static final String REQ_DELETE = "req:delete";
        public static final String REQ_ASSIGN = "req:assign";

        // 项目管理
        public static final String PROJECT_LIST = "project:list";
        public static final String PROJECT_CREATE = "project:create";
        public static final String PROJECT_EDIT = "project:edit";
        public static final String PROJECT_DELETE = "project:delete";
        public static final String PROJECT_VIEW = "project:view";

        // 任务管理
        public static final String TASK_LIST = "task:list";
        public static final String TASK_CREATE = "task:create";
        public static final String TASK_EDIT = "task:edit";
        public static final String TASK_DELETE = "task:delete";
        public static final String TASK_ASSIGN = "task:assign";
        public static final String TASK_STATUS = "task:status";

        // 评审管理
        public static final String REVIEW_LIST = "review:list";
        public static final String REVIEW_CREATE = "review:create";
        public static final String REVIEW_EDIT = "review:edit";
        public static final String REVIEW_VOTE = "review:vote";
        public static final String REVIEW_DECISION = "review:decision";

        // 测试管理
        public static final String TEST_LIST = "test:list";
        public static final String TEST_CREATE = "test:create";
        public static final String TEST_EDIT = "test:edit";
        public static final String TEST_EXECUTE = "test:execute";

        // 文档管理
        public static final String DOC_LIST = "doc:list";
        public static final String DOC_CREATE = "doc:create";
        public static final String DOC_EDIT = "doc:edit";
        public static final String DOC_DELETE = "doc:delete";

        // 报表
        public static final String REPORT_VIEW = "report:view";
        public static final String REPORT_EXPORT = "report:export";

        private Code() {}
    }
}
