package com.doori.doori_backend.post.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

/**
 * 게시글 수정 요청 DTO
 * - 수정 시에도 전체 필드를 다시 받는 full-update 방식
 * - postType: 게시글 유형도 수정 가능 (문자열로 받아 서비스에서 변환)
 * - roomImages: 수정된 방 사진 URL 목록 (null 허용, @ElementCollection 교체 방식)
 */
public record PostUpdateRequest(
    @NotNull(message = "게시글 유형은 필수입니다.")
    String postType,

    @NotBlank(message = "제목은 필수입니다.")
    String title,

    @NotBlank(message = "지역은 필수입니다.")
    String region,

    @NotBlank(message = "대학교는 필수입니다.")
    String university,

    @NotNull(message = "월세는 필수입니다.")
    @Positive(message = "월세는 0보다 커야 합니다.")
    Integer monthlyRent,

    @NotNull(message = "보증금은 필수입니다.")
    @Positive(message = "보증금은 0보다 커야 합니다.")
    Integer deposit,

    @NotBlank(message = "상세 설명은 필수입니다.")
    String description,

    List<String> roomImages
) {
}
