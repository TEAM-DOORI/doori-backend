package com.doori.doori_backend.chat.service;

import com.doori.doori_backend.block.service.BlockPolicyService;
import com.doori.doori_backend.chat.domain.ChatRoom;
import com.doori.doori_backend.chat.domain.ChatRoomMember;
import com.doori.doori_backend.chat.domain.RoomType;
import com.doori.doori_backend.chat.dto.request.CreateRoomRequest;
import com.doori.doori_backend.chat.dto.response.ChatRoomResponse;
import com.doori.doori_backend.chat.repository.ChatRoomMemberRepository;
import com.doori.doori_backend.chat.repository.ChatRoomRepository;
import com.doori.doori_backend.global.error.ErrorCode;
import com.doori.doori_backend.global.exception.CustomException;
import com.doori.doori_backend.global.security.ActiveMemberValidator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository roomRepository;
    private final ChatRoomMemberRepository memberRepository;
    private final ActiveMemberValidator activeMemberValidator;
    private final BlockPolicyService blockPolicyService;

    public List<ChatRoomResponse> getRooms(Long memberId) {
        activeMemberValidator.validateActiveMember(memberId);
        List<ChatRoom> rooms = roomRepository.findAllByMemberId(memberId);
        if (rooms.isEmpty()) {
            return List.of();
        }

        Map<Long, List<Long>> roomMemberIds = new HashMap<>();
        memberRepository.findAllByRoomIdIn(rooms.stream().map(ChatRoom::getId).toList())
            .forEach(member -> roomMemberIds
                .computeIfAbsent(member.getRoomId(), ignored -> new ArrayList<>())
                .add(member.getMemberId()));

        return rooms.stream()
            .filter(room -> !blockPolicyService.hasBlockedParticipant(
                memberId, roomMemberIds.getOrDefault(room.getId(), List.of())
            ))
            .map(ChatRoomResponse::from)
            .toList();
    }

    @Transactional
    public ChatRoomResponse createGroup(CreateRoomRequest request, Long creatorId) {
        activeMemberValidator.validateActiveMember(creatorId);
        ChatRoom room = roomRepository.save(ChatRoom.createGroup(request.name()));

        Set<Long> inviteeIds = new LinkedHashSet<>(request.memberIds());
        inviteeIds.remove(creatorId);

        activeMemberValidator.validateAllActiveMembers(inviteeIds);
        inviteeIds.forEach(inviteeId -> blockPolicyService.validateNotBlockedUser(creatorId, inviteeId));

        List<ChatRoomMember> members = new ArrayList<>();
        members.add(ChatRoomMember.of(room.getId(), creatorId));
        inviteeIds.forEach(id -> members.add(ChatRoomMember.of(room.getId(), id)));
        memberRepository.saveAll(members);
        return ChatRoomResponse.from(room);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public ChatRoomResponse getOrCreateDm(Long targetMemberId, Long requesterId) {
        if (targetMemberId.equals(requesterId)) {
            throw new CustomException(ErrorCode.CHAT_DM_SELF_NOT_ALLOWED);
        }
        activeMemberValidator.validateActiveMember(requesterId);
        blockPolicyService.validateNotBlockedUser(requesterId, targetMemberId);

        return roomRepository.findDmRoom(RoomType.DM, requesterId, targetMemberId)
            .map(ChatRoomResponse::from)
            .orElseGet(() -> {
                activeMemberValidator.validateActiveMember(targetMemberId);
                ChatRoom room = roomRepository.save(ChatRoom.createDm());
                memberRepository.saveAll(List.of(
                    ChatRoomMember.of(room.getId(), requesterId),
                    ChatRoomMember.of(room.getId(), targetMemberId)
                ));
                return ChatRoomResponse.from(room);
            });
    }
}
