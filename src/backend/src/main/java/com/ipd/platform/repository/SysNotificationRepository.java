package com.ipd.platform.repository;

import com.ipd.platform.entity.SysNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * 通知 Repository
 */
@Repository
public interface SysNotificationRepository extends JpaRepository<SysNotification, Long> {

    Page<SysNotification> findByReceiverIdOrderByCreatedAtDesc(Long receiverId, Pageable pageable);

    Page<SysNotification> findByReceiverIdAndIsReadOrderByCreatedAtDesc(Long receiverId, Boolean isRead, Pageable pageable);

    long countByReceiverIdAndIsRead(Long receiverId, Boolean isRead);

    @Modifying
    @Query("UPDATE SysNotification n SET n.isRead = true, n.readAt = :readAt WHERE n.receiverId = :receiverId AND n.isRead = false")
    int markAllAsRead(@Param("receiverId") Long receiverId, @Param("readAt") LocalDateTime readAt);

    @Modifying
    @Query("UPDATE SysNotification n SET n.isRead = true, n.readAt = :readAt WHERE n.id = :id AND n.receiverId = :receiverId")
    int markAsRead(@Param("id") Long id, @Param("receiverId") Long receiverId, @Param("readAt") LocalDateTime readAt);
}
