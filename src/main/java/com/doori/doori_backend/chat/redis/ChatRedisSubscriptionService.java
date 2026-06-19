package com.doori.doori_backend.chat.redis;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatRedisSubscriptionService {

    private final RedisMessageListenerContainer redisMessageListenerContainer;
    private final MessageListenerAdapter chatMessageListenerAdapter;

    // 이 인스턴스에서 현재 구독 중인 roomId 목록 (중복 등록 방지)
    private final Set<Long> subscribedRooms = ConcurrentHashMap.newKeySet();

    public void subscribe(Long roomId) {
        // ConcurrentHashMap.add가 원자적이므로 중복 addMessageListener 호출 방지
        if (subscribedRooms.add(roomId)) {
            redisMessageListenerContainer.addMessageListener(
                chatMessageListenerAdapter, new ChannelTopic("chat:room:" + roomId));
        }
    }

    public void unsubscribe(Long roomId) {
        if (subscribedRooms.remove(roomId)) {
            redisMessageListenerContainer.removeMessageListener(
                chatMessageListenerAdapter, new ChannelTopic("chat:room:" + roomId));
        }
    }
}
