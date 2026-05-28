package com.doori.doori_backend.notification.dto.response;

import com.doori.doori_backend.notification.domain.Notification;
import java.time.LocalDateTime;

public record NotificationItem(
    Long notificationId,
    String type,
    String title,
    String content,
    boolean isRead,
    Long referenceId,
    String referenceType,
    LocalDateTime createdAt,
    LocalDateTime readAt
) {
    public static NotificationItem from(Notification notification) {
        return new NotificationItem(
            notification.getId(),
            notification.getType().name().toLowerCase(),
            notification.getTitle(),
            notification.getContent(),
            notification.isRead(),
            notification.getReferenceId(),
            notification.getReferenceType(),
            notification.getCreatedAt(),
            notification.getReadAt()
        );
    }
}
