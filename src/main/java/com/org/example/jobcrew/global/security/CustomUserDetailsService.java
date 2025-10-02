package com.org.example.jobcrew.global.security;

import com.org.example.jobcrew.domain.Auth.repository.AuthRepository;
import com.org.example.jobcrew.domain.user.entity.User;
import com.org.example.jobcrew.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** JwtAuthFilter 가 호출하는 UserDetailsProvider */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepo;
    private final AuthRepository authRepo;

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        log.debug("CustomuserDetailsService.loadUserByUsername 호출: identifier={}", identifier);

        // identifier가 null이거나 빈 문자열인 경우 처리
        if (identifier == null || identifier.trim().isEmpty()) {
            throw new UsernameNotFoundException("Identifier cannot be null or empty");
        }

        // 1. identifier가 숫자인 경우 → User ID로 조회
        if (identifier.matches("\\d+")) {
            Long userId = Long.valueOf(identifier);
            log.debug("User ID 기반 사용자 조회: {}", userId);

            return userRepo.findById(userId)
                    .map(user -> new CustomUserDetails(
                            user.getId(),
                            user.getEmail(),
                            user.getNickname(),
                            user.isActive(),
                            user.getRole()
                    ))
                    .orElseThrow(() -> new UsernameNotFoundException("User not found by ID: " + userId));
        }

        // 2. identifier가 이메일인 경우
        if (identifier.contains("@")) {
            log.debug("Email 기반 사용자 조회: {}", identifier);
            return userRepo.findByEmailForAuth(identifier)
                    .map(user -> new CustomUserDetails(
                            user.getId(),
                            user.getEmail(),
                            user.getNickname(),
                            user.isActive(),
                            user.getRole()
                    ))
                    .orElseThrow(() -> new UsernameNotFoundException("User not found by email: " + identifier));
        }

        // 3. 그 외는 nickname 기반
        log.debug("Nickname 기반 사용자 조회: {}", identifier);
        return userRepo.findByNickname(identifier)
                .map(user -> new CustomUserDetails(
                        user.getId(),
                        user.getEmail(),
                        user.getNickname(),
                        user.isActive(),
                        user.getRole()
                ))
                .orElseThrow(() -> new UsernameNotFoundException("User not found by nickname: " + identifier));
    }
}

