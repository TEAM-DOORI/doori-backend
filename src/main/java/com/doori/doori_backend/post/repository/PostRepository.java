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
     * - blockedIds가 비어 있지 않을 때만 호출한다 (빈 컬렉션 IN절 오류 방지)
     */
    @Query("""
        SELECT p FROM Post p
        JOIN FETCH p.author
        WHERE (:postType IS NULL OR p.postType = :postType)
          AND p.author.id NOT IN :blockedIds
        ORDER BY p.createdAt DESC
        """)
    Page<Post> findAllByFilterExcludingAuthors(
        @Param("postType") PostType postType,
        @Param("blockedIds") List<Long> blockedIds,
        Pageable pageable
    );
}
