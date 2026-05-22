package com.doori.doori_backend.favorite.dto.response;

import com.doori.doori_backend.post.domain.Post;
import com.doori.doori_backend.post.domain.PostType;
import java.time.LocalDateTime;

/**
 * 찜한 게시글 목록 응답 DTO
 * - 목록 수준의 게시글 정보만 노출 (description, roomImages 전체 목록 제외)
 * - thumbnailImageUrl: roomImages 첫 번째 이미지, 없으면 null
 */
public record FavoritePostResponse(
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
     * Post 엔티티를 FavoritePostResponse 로 변환한다.
     * roomImages는 @ElementCollection이므로 트랜잭션 내에서 호출해야 한다.
     */
    public static FavoritePostResponse from(Post post) {
        String thumbnail = post.getRoomImages().isEmpty()
            ? null
            : post.getRoomImages().get(0);

        return new FavoritePostResponse(
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
}
