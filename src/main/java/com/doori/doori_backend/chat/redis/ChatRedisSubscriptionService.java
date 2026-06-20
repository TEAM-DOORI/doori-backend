package com.doori.doori_backend.chat.redis;

import com.doori.doori_backend.chat.event.ChatRoomSubscribedEvent;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@RequiredArgsConstructor
public class ChatRedisSubscriptionService {

    private final RedisMessageListenerContainer redisMessageListenerContainer;
    private final MessageListenerAdapter chatMessageListenerAdapter;
    private final StringRedisTemplate stringRedisTemplate;

    private final Set<Long> subscribedRooms = ConcurrentHashMap.newKeySet();

    /**
     * 방 생성 트랜잭션 커밋 후 호출 — DB 롤백 시 Redis broadcast가 나가지 않도록 AFTER_COMMIT 사용
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onRoomCreated(ChatRoomSubscribedEvent event) {
        subscribe(event.roomId());
    }

    // 로컬 구독 + 다른 인스턴스 알림 (enterRoom 등 런타임 호출용)
    public void subscribe(Long roomId) {
        if (subscribeLocal(roomId)) {
            stringRedisTemplate.convertAndSend("chat:system:new-room", String.valueOf(roomId));
        }
    }

    // 실제 신규 구독 시에만 true 반환 — 호출자가 중복 broadcast를 방지할 수 있도록
    public boolean subscribeLocal(Long roomId) {
        if (subscribedRooms.add(roomId)) {
            redisMessageListenerContainer.addMessageListener(
                chatMessageListenerAdapter, new ChannelTopic("chat:room:" + roomId));
            return true;
        }
        return false;
    }

    public void unsubscribe(Long roomId) {
        if (subscribedRooms.remove(roomId)) {
            redisMessageListenerContainer.removeMessageListener(
                chatMessageListenerAdapter, new ChannelTopic("chat:room:" + roomId));
        }
    }
}
