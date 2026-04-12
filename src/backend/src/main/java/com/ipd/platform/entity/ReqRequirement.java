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
    private String status = "collected";

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
     * 父需求ID（支持需求分解）
     */
    @Column(name = "parent_id")
    private Long parentId;

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
     * 需求状态枚举
     */
    public static final class Status {
        public static final String COLLECTED = "collected";     // 收集
        public static final String ANALYZED = "analyzed";       // 已分析
        public static final String REVIEWED = "reviewed";        // 已评审
        public static final String DESIGN = "design";            // 设计中
        public static final String DEVELOP = "develop";          // 开发中
        public static final String TEST = "test";                // 测试中
        public static final String RELEASED = "released";       // 已发布
        public static final String CLOSED = "closed";            // 已关闭

        public static String getName(String status) {
            if (status == null) return "未知";
            return switch (status) {
                case COLLECTED -> "收集";
                case ANALYZED -> "已分析";
                case REVIEWED -> "已评审";
                case DESIGN -> "设计";
                case DEVELOP -> "开发";
                case TEST -> "测试";
                case RELEASED -> "已发布";
                case CLOSED -> "已关闭";
                default -> status;
            };
        }
    }
}
