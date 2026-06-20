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
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class StompChatController {

    private final ChatService chatService;
    private final RedisPublisher redisPublisher;

    // @AuthenticationPrincipal은 spring-security-messaging 없이 STOMP에서 동작하지 않음
    // StompAuthChannelInterceptor에서 UsernamePasswordAuthenticationToken(memberId) 설정 →
    // Spring Messaging이 Principal로 Authentication을 그대로 주입 → getPrincipal()로 추출
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessageRequest request, Authentication auth) {
        Long memberId = (Long) auth.getPrincipal();
        ChatMessageResponse response = chatService.sendMessage(request, memberId);
        redisPublisher.publish(request.roomId(), response);
    }

    @MessageMapping("/chat.enter")
    public void enterRoom(@Payload RoomEnterRequest request, Authentication auth) {
        Long memberId = (Long) auth.getPrincipal();
        ChatMessageResponse systemMsg = chatService.enterRoom(request.roomId(), memberId);
        redisPublisher.publish(request.roomId(), systemMsg);
    }

    @MessageMapping("/chat.leave")
    public void leaveRoom(@Payload RoomLeaveRequest request, Authentication auth) {
        Long memberId = (Long) auth.getPrincipal();
        ChatMessageResponse systemMsg = chatService.leaveRoom(request.roomId(), memberId);
        redisPublisher.publish(request.roomId(), systemMsg);
    }

    // DM도 룸 기반 Redis Pub/Sub으로 라우팅 — 다중 인스턴스 환경에서도 안정적으로 전달
    @MessageMapping("/dm.send")
    public void sendDm(@Payload DmMessageRequest request, Authentication auth) {
        Long memberId = (Long) auth.getPrincipal();
        ChatMessageResponse response = chatService.sendDm(request, memberId);
        redisPublisher.publish(request.roomId(), response);
    }
}
