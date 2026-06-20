package com.doori.doori_backend.chat.redis;

import com.doori.doori_backend.chat.event.ChatRoomSubscribedEvent;
import com.doori.doori_backend.chat.event.ChatRoomUnsubscribedEvent;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
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
    @Qualifier("chatMessageListenerAdapter")
    private final MessageListenerAdapter chatMessageListenerAdapter;
    private final StringRedisTemplate stringRedisTemplate;

    private final Set<Long> subscribedRooms = ConcurrentHashMap.newKeySet();

    // 방 생성 TX 커밋 후 실행 — 롤백 시 Redis broadcast 방지
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onRoomCreated(ChatRoomSubscribedEvent event) {
        subscribe(event.roomId());
    }

    // 방 퇴장 TX 커밋 후 실행 — 롤백 시 구독 해제 방지 (멤버 복원 후 메시지 수신 보장)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onRoomUnsubscribed(ChatRoomUnsubscribedEvent event) {
        unsubscribe(event.roomId());
    }

    // 로컬 구독 + 다른 인스턴스 알림 (enterRoom 런타임 호출용)
    public void subscribe(Long roomId) {
        if (subscribeLocal(roomId)) {
            stringRedisTemplate.convertAndSend("chat:system:new-room", String.valueOf(roomId));
        }
    }

    // 실제 신규 구독 시에만 true 반환
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
