package com.doori.doori_backend.chat.dto.response;

import com.doori.doori_backend.chat.domain.ChatMessage;
import com.doori.doori_backend.chat.domain.MessageType;
import java.time.LocalDateTime;

public record ChatMessageResponse(
    Long id,
    Long roomId,
    Long senderId,
    String senderNickname,
    MessageType type,
    String content,
    LocalDateTime createdAt
) {
    public static ChatMessageResponse from(ChatMessage msg) {
        return new ChatMessageResponse(
            msg.getId(),
            msg.getRoomId(),
            msg.getSenderId(),
            msg.getSenderNickname(),
            msg.getType(),
            msg.getContent(),
            msg.getCreatedAt()
        );
    }
}
