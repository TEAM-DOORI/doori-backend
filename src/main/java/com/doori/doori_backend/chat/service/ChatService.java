package com.doori.doori_backend.chat.service;

import com.doori.doori_backend.auth.domain.Member;
import com.doori.doori_backend.auth.repository.MemberRepository;
import com.doori.doori_backend.chat.domain.ChatMessage;
import com.doori.doori_backend.chat.domain.ChatRoom;
import com.doori.doori_backend.chat.domain.ChatRoomMember;
import com.doori.doori_backend.chat.domain.MessageType;
import com.doori.doori_backend.chat.dto.request.ChatMessageRequest;
import com.doori.doori_backend.chat.dto.request.DmMessageRequest;
import com.doori.doori_backend.chat.dto.response.ChatMessageResponse;
import com.doori.doori_backend.chat.dto.response.SliceResponse;
import com.doori.doori_backend.chat.event.ChatRoomUnsubscribedEvent;
import com.doori.doori_backend.chat.redis.ChatRedisSubscriptionService;
import com.doori.doori_backend.chat.repository.ChatMessageRepository;
import com.doori.doori_backend.chat.repository.ChatRoomMemberRepository;
import com.doori.doori_backend.chat.repository.ChatRoomRepository;
import com.doori.doori_backend.global.error.ErrorCode;
import com.doori.doori_backend.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository messageRepository;
    private final ChatRoomRepository roomRepository;
    private final ChatRoomMemberRepository memberRepository;
    private final MemberRepository mutableMemberRepository;
    private final ChatRedisSubscriptionService subscriptionService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public ChatMessageResponse sendMessage(ChatMessageRequest request, Long memberId) {
        return saveMessage(request.roomId(), memberId, request.type(), request.content());
    }

    @Transactional
    public ChatMessageResponse sendDm(DmMessageRequest request, Long memberId) {
        return saveMessage(request.roomId(), memberId, request.type(), request.content());
    }

    private ChatMessageResponse saveMessage(Long roomId, Long memberId,
                                            MessageType type, String content) {
        validateMember(roomId, memberId);
        Member sender = findMember(memberId);
        ChatMessage saved = messageRepository.save(
            ChatMessage.of(roomId, memberId, sender.getNickname(), type, content));
        // 채팅방 목록 정렬 기준 갱신 — 최근 메시지 시각으로 상단 노출
        roomRepository.findById(roomId)
            .ifPresent(room -> room.recordMessage(saved.getCreatedAt()));
        return ChatMessageResponse.from(saved);
    }

    @Transactional
    public ChatMessageResponse enterRoom(Long roomId, Long memberId) {
        // 멤버십 검증 — 비초대 사용자의 자동 가입 방지
        validateMember(roomId, memberId);
        Member member = findMember(memberId);

        // 이 인스턴스의 Redis 구독 활성화 (멱등, 중복 등록 안 함)
        subscriptionService.subscribe(roomId);

        ChatMessage systemMsg = ChatMessage.of(
            roomId, memberId, member.getNickname(),
            MessageType.ENTER, member.getNickname() + "님이 입장했습니다.");
        return ChatMessageResponse.from(messageRepository.save(systemMsg));
    }

    @Transactional
    public ChatMessageResponse leaveRoom(Long roomId, Long memberId) {
        // 검증과 엔티티 취득을 한 번의 쿼리로 처리
        ChatRoomMember chatRoomMember = memberRepository
            .findByRoomIdAndMemberId(roomId, memberId)
            .orElseThrow(() -> new CustomException(ErrorCode.CHAT_ROOM_FORBIDDEN));
        Member member = findMember(memberId);

        memberRepository.delete(chatRoomMember);
        memberRepository.flush();

        // save 먼저 — 실패 시 트랜잭션 롤백으로 delete가 복원되고 unsubscribe는 호출되지 않음
        ChatMessage systemMsg = ChatMessage.of(
            roomId, memberId, member.getNickname(),
            MessageType.LEAVE, member.getNickname() + "님이 퇴장했습니다.");
        ChatMessageResponse response = ChatMessageResponse.from(messageRepository.save(systemMsg));

        // TX 커밋 후 구독 해제 — 롤백 시 DB 멤버 복원과 인메모리 구독 상태 일치 보장
        if (!memberRepository.existsByRoomId(roomId)) {
            eventPublisher.publishEvent(new ChatRoomUnsubscribedEvent(roomId));
        }

        return response;
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

    private Member findMember(Long memberId) {
        return mutableMemberRepository.findById(memberId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }
}
