package com.ipd.platform.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;

/**
 * 需求实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "req_requirement")
public class ReqRequirement extends BaseEntity {

    /**
     * 需求编号（唯一）
     */
    @Column(name = "req_no", nullable = false, unique = true, length = 50)
    private String reqNo;

    /**
     * 需求标题
     */
    @Column(name = "title", nullable = false, length = 500)
    private String title;

    /**
     * 需求描述
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * 验收标准
     */
    @Column(name = "acceptance", columnDefinition = "TEXT")
    private String acceptance;

    /**
     * 需求类型：1-功能，2-技术，3-体验，4-缺陷
     */
    @Column(name = "type")
    @Builder.Default
    private Integer type = 1;

    /**
     * 需求来源：1-客户，2-销售，3-内部，4-竞品，5-战略
     */
    @Column(name = "source")
    @Builder.Default
    private Integer source = 3;

    /**
     * 优先级：0-P0必须，1-P1重要，2-P2一般，3-P3可选
     */
    @Column(name = "priority")
    @Builder.Default
    private Integer priority = 2;

    /**
     * 估算故事点
     */
    @Column(name = "estimate_point", precision = 5, scale = 2)
    private BigDecimal estimatePoint;

    /**
     * 估算人天
     */
    @Column(name = "estimate_hours", precision = 10, scale = 2)
    private BigDecimal estimateHours;

    /**
     * 状态
     */
    @Column(name = "status", length = 30)
    @Builder.Default
    private String status = "new";

    /**
     * 所属产品线ID
     */
    @Column(name = "product_id")
    private Long productId;

    /**
     * 关联项目ID
     */
    @Column(name = "project_id")
    private Long projectId;

    /**
     * 提出人ID
     */
    @Column(name = "creator_id")
    private Long creatorId;

    /**
     * 提出人姓名（不映射数据库）
     */
    @Transient
    private String creatorName;

    /**
     * 负责人ID
     */
    @Column(name = "owner_id")
    private Long ownerId;

    /**
     * 负责人姓名（不映射数据库）
     */
    @Transient
    private String ownerName;

    /**
     * 父需求ID（支持需求分解：IR→SR→AR）
     */
    @Column(name = "parent_id")
    private Long parentId;

    /**
     * 需求层级：1-IR(初始需求)，2-SR(系统需求)，3-AR(分配需求)
     */
    @Column(name = "level")
    @Builder.Default
    private Integer level = 1;

    // ==================== 5W2H 结构化描述（IR层级使用） ====================

    /** Who — 目标用户/干系人 */
    @Column(name = "w_who", length = 500)
    private String who;

    /** What — 需要什么功能/能力 */
    @Column(name = "w_what", columnDefinition = "TEXT")
    private String what;

    /** When — 时间要求/期望时间 */
    @Column(name = "w_when", length = 500)
    private String whenDesc;

    /** Where — 使用场景/环境 */
    @Column(name = "w_where", length = 500)
    private String whereDesc;

    /** Why — 业务动机/价值 */
    @Column(name = "w_why", columnDefinition = "TEXT")
    private String whyDesc;

    /** How — 实现方式/路径 */
    @Column(name = "w_how", columnDefinition = "TEXT")
    private String howDesc;

    /** How Much — 规模/成本/范围 */
    @Column(name = "w_how_much", length = 500)
    private String howMuch;

    // ==================== 枚举常量 ====================

    /**
     * 需求类型枚举
     */
    public static final class Type {
        public static final int FEATURE = 1;     // 功能需求
        public static final int TECHNICAL = 2;   // 技术需求
        public static final int EXPERIENCE = 3;  // 体验需求
        public static final int BUG = 4;        // 缺陷

        public static String getName(Integer type) {
            if (type == null) return "未知";
            return switch (type) {
                case FEATURE -> "功能需求";
                case TECHNICAL -> "技术需求";
                case EXPERIENCE -> "体验需求";
                case BUG -> "缺陷";
                default -> "未知";
            };
        }
    }

    /**
     * 需求来源枚举
     */
    public static final class Source {
        public static final int CUSTOMER = 1;    // 客户
        public static final int SALES = 2;        // 销售
        public static final int INTERNAL = 3;     // 内部
        public static final int COMPETITOR = 4;   // 竞品
        public static final int STRATEGY = 5;     // 战略

        public static String getName(Integer source) {
            if (source == null) return "未知";
            return switch (source) {
                case CUSTOMER -> "客户反馈";
                case SALES -> "销售反馈";
                case INTERNAL -> "内部提出";
                case COMPETITOR -> "竞品分析";
                case STRATEGY -> "战略规划";
                default -> "未知";
            };
        }
    }

    /**
     * 需求优先级枚举
     */
    public static final class Priority {
        public static final int P0 = 0;  // 必须
        public static final int P1 = 1;  // 重要
        public static final int P2 = 2;  // 一般
        public static final int P3 = 3;  // 可选

        public static String getName(Integer priority) {
            if (priority == null) return "未知";
            return switch (priority) {
                case P0 -> "P0 - 必须";
                case P1 -> "P1 - 重要";
                case P2 -> "P2 - 一般";
                case P3 -> "P3 - 可选";
                default -> "未知";
            };
        }
    }

    /**
     * 需求层级枚举
     */
    public static final class Level {
        public static final int IR = 1;   // 初始需求 (Initial Requirement)
        public static final int SR = 2;   // 系统需求 (System Requirement)
        public static final int AR = 3;   // 分配需求 (Allocation Requirement)

        public static String getName(Integer level) {
            if (level == null) return "未知";
            return switch (level) {
                case IR -> "IR-初始需求";
                case SR -> "SR-系统需求";
                case AR -> "AR-分配需求";
                default -> "未知";
            };
        }

        public static String getPrefix(Integer level) {
            if (level == null) return "REQ";
            return switch (level) {
                case IR -> "IR";
                case SR -> "SR";
                case AR -> "AR";
                default -> "REQ";
            };
        }
    }

    /**
     * 需求状态枚举
     */
    public static final class Status {
        public static final String NEW = "new";                 // 新增
        public static final String REVIEWING = "reviewing";     // 评审中
        public static final String ARCHIVED = "archived";       // 已归档
        public static final String TESTING = "testing";         // 测试中
        public static final String ACCEPTED = "accepted";       // 已验收

        public static String getName(String status) {
            if (status == null) return "未知";
            return switch (status) {
                case NEW -> "新增";
                case REVIEWING -> "评审中";
                case ARCHIVED -> "已归档";
                case TESTING -> "测试中";
                case ACCEPTED -> "已验收";
                default -> status;
            };
        }

        /** 获取下一个合法状态 */
        public static String getNextStatus(String current) {
            if (current == null) return null;
            return switch (current) {
                case NEW -> REVIEWING;
                case REVIEWING -> ARCHIVED;
                case ARCHIVED -> TESTING;
                case TESTING -> ACCEPTED;
                default -> null;
            };
        }
    }
}
