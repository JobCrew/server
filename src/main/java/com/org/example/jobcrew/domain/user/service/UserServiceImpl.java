package com.org.example.jobcrew.domain.user.service;

import com.org.example.jobcrew.domain.user.dto.request.ProfileCreateRequest;
import com.org.example.jobcrew.domain.user.dto.request.UserNicknameRequest;
import com.org.example.jobcrew.domain.user.dto.response.ProfileResponse;
import com.org.example.jobcrew.domain.user.dto.response.UserNicknameResponse;
import com.org.example.jobcrew.domain.user.entity.User;
import com.org.example.jobcrew.domain.user.entity.UserProfile;
import com.org.example.jobcrew.domain.user.repository.UserRepository;
import com.org.example.jobcrew.global.exception.CustomException;
import com.org.example.jobcrew.global.exception.ErrorCode;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    /**
     * Profile
     */
    // 내 프로필 조회
    @Override
    public ProfileResponse getMyProfile(Long id) {
        User user = fetchUser(id);              // User 조회
        UserProfile profile = user.getProfile();// UserProfile 추출
        return ProfileResponse.fromEntity(profile, profile.getSkill());
    }
    // 내 프로필 수정 (생성+수정)
    @Transactional
    @Override
    public ProfileResponse updateMyProfile(Long id, @Valid ProfileCreateRequest request) {
        // 1. User 조회
        User user = fetchUser(id);  // 내부에서 userRepository.findById orElseThrow ...

        // 2. Profile 조회
        UserProfile profile = user.getProfile();

        if (profile == null) { // 신규 생성
            profile = UserProfile.builder()
                    .user(user)
                    .nickname(request.getNickname())
                    .username(request.getName())
                    .avatarUrl(request.getAvatarUrl())
                    .age(Integer.valueOf(request.getAge()))
                    .gender(request.getGender())
                    .majorType(request.getMajorType())
                    .educationLevel(request.getEducationLevel())
                    .careerType(request.getCareerType())
                    .region(request.getRegion())
                    .interestJobs(request.getInterestJobs())
                    .skill(request.getSkill())
                    .build();

            user.setProfile(profile); // 연관관계 설정
        } else { // 기존 프로필 수정
            profile.setNickname(request.getNickname());
            profile.setUsername(request.getName());
            profile.setAvatarUrl(request.getAvatarUrl());
            profile.setAge(Integer.valueOf(request.getAge()));
            profile.setGender(request.getGender());
            profile.setMajorType(request.getMajorType());
            profile.setEducationLevel(request.getEducationLevel());
            profile.setCareerType(request.getCareerType());
            profile.setRegion(request.getRegion());

            if (request.getInterestJobs() != null) {
                profile.setInterestJobs(request.getInterestJobs());
            }
            if (request.getSkill() != null) {
                profile.setSkill(request.getSkill());
            }
        }

        // 4. 응답 변환
        return ProfileResponse.fromEntity(profile, profile.getSkill());
    }
    // nickname 설정
    @Transactional
    @Override
    public UserNicknameResponse setNickname(Long id, UserNicknameRequest req, HttpServletResponse res) {
        String newNickname = req.getNickname();

        if(userRepository.existsByNickname(newNickname)) {
            throw new CustomException(ErrorCode.INVALID_NICKNAME, "이미 존재하는 닉네임입니다: " + newNickname);
        }
        User u = userRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        u.getProfile().setNickname(newNickname);

        // 멤버네임만 설정하고 토큰은 건드리지 않음
        // Access Token은 Auth ID 기반, Refresh Token은 UUID 기반으로 유지

        return UserNicknameResponse.from(u);           // dirty-checking flush
    }

    // username 으로 id 찾기
     @Override
    public Long getIdByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException(
                        ErrorCode.USER_NOT_FOUND,
                        String.format("사용자를 찾을 수 없습니다: username=%s", username)
                ));
        return user.getId();
    }

    //nickname 으로 id 찾기
    @Override
    public Long getIdByNickname(String nickname) {
        UserProfile profile = userRepository.findByNickname(nickname)
                .orElseThrow(() -> new CustomException(
                        ErrorCode.USER_NOT_FOUND,
                        String.format("사용자를 찾을 수 없습니다: nickname=%s", nickname)
                ));
        return profile.getUser().getId();
    }


    /* ---------- util ---------- */
    private User fetchUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    private User fetchUser(String nickname) {
        return userRepository.findBynickname(nickname)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

}
