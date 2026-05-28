package com.doori.doori_backend.post.repository;

import com.doori.doori_backend.post.domain.Post;
import com.doori.doori_backend.post.domain.PostType;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long> {

    /**
     * postType 필터링 + 최신순 정렬 목록 조회 (차단 필터 없음)
     */
    @Query("""
        SELECT p FROM Post p
        JOIN FETCH p.author
        WHERE (:postType IS NULL OR p.postType = :postType)
        ORDER BY p.createdAt DESC
        """)
    Page<Post> findAllByFilter(@Param("postType") PostType postType, Pageable pageable);

    /**
     * postType 필터링 + 차단 유저 제외 + 최신순 정렬 목록 조회
     * - NOT EXISTS 서브쿼리로 Block 테이블을 직접 조회하여 성능 개선
     */
    @Query("""
        SELECT p FROM Post p
        JOIN FETCH p.author
        WHERE (:postType IS NULL OR p.postType = :postType)
          AND NOT EXISTS (
              SELECT 1 FROM Block b
              WHERE b.member.id = :memberId
              AND b.target.id = p.author.id
          )
        ORDER BY p.createdAt DESC
        """)
    Page<Post> findAllByFilterExcludingAuthors(
        @Param("memberId") Long memberId,
        @Param("postType") PostType postType,
        Pageable pageable
    );
}
