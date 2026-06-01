package com.doori.doori_backend.user.dto.response;

import com.doori.doori_backend.user.domain.Member;
import java.time.LocalDateTime;

public record MyProfileResponse(
    Long userId,
    String email,
    String name,
    String nickname,
    String gender,
    String schoolName,
    String profileImageUrl,
    String status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    LocalDateTime lastLoginAt
) {
    public static MyProfileResponse from(Member member) {
        return new MyProfileResponse(
            member.getId(),
            member.getEmail(),
            member.getName(),
            member.getNickname(),
            member.getGender().name(),
            member.getSchool().getDisplayName(),
            member.getProfileImageUrl(),
            member.getStatus().name().toLowerCase(),
            member.getCreatedAt(),
            member.getUpdatedAt(),
            member.getLastLoginAt()
        );
    }
}
