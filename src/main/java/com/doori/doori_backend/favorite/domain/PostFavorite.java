package com.doori.doori_backend.favorite.domain;

import com.doori.doori_backend.user.domain.Member;
import com.doori.doori_backend.post.domain.Post;
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
 * 게시글 찜 엔티티
 * - member: 찜을 누른 사용자 (주체)
 * - post: 찜 대상 게시글
 * - (member_id, post_id) 복합 유니크 제약으로 중복 찜을 DB 레벨에서 방지
 */
@Entity
@Table(
    name = "post_favorite",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_post_favorite",
        columnNames = {"member_id", "post_id"}
    )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class PostFavorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 찜을 누른 사용자 — LAZY 로딩
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    // 찜 대상 게시글 — LAZY 로딩
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public PostFavorite(Member member, Post post) {
        this.member = member;
        this.post = post;
    }
}
