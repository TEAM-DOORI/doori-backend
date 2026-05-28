package com.doori.doori_backend.block.service;

import com.doori.doori_backend.auth.domain.Member;
import com.doori.doori_backend.auth.domain.MemberStatus;
import com.doori.doori_backend.auth.repository.MemberRepository;
import com.doori.doori_backend.block.domain.Block;
import com.doori.doori_backend.block.dto.response.BlockedUserResponse;
import com.doori.doori_backend.block.repository.BlockRepository;
import com.doori.doori_backend.global.discord.BlockCreatedEvent;
import com.doori.doori_backend.global.error.ErrorCode;
import com.doori.doori_backend.global.exception.CustomException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BlockService {

    private final BlockRepository blockRepository;
    private final MemberRepository memberRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void blockUser(Long memberId, Long targetId) {
        if (memberId.equals(targetId)) {
            throw new CustomException(ErrorCode.BLOCK_SELF_NOT_ALLOWED);
        }

        Member member = findActiveMember(memberId);
        Member target = findActiveMember(targetId);

        if (blockRepository.existsByMemberIdAndTargetId(memberId, targetId)) {
            throw new CustomException(ErrorCode.BLOCK_DUPLICATE);
        }

        blockRepository.save(Block.builder()
            .member(member)
            .target(target)
            .build());

        eventPublisher.publishEvent(
            new BlockCreatedEvent(memberId, member.getNickname(), target.getNickname())
        );
    }

    @Transactional
    public void unblockUser(Long memberId, Long targetId) {
        Block block = blockRepository.findByMemberIdAndTargetId(memberId, targetId)
            .orElseThrow(() -> new CustomException(ErrorCode.BLOCK_NOT_FOUND));

        blockRepository.delete(block);
    }

    @Transactional(readOnly = true)
    public List<BlockedUserResponse> getBlockedUsers(Long memberId) {
        return blockRepository.findAllByMemberIdWithTarget(memberId)
            .stream()
            .map(block -> BlockedUserResponse.from(block.getTarget()))
            .toList();
    }

    @Transactional(readOnly = true)
    public List<Long> getBlockedTargetIds(Long memberId) {
        return blockRepository.findBlockedTargetIds(memberId);
    }

    @Transactional(readOnly = true)
    public boolean isBlocked(Long memberId, Long targetId) {
        return blockRepository.existsByMemberIdAndTargetId(memberId, targetId);
    }

    private Member findActiveMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        if (member.getStatus() != MemberStatus.ACTIVE) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }
        return member;
    }
}
