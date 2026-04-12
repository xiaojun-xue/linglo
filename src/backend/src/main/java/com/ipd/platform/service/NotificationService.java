package com.ipd.platform.service;

import com.ipd.platform.entity.SysNotification;
import com.ipd.platform.repository.SysNotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 通知服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final SysNotificationRepository notificationRepository;

    /**
     * 发送通知
     */
    @Async
    @Transactional
    public void sendNotification(Long receiverId, String title, String content,
                                  Integer type, String businessType, Long businessId) {
        SysNotification notification = SysNotification.builder()
                .receiverId(receiverId)
                .title(title)
                .content(content)
                .type(type != null ? type : SysNotification.Type.SYSTEM)
                .businessType(businessType)
                .businessId(businessId)
                .senderId(1L) // 系统发送
                .isRead(false)
                .build();
        
        notificationRepository.save(notification);
        log.info("发送通知给用户 {}: {}", receiverId, title);
    }

    /**
     * 发送任务通知
     */
    @Async
    @Transactional
    public void sendTaskNotification(Long assigneeId, String action, String taskTitle, Long taskId) {
        sendNotification(
                assigneeId,
                String.format("任务%s: %s", action, taskTitle),
                String.format("您有一个任务被%s: %s", action, taskTitle),
                SysNotification.Type.TASK,
                SysNotification.BusinessType.TASK,
                taskId
        );
    }

    /**
     * 发送评审通知
     */
    @Async
    @Transactional
    public void sendReviewNotification(Long reviewerId, String reviewTitle, Long reviewId, String action) {
        sendNotification(
                reviewerId,
                String.format("评审%s: %s", action, reviewTitle),
                String.format("您有一个评审需要%s: %s", action, reviewTitle),
                SysNotification.Type.REVIEW,
                SysNotification.BusinessType.REVIEW,
                reviewId
        );
    }

    /**
     * 获取通知列表
     */
    public Page<SysNotification> getNotifications(Long receiverId, Boolean isRead, int page, int size) {
        if (isRead != null) {
            return notificationRepository.findByReceiverIdAndIsReadOrderByCreatedAtDesc(
                    receiverId, isRead, PageRequest.of(page, size));
        }
        return notificationRepository.findByReceiverIdOrderByCreatedAtDesc(
                receiverId, PageRequest.of(page, size));
    }

    /**
     * 获取未读数量
     */
    public long getUnreadCount(Long receiverId) {
        return notificationRepository.countByReceiverIdAndIsRead(receiverId, false);
    }

    /**
     * 标记单条已读
     */
    @Transactional
    public void markAsRead(Long id, Long receiverId) {
        notificationRepository.markAsRead(id, receiverId, LocalDateTime.now());
    }

    /**
     * 全部标记已读
     */
    @Transactional
    public void markAllAsRead(Long receiverId) {
        notificationRepository.markAllAsRead(receiverId, LocalDateTime.now());
    }
}
