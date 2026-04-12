package com.ipd.platform.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 通知实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "sys_notification")
public class SysNotification extends BaseEntity {

    /**
     * 通知类型：1-系统通知，2-任务通知，3-评审通知，4-需求通知，5-项目通知
     */
    @Column(name = "type")
    private Integer type;

    /**
     * 通知标题
     */
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    /**
     * 通知内容
     */
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    /**
     * 关联业务ID（如任务ID、评审ID等）
     */
    @Column(name = "business_id")
    private Long businessId;

    /**
     * 关联业务类型（如 TASK、REVIEW、REQUIREMENT 等）
     */
    @Column(name = "business_type", length = 50)
    private String businessType;

    /**
     * 接收人ID
     */
    @Column(name = "receiver_id", nullable = false)
    private Long receiverId;

    /**
     * 发送人ID
     */
    @Column(name = "sender_id")
    private Long senderId;

    /**
     * 发送人名称（不映射数据库）
     */
    @Transient
    private String senderName;

    /**
     * 状态：0-未读，1-已读
     */
    @Column(name = "is_read")
    @Builder.Default
    private Boolean isRead = false;

    /**
     * 阅读时间
     */
    @Column(name = "read_at")
    private LocalDateTime readAt;

    /**
     * 通知类型枚举
     */
    public static final class Type {
        public static final int SYSTEM = 1;     // 系统通知
        public static final int TASK = 2;        // 任务通知
        public static final int REVIEW = 3;      // 评审通知
        public static final int REQUIREMENT = 4;  // 需求通知
        public static final int PROJECT = 5;    // 项目通知

        public static String getName(Integer type) {
            if (type == null) return "未知";
            return switch (type) {
                case SYSTEM -> "系统通知";
                case TASK -> "任务通知";
                case REVIEW -> "评审通知";
                case REQUIREMENT -> "需求通知";
                case PROJECT -> "项目通知";
                default -> "未知";
            };
        }
    }

    /**
     * 业务类型枚举
     */
    public static final class BusinessType {
        public static final String TASK = "TASK";
        public static final String REVIEW = "REVIEW";
        public static final String REQUIREMENT = "REQUIREMENT";
        public static final String PROJECT = "PROJECT";
        public static final String SPRINT = "SPRINT";
    }
}
