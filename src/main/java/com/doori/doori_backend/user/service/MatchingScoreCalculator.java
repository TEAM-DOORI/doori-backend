package com.doori.doori_backend.user.service;

import com.doori.doori_backend.lifestyle.domain.LifestyleProfile;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * 매칭 스코어 계산기.
 *
 * 현재 배점은 임시값이며, 회의 후 확정할 예정입니다.
 * 각 항목별 배점 근거:
 *   - isSmoker (20pt): 흡연 여부는 동거 적합성에 크게 영향을 미침
 *   - sleepTime (15pt): 취침 패턴 일치는 생활 리듬 호환성의 핵심
 *   - wakeUpTime (10pt): 기상 패턴 일치
 *   - cleaningCycle (15pt): 청소 주기 일치
 *   - cleanlinessLevel (15pt): ImportanceLevel.score 차이가 작을수록 높은 점수
 *   - noiseSensitivity (15pt): ImportanceLevel.score 차이가 작을수록 높은 점수
 *   - atmosphere (10pt): 생활 분위기 일치
 * 합계: 100pt
 */
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
        if (a.getCleanlinessLevel() != null && b.getCleanlinessLevel() != null) {
            score += scoreByProximity(a.getCleanlinessLevel().getScore(), b.getCleanlinessLevel().getScore(), 15);
        }
        if (a.getNoiseSensitivity() != null && b.getNoiseSensitivity() != null) {
            score += scoreByProximity(a.getNoiseSensitivity().getScore(), b.getNoiseSensitivity().getScore(), 15);
        }
        if (a.getAtmosphere() != null && a.getAtmosphere().equals(b.getAtmosphere())) {
            score += 10;
        }

        return score;
    }

    public List<String> matchedCriteria(LifestyleProfile a, LifestyleProfile b) {
        List<String> matched = new ArrayList<>();
        if (a.getIsSmoker() != null && a.getIsSmoker().equals(b.getIsSmoker())) matched.add("SMOKING");
        if (a.getSleepTime() != null && a.getSleepTime().equals(b.getSleepTime())) matched.add("SLEEP_TIME");
        if (a.getWakeUpTime() != null && a.getWakeUpTime().equals(b.getWakeUpTime())) matched.add("WAKE_UP_TIME");
        if (a.getCleaningCycle() != null && a.getCleaningCycle().equals(b.getCleaningCycle())) matched.add("CLEANING_CYCLE");
        if (a.getAtmosphere() != null && a.getAtmosphere().equals(b.getAtmosphere())) matched.add("ATMOSPHERE");
        return matched;
    }

    private int scoreByProximity(int scoreA, int scoreB, int maxPts) {
        int diff = Math.abs(scoreA - scoreB);
        if (diff == 0) return maxPts;
        if (diff == 2) return maxPts / 2;
        return 0;
    }
}
