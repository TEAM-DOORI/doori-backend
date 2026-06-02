package com.doori.doori_backend.notification.service;

import com.doori.doori_backend.global.error.ErrorCode;
import com.doori.doori_backend.global.exception.CustomException;
import com.doori.doori_backend.notification.domain.Notification;
import com.doori.doori_backend.notification.domain.NotificationType;
import com.doori.doori_backend.notification.dto.response.NotificationItem;
import com.doori.doori_backend.notification.dto.response.NotificationListResponse;
import com.doori.doori_backend.notification.repository.NotificationRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final int DEFAULT_LIMIT = 20;

    private final NotificationRepository notificationRepository;

    @Transactional(readOnly = true)
    public NotificationListResponse getNotifications(Long memberId, String typeParam, String cursor) {
        Long cursorId = parseCursor(cursor);
        NotificationType type = parseType(typeParam);
        int fetchSize = DEFAULT_LIMIT + 1;

        List<Notification> notifications = notificationRepository.findByReceiverWithCursor(
            memberId, cursorId, type, PageRequest.of(0, fetchSize));

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

    @Transactional
    public void markAllAsRead(Long memberId) {
        notificationRepository.markAllAsReadByMemberId(memberId);
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

    private NotificationType parseType(String type) {
        if (type == null || type.isBlank()) {
            return null;
        }
        try {
            return NotificationType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.COMMON_BAD_REQUEST, "유효하지 않은 알림 타입입니다.");
        }
    }
}
