package com.doori.doori_backend.chat.redis;

import com.doori.doori_backend.chat.dto.response.ChatMessageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisPublisher {

    private final RedisTemplate<String, Object> chatRedisTemplate;

    public void publish(Long roomId, ChatMessageResponse message) {
        chatRedisTemplate.convertAndSend("chat:room:" + roomId, message);
    }
}
