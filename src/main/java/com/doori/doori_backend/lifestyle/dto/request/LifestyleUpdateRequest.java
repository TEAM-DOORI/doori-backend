package com.doori.doori_backend.lifestyle.dto.request;

import com.doori.doori_backend.lifestyle.domain.Atmosphere;
import com.doori.doori_backend.lifestyle.domain.CleaningCycle;
import com.doori.doori_backend.lifestyle.domain.HousingType;
import com.doori.doori_backend.lifestyle.domain.ImportanceLevel;
import com.doori.doori_backend.lifestyle.domain.PriorityCriteria;
import com.doori.doori_backend.lifestyle.domain.SleepTime;
import com.doori.doori_backend.lifestyle.domain.WakeUpTime;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record LifestyleUpdateRequest(
    @NotNull HousingType housingType,
    String preferredRegion,
    @NotNull Boolean isSmoker,
    @NotNull SleepTime sleepTime,
    @NotNull WakeUpTime wakeUpTime,
    @NotNull CleaningCycle cleaningCycle,
    @NotNull ImportanceLevel cleanlinessLevel,
    @NotNull ImportanceLevel noiseSensitivity,
    @NotNull Atmosphere atmosphere,
    PriorityCriteria priorityCriteria,
    @Size(max = 200) String bio,
    @Size(max = 200) String roommateWish
) {}
