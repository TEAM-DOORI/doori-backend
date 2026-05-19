package com.doori.doori_backend.post.dto.response;

import com.doori.doori_backend.post.domain.Post;
import com.doori.doori_backend.post.domain.PostType;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 게시글 단건 상세 응답 DTO
 * - from() 팩토리 메서드로 Post 엔티티를 변환
 * - authorId, authorNickname: 작성자 정보 (Member 조인 없이 기본 정보만 노출)
 */
public record PostResponse(
	Long postId,
	PostType postType,
	String title,
	String region,
	String university,
	Integer monthlyRent,
	Integer deposit,
	String description,
	List<String> roomImages,
	Long authorId,
	String authorNickname,
	LocalDateTime createdAt,
	LocalDateTime updatedAt
) {

	/**
	 * Post 엔티티를 PostResponse 로 변환한다.
	 * @ElementCollection인 roomImages는 트랜잭션 내에서 호출해야 LazyInitializationException을 방지할 수 있다.
	 */
	public static PostResponse from(Post post) {
		return new PostResponse(
			post.getPostId(),
			post.getPostType(),
			post.getTitle(),
			post.getRegion(),
			post.getUniversity(),
			post.getMonthlyRent(),
			post.getDeposit(),
			post.getDescription(),
			List.copyOf(post.getRoomImages()),
			post.getAuthor().getId(),
			post.getAuthor().getNickname(),
			post.getCreatedAt(),
			post.getUpdatedAt()
		);
	}
}
