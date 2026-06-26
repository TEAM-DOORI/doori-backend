package com.doori.doori_backend.chat.redis;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatRedisSubscriptionService {

    private final RedisMessageListenerContainer redisMessageListenerContainer;
    @Qualifier("chatMessageListenerAdapter")
    private final MessageListenerAdapter chatMessageListenerAdapter;

    private final Map<Long, Integer> roomSubscriberCounts = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Long>> sessionSubscriptions = new ConcurrentHashMap<>();

    public void registerSubscription(String sessionId, String subscriptionId, Long roomId) {
        if (sessionId == null || subscriptionId == null || roomId == null) {
            return;
        }

        sessionSubscriptions.compute(sessionId, (key, subscriptions) -> {
            Map<String, Long> updatedSubscriptions =
                subscriptions == null ? new ConcurrentHashMap<>() : subscriptions;

            Long previousRoomId = updatedSubscriptions.put(subscriptionId, roomId);
            if (previousRoomId != null && !previousRoomId.equals(roomId)) {
                decrementRoomSubscriberCount(previousRoomId);
            }
            if (previousRoomId == null) {
                incrementRoomSubscriberCount(roomId);
            }
            return updatedSubscriptions;
        });
    }

    public void unregisterSubscription(String sessionId, String subscriptionId) {
        if (sessionId == null || subscriptionId == null) {
            return;
        }

        sessionSubscriptions.computeIfPresent(sessionId, (key, subscriptions) -> {
            Long roomId = subscriptions.remove(subscriptionId);
            if (roomId != null) {
                decrementRoomSubscriberCount(roomId);
            }
            return subscriptions.isEmpty() ? null : subscriptions;
        });
    }

    public void unregisterSession(String sessionId) {
        if (sessionId == null) {
            return;
        }

        Map<String, Long> subscriptions = sessionSubscriptions.remove(sessionId);
        if (subscriptions == null) {
            return;
        }

        subscriptions.values().forEach(this::decrementRoomSubscriberCount);
    }

    int getSubscriberCount(Long roomId) {
        return roomSubscriberCounts.getOrDefault(roomId, 0);
    }

    private void incrementRoomSubscriberCount(Long roomId) {
        roomSubscriberCounts.compute(roomId, (key, count) -> {
            int updatedCount = count == null ? 1 : count + 1;
            if (updatedCount == 1) {
                subscribe(roomId);
            }
            return updatedCount;
        });
    }

    private void decrementRoomSubscriberCount(Long roomId) {
        roomSubscriberCounts.computeIfPresent(roomId, (key, count) -> {
            int updatedCount = count - 1;
            if (updatedCount <= 0) {
                unsubscribe(roomId);
                return null;
            }
            return updatedCount;
        });
    }

    private void subscribe(Long roomId) {
        redisMessageListenerContainer.addMessageListener(
            chatMessageListenerAdapter, new ChannelTopic("chat:room:" + roomId));
    }

    private void unsubscribe(Long roomId) {
        redisMessageListenerContainer.removeMessageListener(
            chatMessageListenerAdapter, new ChannelTopic("chat:room:" + roomId));
    }
}
