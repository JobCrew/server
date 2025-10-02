package com.org.example.jobcrew.domain.user.entity;
/**
 * 전공자/비전공자 를 나타내는 열거형
 */
public enum MajorType {
    MAJOR("전공"),
    NON_MAJOR("비전공");

    private final String displayName;
    MajorType(String displayName) {
        this.displayName = displayName;
    }
    public String getDisplayName() {
        return displayName;
    }
}

