package com.doori.doori_backend.auth.repository;

import com.doori.doori_backend.auth.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {
    void deleteByMemberId(Long memberId);
}
