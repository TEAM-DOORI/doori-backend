package com.doori.doori_backend.lifestyle.repository;

import com.doori.doori_backend.auth.domain.Member;
import com.doori.doori_backend.auth.domain.MemberStatus;
import com.doori.doori_backend.lifestyle.domain.HousingType;
import com.doori.doori_backend.lifestyle.domain.LifestyleProfile;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LifestyleProfileRepository extends JpaRepository<LifestyleProfile, Long> {

    Optional<LifestyleProfile> findByMember(Member member);

    @Query("""
        SELECT lp FROM LifestyleProfile lp
        WHERE lp.member.status = :status
          AND lp.member.id != :excludeMemberId
        """)
    List<LifestyleProfile> findActiveExcluding(
        @Param("status") MemberStatus status,
        @Param("excludeMemberId") Long excludeMemberId,
        Pageable pageable
    );

    @Query("""
        SELECT lp FROM LifestyleProfile lp
        WHERE lp.member.status = :status
          AND (:housingType IS NULL OR lp.housingType = :housingType)
          AND (:isSmoker IS NULL OR lp.isSmoker = :isSmoker)
          AND lp.member.id > :cursorId
        ORDER BY lp.member.id ASC
        """)
    List<LifestyleProfile> findForExplore(
        @Param("status") MemberStatus status,
        @Param("housingType") HousingType housingType,
        @Param("isSmoker") Boolean isSmoker,
        @Param("cursorId") Long cursorId,
        Pageable pageable
    );
}
