package com.doori.doori_backend.user.service;

import com.doori.doori_backend.auth.domain.Member;
import com.doori.doori_backend.auth.domain.MemberStatus;
import com.doori.doori_backend.auth.repository.MemberRepository;
import com.doori.doori_backend.auth.repository.RefreshTokenRepository;
import com.doori.doori_backend.global.error.ErrorCode;
import com.doori.doori_backend.global.exception.CustomException;
import com.doori.doori_backend.user.dto.request.DeleteAccountRequest;
import com.doori.doori_backend.user.dto.request.UpdateProfileRequest;
import com.doori.doori_backend.user.dto.response.MyProfileResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public MyProfileResponse getMyProfile(Long memberId) {
        Member member = findActiveMember(memberId);
        return MyProfileResponse.from(member);
    }

    @Transactional
    public void updateProfile(Long memberId, UpdateProfileRequest request) {
        Member member = findActiveMember(memberId);
        if (request.nickname() != null
            && memberRepository.existsByNicknameAndIdNot(request.nickname(), memberId)) {
            throw new CustomException(ErrorCode.USER_NICKNAME_DUPLICATE);
        }
        String nickname = request.nickname() != null ? request.nickname() : member.getNickname();
        member.updateProfile(request.name(), nickname);
    }

    @Transactional
    public void deleteAccount(Long memberId, DeleteAccountRequest request) {
        Member member = findActiveMember(memberId);
        if (!passwordEncoder.matches(request.password(), member.getPassword())) {
            throw new CustomException(ErrorCode.AUTH_INVALID_CREDENTIALS);
        }
        member.deactivate();
        refreshTokenRepository.deleteByMemberId(memberId);
    }

    private Member findActiveMember(Long memberId) {
        return memberRepository.findById(memberId)
            .filter(m -> m.getStatus() == MemberStatus.ACTIVE)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }
}
