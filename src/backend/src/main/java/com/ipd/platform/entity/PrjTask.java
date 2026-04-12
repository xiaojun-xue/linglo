package com.ipd.platform.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 任务实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "prj_task")
public class PrjTask extends BaseEntity {

    /**
     * 任务编号（唯一）
     */
    @Column(name = "task_no", nullable = false, unique = true, length = 50)
    private String taskNo;

    /**
     * 任务标题
     */
    @Column(name = "title", nullable = false, length = 500)
    private String title;

    /**
     * 任务描述
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * 任务类型：1-开发，2-测试，3-设计，4-文档，5-其他
     */
    @Column(name = "type")
    @Builder.Default
    private Integer type = 1;

    /**
     * 优先级：0-紧急，1-高，2-中，3-低
     */
    @Column(name = "priority")
    @Builder.Default
    private Integer priority = 2;

    /**
     * 状态
     */
    @Column(name = "status", length = 30)
    @Builder.Default
    private String status = "todo";

    /**
     * 所属项目ID
     */
    @Column(name = "project_id", nullable = false)
    private Long projectId;

    /**
     * 所属Sprint ID
     */
    @Column(name = "sprint_id")
    private Long sprintId;

    /**
     * 关联需求ID
     */
    @Column(name = "requirement_id")
    private Long requirementId;

    /**
     * 父任务ID（支持多级拆分）
     */
    @Column(name = "parent_id")
    private Long parentId;

    /**
     * 负责人ID
     */
    @Column(name = "assignee_id")
    private Long assigneeId;

    /**
     * 负责人姓名（不映射数据库）
     */
    @Transient
    private String assigneeName;

    /**
     * 报告人ID
     */
    @Column(name = "reporter_id")
    private Long reporterId;

    /**
     * 估算工时（小时）
     */
    @Column(name = "estimate_hours", precision = 10, scale = 2)
    private BigDecimal estimateHours;

    /**
     * 实际工时（小时）
     */
    @Column(name = "actual_hours", precision = 10, scale = 2)
    private BigDecimal actualHours;

    /**
     * 开始日期
     */
    @Column(name = "start_date")
    private LocalDate startDate;

    /**
     * 截止日期
     */
    @Column(name = "due_date")
    private LocalDate dueDate;

    /**
     * 完成时间
     */
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    /**
     * 排序号
     */
    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 0;

    // ==================== 枚举常量 ====================

    /**
     * 任务类型枚举
     */
    public static final class Type {
        public static final int DEVELOP = 1;    // 开发
        public static final int TEST = 2;       // 测试
        public static final int DESIGN = 3;     // 设计
        public static final int DOC = 4;       // 文档
        public static final int OTHER = 5;      // 其他

        public static String getName(Integer type) {
            if (type == null) return "未知";
            return switch (type) {
                case DEVELOP -> "开发";
                case TEST -> "测试";
                case DESIGN -> "设计";
                case DOC -> "文档";
                case OTHER -> "其他";
                default -> "未知";
            };
        }
    }

    /**
     * 任务优先级枚举
     */
    public static final class Priority {
        public static final int URGENT = 0;  // 紧急
        public static final int HIGH = 1;    // 高
        public static final int MEDIUM = 2;  // 中
        public static final int LOW = 3;     // 低

        public static String getName(Integer priority) {
            if (priority == null) return "未知";
            return switch (priority) {
                case URGENT -> "紧急";
                case HIGH -> "高";
                case MEDIUM -> "中";
                case LOW -> "低";
                default -> "未知";
            };
        }
    }

    /**
     * 任务状态枚举
     */
    public static final class Status {
        public static final String TODO = "todo";         // 待办
        public static final String IN_PROGRESS = "in_progress";  // 进行中
        public static final String IN_TEST = "in_test";   // 待测试
        public static final String DONE = "done";         // 完成
        public static final String CLOSED = "closed";     // 已关闭

        public static String getName(String status) {
            if (status == null) return "未知";
            return switch (status) {
                case TODO -> "待办";
                case IN_PROGRESS -> "进行中";
                case IN_TEST -> "待测试";
                case DONE -> "完成";
                case CLOSED -> "已关闭";
                default -> status;
            };
        }
    }
}
