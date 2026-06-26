package com.doori.doori_backend.user.service;

import com.doori.doori_backend.lifestyle.domain.ImportanceLevel;
import com.doori.doori_backend.lifestyle.domain.LifestyleProfile;
import org.springframework.stereotype.Component;

@Component
public class MatchingScoreCalculator {

    public int calculate(LifestyleProfile a, LifestyleProfile b) {
        int score = 0;

        if (a.getIsSmoker() != null && a.getIsSmoker().equals(b.getIsSmoker())) {
            score += 20;
        }
        if (a.getSleepTime() != null && a.getSleepTime().equals(b.getSleepTime())) {
            score += 15;
        }
        if (a.getWakeUpTime() != null && a.getWakeUpTime().equals(b.getWakeUpTime())) {
            score += 10;
        }
        if (a.getCleaningCycle() != null && a.getCleaningCycle().equals(b.getCleaningCycle())) {
            score += 15;
        }
        score += proximityScore(a.getCleanlinessLevel(), b.getCleanlinessLevel(), 15);
        score += proximityScore(a.getNoiseSensitivity(), b.getNoiseSensitivity(), 15);
        if (a.getAtmosphere() != null && a.getAtmosphere().equals(b.getAtmosphere())) {
            score += 10;
        }

        return score;
    }

    private int proximityScore(ImportanceLevel a, ImportanceLevel b, int maxPt) {
        if (a == null || b == null) {
            return 0;
        }
        int diff = Math.abs(a.getScore() - b.getScore());
        if (diff == 0) return maxPt;
        if (diff == 2) return maxPt / 2;
        return 0;
    }
}
