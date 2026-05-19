package com.doori.doori_backend.post.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

/**
 * 게시글 생성 요청 DTO
 * - postType: 게시글 유형 (TRANSFER/SUBLEASE/WANTED), 문자열로 받아 서비스에서 변환
 * - roomImages: 방 사진 URL 목록 (선택값, 최대 5장 권장)
 */
public record PostCreateRequest(
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
