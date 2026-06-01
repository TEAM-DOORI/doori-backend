package com.doori.doori_backend.block.dto.response;

import com.doori.doori_backend.user.domain.Member;

public record BlockedUserResponse(
    Long userId,
    String nickname,
    String gender,
    String schoolName,
    String profileImageUrl
) {

    public static BlockedUserResponse from(Member member) {
        return new BlockedUserResponse(
            member.getId(),
            member.getNickname(),
            member.getGender().name(),
            member.getSchool().getDisplayName(),
            member.getProfileImageUrl()
        );
    }
}
