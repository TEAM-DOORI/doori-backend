package com.doori.doori_backend.user.repository;

import com.doori.doori_backend.user.domain.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByNicknameAndIdNot(String nickname, Long id);
}
