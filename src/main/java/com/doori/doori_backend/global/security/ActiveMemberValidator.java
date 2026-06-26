package com.doori.doori_backend.global.security;

import com.doori.doori_backend.global.error.ErrorCode;
import com.doori.doori_backend.global.exception.CustomException;
import com.doori.doori_backend.user.domain.Member;
import com.doori.doori_backend.user.domain.MemberStatus;
import com.doori.doori_backend.user.repository.MemberRepository;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ActiveMemberValidator {

    private final MemberRepository memberRepository;

    public Member getActiveMember(Long memberId) {
        return memberRepository.findById(memberId)
            .filter(member -> member.getStatus() == MemberStatus.ACTIVE)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    public void validateActiveMember(Long memberId) {
        getActiveMember(memberId);
    }

    public void validateAllActiveMembers(Collection<Long> memberIds) {
        if (memberIds == null || memberIds.isEmpty()) {
            return;
        }

        List<Member> members = memberRepository.findAllById(memberIds);
        if (members.size() != memberIds.size()) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        boolean hasInactiveMember = members.stream()
            .anyMatch(member -> member.getStatus() != MemberStatus.ACTIVE);
        if (hasInactiveMember) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }
    }
}
