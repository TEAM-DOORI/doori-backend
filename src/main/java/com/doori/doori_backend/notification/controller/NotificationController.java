package com.doori.doori_backend.notification.controller;

import com.doori.doori_backend.global.response.ApiResponse;
import com.doori.doori_backend.notification.domain.NotificationType;
import com.doori.doori_backend.notification.dto.response.NotificationListResponse;
import com.doori.doori_backend.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<ApiResponse<NotificationListResponse>> getNotifications(
        @RequestParam(required = false) NotificationType type,
        @RequestParam(required = false) String cursor,
        Authentication authentication
    ) {
        Long memberId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.success(notificationService.getNotifications(memberId, type, cursor)));
    }

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(
        @PathVariable Long notificationId,
        Authentication authentication
    ) {
        Long memberId = (Long) authentication.getPrincipal();
        notificationService.markAsRead(memberId, notificationId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(Authentication authentication) {
        Long memberId = (Long) authentication.getPrincipal();
        notificationService.markAllAsRead(memberId);
        return ResponseEntity.noContent().build();
    }
}
