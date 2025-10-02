package com.org.example.jobcrew.global.config;

import com.org.example.jobcrew.domain.Auth.oauth2.handler.OAuth2SuccessHandler;
import com.org.example.jobcrew.global.security.jwt.JwtAuthFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    /* ──────────── Dependencies ──────────── */
    private final JwtAuthFilter jwtAuthFilter;
    private final OAuth2SuccessHandler successHandler;
    private final Environment environment;



    /* ──────────── Security Filter Chain ──────────── */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // CSRF 비활성화
                // 세션 없이 동작(JWT)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 인가 규칙
                .authorizeHttpRequests(auth -> auth
                        // 프리플라이트(OPTIONS) 전체 허용
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 헬스체크/기본 공개 엔드포인트
                        .requestMatchers("/", "/favicon.ico", "/robots.txt").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()

                        // Swagger & 정적 리소스
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html",
                                "/swagger-resources/**",
                                "/webjars/**",
                                "/error"
                        ).permitAll()

                        // k6 테스트
                    .requestMatchers(
                        "/api/chat-message/**",
                        "/analyze/**",
                        "/ws-chatroom"


                    ).permitAll()

                        // 인증/회원 관련 공개 API
                        .requestMatchers("/api/auth/**").permitAll()

                        // WebSocket 핸드셰이크 경로 (SockJS info 포함) 공개
                        .requestMatchers(
                                "/ws-chatroom/**",
                                "/ws/**",
                                "/sockjs-node/**"
                        ).permitAll()

                                // 개발/프로덕션 공통 허용 경로 (모든 환경에서 적용)
        .requestMatchers(new String[]{
                "/",
                "/index.html",
                "/notification-test.html",
                "/test-login.html",
                "/test-signup.html",
                "/test-info.html",
                "/js/**",
                "/api/test/**",
                "/test/**",
                "/dev/**",
                "/debug/**",
                "/h2-console/**",
                "/actuator/**",     // 모든 환경에서 actuator 허용
                "/upload-csv"
        }).permitAll()

                        // 공개 GET 조회
                        .requestMatchers(HttpMethod.GET,
                                "/api/users/*",
                                "/api/users/*/status"
                        ).permitAll()
                        // 공개 POST 요청 (회원가입/로그인 등)
                        .requestMatchers(HttpMethod.POST,
                                "/api/auth/**"
                        ).permitAll()
                        .requestMatchers("/api/user/**").permitAll()
                        //server health 체크
                        .requestMatchers("/health").permitAll()

                        // SSE(알림)는 인증 필요
                        .requestMatchers("/api/notify/sse").authenticated()

                        // 이외 전부 인증
                        .anyRequest().authenticated()
                )

                // OAuth2 로그인 (성공 시 커스텀 핸들러)
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(successHandler)
                )

                // 예외 처리 - JSON 응답
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint())
                        .accessDeniedHandler(accessDeniedHandler())
                )

                // JWT 필터 위치 조정
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        // 모든 환경에서 동일한 설정 적용
        http.headers(headers -> headers.frameOptions(frame -> frame.disable()));
        log.info("🔧 All profiles: wide-open CORS + dev routes permitted + H2 frame allowed");

        return http.build();
    }

    /* ──────────── Beans ──────────── */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (req, res, ex) -> jsonError(res, HttpStatus.UNAUTHORIZED, "인증이 필요합니다.");
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (req, res, ex) -> jsonError(res, HttpStatus.FORBIDDEN, "접근 권한이 없습니다.");
    }

    /* ──────────── Util ──────────── */
    private void jsonError(HttpServletResponse res, HttpStatus status, String msg) throws java.io.IOException {
        log.warn("[SECURITY] {}: {}", status, msg);
        res.setStatus(status.value());
        res.setContentType("application/json;charset=UTF-8");
        res.getWriter().write("{\"message\": \"" + msg + "\"}");
    }

}