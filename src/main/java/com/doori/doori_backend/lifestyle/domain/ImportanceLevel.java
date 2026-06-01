package com.doori.doori_backend.lifestyle.domain;

/**
 * 청결도, 소음 등 각 항목에 대한 중요도/민감도를 수치로 표현합니다.
 *
 * 현재 점수값(score)은 임시값으로, 매칭 스코어 계산 로직 확정 후 회의를 거쳐 조정할 예정입니다.
 * - VERY_IMPORTANT(5): 매우 중요해요 / 매우 민감해요
 * - NORMAL(3):         보통이에요
 * - NOT_CARE(1):       크게 신경 안 써요 / 둔감해요
 */
public enum ImportanceLevel {
    VERY_IMPORTANT(5),
    NORMAL(3),
    NOT_CARE(1);

    private final int score;

    ImportanceLevel(int score) {
        this.score = score;
    }

    public int getScore() {
        return score;
    }
}
