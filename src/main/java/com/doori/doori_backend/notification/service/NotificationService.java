package com.doori.doori_backend.notification.service;

import com.doori.doori_backend.global.error.ErrorCode;
import com.doori.doori_backend.global.exception.CustomException;
import com.doori.doori_backend.notification.domain.Notification;
import com.doori.doori_backend.notification.dto.response.NotificationItem;
import com.doori.doori_backend.notification.dto.response.NotificationListResponse;
import com.doori.doori_backend.notification.repository.NotificationRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final int DEFAULT_LIMIT = 20;

    private final NotificationRepository notificationRepository;

    @Transactional(readOnly = true)
    public NotificationListResponse getNotifications(Long memberId, String cursor) {
        Long cursorId = parseCursor(cursor);
        int fetchSize = DEFAULT_LIMIT + 1;

        List<Notification> notifications = notificationRepository.findByReceiverWithCursor(
            memberId, cursorId, fetchSize);

        boolean hasMore = notifications.size() == fetchSize;
        if (hasMore) {
            notifications = notifications.subList(0, DEFAULT_LIMIT);
        }

        List<NotificationItem> items = notifications.stream()
            .map(NotificationItem::from)
            .toList();

        String nextCursor = hasMore ? String.valueOf(items.getLast().notificationId()) : null;

        long unreadCount = notificationRepository.countUnreadByMemberId(memberId);

        return new NotificationListResponse(items, unreadCount, nextCursor, hasMore);
    }

    @Transactional
    public void markAsRead(Long memberId, Long notificationId) {
        Notification notification = notificationRepository.findByIdAndReceiverId(notificationId, memberId)
            .orElseThrow(() -> new CustomException(ErrorCode.NOTIFICATION_NOT_FOUND));

        notification.markAsRead();
    }

    private Long parseCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(cursor);
        } catch (NumberFormatException e) {
            throw new CustomException(ErrorCode.COMMON_BAD_REQUEST, "유효하지 않은 커서 값입니다.");
        }
    }

    @Transactional
    public void markAllAsRead(Long memberId) {
        notificationRepository.markAllAsReadByMemberId(memberId);
    }
}
