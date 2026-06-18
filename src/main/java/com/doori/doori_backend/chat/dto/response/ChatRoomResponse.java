package com.doori.doori_backend.chat.dto.response;

import com.doori.doori_backend.chat.domain.ChatRoom;
import com.doori.doori_backend.chat.domain.RoomType;

public record ChatRoomResponse(
    Long id,
    String name,
    RoomType type
) {
    public static ChatRoomResponse from(ChatRoom room) {
        return new ChatRoomResponse(room.getId(), room.getName(), room.getType());
    }
}
