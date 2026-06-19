package com.doori.doori_backend.chat.config;

import com.doori.doori_backend.chat.redis.ChatRedisSubscriptionService;
import com.doori.doori_backend.chat.repository.ChatRoomMemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatSubscriptionInitializer {

    private final ChatRoomMemberRepository memberRepository;
    private final ChatRedisSubscriptionService subscriptionService;

    // 앱 재시작 시 DB 기반으로 기존 채팅방 구독 복구
    @EventListener(ApplicationReadyEvent.class)
    public void restoreSubscriptions() {
        memberRepository.findAllDistinctRoomIds().forEach(subscriptionService::subscribe);
        log.info("Redis 채팅방 구독 복구 완료");
    }
}
