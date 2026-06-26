package com.doori.doori_backend.lifestyle.domain;

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
