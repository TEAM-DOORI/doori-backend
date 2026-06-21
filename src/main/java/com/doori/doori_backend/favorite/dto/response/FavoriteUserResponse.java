package com.doori.doori_backend.favorite.dto.response;

import com.doori.doori_backend.user.domain.Member;

/**
 * 찜한 유저 목록 응답 DTO
 * - 찜 대상 사용자의 기본 프로필 정보만 노출
 */
public record FavoriteUserResponse(
    Long userId,
    String nickname,
    String gender,
    String schoolName,
    String profileImageUrl
) {

    /**
     * Member 엔티티를 FavoriteUserResponse 로 변환한다.
     */
    public static FavoriteUserResponse from(Member member) {
        return new FavoriteUserResponse(
            member.getId(),
            member.getNickname(),
            member.getGender().name(),
            member.getSchool().getDisplayName(),
            member.getProfileImageUrl()
        );
    }
}
