package com.org.example.jobcrew.domain.user.service;

import com.org.example.jobcrew.domain.user.dto.response.ProfileResponse;
import jakarta.validation.Valid;
import org.springframework.transaction.annotation.Transactional;

public interface UserService {
    ProfileResponse getMyProfile(Long id);

    @Transactional
    ProfileResponse updateMyProfile(Long id, @Valid ProfileCreateRequest r);
}
