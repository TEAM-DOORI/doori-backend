package com.doori.doori_backend.lifestyle.service;

import com.doori.doori_backend.global.error.ErrorCode;
import com.doori.doori_backend.global.exception.CustomException;
import com.doori.doori_backend.lifestyle.domain.LifestyleProfile;
import com.doori.doori_backend.lifestyle.dto.request.LifestyleUpdateRequest;
import com.doori.doori_backend.lifestyle.dto.response.LifestyleProfileResponse;
import com.doori.doori_backend.lifestyle.repository.LifestyleProfileRepository;
import com.doori.doori_backend.user.domain.Member;
import com.doori.doori_backend.user.domain.MemberStatus;
import com.doori.doori_backend.user.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LifestyleService {

    private final LifestyleProfileRepository lifestyleProfileRepository;
    private final MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public LifestyleProfileResponse getMyLifestyle(Long memberId) {
        LifestyleProfile profile = lifestyleProfileRepository.findByMemberId(memberId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_LIFESTYLE_PROFILE_REQUIRED));
        return LifestyleProfileResponse.from(profile);
    }

    @Transactional
    public void updateLifestyle(Long memberId, LifestyleUpdateRequest request) {
        Member member = memberRepository.findById(memberId)
            .filter(m -> m.getStatus() == MemberStatus.ACTIVE)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        LifestyleProfile profile = lifestyleProfileRepository.findByMember(member)
            .orElseGet(() -> lifestyleProfileRepository.save(
                LifestyleProfile.builder().member(member).build()
            ));

        profile.update(
            request.housingType(),
            request.preferredRegion(),
            request.isSmoker(),
            request.sleepTime(),
            request.wakeUpTime(),
            request.cleaningCycle(),
            request.cleanlinessLevel(),
            request.noiseSensitivity(),
            request.atmosphere(),
            request.priorityCriteria(),
            request.bio(),
            request.roommateWish()
        );
    }
}
