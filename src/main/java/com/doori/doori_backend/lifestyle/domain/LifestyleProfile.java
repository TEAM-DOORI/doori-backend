package com.doori.doori_backend.lifestyle.domain;

import com.doori.doori_backend.user.domain.Member;
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

    @Enumerated(EnumType.STRING)
    private SleepTime sleepTime;

    @Enumerated(EnumType.STRING)
    private WakeUpTime wakeUpTime;

    @Enumerated(EnumType.STRING)
    private CleaningCycle cleaningCycle;

    @Enumerated(EnumType.STRING)
    private ImportanceLevel cleanlinessLevel;

    @Enumerated(EnumType.STRING)
    private ImportanceLevel noiseSensitivity;

    @Enumerated(EnumType.STRING)
    private Atmosphere atmosphere;

    @Enumerated(EnumType.STRING)
    private PriorityCriteria priorityCriteria;

    @Column(length = 200)
    private String bio;

    @Column(length = 200)
    private String roommateWish;

    @Builder
    public LifestyleProfile(Member member, HousingType housingType, String preferredRegion, Boolean isSmoker) {
        this.member = member;
        this.housingType = housingType;
        this.preferredRegion = preferredRegion;
        this.isSmoker = isSmoker;
    }

    public void update(
        HousingType housingType,
        String preferredRegion,
        Boolean isSmoker,
        SleepTime sleepTime,
        WakeUpTime wakeUpTime,
        CleaningCycle cleaningCycle,
        ImportanceLevel cleanlinessLevel,
        ImportanceLevel noiseSensitivity,
        Atmosphere atmosphere,
        PriorityCriteria priorityCriteria,
        String bio,
        String roommateWish
    ) {
        this.housingType = housingType;
        this.preferredRegion = preferredRegion;
        this.isSmoker = isSmoker;
        this.sleepTime = sleepTime;
        this.wakeUpTime = wakeUpTime;
        this.cleaningCycle = cleaningCycle;
        this.cleanlinessLevel = cleanlinessLevel;
        this.noiseSensitivity = noiseSensitivity;
        this.atmosphere = atmosphere;
        this.priorityCriteria = priorityCriteria;
        this.bio = bio;
        this.roommateWish = roommateWish;
    }

    public boolean isComplete() {
        return housingType != null
            && isSmoker != null
            && sleepTime != null
            && wakeUpTime != null
            && cleaningCycle != null
            && cleanlinessLevel != null
            && noiseSensitivity != null
            && atmosphere != null;
    }
}
