package com.doori.doori_backend.chat.security;

import com.doori.doori_backend.auth.jwt.JwtProvider;
import java.util.List;
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

    private final JwtProvider jwtProvider;

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

            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                memberId,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
            );
            accessor.setUser(auth);

        } else if (!StompCommand.DISCONNECT.equals(accessor.getCommand())) {
            // SEND/SUBSCRIBE/UNSUBSCRIBE 등 — CONNECT 없이 들어온 프레임 차단
            if (accessor.getUser() == null) {
                throw new MessageDeliveryException("인증되지 않은 요청입니다.");
            }
        }

        return message;
    }
}
