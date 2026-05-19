package com.doori.doori_backend.post.dto.response;

import com.doori.doori_backend.post.domain.Post;
import com.doori.doori_backend.post.domain.PostType;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 게시글 목록 응답 DTO (아이템 단위)
 * - 목록 조회 시 불필요한 description, roomImages를 제외해 응답 크기를 줄인다.
 * - 썸네일은 roomImages 첫 번째 이미지를 사용한다.
 */
public record PostListResponse(
    Long postId,
    PostType postType,
    String title,
    String region,
    String university,
    Integer monthlyRent,
    Integer deposit,
    String thumbnailImageUrl,
    Long authorId,
    String authorNickname,
    LocalDateTime createdAt
) {

    /**
     * Post 엔티티를 PostListResponse 로 변환한다.
     * thumbnailImageUrl은 roomImages의 첫 번째 항목이며, 없으면 null이다.
     */
    public static PostListResponse from(Post post) {
        String thumbnail = post.getRoomImages().isEmpty()
            ? null
            : post.getRoomImages().get(0);

        return new PostListResponse(
            post.getPostId(),
            post.getPostType(),
            post.getTitle(),
            post.getRegion(),
            post.getUniversity(),
            post.getMonthlyRent(),
            post.getDeposit(),
            thumbnail,
            post.getAuthor().getId(),
            post.getAuthor().getNickname(),
            post.getCreatedAt()
        );
    }

    /**
     * 목록 래퍼 응답 레코드
     * - posts: 게시글 목록 아이템
     * - total: 필터 조건에 맞는 전체 게시글 수 (페이지네이션 클라이언트 처리용)
     */
    public record Wrapper(List<PostListResponse> posts, int total) {

        public static Wrapper of(List<PostListResponse> posts, int total) {
            return new Wrapper(posts, total);
        }
    }
}
