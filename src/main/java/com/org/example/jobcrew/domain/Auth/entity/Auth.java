package com.org.example.jobcrew.domain.Auth.entity;

import com.org.example.jobcrew.domain.user.entity.User;
import com.org.example.jobcrew.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 인증 정보를 저장하는 엔티티
 * - 이메일/비밀번호 인증
 * - 소셜 로그인 정보
 * - 리프레시 토큰 관리
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(uniqueConstraints = {
    @UniqueConstraint(columnNames = {"provider", "provider_user_id"}),
    @UniqueConstraint(columnNames = {"email", "provider"})
})
public class Auth extends BaseEntity {


    // 인증 수단 (LOCAL, GOOGLE, KAKAO 등)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Provider provider;

    // 이메일 (LOCAL의 경우 필수, 소셜의 경우 선택적)
    @Column(nullable = false)
    private String email;

    // 비밀번호 해시 (LOCAL 로그인 전용)
    private String passwordHash;

    // 소셜 공급자의 사용자 ID (소셜 로그인 전용)
    @Column(name = "provider_user_id")
    private String providerUserId;

    // 리프레시 토큰
    @Column(name = "refresh_token", columnDefinition = "text")
    private String refreshToken;

    // 리프레시 토큰 만료 시간
    @Column(name = "refresh_token_expiry")
    private LocalDateTime refreshTokenExpiry;

    // 마지막 로그인 시간 (UTC)
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    // 연관된 회원
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // == 생성 메서드 == //
    public static Auth createLocalAuth(String email, String passwordHash, User user) {
        return Auth.builder()
                .provider(Provider.LOCAL)
                .email(email)
                .passwordHash(passwordHash)
                .user(user)
                .build();
    }

    public static Auth createSocialAuth(Provider provider, String email, String providerUserId, User user) {
        return Auth.builder()
                .provider(provider)
                .email(email)
                .providerUserId(providerUserId)
                .user(user)
                .build();
    }

    // == 비즈니스 로직 == //
    /**
     * 리프레시 토큰을 설정합니다.
     */
    public void setRefreshToken(String refreshToken, LocalDateTime expiry) {
        this.refreshToken = refreshToken;
        this.refreshTokenExpiry = expiry;
        this.lastLoginAt = LocalDateTime.now();
    }

    /**
     * 리프레시 토큰을 무효화합니다.
     */
    public void invalidateRefreshToken() {
        this.refreshToken = null;
        this.refreshTokenExpiry = null;
    }

    /**
     * 리프레시 토큰이 유효한지 확인합니다.
     */
    public boolean isRefreshTokenValid(String token) {
        return this.refreshToken != null && 
               this.refreshToken.equals(token) && 
               this.refreshTokenExpiry != null && 
               this.refreshTokenExpiry.isAfter(LocalDateTime.now());
    }

    // == 연관관계 편의 메서드 == //
    public void setUser(User User) {
        this.user = User;
    }

    public void updateLastLoginInfo(String loginIp) {
        this.lastLoginAt = LocalDateTime.now();
    }
}