package com.org.example.jobcrew.global.test;

import com.org.example.jobcrew.domain.Auth.entity.Auth;
import com.org.example.jobcrew.domain.Auth.entity.Provider;
import com.org.example.jobcrew.domain.Auth.repository.AuthRepository;
import com.org.example.jobcrew.domain.user.entity.Role;
import com.org.example.jobcrew.domain.user.entity.User;
import com.org.example.jobcrew.domain.user.entity.UserProfile;
import com.org.example.jobcrew.domain.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 개발/테스트용 컨트롤러
 *
 * 제공 기능:
 * - 테스트 계정 생성
 * - 기본 테스트 계정들 생성
 * - 데이터베이스 상태 확인
 */
@RestController
@RequestMapping("/api/test")
@Tag(name = "테스트", description = "개발 및 테스트용 API (개발 환경에서만 사용)")
@Slf4j
@Component
@Profile("dev") // dev 환경에서만 실행
@RequiredArgsConstructor
public class DevTestDataInitializer implements ApplicationRunner {
    private final UserRepository userRepository;
    private final AuthRepository authRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        log.info("🚀 [DEV] 기본 테스트 계정 초기화 시작");

        createTestUser("test@example.com", "password123!", "testuser");
        createTestUser("admin@example.com", "admin123!", "admin");
        createTestUser("user1@example.com", "user123!", "user1");

        log.info("✅ [DEV] 기본 테스트 계정 초기화 완료");
    }

    private void createTestUser(String email, String password, String nickname) {
        if (userRepository.existsByNickname(nickname)) {
            log.info("⚠️ 이미 존재하는 계정: {}", nickname);
            return;
        }

        User user = User.builder()
                .email(email)
                .nickname(nickname)
                .role(Role.USER)
                .active(true)
                .build();
        userRepository.save(user);

        UserProfile profile = UserProfile.builder()
                .user(user)
                .completed(false)
                .build();
        user.setProfile(profile);
        userRepository.save(user);

        Auth auth = Auth.builder()
                .user(user)
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .provider(Provider.LOCAL)
                .build();
        authRepository.save(auth);

        log.info("✅ 테스트 계정 생성됨: {} ({})", email, nickname);
    }
}
