package com.ipd.platform.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDate;
import java.math.BigDecimal;

/**
 * Sprint 迭代实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "prj_sprint")
public class PrjSprint extends BaseEntity {

    /**
     * Sprint 编号
     */
    @Column(name = "sprint_no", nullable = false, unique = true, length = 50)
    private String sprintNo;

    /**
     * Sprint 名称
     */
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    /**
     * 关联项目ID
     */
    @Column(name = "project_id", nullable = false)
    private Long projectId;

    /**
     * Sprint 目标
     */
    @Column(name = "goal", columnDefinition = "TEXT")
    private String goal;

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
     * 状态：1-规划，2-进行中，3-已完成，4-已关闭
     */
    @Column(name = "status")
    @Builder.Default
    private Integer status = 1;

    /**
     * 总故事点
     */
    @Column(name = "total_point", precision = 10, scale = 2)
    private BigDecimal totalPoint;

    /**
     * 已完成故事点
     */
    @Column(name = "completed_point", precision = 10, scale = 2)
    private BigDecimal completedPoint;

    /**
     * 总任务数
     */
    @Column(name = "total_tasks")
    private Integer totalTasks;

    /**
     * 已完成任务数
     */
    @Column(name = "completed_tasks")
    private Integer completedTasks;

    // ==================== 枚举常量 ====================

    /**
     * Sprint 状态枚举
     */
    public static final class Status {
        public static final int PLANNING = 1;     // 规划中
        public static final int IN_PROGRESS = 2;   // 进行中
        public static final int COMPLETED = 3;      // 已完成
        public static final int CLOSED = 4;        // 已关闭

        public static String getName(Integer status) {
            if (status == null) return "未知";
            return switch (status) {
                case PLANNING -> "规划中";
                case IN_PROGRESS -> "进行中";
                case COMPLETED -> "已完成";
                case CLOSED -> "已关闭";
                default -> "未知";
            };
        }
    }
}
