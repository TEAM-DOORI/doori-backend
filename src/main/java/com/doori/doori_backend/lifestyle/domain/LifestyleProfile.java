package com.doori.doori_backend.lifestyle.domain;

import com.doori.doori_backend.auth.domain.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "lifestyle_profile")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LifestyleProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", unique = true, nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    private HousingType housingType;

    private String preferredRegion;

    private Boolean isSmoker;

    @Builder
    public LifestyleProfile(Member member, HousingType housingType, String preferredRegion, Boolean isSmoker) {
        this.member = member;
        this.housingType = housingType;
        this.preferredRegion = preferredRegion;
        this.isSmoker = isSmoker;
    }

    public boolean isComplete() {
        return housingType != null && preferredRegion != null;
    }
}
