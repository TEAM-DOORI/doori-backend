package com.doori.doori_backend.chat.dto.request;

import com.doori.doori_backend.chat.domain.MessageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ChatMessageRequest(
    @NotNull Long roomId,
    MessageType type,   // null이면 TEXT로 처리
    @NotBlank String content  // TEXT면 텍스트, IMAGE/FILE이면 S3 URL
) {
    public ChatMessageRequest {
        if (type == null) type = MessageType.TEXT;
    }
}
