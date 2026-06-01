package com.doori.doori_backend.post.domain;

import com.doori.doori_backend.user.domain.Member;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * 게시글 엔티티
 * - author: LAZY 로딩으로 성능 최적화, 조회 시 트랜잭션 내에서 접근해야 함
 * - roomImages: @ElementCollection으로 별도 테이블에 저장, 트랜잭션 내 접근 필수
 */
@Entity
@Table(name = "post")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postId;

    // 작성자 — LAZY 로딩: N+1 방지를 위해 필요 시 fetch join 사용
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member author;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PostType postType;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String region;

    @Column(nullable = false)
    private String university;

    // 월세 (nullable: WANTED 타입은 선택값일 수 있음)
    private Integer monthlyRent;

    // 보증금 (nullable: WANTED 타입은 선택값일 수 있음)
    private Integer deposit;

    // 본문 설명 — TEXT로 긴 내용 저장 허용
    @Column(columnDefinition = "TEXT")
    private String description;

    // 방 사진 URL 목록 — 별도 테이블로 관리
    @ElementCollection
    @CollectionTable(name = "post_room_image", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "image_url")
    private List<String> roomImages = new ArrayList<>();

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Builder
    public Post(
        Member author,
        PostType postType,
        String title,
        String region,
        String university,
        Integer monthlyRent,
        Integer deposit,
        String description,
        List<String> roomImages
    ) {
        this.author = author;
        this.postType = postType;
        this.title = title;
        this.region = region;
        this.university = university;
        this.monthlyRent = monthlyRent;
        this.deposit = deposit;
        this.description = description;
        if (roomImages != null) {
            this.roomImages.addAll(roomImages);
        }
    }

    /**
     * 게시글 전체 수정 (full-update 방식)
     * roomImages는 기존 목록을 clear 후 새 목록으로 교체한다.
     * @ElementCollection 특성상 orphan removal이 자동 처리된다.
     */
    public void update(
        PostType postType,
        String title,
        String region,
        String university,
        Integer monthlyRent,
        Integer deposit,
        String description,
        List<String> roomImages
    ) {
        this.postType = postType;
        this.title = title;
        this.region = region;
        this.university = university;
        this.monthlyRent = monthlyRent;
        this.deposit = deposit;
        this.description = description;
        this.roomImages.clear();
        if (roomImages != null) {
            this.roomImages.addAll(roomImages);
        }
    }

    /**
     * 게시글 소유권 검증
     * @param memberId 현재 요청한 사용자 ID
     * @return 작성자 본인이면 true
     */
    public boolean isOwnedBy(Long memberId) {
        return this.author.getId().equals(memberId);
    }
}
