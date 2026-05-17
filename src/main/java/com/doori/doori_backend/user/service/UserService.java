package com.doori.doori_backend.user.service;

import com.doori.doori_backend.auth.domain.Member;
import com.doori.doori_backend.auth.domain.MemberStatus;
import com.doori.doori_backend.auth.repository.MemberRepository;
import com.doori.doori_backend.auth.repository.RefreshTokenRepository;
import com.doori.doori_backend.global.error.ErrorCode;
import com.doori.doori_backend.global.exception.CustomException;
import com.doori.doori_backend.lifestyle.domain.HousingType;
import com.doori.doori_backend.lifestyle.domain.LifestyleProfile;
import com.doori.doori_backend.lifestyle.repository.LifestyleProfileRepository;
import com.doori.doori_backend.user.dto.request.DeleteAccountRequest;
import com.doori.doori_backend.user.dto.request.UpdateProfileRequest;
import com.doori.doori_backend.user.dto.response.ExploreItem;
import com.doori.doori_backend.user.dto.response.ExploreResponse;
import com.doori.doori_backend.user.dto.response.MyProfileResponse;
import com.doori.doori_backend.user.dto.response.RecommendationsResponse;
import com.doori.doori_backend.user.dto.response.UserProfileResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final int RECOMMENDATION_LIMIT = 10;

    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final LifestyleProfileRepository lifestyleProfileRepository;
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
        member.updateProfile(request.name(), request.nickname());
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(Long requesterId, Long targetId) {
        Member target = memberRepository.findById(targetId)
            .filter(m -> m.getStatus() == MemberStatus.ACTIVE)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        int matchingScore = computeMatchingScore(requesterId, targetId);
        return UserProfileResponse.of(target, matchingScore);
    }

    @Transactional(readOnly = true)
    public RecommendationsResponse getRecommendations(Long memberId) {
        Member member = findActiveMember(memberId);
        LifestyleProfile myProfile = lifestyleProfileRepository.findByMember(member)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_LIFESTYLE_PROFILE_REQUIRED));

        if (!myProfile.isComplete()) {
            throw new CustomException(ErrorCode.USER_LIFESTYLE_PROFILE_REQUIRED);
        }

        List<LifestyleProfile> candidates = lifestyleProfileRepository.findActiveExcluding(
            MemberStatus.ACTIVE, memberId, PageRequest.of(0, 50)
        );

        List<ExploreItem> recommendations = candidates.stream()
            .map(profile -> {
                int score = calculateScore(myProfile, profile);
                return ExploreItem.of(profile.getMember(), profile, score, false);
            })
            .sorted(Comparator.comparingInt(ExploreItem::matchingScore).reversed())
            .limit(RECOMMENDATION_LIMIT)
            .toList();

        return new RecommendationsResponse(recommendations);
    }

    @Transactional(readOnly = true)
    public ExploreResponse explore(
        Long memberId,
        String residenceType,
        Boolean isSmoker,
        String cursor,
        int limit
    ) {
        HousingType housingType = parseHousingType(residenceType);
        long cursorId = decodeCursor(cursor);

        List<LifestyleProfile> results = lifestyleProfileRepository.findForExplore(
            MemberStatus.ACTIVE, housingType, isSmoker, cursorId,
            PageRequest.of(0, limit + 1)
        );

        boolean hasMore = results.size() > limit;
        List<LifestyleProfile> page = hasMore ? results.subList(0, limit) : results;

        Optional<LifestyleProfile> myProfile = lifestyleProfileRepository.findByMember(
            findActiveMember(memberId)
        );

        List<ExploreItem> items = page.stream()
            .filter(p -> !p.getMember().getId().equals(memberId))
            .map(profile -> {
                int score = myProfile.map(mp -> calculateScore(mp, profile)).orElse(0);
                return ExploreItem.of(profile.getMember(), profile, score, false);
            })
            .toList();

        String nextCursor = hasMore ? encodeCursor(page.get(page.size() - 1).getMember().getId()) : null;
        return new ExploreResponse(items, nextCursor, hasMore);
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

    private int computeMatchingScore(Long requesterId, Long targetId) {
        Optional<LifestyleProfile> requesterProfile = lifestyleProfileRepository.findByMemberId(requesterId);
        Optional<LifestyleProfile> targetProfile = lifestyleProfileRepository.findByMemberId(targetId);

        if (requesterProfile.isEmpty() || targetProfile.isEmpty()) {
            return 0;
        }
        return calculateScore(requesterProfile.get(), targetProfile.get());
    }

    private int calculateScore(LifestyleProfile a, LifestyleProfile b) {
        int score = 0;
        if (a.getHousingType() != null && a.getHousingType().equals(b.getHousingType())) {
            score += 30;
        }
        if (a.getPreferredRegion() != null && a.getPreferredRegion().equals(b.getPreferredRegion())) {
            score += 30;
        }
        if (a.getIsSmoker() != null && a.getIsSmoker().equals(b.getIsSmoker())) {
            score += 20;
        }
        if (a.getMember().getSchool().equals(b.getMember().getSchool())) {
            score += 20;
        }
        return score;
    }

    private HousingType parseHousingType(String residenceType) {
        if (residenceType == null || residenceType.isBlank()) {
            return null;
        }
        try {
            return HousingType.fromValue(residenceType);
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.COMMON_BAD_REQUEST);
        }
    }

    private long decodeCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return 0L;
        }
        try {
            byte[] decoded = Base64.getDecoder().decode(cursor);
            return Long.parseLong(new String(decoded, StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new CustomException(ErrorCode.COMMON_BAD_REQUEST);
        }
    }

    private String encodeCursor(Long id) {
        return Base64.getEncoder().encodeToString(String.valueOf(id).getBytes(StandardCharsets.UTF_8));
    }

}
