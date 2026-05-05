package com.doori.doori_backend.auth.dto.response;

import com.doori.doori_backend.auth.domain.Member;

public record SignupResponse(
    Long userId,
    String email,
    String name,
    String nickname,
    String schoolName
) {
    public static SignupResponse from(Member member) {
        return new SignupResponse(
            member.getId(),
            member.getEmail(),
            member.getName(),
            member.getNickname(),
            member.getSchool().getDisplayName()
        );
    }
}
