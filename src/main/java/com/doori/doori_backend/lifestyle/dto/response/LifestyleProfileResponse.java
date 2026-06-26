package com.doori.doori_backend.lifestyle.dto.response;

import com.doori.doori_backend.lifestyle.domain.Atmosphere;
import com.doori.doori_backend.lifestyle.domain.CleaningCycle;
import com.doori.doori_backend.lifestyle.domain.HousingType;
import com.doori.doori_backend.lifestyle.domain.ImportanceLevel;
import com.doori.doori_backend.lifestyle.domain.LifestyleProfile;
import com.doori.doori_backend.lifestyle.domain.PriorityCriteria;
import com.doori.doori_backend.lifestyle.domain.SleepTime;
import com.doori.doori_backend.lifestyle.domain.WakeUpTime;

public record LifestyleProfileResponse(
    String housingType,
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
    String roommateWish,
    boolean isComplete
) {
    public static LifestyleProfileResponse from(LifestyleProfile profile) {
        return new LifestyleProfileResponse(
            profile.getHousingType() != null ? profile.getHousingType().getValue() : null,
            profile.getPreferredRegion(),
            profile.getIsSmoker(),
            profile.getSleepTime(),
            profile.getWakeUpTime(),
            profile.getCleaningCycle(),
            profile.getCleanlinessLevel(),
            profile.getNoiseSensitivity(),
            profile.getAtmosphere(),
            profile.getPriorityCriteria(),
            profile.getBio(),
            profile.getRoommateWish(),
            profile.isComplete()
        );
    }
}
