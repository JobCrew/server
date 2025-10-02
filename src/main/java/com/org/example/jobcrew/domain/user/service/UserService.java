package com.org.example.jobcrew.domain.user.service;

import com.org.example.jobcrew.domain.user.dto.request.ProfileCreateRequest;
import com.org.example.jobcrew.domain.user.dto.request.UserNicknameRequest;
import com.org.example.jobcrew.domain.user.dto.response.ProfileResponse;
import com.org.example.jobcrew.domain.user.dto.response.UserNicknameResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.transaction.annotation.Transactional;

public interface UserService {
    ProfileResponse getMyProfile(Long id);

    @Transactional
    ProfileResponse updateMyProfile(Long id, @Valid ProfileCreateRequest request);

    // nickname 설정
    @Transactional
    UserNicknameResponse setNickname(Long id, UserNicknameRequest req, HttpServletResponse res);

//    // username 으로 id 찾기
//    Long getIdByUsername(String username);

    Long getIdByNickname(String nickname);
}
