package com.org.example.jobcrew.domain.user.entity;
/**
 * 신입/경력을 나타내는 열거형
 */
public enum CareerType {
    NEW("신입"),
    EXPERIENCED("경력");

    private final String displayName;

    CareerType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
