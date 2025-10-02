package com.org.example.jobcrew.domain.user.dto.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.org.example.jobcrew.domain.user.entity.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Builder
@AllArgsConstructor
public class ProfileResponse {
    @JsonSerialize(using = ToStringSerializer.class)

    /* ==================== 프로필 기본 ==================== */
    private String nickname;        // 닉네임 (필수)
    private String username;        // 실명 (선택)
    private Integer age;            // 나이 (필수)

    private String avatarUrl;       // 프로필 이미지 (선택)

    /* ==================== 관심 및 학력 ==================== */
    private Set<UserJobTag> interestJobs;       // 관심 직무 (필수)
    private UserEducationLevel educationLevel;  // 최종학력 (필수)

    /* ==================== 기타 정보 ==================== */
    private Gender gender;          // 성별 (필수)
    private MajorType majorType;    // 전공 여부 (필수)
    private CareerType careerType;  // 경력 여부 (필수)

    private String region;          // 지역 (선택)

    /* ==================== 보유 스킬 ==================== */
    private Set<UserSkillTag> skills; // 보유 스킬 (선택)

    /* ==================== 시간 ==================== */
    // BaseEntity createdAt, updatedAt → ISO-8601 문자열 직렬화
    @JsonSerialize(using = ToStringSerializer.class)
    private LocalDateTime createdAt;

    @JsonSerialize(using = ToStringSerializer.class)
    private LocalDateTime updatedAt;

    /* ==================== 정적 팩토리 메서드 ==================== */
    public static ProfileResponse fromEntity(UserProfile profile, Set<UserSkillTag> skills) {
        return ProfileResponse.builder()
                .nickname(profile.getNickname())
                .username(profile.getUsername())
                .age(profile.getAge())
                .avatarUrl(profile.getAvatarUrl())
                .interestJobs(profile.getInterestJobs())
                .educationLevel(profile.getEducationLevel())
                .gender(profile.getGender())
                .majorType(profile.getMajorType())
                .careerType(profile.getCareerType())
                .region(profile.getRegion())
                .skills(skills)
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }

}
