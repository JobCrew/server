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
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
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
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/test")
@Tag(name = "테스트", description = "개발 및 테스트용 API (개발 환경에서만 사용)")
public class TestController {

    private final UserRepository userRepository;
    private final AuthRepository authRepository;
    private final PasswordEncoder passwordEncoder;

    @Operation(
            summary = "테스트 계정 생성",
            description = "개발 및 테스트를 위한 임시 계정을 생성합니다. " +
                    "email, password, nickname이 모두 필요합니다."
    )
    @PostMapping("/create-account")
    public ResponseEntity<Map<String, Object>> createTestAccount(@RequestBody Map<String, String> request) {
        Map<String, Object> result = new HashMap<>();

        try {
            String email = request.get("email");
            String password = request.get("password");
            String nickname = request.get("nickname");

            if (email == null || password == null || nickname == null) {
                result.put("success", false);
                result.put("message", "email, password, nickname이 모두 필요합니다.");
                return ResponseEntity.badRequest().body(result);
            }

            // 중복 체크
            if (userRepository.existsByNickname(nickname)) {
                result.put("success", false);
                result.put("message", "이미 존재하는 nickname입니다: " + nickname);
                return ResponseEntity.badRequest().body(result);
            }

            // user 생성
            User user = User.builder()
                    .email(email)
                    .nickname(nickname)
                    .role(Role.valueOf("USER"))
                    .active(true)
                    .build();

            userRepository.save(user);

            // userProfile 생성
            UserProfile profile = UserProfile.builder()
                    .user(user)
                    .completed(false)
                    .build();

            user.setProfile(profile);
            userRepository.save(user);

            // Auth 생성
            Auth auth = Auth.builder()
                    .user(user)
                    .email(email)
                    .passwordHash(passwordEncoder.encode(password))
                    .provider(Provider.valueOf("LOCAL"))
                    .build();

            authRepository.save(auth);

            result.put("success", true);
            result.put("message", "테스트 계정이 생성되었습니다.");
            result.put("userId", user.getId());
            result.put("email", email);
            result.put("nickname",nickname);

            log.info("테스트 계정 생성: {} ({})", email, nickname);

        } catch (Exception e) {
            log.error("테스트 계정 생성 중 오류 발생", e);
            result.put("success", false);
            result.put("message", "계정 생성 중 오류 발생: " + e.getMessage());
        }

        return ResponseEntity.ok(result);
    }

    @Operation(
            summary = "기본 테스트 계정들 생성",
            description = "개발 및 테스트를 위한 기본 테스트 계정들을 일괄 생성합니다. " +
                    "testuser, testuser2, testuser3 계정이 생성됩니다."
    )
    @PostMapping("/create-default-accounts")
    public ResponseEntity<Map<String, Object>> createDefaultAccounts() {
        Map<String, Object> result = new HashMap<>();

        try {
            createTestAccount(Map.of(
                    "email", "test@example.com",
                    "password", "password123!",
                    "nickname", "testuser"
            ));

            createTestAccount(Map.of(
                    "email", "admin@example.com",
                    "password", "admin123!",
                    "nickname", "admin"
            ));

            createTestAccount(Map.of(
                    "email", "user1@example.com",
                    "password", "user123!",
                    "nickname", "user1"
            ));

            result.put("success", true);
            result.put("message", "기본 테스트 계정들이 생성되었습니다.");

        } catch (Exception e) {
            log.error("기본 테스트 계정 생성 중 오류 발생", e);
            result.put("success", false);
            result.put("message", "기본 계정 생성 중 오류 발생: " + e.getMessage());
        }

        return ResponseEntity.ok(result);
    }

    /**
     * 데이터베이스 상태 확인
     */
    @GetMapping("/db-status")
    public ResponseEntity<Map<String, Object>> checkDatabaseStatus() {
        Map<String, Object> result = new HashMap<>();

        try {
            long userCount = userRepository.count();
            long authCount = authRepository.count();

            result.put("success", true);
            result.put("userCount", userCount);
            result.put("authCount", authCount);
            result.put("message", "데이터베이스 상태 정상");

        } catch (Exception e) {
            log.error("데이터베이스 상태 확인 중 오류 발생", e);
            result.put("success", false);
            result.put("message", "데이터베이스 상태 확인 중 오류 발생: " + e.getMessage());
        }

        return ResponseEntity.ok(result);
    }
}