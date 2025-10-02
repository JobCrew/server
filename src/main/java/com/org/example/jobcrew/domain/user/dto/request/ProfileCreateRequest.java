package com.org.example.jobcrew.domain.user.dto.request;

import com.org.example.jobcrew.domain.user.entity.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Set;
@Getter
@Builder
@AllArgsConstructor
public class ProfileCreateRequest {
    /** 필수 */
    @NotBlank(message = "닉네임은 필수입니다")
    private String nickname;
    private String age;
    private Gender gender;
    private MajorType majorType;
    private UserEducationLevel educationLevel;
    private CareerType careerType;

    /** 선택 */
    private String name;
    private String avatarUrl;
    private Set<UserJobTag> interestJobs;
    private String region;
    private Set<UserSkillTag> skill;

}
