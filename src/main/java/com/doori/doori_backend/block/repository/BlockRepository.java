package com.doori.doori_backend.block.repository;

import com.doori.doori_backend.block.domain.Block;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BlockRepository extends JpaRepository<Block, Long> {

    boolean existsByMemberIdAndTargetId(Long memberId, Long targetId);

    Optional<Block> findByMemberIdAndTargetId(Long memberId, Long targetId);

    @Query("SELECT b FROM Block b JOIN FETCH b.target WHERE b.member.id = :memberId")
    List<Block> findAllByMemberIdWithTarget(@Param("memberId") Long memberId);

    @Query("SELECT b.target.id FROM Block b WHERE b.member.id = :memberId")
    List<Long> findBlockedTargetIds(@Param("memberId") Long memberId);
}
