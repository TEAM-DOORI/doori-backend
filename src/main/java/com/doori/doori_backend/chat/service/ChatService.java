package com.doori.doori_backend.chat.service;

import com.doori.doori_backend.auth.domain.Member;
import com.doori.doori_backend.auth.repository.MemberRepository;
import com.doori.doori_backend.chat.domain.ChatMessage;
import com.doori.doori_backend.chat.domain.ChatRoomMember;
import com.doori.doori_backend.chat.domain.MessageType;
import com.doori.doori_backend.chat.dto.request.ChatMessageRequest;
import com.doori.doori_backend.chat.dto.request.DmMessageRequest;
import com.doori.doori_backend.chat.dto.response.ChatMessageResponse;
import com.doori.doori_backend.chat.dto.response.SliceResponse;
import com.doori.doori_backend.chat.repository.ChatMessageRepository;
import com.doori.doori_backend.chat.repository.ChatRoomMemberRepository;
import com.doori.doori_backend.global.error.ErrorCode;
import com.doori.doori_backend.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository messageRepository;
    private final ChatRoomMemberRepository memberRepository;
    private final MemberRepository mutableMemberRepository;
    private final RedisMessageListenerContainer listenerContainer;
    private final MessageListenerAdapter chatMessageListenerAdapter;

    @Transactional
    public ChatMessageResponse sendMessage(ChatMessageRequest request, Long memberId) {
        validateMember(request.roomId(), memberId);
        Member sender = findMember(memberId);

        ChatMessage saved = messageRepository.save(
            ChatMessage.of(request.roomId(), memberId, sender.getNickname(),
                request.type(), request.content())
        );
        return ChatMessageResponse.from(saved);
    }

    @Transactional
    public ChatMessageResponse sendDm(DmMessageRequest request, Long memberId) {
        validateMember(request.roomId(), memberId);
        Member sender = findMember(memberId);

        ChatMessage saved = messageRepository.save(
            ChatMessage.of(request.roomId(), memberId, sender.getNickname(),
                request.type(), request.content())
        );
        return ChatMessageResponse.from(saved);
    }

    @Transactional
    public ChatMessageResponse enterRoom(Long roomId, Long memberId) {
        Member member = findMember(memberId);

        memberRepository.findByRoomIdAndMemberId(roomId, memberId)
            .ifPresentOrElse(
                m -> {},
                () -> memberRepository.save(ChatRoomMember.of(roomId, memberId))
            );

        // 채팅방 입장 시 Redis 채널 구독 등록 (서버 재시작 후 복구 포함)
        listenerContainer.addMessageListener(
            chatMessageListenerAdapter, new ChannelTopic("chat:room:" + roomId));

        ChatMessage systemMsg = ChatMessage.of(
            roomId, memberId, member.getNickname(),
            MessageType.ENTER, member.getNickname() + "님이 입장했습니다.");
        return ChatMessageResponse.from(messageRepository.save(systemMsg));
    }

    @Transactional
    public ChatMessageResponse leaveRoom(Long roomId, Long memberId) {
        Member member = findMember(memberId);

        memberRepository.findByRoomIdAndMemberId(roomId, memberId)
            .ifPresent(memberRepository::delete);

        ChatMessage systemMsg = ChatMessage.of(
            roomId, memberId, member.getNickname(),
            MessageType.LEAVE, member.getNickname() + "님이 퇴장했습니다.");
        return ChatMessageResponse.from(messageRepository.save(systemMsg));
    }

    public SliceResponse<ChatMessageResponse> getHistory(Long roomId, Long cursorId, int size, Long memberId) {
        validateMember(roomId, memberId);
        Slice<ChatMessageResponse> result = messageRepository
            .findByRoomIdAndIdLessThanOrderByIdDesc(roomId, cursorId, PageRequest.of(0, size))
            .map(ChatMessageResponse::from);
        return SliceResponse.from(result);
    }

    private void validateMember(Long roomId, Long memberId) {
        if (!memberRepository.existsByRoomIdAndMemberId(roomId, memberId)) {
            throw new CustomException(ErrorCode.CHAT_ROOM_FORBIDDEN);
        }
    }

    // DM 방에서 나(senderId)를 제외한 수신자 ID 반환
    public Long findDmReceiverId(Long roomId, Long senderId) {
        return memberRepository.findByRoomId(roomId).stream()
            .map(ChatRoomMember::getMemberId)
            .filter(id -> !id.equals(senderId))
            .findFirst()
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    private Member findMember(Long memberId) {
        return mutableMemberRepository.findById(memberId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }
}
