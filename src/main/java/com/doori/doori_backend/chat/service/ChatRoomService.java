package com.doori.doori_backend.chat.service;

import com.doori.doori_backend.auth.repository.MemberRepository;
import com.doori.doori_backend.chat.domain.ChatRoom;
import com.doori.doori_backend.chat.domain.ChatRoomMember;
import com.doori.doori_backend.chat.domain.RoomType;
import com.doori.doori_backend.chat.dto.request.CreateRoomRequest;
import com.doori.doori_backend.chat.dto.response.ChatRoomResponse;
import com.doori.doori_backend.chat.event.ChatRoomSubscribedEvent;
import com.doori.doori_backend.chat.repository.ChatRoomMemberRepository;
import com.doori.doori_backend.chat.repository.ChatRoomRepository;
import com.doori.doori_backend.global.error.ErrorCode;
import com.doori.doori_backend.global.exception.CustomException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository roomRepository;
    private final ChatRoomMemberRepository memberRepository;
    private final MemberRepository mutableMemberRepository;
    private final ApplicationEventPublisher eventPublisher;

    public List<ChatRoomResponse> getRooms(Long memberId) {
        return roomRepository.findAllByMemberId(memberId).stream()
            .map(ChatRoomResponse::from)
            .toList();
    }

    @Transactional
    public ChatRoomResponse createGroup(CreateRoomRequest request, Long creatorId) {
        ChatRoom room = roomRepository.save(ChatRoom.createGroup(request.name()));

        Set<Long> inviteeIds = new LinkedHashSet<>(request.memberIds());
        inviteeIds.remove(creatorId);

        // 존재하지 않는 멤버 ID 포함 시 FK 없는 환경에서도 잘못된 데이터 방지
        // findAllById로 단일 IN 쿼리 — existsById N회 대신 1회로 처리
        if (mutableMemberRepository.findAllById(inviteeIds).size() != inviteeIds.size()) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        List<ChatRoomMember> members = new ArrayList<>();
        members.add(ChatRoomMember.of(room.getId(), creatorId));
        inviteeIds.forEach(id -> members.add(ChatRoomMember.of(room.getId(), id)));
        memberRepository.saveAll(members);

        // DB 커밋 후 Redis 구독 — 롤백 시 phantom broadcast 방지
        eventPublisher.publishEvent(new ChatRoomSubscribedEvent(room.getId()));
        return ChatRoomResponse.from(room);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public ChatRoomResponse getOrCreateDm(Long targetMemberId, Long requesterId) {
        if (targetMemberId.equals(requesterId)) {
            throw new CustomException(ErrorCode.CHAT_DM_SELF_NOT_ALLOWED);
        }

        return roomRepository.findDmRoom(RoomType.DM, requesterId, targetMemberId)
            .map(ChatRoomResponse::from)
            .orElseGet(() -> {
                if (!mutableMemberRepository.existsById(targetMemberId)) {
                    throw new CustomException(ErrorCode.USER_NOT_FOUND);
                }
                ChatRoom room = roomRepository.save(ChatRoom.createDm());
                memberRepository.saveAll(List.of(
                    ChatRoomMember.of(room.getId(), requesterId),
                    ChatRoomMember.of(room.getId(), targetMemberId)
                ));
                // DB 커밋 후 Redis 구독 — 롤백 시 phantom broadcast 방지
                eventPublisher.publishEvent(new ChatRoomSubscribedEvent(room.getId()));
                return ChatRoomResponse.from(room);
            });
    }
}
