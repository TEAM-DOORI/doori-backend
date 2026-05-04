package com.doori.doori_backend.auth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "refresh_token")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

    @Id
    private String token;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Builder
    public RefreshToken(String token, Long memberId, LocalDateTime expiresAt) {
        this.token = token;
        this.memberId = memberId;
        this.expiresAt = expiresAt;
    }
}
