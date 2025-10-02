package com.org.example.jobcrew.domain.user.dto.request;

import com.org.example.jobcrew.domain.user.entity.Gender;
import com.org.example.jobcrew.domain.user.entity.MajorType;
import com.org.example.jobcrew.domain.user.entity.UserEducationLevel;
import jakarta.validation.constraints.NotBlank;

public class ProfileCreateRequest {
    /** 필수 */
    @NotBlank(message = "닉네임은 필수입니다")
    private String nickname;
    private String age;
    private Gender gender;
    private MajorType majorType;
    private UserEducationLevel educationLevel;
}
