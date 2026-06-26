package com.doori.doori_backend.notification.domain;

public enum NotificationType {
    MATCH_REQUEST,      // 매칭 요청
    MATCH_RESULT,       // 매칭 결과
    CHAT,               // 채팅 알림 (채팅 탭)
    LIFESTYLE_RULE,     // 생활 규칙 알림 (룸메와의 생활 규칙 설정 — 트리거 로직 미구현, 타입 예약)
    COMMUNITY_COMMENT,  // 커뮤니티 댓글
    SYSTEM              // 시스템 알림
}
