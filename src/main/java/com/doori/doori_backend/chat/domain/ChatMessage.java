package com.doori.doori_backend.chat.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(
    name = "chat_message",
    indexes = @Index(name = "idx_chat_message_room_id", columnList = "room_id, id DESC")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_id", nullable = false)
    private Long roomId;

    @Column(name = "sender_id", nullable = false)
    private Long senderId;

    @Column(nullable = false)
    private String senderNickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageType type;

    // TEXT면 텍스트, IMAGE/FILE이면 S3 URL
    @Column(nullable = false, length = 2000)
    private String content;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public static ChatMessage of(Long roomId, Long senderId, String nickname,
                                 MessageType type, String content) {
        ChatMessage msg = new ChatMessage();
        msg.roomId = roomId;
        msg.senderId = senderId;
        msg.senderNickname = nickname;
        msg.type = type;
        msg.content = content;
        return msg;
    }
}
