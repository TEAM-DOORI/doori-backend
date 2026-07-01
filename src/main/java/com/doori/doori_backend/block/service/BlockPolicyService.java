package com.doori.doori_backend.block.service;

import com.doori.doori_backend.block.repository.BlockRepository;
import com.doori.doori_backend.global.error.ErrorCode;
import com.doori.doori_backend.global.exception.CustomException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BlockPolicyService {

    private final BlockRepository blockRepository;

    public void validateNotBlockedUser(Long memberId, Long targetId) {
        validateNotBlocked(memberId, targetId, () -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    public void validateNotBlockedPostAuthor(Long memberId, Long authorId) {
        validateNotBlocked(memberId, authorId, () -> new CustomException(ErrorCode.POST_NOT_FOUND));
    }

    public void validateNotBlockedChatRoom(Long memberId, Collection<Long> participantIds) {
        if (memberId == null || participantIds == null || participantIds.isEmpty()) {
            return;
        }

        Set<Long> blockedIds = Set.copyOf(blockRepository.findBlockedTargetIds(memberId));
        boolean containsBlockedParticipant = participantIds.stream()
            .filter(participantId -> !memberId.equals(participantId))
            .anyMatch(blockedIds::contains);

        if (containsBlockedParticipant) {
            throw new CustomException(ErrorCode.CHAT_ROOM_FORBIDDEN);
        }
    }

    public boolean hasBlockedParticipant(Long memberId, Collection<Long> participantIds) {
        if (memberId == null || participantIds == null || participantIds.isEmpty()) {
            return false;
        }

        Set<Long> blockedIds = Set.copyOf(blockRepository.findBlockedTargetIds(memberId));
        return participantIds.stream()
            .filter(participantId -> !memberId.equals(participantId))
            .anyMatch(blockedIds::contains);
    }

    public <T> List<T> filterBlockedTargets(Long memberId, Collection<T> values, java.util.function.Function<T, Long> idExtractor) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }

        Set<Long> blockedIds = Set.copyOf(blockRepository.findBlockedTargetIds(memberId));
        return values.stream()
            .filter(value -> !blockedIds.contains(idExtractor.apply(value)))
            .toList();
    }

    private void validateNotBlocked(Long memberId, Long targetId, Supplier<CustomException> exceptionSupplier) {
        if (memberId == null || targetId == null) {
            return;
        }
        if (blockRepository.existsByMemberIdAndTargetId(memberId, targetId)) {
            throw exceptionSupplier.get();
        }
    }
}
