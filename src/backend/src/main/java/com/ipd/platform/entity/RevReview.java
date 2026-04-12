package com.ipd.platform.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 评审实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "rev_review")
public class RevReview extends BaseEntity {

    /**
     * 评审编号（唯一）
     */
    @Column(name = "review_no", nullable = false, unique = true, length = 50)
    private String reviewNo;

    /**
     * 评审标题
     */
    @Column(name = "title", nullable = false, length = 500)
    private String title;

    /**
     * 评审类型：1-CDCP，2-PDCP，3-TR4，4-ADCP
     */
    @Column(name = "type", nullable = false)
    private Integer type;

    /**
     * 关联的IPD阶段
     */
    @Column(name = "stage")
    private Integer stage;

    /**
     * 关联项目ID
     */
    @Column(name = "project_id")
    private Long projectId;

    /**
     * 评审状态：1-草稿，2-待评审，3-评审中，4-通过，5-条件通过，6-拒绝，7-已撤回
     */
    @Column(name = "status")
    @Builder.Default
    private Integer status = 1;

    /**
     * 最终决策：1-通过，2-条件通过，3-拒绝
     */
    @Column(name = "decision")
    private Integer decision;

    /**
     * 评审结论
     */
    @Column(name = "conclusion", columnDefinition = "TEXT")
    private String conclusion;

    /**
     * 计划评审时间
     */
    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    /**
     * 实际评审时间
     */
    @Column(name = "held_at")
    private LocalDateTime heldAt;

    /**
     * 评审主持人ID
     */
    @Column(name = "host_id")
    private Long hostId;

    // ==================== 枚举常量 ====================

    /**
     * 评审类型枚举
     */
    public static final class Type {
        public static final int CDCP = 1;  // 概念决策评审
        public static final int PDCP = 2;  // 计划决策评审
        public static final int TR4 = 3;   // 技术评审4
        public static final int ADCP = 4;  // 可发布决策评审

        public static String getName(Integer type) {
            if (type == null) return "未知";
            return switch (type) {
                case CDCP -> "CDCP - 概念决策评审";
                case PDCP -> "PDCP - 计划决策评审";
                case TR4 -> "TR4 - 技术评审";
                case ADCP -> "ADCP - 可发布评审";
                default -> "未知";
            };
        }

        public static String getShortName(Integer type) {
            if (type == null) return "未知";
            return switch (type) {
                case CDCP -> "CDCP";
                case PDCP -> "PDCP";
                case TR4 -> "TR4";
                case ADCP -> "ADCP";
                default -> "未知";
            };
        }
    }

    /**
     * 评审状态枚举
     */
    public static final class Status {
        public static final int DRAFT = 1;       // 草稿
        public static final int PENDING = 2;     // 待评审
        public static final int IN_REVIEW = 3;   // 评审中
        public static final int PASSED = 4;      // 通过
        public static final int CONDITIONAL = 5;  // 条件通过
        public static final int REJECTED = 6;    // 拒绝
        public static final int WITHDRAWN = 7;   // 已撤回

        public static String getName(Integer status) {
            if (status == null) return "未知";
            return switch (status) {
                case DRAFT -> "草稿";
                case PENDING -> "待评审";
                case IN_REVIEW -> "评审中";
                case PASSED -> "通过";
                case CONDITIONAL -> "条件通过";
                case REJECTED -> "拒绝";
                case WITHDRAWN -> "已撤回";
                default -> "未知";
            };
        }
    }

    /**
     * 评审决策枚举
     */
    public static final class Decision {
        public static final int PASS = 1;        // 通过
        public static final int CONDITIONAL = 2; // 条件通过
        public static final int REJECT = 3;      // 拒绝

        public static String getName(Integer decision) {
            if (decision == null) return "未知";
            return switch (decision) {
                case PASS -> "通过";
                case CONDITIONAL -> "条件通过";
                case REJECT -> "拒绝";
                default -> "未知";
            };
        }
    }
}
