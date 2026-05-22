package com.doori.doori_backend.favorite.repository;

import com.doori.doori_backend.favorite.domain.PostFavorite;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostFavoriteRepository extends JpaRepository<PostFavorite, Long> {

    /**
     * 특정 사용자가 특정 게시글을 찜했는지 확인
     */
    @Query("SELECT COUNT(pf) > 0 FROM PostFavorite pf WHERE pf.member.id = :memberId AND pf.post.postId = :postId")
    boolean existsByMemberAndPost(@Param("memberId") Long memberId, @Param("postId") Long postId);

    /**
     * 특정 사용자가 특정 게시글에 대해 등록한 찜 조회 (삭제 시 사용)
     */
    @Query("SELECT pf FROM PostFavorite pf WHERE pf.member.id = :memberId AND pf.post.postId = :postId")
    Optional<PostFavorite> findByMemberAndPost(@Param("memberId") Long memberId, @Param("postId") Long postId);

    /**
     * 특정 사용자가 찜한 게시글 목록 조회
     * - post, post.author, post.roomImages를 fetch join하여 N+1 방지
     */
    @Query("""
        SELECT pf FROM PostFavorite pf
        JOIN FETCH pf.post p
        JOIN FETCH p.author
        LEFT JOIN FETCH p.roomImages
        WHERE pf.member.id = :memberId
        ORDER BY pf.createdAt DESC
        """)
    List<PostFavorite> findAllByMemberIdWithPost(@Param("memberId") Long memberId);
}
