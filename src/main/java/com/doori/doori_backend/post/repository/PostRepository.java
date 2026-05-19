package com.doori.doori_backend.post.repository;

import com.doori.doori_backend.post.domain.Post;
import com.doori.doori_backend.post.domain.PostType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long> {

    /**
     * postType 필터링 + 최신순 정렬 목록 조회
     * - postType이 null이면 전체 목록, 아니면 해당 타입만 반환
     * - author를 fetch join하여 PostListResponse 변환 시 N+1 방지
     */
    @Query("""
        SELECT p FROM Post p
        JOIN FETCH p.author
        WHERE (:postType IS NULL OR p.postType = :postType)
        ORDER BY p.createdAt DESC
        """)
    Page<Post> findAllByFilter(@Param("postType") PostType postType, Pageable pageable);
}
