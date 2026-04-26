package com.ipd.platform.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 项目实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "prj_project")
public class PrjProject extends BaseEntity {

    /**
     * 项目编号（唯一）
     */
    @Column(name = "project_no", nullable = false, unique = true, length = 50)
    private String projectNo;

    /**
     * 项目名称
     */
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    /**
     * 项目描述
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * 项目类型：1-敏捷，2-瀑布，3-看板
     */
    @Column(name = "type")
    @Builder.Default
    private Integer type = 1;

    /**
     * IPD阶段：1-概念，2-计划，3-开发，4-发布
     */
    @Column(name = "stage")
    @Builder.Default
    private Integer stage = 1;

    /**
     * 项目状态：1-未开始，2-进行中，3-暂停，4-已完成，5-已归档
     */
    @Column(name = "status")
    @Builder.Default
    private Integer status = 1;

    /**
     * 所属产品线ID
     */
    @Column(name = "product_id")
    private Long productId;

    /**
     * 开始日期
     */
    @Column(name = "start_date")
    private LocalDate startDate;

    /**
     * 结束日期
     */
    @Column(name = "end_date")
    private LocalDate endDate;

    /**
     * 预算
     */
    @Column(name = "budget", precision = 15, scale = 2)
    private BigDecimal budget;

    /**
     * 项目进度（0-100）
     */
    @Column(name = "progress")
    @Builder.Default
    private Integer progress = 0;

    /**
     * 项目经理ID
     */
    @Column(name = "manager_id")
    private Long managerId;

    /**
     * 项目经理姓名（不映射数据库）
     */
    @Transient
    private String managerName;

    // ==================== 枚举常量 ====================

    /**
     * 项目类型枚举
     */
    public static final class Type {
        public static final int AGILE = 1;     // 敏捷
        public static final int WATERFALL = 2; // 瀑布
        public static final int KANBAN = 3;   // 看板

        public static String getName(Integer type) {
            if (type == null) return "未知";
            return switch (type) {
                case AGILE -> "敏捷开发";
                case WATERFALL -> "瀑布流";
                case KANBAN -> "看板";
                default -> "未知";
            };
        }
    }

    /**
     * IPD阶段枚举
     */
    public static final class Stage {
        public static final int CONCEPT = 1;   // 概念阶段
        public static final int PLAN = 2;      // 计划阶段
        public static final int DEVELOP = 3;   // 开发阶段
        public static final int VERIFY = 4;    // 验证阶段
        public static final int LAUNCH = 5;    // 发布阶段

        /**
         * 阶段推进所需的评审类型映射
         * 概念→计划: CDCP, 计划→开发: PDCP, 开发→验证: TR4, 验证→发布: ADCP
         */
        public static Integer getRequiredReviewType(int currentStage) {
            return switch (currentStage) {
                case CONCEPT -> 1;  // CDCP
                case PLAN -> 2;     // PDCP
                case DEVELOP -> 3;  // TR4
                case VERIFY -> 4;   // ADCP
                default -> null;
            };
        }

        public static String getRequiredReviewName(int currentStage) {
            return switch (currentStage) {
                case CONCEPT -> "CDCP评审";
                case PLAN -> "PDCP评审";
                case DEVELOP -> "TR4评审";
                case VERIFY -> "ADCP评审";
                default -> null;
            };
        }

        public static String getName(Integer stage) {
            if (stage == null) return "未知";
            return switch (stage) {
                case CONCEPT -> "概念阶段";
                case PLAN -> "计划阶段";
                case DEVELOP -> "开发阶段";
                case VERIFY -> "验证阶段";
                case LAUNCH -> "发布阶段";
                default -> "未知";
            };
        }

        public static String getNameEn(Integer stage) {
            if (stage == null) return "Unknown";
            return switch (stage) {
                case CONCEPT -> "Concept";
                case PLAN -> "Plan";
                case DEVELOP -> "Develop";
                case VERIFY -> "Verify";
                case LAUNCH -> "Launch";
                default -> "Unknown";
            };
        }
    }

    /**
     * 项目状态枚举
     */
    public static final class Status {
        public static final int NOT_STARTED = 1;  // 未开始
        public static final int IN_PROGRESS = 2;   // 进行中
        public static final int PAUSED = 3;        // 暂停
        public static final int COMPLETED = 4;      // 已完成
        public static final int ARCHIVED = 5;       // 已归档

        public static String getName(Integer status) {
            if (status == null) return "未知";
            return switch (status) {
                case NOT_STARTED -> "未开始";
                case IN_PROGRESS -> "进行中";
                case PAUSED -> "暂停";
                case COMPLETED -> "已完成";
                case ARCHIVED -> "已归档";
                default -> "未知";
            };
        }
    }
}
