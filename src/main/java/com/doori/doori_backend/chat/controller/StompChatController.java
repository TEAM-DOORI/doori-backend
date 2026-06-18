package com.doori.doori_backend.chat.controller;

import com.doori.doori_backend.chat.dto.request.ChatMessageRequest;
import com.doori.doori_backend.chat.dto.request.DmMessageRequest;
import com.doori.doori_backend.chat.dto.request.RoomEnterRequest;
import com.doori.doori_backend.chat.dto.request.RoomLeaveRequest;
import com.doori.doori_backend.chat.dto.response.ChatMessageResponse;
import com.doori.doori_backend.chat.redis.RedisPublisher;
import com.doori.doori_backend.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class StompChatController {

    private final ChatService chatService;
    private final RedisPublisher redisPublisher;
    private final SimpMessagingTemplate messagingTemplate;

    // 클라이언트: stompClient.send('/app/chat.send', {}, JSON.stringify({roomId, type, content}))
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessageRequest request,
                            @AuthenticationPrincipal Long memberId) {
        ChatMessageResponse response = chatService.sendMessage(request, memberId);
        redisPublisher.publish(request.roomId(), response);
    }

    // 채팅방 입장 — Redis 구독 등록 + 입장 메시지 브로드캐스트
    @MessageMapping("/chat.enter")
    public void enterRoom(@Payload RoomEnterRequest request,
                          @AuthenticationPrincipal Long memberId) {
        ChatMessageResponse systemMsg = chatService.enterRoom(request.roomId(), memberId);
        redisPublisher.publish(request.roomId(), systemMsg);
    }

    // 채팅방 퇴장
    @MessageMapping("/chat.leave")
    public void leaveRoom(@Payload RoomLeaveRequest request,
                          @AuthenticationPrincipal Long memberId) {
        ChatMessageResponse systemMsg = chatService.leaveRoom(request.roomId(), memberId);
        redisPublisher.publish(request.roomId(), systemMsg);
    }

    // 1:1 DM 전송 — Spring user destination으로 수신자만 수신
    @MessageMapping("/dm.send")
    public void sendDm(@Payload DmMessageRequest request,
                       @AuthenticationPrincipal Long memberId) {
        ChatMessageResponse response = chatService.sendDm(request, memberId);
        Long receiverId = chatService.findDmReceiverId(request.roomId(), memberId);

        // 수신자: /user/{receiverId}/queue/dm
        messagingTemplate.convertAndSendToUser(
            String.valueOf(receiverId), "/queue/dm", response);
        // 발신자 화면에도 표시
        messagingTemplate.convertAndSendToUser(
            String.valueOf(memberId), "/queue/dm", response);
    }
}
