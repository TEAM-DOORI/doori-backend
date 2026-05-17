package com.doori.doori_backend.user.dto.response;

import com.doori.doori_backend.auth.domain.Member;

public record UserProfileResponse(
    Long userId,
    String name,
    String nickname,
    String gender,
    String schoolName,
    String profileImageUrl,
    int matchingScore
) {
    public static UserProfileResponse of(Member member, int matchingScore) {
        return new UserProfileResponse(
            member.getId(),
            member.getName(),
            member.getNickname(),
            member.getGender().name(),
            member.getSchool().getDisplayName(),
            member.getProfileImageUrl(),
            matchingScore
        );
    }
}
