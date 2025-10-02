package com.org.example.jobcrew.domain.user.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.org.example.jobcrew.global.exception.CustomException;
import com.org.example.jobcrew.global.exception.ErrorCode;

/**
 * 사용자 관심키워드/보유스킬
 * ──────────────────────────────────────────────────────
 * • UserProfile 1 : 1 관계 (cascade + orphanRemoval)
 */
public enum UserSkillTag {
    PYTHON,
    JAVA,
    C,
    CPP,
    JAVASCRIPT,
    TYPESCRIPT,
    SQL,
    KOTLIN,
    GO,
    RUST,
    RUBY_ON_RAILS,
    PHP,
    MYSQL,
    POSTGRESQL,
    MONGODB,
    ORACLE_DB,
    FIREBASE_REALTIME_DB,
    HTML,
    CSS_SCSS,
    REACT,
    VUE,
    NEXT_JS,
    ANGULAR,
    GIT,
    DOCKER,
    KUBERNETES,
    AWS,
    GCP,
    AZURE,
    JENKINS,
    SPRING,
    DJANGO,
    FLASK,
    EXPRESS,
    NESTJS,
    FASTAPI,
    JUNIT,
    PYTEST,
    JEST,
    CYPRESS,
    SELENIUM,
    K6;

    @JsonValue
    public String getValue() {
        return this.name().toLowerCase(); // JSON에서는 소문자로 내려가도록
    }
    @JsonCreator
    public static UserSkillTag fromValue(String value) {
        try {
            return UserSkillTag.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.INVALID_SKILL_TAG, "잘못된 입력: " + value);
        }
    }
}
