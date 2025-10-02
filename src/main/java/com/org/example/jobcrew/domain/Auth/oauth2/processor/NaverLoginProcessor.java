package com.org.example.jobcrew.domain.Auth.oauth2.processor;

import com.org.example.jobcrew.domain.Auth.dto.TokenBundle;
import com.org.example.jobcrew.domain.Auth.entity.Auth;
import com.org.example.jobcrew.domain.Auth.entity.Provider;
import com.org.example.jobcrew.domain.Auth.repository.AuthRepository;
import com.org.example.jobcrew.domain.user.entity.User;
import com.org.example.jobcrew.domain.user.entity.UserProfile;
import com.org.example.jobcrew.domain.user.repository.UserRepository;
import com.org.example.jobcrew.global.security.jwt.JwtTokenProvider;
import com.org.example.jobcrew.global.util.UserAgentUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class NaverLoginProcessor implements OAuth2LoginProcessor {

    private final AuthRepository authRepository;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public Provider provider() {
        return Provider.NAVER;
    }

    @Override
    @Transactional
    public TokenBundle process(OAuth2User oAuth2User, HttpServletRequest request) {
        // 네이버는 response 안에 사용자 정보가 들어있음
        Map<String, Object> response = oAuth2User.getAttribute("response");
        if (response == null) {
            throw new IllegalStateException("Naver OAuth2 response is missing");
        }

        String providerUserId = (String) response.get("id");
        String email = (String) response.get("email");
        String name = (String) response.get("name");
        String nickname = (String) response.getOrDefault("nickname", name);
        String picture = (String) response.get("profile_image");

        // 1. 기존 사용자 확인 또는 신규 생성
        Optional<Auth> optionalAuth = authRepository.findByProviderAndProviderUserId(provider(), providerUserId);
        Auth auth = optionalAuth.orElseGet(() -> create(providerUserId, email, nickname, picture));

        // 2. 마지막 로그인 정보 업데이트
        auth.updateLastLoginInfo(UserAgentUtils.getClientIP(request));

        // 3. 토큰 발급
        String accessToken = jwtTokenProvider.createAccessToken(auth.getId());
        String refreshToken = jwtTokenProvider.createRefreshToken(auth.getUser().getId());
        LocalDateTime refreshTokenExpiry = LocalDateTime.now()
                .plus(Duration.ofMillis(jwtTokenProvider.getRefreshTokenExpirationTime()));

        // 4. Refresh Token 저장
        auth.setRefreshToken(refreshToken, refreshTokenExpiry);
        authRepository.save(auth);

        log.info("Naver 로그인 완료: {}", email);
        return new TokenBundle(accessToken, refreshToken, auth.getUser().isProfileCompleted());
    }

    /**
     * 신규 사용자 생성
     */
    @Transactional
    public Auth create(String providerUserId, String email, String nickname, String picture) {
        // 1. UserProfile 기본값 생성
        UserProfile profile = UserProfile.builder()
                .avatarUrl(picture)
                .completed(false)
                .build();

        // 2. User 생성
        User user = User.builder()
                .email(email != null ? email : "naver_" + providerUserId + "@naver.com") // 이메일 제공 거부 시 임시 생성
                .nickname(nickname != null ? nickname : "네이버사용자")
                .profile(profile)
                .build();

        profile.setUser(user);
        User savedUser = userRepository.save(user);

        // 3. Auth 엔티티 생성 및 저장
        Auth auth = Auth.builder()
                .provider(provider())
                .providerUserId(providerUserId)
                .email(email)
                .user(savedUser)
                .lastLoginAt(LocalDateTime.now())
                .build();

        return authRepository.save(auth);
    }
}
