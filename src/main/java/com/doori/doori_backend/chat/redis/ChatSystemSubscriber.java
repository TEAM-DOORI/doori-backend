package com.doori.doori_backend.chat.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatSystemSubscriber {

    private final ChatRedisSubscriptionService subscriptionService;

    // 다른 인스턴스가 새 방을 생성했을 때 호출 — 이 인스턴스도 로컬 구독 등록
    // subscribeLocal 사용: 재방송 없이 로컬만 처리 (무한 루프 방지)
    public void onNewRoom(String roomIdStr, String channel) {
        try {
            subscriptionService.subscribeLocal(Long.parseLong(roomIdStr));
        } catch (Exception e) {
            log.error("신규 방 구독 전파 실패: roomId={}", roomIdStr, e);
        }
    }

    // 다른 인스턴스에서 방이 비워졌을 때 호출 — 이 인스턴스도 로컬 구독 해제
    // unsubscribe 사용: 이미 idempotent (subscribedRooms.remove 가 false면 skip)
    public void onRoomRemoved(String roomIdStr, String channel) {
        try {
            subscriptionService.unsubscribe(Long.parseLong(roomIdStr));
        } catch (Exception e) {
            log.error("방 구독 해제 전파 실패: roomId={}", roomIdStr, e);
        }
    }
}
