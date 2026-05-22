package com.doori.doori_backend.favorite.domain;

import com.doori.doori_backend.auth.domain.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * 유저 찜 엔티티
 * - member: 찜을 누른 사용자 (주체)
 * - target: 찜 대상 사용자
 * - (member_id, target_id) 복합 유니크 제약으로 중복 찜을 DB 레벨에서 방지
 * - 자기 자신 찜 방지는 Service 레이어에서 검증
 */
@Entity
@Table(
    name = "user_favorite",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_user_favorite",
        columnNames = {"member_id", "target_id"}
    )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class UserFavorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 찜을 누른 사용자 — LAZY 로딩
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    // 찜 대상 사용자 — LAZY 로딩
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_id", nullable = false)
    private Member target;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public UserFavorite(Member member, Member target) {
        this.member = member;
        this.target = target;
    }
}
