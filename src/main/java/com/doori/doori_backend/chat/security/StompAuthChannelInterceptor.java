package com.doori.doori_backend.chat.security;

import com.doori.doori_backend.auth.jwt.JwtProvider;
import com.doori.doori_backend.block.service.BlockPolicyService;
import com.doori.doori_backend.chat.redis.ChatRedisSubscriptionService;
import com.doori.doori_backend.chat.repository.ChatRoomMemberRepository;
import com.doori.doori_backend.chat.repository.ChatRoomRepository;
import com.doori.doori_backend.global.error.ErrorCode;
import com.doori.doori_backend.global.exception.CustomException;
import com.doori.doori_backend.global.security.ActiveMemberValidator;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private static final String ACCESS_TOKEN_EXPIRY_KEY = "accessTokenExpiry";
    private static final String MEMBER_ID_KEY = "memberId";
    private static final String ROOM_TOPIC_PREFIX = "/topic/chat.room.";

    private final JwtProvider jwtProvider;
    private final ActiveMemberValidator activeMemberValidator;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ChatRedisSubscriptionService chatRedisSubscriptionService;
    private final BlockPolicyService blockPolicyService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        // command == null 이면 heartbeat 프레임 — 통과
        if (accessor == null || accessor.getCommand() == null) return message;

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new MessageDeliveryException("Authorization 헤더가 없습니다.");
            }
            String token = authHeader.substring(7);
            Long memberId = jwtProvider.validateAccessTokenAndGetMemberId(token);
            activeMemberValidator.validateActiveMember(memberId);

            Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
            if (sessionAttributes != null) {
                long expiryEpochMilli =
                    jwtProvider.getAccessTokenExpiry(token).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                sessionAttributes.put(MEMBER_ID_KEY, memberId);
                sessionAttributes.put(ACCESS_TOKEN_EXPIRY_KEY, expiryEpochMilli);
            }

            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                memberId,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
            );
            accessor.setUser(auth);

        } else if (StompCommand.DISCONNECT.equals(accessor.getCommand())) {
            chatRedisSubscriptionService.unregisterSession(accessor.getSessionId());
        } else {
            Long memberId = validateSession(accessor);

            if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
                handleSubscribe(accessor, memberId);
            } else if (StompCommand.UNSUBSCRIBE.equals(accessor.getCommand())) {
                chatRedisSubscriptionService.unregisterSubscription(
                    accessor.getSessionId(),
                    accessor.getSubscriptionId()
                );
            }
        }

        return message;
    }

    private Long validateSession(StompHeaderAccessor accessor) {
        if (accessor.getUser() == null) {
            throw new MessageDeliveryException("인증되지 않은 요청입니다.");
        }

        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
        if (sessionAttributes == null) {
            throw new MessageDeliveryException("세션 인증 정보가 없습니다.");
        }

        Object memberIdValue = sessionAttributes.get(MEMBER_ID_KEY);
        Object expiryValue = sessionAttributes.get(ACCESS_TOKEN_EXPIRY_KEY);
        if (!(memberIdValue instanceof Long memberId) || !(expiryValue instanceof Long expiryEpochMilli)) {
            throw new MessageDeliveryException("세션 인증 정보가 올바르지 않습니다.");
        }

        if (System.currentTimeMillis() > expiryEpochMilli) {
            throw new MessageDeliveryException(ErrorCode.AUTH_EXPIRED_TOKEN.getDefaultMessage());
        }

        try {
            activeMemberValidator.validateActiveMember(memberId);
        } catch (CustomException e) {
            throw new MessageDeliveryException(e.getMessage());
        }
        return memberId;
    }

    private void handleSubscribe(StompHeaderAccessor accessor, Long memberId) {
        Long roomId = extractRoomId(accessor.getDestination());
        if (roomId == null) {
            return;
        }

        if (!chatRoomRepository.existsById(roomId)) {
            throw new MessageDeliveryException(ErrorCode.CHAT_ROOM_NOT_FOUND.getDefaultMessage());
        }
        if (!chatRoomMemberRepository.existsByRoomIdAndMemberId(roomId, memberId)) {
            throw new MessageDeliveryException(ErrorCode.CHAT_ROOM_FORBIDDEN.getDefaultMessage());
        }
        try {
            blockPolicyService.validateNotBlockedChatRoom(memberId, chatRoomMemberRepository.findMemberIdsByRoomId(roomId));
        } catch (CustomException e) {
            throw new MessageDeliveryException(e.getMessage());
        }

        chatRedisSubscriptionService.registerSubscription(
            accessor.getSessionId(),
            accessor.getSubscriptionId(),
            roomId
        );
    }

    private Long extractRoomId(String destination) {
        if (destination == null || !destination.startsWith(ROOM_TOPIC_PREFIX)) {
            return null;
        }
        try {
            return Long.parseLong(destination.substring(ROOM_TOPIC_PREFIX.length()));
        } catch (NumberFormatException e) {
            throw new MessageDeliveryException("유효하지 않은 채팅방 경로입니다.");
        }
    }
}
