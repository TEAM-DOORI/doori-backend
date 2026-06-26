package com.doori.doori_backend.notification.repository;

import com.doori.doori_backend.notification.domain.Notification;
import com.doori.doori_backend.notification.domain.NotificationType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // 커서 기반 페이지네이션: type=null이면 전체 조회, 지정 시 해당 타입만 조회
    @Query("""
        SELECT n FROM Notification n
        WHERE n.receiver.id = :memberId
          AND (:cursor IS NULL OR n.id < :cursor)
          AND (:type IS NULL OR n.type = :type)
        ORDER BY n.id DESC
        """)
    List<Notification> findByReceiverWithCursor(
        @Param("memberId") Long memberId,
        @Param("cursor") Long cursor,
        @Param("type") NotificationType type,
        Pageable pageable
    );

    @Query("SELECT n FROM Notification n WHERE n.id = :notificationId AND n.receiver.id = :memberId")
    Optional<Notification> findByIdAndReceiverId(
        @Param("notificationId") Long notificationId,
        @Param("memberId") Long memberId
    );

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.receiver.id = :memberId AND n.isRead = false")
    long countUnreadByMemberId(@Param("memberId") Long memberId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = CURRENT_TIMESTAMP WHERE n.receiver.id = :memberId AND n.isRead = false")
    void markAllAsReadByMemberId(@Param("memberId") Long memberId);
}
