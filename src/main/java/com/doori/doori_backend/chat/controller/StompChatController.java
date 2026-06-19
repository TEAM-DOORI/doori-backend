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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class StompChatController {

    private final ChatService chatService;
    private final RedisPublisher redisPublisher;

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessageRequest request,
                            @AuthenticationPrincipal Long memberId) {
        ChatMessageResponse response = chatService.sendMessage(request, memberId);
        redisPublisher.publish(request.roomId(), response);
    }

    @MessageMapping("/chat.enter")
    public void enterRoom(@Payload RoomEnterRequest request,
                          @AuthenticationPrincipal Long memberId) {
        ChatMessageResponse systemMsg = chatService.enterRoom(request.roomId(), memberId);
        redisPublisher.publish(request.roomId(), systemMsg);
    }

    @MessageMapping("/chat.leave")
    public void leaveRoom(@Payload RoomLeaveRequest request,
                          @AuthenticationPrincipal Long memberId) {
        ChatMessageResponse systemMsg = chatService.leaveRoom(request.roomId(), memberId);
        redisPublisher.publish(request.roomId(), systemMsg);
    }

    // DM도 룸 기반 Redis Pub/Sub으로 라우팅 — 다중 인스턴스 환경에서도 안정적으로 전달
    // 클라이언트: /topic/chat.room.{dmRoomId} 구독
    @MessageMapping("/dm.send")
    public void sendDm(@Payload DmMessageRequest request,
                       @AuthenticationPrincipal Long memberId) {
        ChatMessageResponse response = chatService.sendDm(request, memberId);
        redisPublisher.publish(request.roomId(), response);
    }
}
