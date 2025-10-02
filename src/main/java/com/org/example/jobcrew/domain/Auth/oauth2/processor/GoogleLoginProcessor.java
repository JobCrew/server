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
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class GoogleLoginProcessor implements OAuth2LoginProcessor {

    private final AuthRepository authRepository;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public Provider provider() {
        return Provider.GOOGLE; // 여기서는 구글 로그인 전용 프로세서
    }

    @Override
    @Transactional
    public TokenBundle process(OAuth2User oAuth2User, HttpServletRequest request) {
        String providerUserId = oAuth2User.getAttribute("sub");
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name"); // google user nickanme 을 name값으로 초기화 (기본)
        String picture = oAuth2User.getAttribute("picture");

        // 1. 기존 사용자 확인 또는 신규 생성
        Optional<Auth> optionalAuth = authRepository.findByProviderAndProviderUserId(provider(), providerUserId);
        Auth auth = optionalAuth.orElseGet(() -> create(oAuth2User, request));

        // 2. 마지막 로그인 정보 업데이트
        auth.updateLastLoginInfo(UserAgentUtils.getClientIP(request));

        // 3. 토큰 발급
        String accessToken = jwtTokenProvider.createAccessToken(auth.getId());        // Auth ID 기반
        String refreshToken = jwtTokenProvider.createRefreshToken(auth.getUser().getId()); // User ID 기반
        LocalDateTime refreshTokenExpiry = LocalDateTime.now()
                .plus(Duration.ofMillis(jwtTokenProvider.getRefreshTokenExpirationTime()));

        // 4. Refresh Token 저장
        auth.setRefreshToken(refreshToken, refreshTokenExpiry);
        authRepository.save(auth);

        log.info("Google 로그인 완료: {}", email);
        return new TokenBundle(accessToken, refreshToken, auth.getUser().isProfileCompleted());
    }

    /**
     * 신규 사용자 생성
     */
    @Transactional
    public Auth create(OAuth2User oAuth2User, HttpServletRequest request) {
        String email = oAuth2User.getAttribute("email");
        String providerUserId = oAuth2User.getAttribute("sub");
        String name = oAuth2User.getAttribute("name");
        String picture = oAuth2User.getAttribute("picture");

        // 1. UserProfile 기본값 생성 (선택 정보는 null)
        UserProfile profile = UserProfile.builder()
                .avatarUrl(picture)
                .completed(false)
                .build();

        // 2. User 생성
        User user = User.builder()
                .email(email)
                .nickname(name)   // 소셜 로그인 기본 닉네임을 name으로
                .profile(profile)
                .build();

        profile.setUser(user); // 연관관계 편의 메서드
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
