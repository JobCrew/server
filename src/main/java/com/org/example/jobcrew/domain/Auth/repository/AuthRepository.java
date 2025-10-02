package com.org.example.jobcrew.domain.Auth.repository;

import com.org.example.jobcrew.domain.Auth.entity.Auth;
import com.org.example.jobcrew.domain.Auth.entity.Provider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * 인증 정보를 관리하는 리포지토리
 * - 이메일/비밀번호 인증 정보
 * - 소셜 로그인 정보
 * - 리프레시 토큰
 */
public interface AuthRepository extends JpaRepository<Auth, Long> {
    
    // 이메일로 인증 정보 조회
    Optional<Auth> findByEmail(String email);
    
    // 이메일과 공급자로 인증 정보 조회
    Optional<Auth> findByEmailAndProvider(String email, Provider provider);
    
    // 공급자와 공급자 사용자 ID로 인증 정보 조회
    Optional<Auth> findByProviderAndProviderUserId(Provider provider, String providerUserId);
    
    // 회원 ID로 인증 정보 조회
    @Query("SELECT a FROM Auth a WHERE a.user.id = :userId")
    Optional<Auth> findByUserId(@Param("userId") Long userId);
    
    // 리프레시 토큰으로 인증 정보 조회
    Optional<Auth> findByRefreshToken(String refreshToken);
    
    /**
     * Auth와 user 정보를 함께 조회 (LazyInitializationException 방지)
     */
    @Query("SELECT a FROM Auth a JOIN FETCH a.user WHERE a.id = :id")
    Optional<Auth> findByIdWithUser(@Param("id") Long id);

}