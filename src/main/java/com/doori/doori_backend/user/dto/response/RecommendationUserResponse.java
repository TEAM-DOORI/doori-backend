package com.doori.doori_backend.user.dto.response;

import com.doori.doori_backend.auth.domain.Member;
import com.doori.doori_backend.lifestyle.domain.LifestyleProfile;

public record RecommendationUserResponse(
    Long userId,
    String name,
    String nickname,
    String gender,
    String profileImageUrl,
    String housingType,
    String preferredRegion,
    int matchingScore,
    boolean isFavorite
) {
    public static RecommendationUserResponse of(
        Member member,
        LifestyleProfile profile,
        int matchingScore,
        boolean isFavorite
    ) {
        return new RecommendationUserResponse(
            member.getId(),
            member.getName(),
            member.getNickname(),
            member.getGender().name(),
            member.getProfileImageUrl(),
            profile.getHousingType() != null ? profile.getHousingType().getValue() : null,
            profile.getPreferredRegion(),
            matchingScore,
            isFavorite
        );
    }
}
