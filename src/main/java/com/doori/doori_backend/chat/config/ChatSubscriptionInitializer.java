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

    @EventListener(ApplicationReadyEvent.class)
    public void restoreSubscriptions() {
        try {
            redisMessageListenerContainer.addMessageListener(
                chatSystemListenerAdapter, new ChannelTopic("chat:system:new-room"));

            List<Long> roomIds = memberRepository.findAllDistinctRoomIds();
            roomIds.forEach(subscriptionService::subscribeLocal);
            log.info("Redis 채팅방 구독 복구 완료: {}개", roomIds.size());
        } catch (Exception e) {
            log.error("Redis 채팅방 구독 복구 실패 — 기동은 계속됨", e);
        }
    }
}
