package com.doori.doori_backend.user.dto.response;

import com.doori.doori_backend.user.domain.Member;
import com.doori.doori_backend.lifestyle.domain.LifestyleProfile;
import java.util.List;

public record ProfileCardResponse(
    Long userId,
    String name,
    String nickname,
    String gender,
    String schoolName,
    String profileImageUrl,
    String bio,
    int matchingScore,
    List<String> matchedCriteria
) {
    public static ProfileCardResponse of(Member member, LifestyleProfile profile,
        int matchingScore, List<String> matchedCriteria) {
        return new ProfileCardResponse(
            member.getId(),
            member.getName(),
            member.getNickname(),
            member.getGender().name(),
            member.getSchool().getDisplayName(),
            member.getProfileImageUrl(),
            profile != null ? profile.getBio() : null,
            matchingScore,
            matchedCriteria
        );
    }
}
