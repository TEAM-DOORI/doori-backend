package com.doori.doori_backend.auth.dto.response;

import com.doori.doori_backend.auth.domain.Member;

public record UserInfo(
    Long userId,
    String name,
    String nickname,
    String profileImageUrl,
    String schoolName
) {
    public static UserInfo from(Member member) {
        return new UserInfo(
            member.getId(),
            member.getName(),
            member.getNickname(),
            member.getProfileImageUrl(),
            member.getSchool().getDisplayName()
        );
    }
}
