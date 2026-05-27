package com.doori.doori_backend.notification.dto.response;

import java.util.List;

public record NotificationListResponse(
    List<NotificationItem> items,
    long unreadCount,
    String nextCursor,
    boolean hasMore
) {
}
