package com.org.example.jobcrew.domain.user.service;

import com.org.example.jobcrew.domain.user.dto.response.ProfileResponse;
import com.org.example.jobcrew.domain.user.entity.UserProfile;
import com.org.example.jobcrew.domain.user.repository.UserRepository;
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
    @Override
    public ProfileResponse getMyProfile(Long id) {
        return ProfileResponse.fromEntity(fetchMember(id));
    }

    @Transactional
    @Override
    public ProfileResponse updateMyProfile(Long id, @Valid UpdateProfileRequest request) {
        UserProfile profile = fetchMember(id);

        // 필수 필드 업데이트
        profile.setNickname(request.getNickname());
        profile.setAge(request.getAge());
        profile.setGender(request.getGender());
        profile.setInterestJobs(request.getInterestJobs());
        profile.setEducationLevel(request.getEducationLevel());
        profile.setMajorType(request.getMajorType());
        profile.setCareerType(request.getCareerType());

        // 선택 필드 업데이트
        profile.setUsername(request.getUsername());
        profile.setAvatarUrl(request.getAvatarUrl());
        profile.setRegion(request.getRegion());

        // 스킬 업데이트
        if (request.getSkills() != null) {
            profile.setSkills(request.getSkills());
        }

        return ProfileResponse.fromEntity(profile);
    }
}
