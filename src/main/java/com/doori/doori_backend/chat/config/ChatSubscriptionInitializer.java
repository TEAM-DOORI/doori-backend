package com.doori.doori_backend.chat.config;

import com.doori.doori_backend.chat.redis.ChatRedisSubscriptionService;
import com.doori.doori_backend.chat.repository.ChatRoomMemberRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "chat.subscription.init.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class ChatSubscriptionInitializer {

    private final ChatRoomMemberRepository memberRepository;
    private final ChatRedisSubscriptionService subscriptionService;
    private final RedisMessageListenerContainer redisMessageListenerContainer;
    @Qualifier("chatSystemListenerAdapter")
    private final MessageListenerAdapter chatSystemListenerAdapter;
    @Qualifier("chatSystemRoomRemovedListenerAdapter")
    private final MessageListenerAdapter chatSystemRoomRemovedListenerAdapter;

    @EventListener(ApplicationReadyEvent.class)
    public void restoreSubscriptions() {
        // 시스템 채널 등록 실패는 독립적으로 처리 — 실패 시 다른 인스턴스의 방 생성/삭제 알림을 못 받음
        try {
            redisMessageListenerContainer.addMessageListener(
                chatSystemListenerAdapter, new ChannelTopic("chat:system:new-room"));
            redisMessageListenerContainer.addMessageListener(
                chatSystemRoomRemovedListenerAdapter, new ChannelTopic("chat:system:room-removed"));
        } catch (Exception e) {
            log.error("시스템 채널 리스너 등록 실패 — 다른 인스턴스의 방 생성/삭제 알림을 수신할 수 없습니다", e);
        }

        // 방 구독 복구는 시스템 채널 실패와 무관하게 진행
        try {
            List<Long> roomIds = memberRepository.findAllDistinctRoomIds();
            roomIds.forEach(subscriptionService::subscribeLocal);
            log.info("Redis 채팅방 구독 복구 완료: {}개", roomIds.size());
        } catch (Exception e) {
            log.error("Redis 채팅방 구독 복구 실패", e);
        }
    }
}
