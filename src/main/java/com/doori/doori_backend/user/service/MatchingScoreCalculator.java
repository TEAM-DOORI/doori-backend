package com.doori.doori_backend.user.service;

import com.doori.doori_backend.lifestyle.domain.LifestyleProfile;
import org.springframework.stereotype.Component;

@Component
public class MatchingScoreCalculator {

    public int calculate(LifestyleProfile a, LifestyleProfile b) {
        int score = 0;
        if (a.getHousingType() != null && a.getHousingType().equals(b.getHousingType())) {
            score += 30;
        }
        if (a.getPreferredRegion() != null && a.getPreferredRegion().equals(b.getPreferredRegion())) {
            score += 30;
        }
        if (a.getIsSmoker() != null && a.getIsSmoker().equals(b.getIsSmoker())) {
            score += 20;
        }
        if (a.getMember().getSchool().equals(b.getMember().getSchool())) {
            score += 20;
        }
        return score;
    }
}
