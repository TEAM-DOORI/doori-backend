package com.doori.doori_backend.chat.service;

import com.doori.doori_backend.block.service.BlockPolicyService;
import com.doori.doori_backend.user.domain.Member;
import com.doori.doori_backend.chat.domain.ChatMessage;
import com.doori.doori_backend.chat.domain.ChatRoomMember;
import com.doori.doori_backend.chat.domain.MessageType;
import com.doori.doori_backend.chat.dto.request.ChatMessageRequest;
import com.doori.doori_backend.chat.dto.request.DmMessageRequest;
import com.doori.doori_backend.chat.dto.response.ChatMessageResponse;
import com.doori.doori_backend.chat.dto.response.SliceResponse;
import com.doori.doori_backend.chat.repository.ChatMessageRepository;
import com.doori.doori_backend.chat.repository.ChatRoomMemberRepository;
import com.doori.doori_backend.chat.repository.ChatRoomRepository;
import com.doori.doori_backend.global.error.ErrorCode;
import com.doori.doori_backend.global.exception.CustomException;
import com.doori.doori_backend.global.security.ActiveMemberValidator;
import lombok.RequiredArgsConstructor;
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
    private final ActiveMemberValidator activeMemberValidator;
    private final BlockPolicyService blockPolicyService;

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
        blockPolicyService.validateNotBlockedChatRoom(memberId, memberRepository.findMemberIdsByRoomId(roomId));
        Member sender = activeMemberValidator.getActiveMember(memberId);
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
        blockPolicyService.validateNotBlockedChatRoom(memberId, memberRepository.findMemberIdsByRoomId(roomId));
        Member member = activeMemberValidator.getActiveMember(memberId);

        ChatMessage systemMsg = messageRepository.save(
            ChatMessage.of(roomId, memberId, member.getNickname(),
                MessageType.ENTER, member.getNickname() + "님이 입장했습니다."));
        // 입장 이벤트도 lastMessageAt 갱신 — 채팅방 목록 정렬 기준 통일
        roomRepository.findById(roomId)
            .ifPresent(room -> room.recordMessage(systemMsg.getCreatedAt()));

        return ChatMessageResponse.from(systemMsg);
    }

    @Transactional
    public ChatMessageResponse leaveRoom(Long roomId, Long memberId) {
        activeMemberValidator.validateActiveMember(memberId);
        if (!roomRepository.existsById(roomId)) {
            throw new CustomException(ErrorCode.CHAT_ROOM_NOT_FOUND);
        }
        blockPolicyService.validateNotBlockedChatRoom(memberId, memberRepository.findMemberIdsByRoomId(roomId));

        ChatRoomMember chatRoomMember = memberRepository
            .findByRoomIdAndMemberId(roomId, memberId)
            .orElseThrow(() -> new CustomException(ErrorCode.CHAT_ROOM_FORBIDDEN));
        Member member = activeMemberValidator.getActiveMember(memberId);

        memberRepository.delete(chatRoomMember);
        memberRepository.flush();

        ChatMessage systemMsg = messageRepository.save(
            ChatMessage.of(roomId, memberId, member.getNickname(),
                MessageType.LEAVE, member.getNickname() + "님이 퇴장했습니다."));
        // 퇴장 이벤트도 lastMessageAt 갱신 — 채팅방 목록 정렬 기준 통일
        roomRepository.findById(roomId)
            .ifPresent(room -> room.recordMessage(systemMsg.getCreatedAt()));

        return ChatMessageResponse.from(systemMsg);
    }

    public SliceResponse<ChatMessageResponse> getHistory(Long roomId, Long cursorId, int size, Long memberId) {
        validateMember(roomId, memberId);
        blockPolicyService.validateNotBlockedChatRoom(memberId, memberRepository.findMemberIdsByRoomId(roomId));
        Slice<ChatMessageResponse> result = messageRepository
            .findByRoomIdAndIdLessThanOrderByIdDesc(roomId, cursorId, PageRequest.of(0, size))
            .map(ChatMessageResponse::from);
        return SliceResponse.from(result);
    }

    private void validateMember(Long roomId, Long memberId) {
        activeMemberValidator.validateActiveMember(memberId);
        if (!roomRepository.existsById(roomId)) {
            throw new CustomException(ErrorCode.CHAT_ROOM_NOT_FOUND);
        }
        if (!memberRepository.existsByRoomIdAndMemberId(roomId, memberId)) {
            throw new CustomException(ErrorCode.CHAT_ROOM_FORBIDDEN);
        }
    }
}
