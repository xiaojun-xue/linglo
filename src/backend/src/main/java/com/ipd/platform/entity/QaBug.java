package com.ipd.platform.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * 缺陷实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "qa_bug")
public class QaBug extends BaseEntity {

    /**
     * Bug 编号
     */
    @Column(name = "bug_no", nullable = false, unique = true, length = 50)
    private String bugNo;

    /**
     * Bug 标题
     */
    @Column(name = "title", nullable = false, length = 500)
    private String title;

    /**
     * Bug 描述
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * 严重等级：0-致命，1-严重，2-一般，3-轻微，4-建议
     */
    @Column(name = "severity")
    @Builder.Default
    private Integer severity = 2;

    /**
     * 优先级：0-紧急，1-高，2-中，3-低
     */
    @Column(name = "priority")
    @Builder.Default
    private Integer priority = 2;

    /**
     * 状态：1-新建，2-已确认，3-已分配，4-修复中，5-待验证，6-已关闭，7-已拒绝
     */
    @Column(name = "status")
    @Builder.Default
    private Integer status = 1;

    /**
     * 关联项目ID
     */
    @Column(name = "project_id")
    private Long projectId;

    /**
     * 关联任务ID
     */
    @Column(name = "task_id")
    private Long taskId;

    /**
     * 关联测试用例ID
     */
    @Column(name = "test_case_id")
    private Long testCaseId;

    /**
     * 发现版本
     */
    @Column(name = "found_version", length = 50)
    private String foundVersion;

    /**
     * 修复版本
     */
    @Column(name = "fixed_version", length = 50)
    private String fixedVersion;

    /**
     * 负责人ID
     */
    @Column(name = "assignee_id")
    private Long assigneeId;

    /**
     * 负责人姓名
     */
    @Transient
    private String assigneeName;

    /**
     * 报告人ID
     */
    @Column(name = "reporter_id")
    private Long reporterId;

    /**
     * 报告人姓名
     */
    @Transient
    private String reporterName;

    /**
     * 严重等级枚举
     */
    public static final class Severity {
        public static final int FATAL = 0;   // 致命
        public static final int CRITICAL = 1; // 严重
        public static final int NORMAL = 2;   // 一般
        public static final int MINOR = 3;    // 轻微
        public static final int SUGGESTION = 4; // 建议

        public static String getName(Integer severity) {
            if (severity == null) return "未知";
            return switch (severity) {
                case FATAL -> "致命";
                case CRITICAL -> "严重";
                case NORMAL -> "一般";
                case MINOR -> "轻微";
                case SUGGESTION -> "建议";
                default -> "未知";
            };
        }
    }

    /**
     * 状态枚举
     */
    public static final class Status {
        public static final int NEW = 1;          // 新建
        public static final int CONFIRMED = 2;    // 已确认
        public static final int ASSIGNED = 3;     // 已分配
        public static final int FIXING = 4;        // 修复中
        public static final int PENDING_VERIFY = 5; // 待验证
        public static final int CLOSED = 6;       // 已关闭
        public static final int REJECTED = 7;     // 已拒绝

        public static String getName(Integer status) {
            if (status == null) return "未知";
            return switch (status) {
                case NEW -> "新建";
                case CONFIRMED -> "已确认";
                case ASSIGNED -> "已分配";
                case FIXING -> "修复中";
                case PENDING_VERIFY -> "待验证";
                case CLOSED -> "已关闭";
                case REJECTED -> "已拒绝";
                default -> "未知";
            };
        }
    }
}
