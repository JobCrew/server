package com.org.example.jobcrew.domain.user.entity;

import com.org.example.jobcrew.global.entity.BaseEntity;
import com.org.example.jobcrew.global.exception.CustomException;
import com.org.example.jobcrew.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 서비스 내 ‘회원’ 루트 엔티티
 * ──────────────────────────────────────────────────────
 * • UserProfile 1 : 1 관계 (cascade + orphanRemoval)
 */
@Entity
@Table(
        name = "users",
        indexes = @Index(name = "idx_User_email", columnList = "email", unique = true)
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class User extends BaseEntity {


    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;       // 활성화 여부

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Role role = Role.USER;

    @Column(name = "last_active_at")
    private LocalDateTime lastActiveAt;   // 마지막 로그인 시간

    /* ==================== 프로필 연관 ==================== */

    @OneToOne(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            orphanRemoval = true
    )
    private UserProfile profile;

    /* === 연관관계 편의 메서드 === */
    public void setProfile(UserProfile profile) {
        this.profile = profile;
        if (profile != null && profile.getUser() != this) {
            profile.setUser(this); // 양방향 연관관계 동기화
        }
    }

    /* ==================== 정적 생성 ==================== */

    public static User create(String email) {
        if (!StringUtils.hasText(email))
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "이메일은 필수입니다.");
        return User.builder()
                .email(email.trim().toLowerCase())
                .build();
    }


    /* ==================== equals & hashCode ==================== */

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User other)) return false;
        return getId() != null && getId().equals(other.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
