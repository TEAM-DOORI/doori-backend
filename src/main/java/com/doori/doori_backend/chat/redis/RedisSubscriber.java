package com.doori.doori_backend.chat.redis;

import com.doori.doori_backend.chat.dto.response.ChatMessageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisSubscriber {

    private final ObjectMapper objectMapper;
    private final SimpMessagingTemplate messagingTemplate;

    // MessageListenerAdapter가 리플렉션으로 호출 — 메서드 시그니처 변경 금지
    public void onMessage(String message, String channel) {
        try {
            ChatMessageResponse chatMessage =
                    objectMapper.readValue(message, ChatMessageResponse.class);

            // 채널명: "chat:room:{roomId}" → STOMP 토픽으로 브로드캐스트
            String roomId = channel.replace("chat:room:", "");
            messagingTemplate.convertAndSend("/topic/chat.room." + roomId, chatMessage);

        } catch (Exception e) {
            log.error("Redis 메시지 역직렬화 실패: channel={}, message={}", channel, message, e);
        }
    }
}
