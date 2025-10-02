package com.org.example.jobcrew.domain.Auth.service;

import com.org.example.jobcrew.domain.Auth.dto.TokenBundle;
import com.org.example.jobcrew.domain.Auth.entity.Auth;
import com.org.example.jobcrew.domain.Auth.entity.Provider;
import com.org.example.jobcrew.domain.Auth.repository.AuthRepository;
import com.org.example.jobcrew.global.exception.CustomException;
import com.org.example.jobcrew.global.exception.ErrorCode;
import com.org.example.jobcrew.global.security.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 인증 관련 서비스
 * - 로그인/로그아웃 처리
 * - 토큰 재발급
 * - 인증 정보 관리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final AuthRepository authRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    /**
     * 이메일/비밀번호 로그인 처리
     */
    @Transactional
    public TokenBundle login(String email, String password, HttpServletRequest request) {
        // 1. 사용자 인증
        Auth auth = authRepository.findByEmailAndProvider(email, Provider.LOCAL)
                .orElseThrow(() -> new CustomException(ErrorCode.AUTH_BAD_CREDENTIAL));
        
        // 2. 비밀번호 검증
        if (!passwordEncoder.matches(password, auth.getPasswordHash())) {
            throw new CustomException(ErrorCode.AUTH_BAD_CREDENTIAL);
        }

        // 3. 토큰 생성 및 사용자 캐싱
        // Access Token은 Auth ID 기반, Refresh Token은 user ID 기반
        String accessToken = jwtTokenProvider.createAccessToken(auth.getId());
        String refreshToken = jwtTokenProvider.createRefreshToken(auth.getUser().getId());
        
        // 4. 리프레시 토큰 저장
        LocalDateTime refreshTokenExpiry = LocalDateTime.now()
                .plus(Duration.ofMillis(jwtTokenProvider.getRefreshTokenExpirationTime()));
        auth.setRefreshToken(refreshToken, refreshTokenExpiry);
        
        // 5. 사용자 정보 캐싱 (온라인 상태 관리)
//        cacheUserInfo(auth);
        
        log.info("User logged in: {}", email);
        return new TokenBundle(accessToken, refreshToken, auth.getUser().isProfileCompleted());
    }
    
    /**
     * 소셜 로그인 처리
     */
    @Transactional
    public TokenBundle socialLogin(Provider provider, String email, String providerUserId, HttpServletRequest request) {
        // 1. 소셜 계정으로 인증 정보 조회
        Auth auth = authRepository.findByProviderAndProviderUserId(provider, providerUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.AUTH_SOCIAL_ACCOUNT_NOT_FOUND));
        
        // 2. 토큰 생성
        // Access Token은 Auth ID 기반, Refresh Token은 user ID 기반
        String accessToken = jwtTokenProvider.createAccessToken(auth.getId());
        String refreshToken = jwtTokenProvider.createRefreshToken(auth.getUser().getId());
        
        // 3. 리프레시 토큰 갱신
        LocalDateTime refreshTokenExpiry = LocalDateTime.now()
                .plus(Duration.ofMillis(jwtTokenProvider.getRefreshTokenExpirationTime()));
        auth.setRefreshToken(refreshToken, refreshTokenExpiry);
        
//        // 4. 사용자 정보 캐싱 (온라인 상태 관리)
//        cacheUserInfo(auth);
        
        log.info("Social login successful: {} - {}", provider, email);
        return new TokenBundle(accessToken, refreshToken, auth.getUser().isProfileCompleted());
    }
    
    /**
     * 로그아웃 처리
     */
    @Transactional
    public void logout(Long userId) {
        // 1. 인증 정보에서 리프레시 토큰 제거
        authRepository.findByUserId(userId).ifPresent(auth -> {
            auth.invalidateRefreshToken();
//            // 2. Redis에서 사용자 캐시 제거 (오프라인 상태로 변경)
//            userCacheService.delete(userId);
            log.info("User logged out: {}", userId);
        });
    }
    
    /**
     * 액세스 토큰 재발급
     */
    @Transactional
    public TokenBundle refreshToken(String refreshToken, String expiredAccessToken) {
        // 1. 리프레시 토큰으로 인증 정보 조회
        Auth auth = authRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new CustomException(ErrorCode.AUTH_INVALID_REFRESH_TOKEN));
        
        // 2. 리프레시 토큰 유효성 검사
        if (!auth.isRefreshTokenValid(refreshToken)) {
            throw new CustomException(ErrorCode.AUTH_EXPIRED_REFRESH_TOKEN);
        }
        
        // 3. 만료된 Access Token에서 Auth ID 추출 및 검증
        try {
            Long tokenAuthId = jwtTokenProvider.getAuthIdFromAccessToken(expiredAccessToken);
            if (!tokenAuthId.equals(auth.getId())) {
                log.warn("Access token auth ID mismatch. Token: {}, DB: {}", 
                        tokenAuthId, auth.getId());
                throw new CustomException(ErrorCode.AUTH_INVALID_REFRESH_TOKEN);
            }
        } catch (Exception e) {
            log.warn("Failed to extract auth ID from expired access token: {}", e.getMessage());
            throw new CustomException(ErrorCode.AUTH_INVALID_REFRESH_TOKEN);
        }
        
        // 4. 새로운 액세스 토큰 발급 (Auth ID 기반)
        String newAccessToken = jwtTokenProvider.createAccessToken(auth.getId());
        log.info("Access token refreshed for auth ID: {}", auth.getId());
        
        return new TokenBundle(newAccessToken, refreshToken, auth.getUser().isProfileCompleted());
    }
    
    /**
     * 사용자 정보를 Redis에 캐싱 (온라인 상태 관리)
     */
//    private void cacheUserInfo(Auth auth) {
//        user user = auth.getuser();
//        userCacheService.cache(user);
//    }



    /**
     * Access Token 갱신 (기존 방식 - 하위 호환성)
     * @param refreshToken 클라이언트가 보낸 Refresh Token
     * @return 새로운 Access Token
     */
    @Transactional
    public String refresh(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new CustomException(ErrorCode.AUTH_INVALID_REFRESH_TOKEN);
        }

        // 1. 토큰으로 Auth 찾기
        Auth auth = authRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new CustomException(ErrorCode.AUTH_INVALID_REFRESH_TOKEN));

        // 2. 만료 확인
        if (!auth.isRefreshTokenValid(refreshToken)) {
            auth.invalidateRefreshToken();
            throw new CustomException(ErrorCode.AUTH_EXPIRED_REFRESH_TOKEN);
        }

        // 3. 새 AccessToken 발급
        Long userId = auth.getUser().getId();
        return jwtTokenProvider.createAccessToken(userId);
    }

    /**
     * 로그아웃 처리
     * @param refreshToken 클라이언트의 Refresh Token
     */
    /**
     * Refresh Token을 사용한 로그아웃 처리
     * @param refreshToken 클라이언트의 Refresh Token
     */
    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            log.warn("Logout attempt with empty refresh token");
            return;
        }

        try {
            // 1. Refresh Token으로 직접 Auth 조회 (더 안전한 방법)
            authRepository.findByRefreshToken(refreshToken).ifPresent(auth -> {
                // 2. Refresh Token 무효화
                auth.invalidateRefreshToken();
                authRepository.save(auth);
            });
        } catch (Exception e) {
            // 토큰 파싱 실패 시 로깅만 하고 종료 (이미 무효화된 토큰)
            log.warn("Error during logout: {}", e.getMessage());
        }
    }
}
