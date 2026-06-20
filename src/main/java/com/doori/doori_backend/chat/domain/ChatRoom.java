package com.doori.doori_backend.chat.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "chat_room")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoomType type;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // 채팅방 목록 정렬 기준 — 마지막 메시지 발송 시각
    private LocalDateTime lastMessageAt;

    public void recordMessage(LocalDateTime time) {
        this.lastMessageAt = time;
    }

    public static ChatRoom createGroup(String name) {
        ChatRoom room = new ChatRoom();
        room.name = name;
        room.type = RoomType.GROUP;
        return room;
    }

    public static ChatRoom createDm() {
        ChatRoom room = new ChatRoom();
        room.type = RoomType.DM;
        return room;
    }
}
