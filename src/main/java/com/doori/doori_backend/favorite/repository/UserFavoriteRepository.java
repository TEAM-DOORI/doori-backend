package com.doori.doori_backend.favorite.repository;

import com.doori.doori_backend.favorite.domain.UserFavorite;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserFavoriteRepository extends JpaRepository<UserFavorite, Long> {

    /**
     * 특정 사용자가 특정 대상을 찜했는지 확인
     */
    boolean existsByMemberIdAndTargetId(Long memberId, Long targetId);

    /**
     * 특정 사용자가 특정 대상에 대해 등록한 찜 조회 (삭제 시 사용)
     */
    Optional<UserFavorite> findByMemberIdAndTargetId(Long memberId, Long targetId);

    /**
     * 특정 사용자가 찜한 유저 목록 조회
     * - target을 fetch join하여 N+1 방지
     */
    @Query("""
        SELECT uf FROM UserFavorite uf
        JOIN FETCH uf.target
        WHERE uf.member.id = :memberId
        ORDER BY uf.createdAt DESC
        """)
    List<UserFavorite> findAllByMemberIdWithTarget(@Param("memberId") Long memberId);
}
