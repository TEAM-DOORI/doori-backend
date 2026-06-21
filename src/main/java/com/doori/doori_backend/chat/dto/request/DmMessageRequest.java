package com.doori.doori_backend.chat.dto.request;

import com.doori.doori_backend.chat.domain.MessageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DmMessageRequest(
    @NotNull Long roomId,
    MessageType type,
    @NotBlank String content
) {
    public DmMessageRequest {
        if (type == null) type = MessageType.TEXT;
    }
}
