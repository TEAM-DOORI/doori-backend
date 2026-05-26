package com.doori.doori_backend.notification.repository;

import com.doori.doori_backend.notification.domain.Notification;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // 커서 기반 페이지네이션: cursor 미지정 시 최신순, cursor 지정 시 해당 ID 이전 항목 조회
    @Query("SELECT n FROM Notification n WHERE n.receiver.id = :memberId AND (:cursor IS NULL OR n.id < :cursor) ORDER BY n.id DESC")
    List<Notification> findByReceiverWithCursor(
        @Param("memberId") Long memberId,
        @Param("cursor") Long cursor,
        Pageable pageable
    );

    @Query("SELECT n FROM Notification n WHERE n.id = :notificationId AND n.receiver.id = :memberId")
    Optional<Notification> findByIdAndReceiverId(
        @Param("notificationId") Long notificationId,
        @Param("memberId") Long memberId
    );

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.receiver.id = :memberId AND n.isRead = false")
    long countUnreadByMemberId(@Param("memberId") Long memberId);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = CURRENT_TIMESTAMP WHERE n.receiver.id = :memberId AND n.isRead = false")
    void markAllAsReadByMemberId(@Param("memberId") Long memberId);
}
