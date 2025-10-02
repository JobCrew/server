package com.org.example.jobcrew.domain.user.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.org.example.jobcrew.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "user_profiles")
public class UserProfile extends BaseEntity {
    
    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    /* ==================== 프로필 기본 ==================== */

    @Column
    private String username;        // 이름, 선택

    @Column(length = 500)
    private String avatarUrl;       // 프로필이미지

    /* ==================== 관심 및 학력 ==================== */

    @Enumerated(EnumType.STRING)
    @Column(name = "interest_job")
    @Builder.Default
    private Set<UserJobTag> interestJobs = new HashSet<>(); // 관심직무

    @Column
    private UserEducationLevel educationLevel; // 최종학력

    /* ==================== 기타 정보 ===================== */
    @Column(nullable = true)
    //@Column(nullable = false) -> 테스트위해서 변경
    private Integer age;            // 나이 (필수)

    @Enumerated(EnumType.STRING)
    //@Column(nullable = false, length = 10)
    @Column(nullable = true, length = 10)
    private Gender gender;          // 성별
    @Enumerated(EnumType.STRING)
    //@Column(nullable = false, length = 20)
    @Column(nullable = true, length = 20)
    private MajorType majorType;    // 전공 여부
    @Enumerated(EnumType.STRING)
    //@Column(nullable = false, length = 20)
    @Column(nullable = true, length = 20)
    private CareerType careerType;  // 경력 여부

    @Column(length = 100)
    private String region;          // 지역 (선택, 시 단위까지)

    @Enumerated(EnumType.STRING)
    @Column
    private Set<UserSkillTag> skill= new HashSet<>(); //보유스킬

    //@Column(nullable = false)
    @Column(nullable = true)
    @Builder.Default
    private boolean completed = false;                // 프로필 완료 여부

}
