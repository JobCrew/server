package com.org.example.jobcrew.global.security.jwt;

import com.org.example.jobcrew.global.security.CustomUserDetails;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;

    /** JWT 형식인지 간단히 확인 (header.payload.signature) */
    private boolean looksLikeJwt(String token) {
        return token != null && token.chars().filter(ch -> ch == '.').count() == 2;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain) throws ServletException, IOException {

        // WebSocket 관련 경로는 JWT 필터 스킵
        String requestURI = req.getRequestURI();
        log.info(">>> JwtAuthFilter requestURI = {}", requestURI);

        if (requestURI.startsWith("/signup") || requestURI.startsWith("/api/auth") || requestURI.startsWith("/ws-chatroom/")) {
            chain.doFilter(req, res);
            return;
        }

        String token = null;

        // 1. Authorization 헤더에서 토큰 확인
        String bearer = req.getHeader("Authorization");
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            token = bearer.substring(7).trim();
        }

        // 2. 쿼리 파라미터에서 토큰 확인 (SSE 연결용)
        if (token == null) {
            String queryToken = req.getParameter("token");
            if (StringUtils.hasText(queryToken)) {
                token = queryToken.trim();
            }
        }

        if (!StringUtils.hasText(token)) {
            chain.doFilter(req, res);      // 토큰 없으면 다음 필터로
            return;
        }

        // 1️⃣ 형식 안 맞으면 바로 패스
        if (!looksLikeJwt(token)) {
            log.debug("Skip non-JWT token: {}", token);
            chain.doFilter(req, res);
            return;
        }

        // 2️⃣ 이미 인증된 상태라면 중복 세팅 방지
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            chain.doFilter(req, res);
            return;
        }

        try {
            // Access Token에서 Auth ID 추출
            Long authId = jwtTokenProvider.getAuthIdFromAccessToken(token);

            log.debug("JWT 인증 처리: authId={}", authId);

            // Auth ID로 사용자 조회 (기존 로직 활용)
            UserDetails user = userDetailsService.loadUserByUsername(authId.toString());
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

            /* ④ Presence / TTL 관리 ---------------------------- */
            Long id = ((CustomUserDetails) user).getId();
            /* -------------------------------------------------- */

            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        catch (ExpiredJwtException e) {
            log.debug("JWT expired: {}", e.getMessage());
        }
        catch (JwtException | IllegalArgumentException e) {
            log.debug("Invalid JWT: {}", e.getMessage());
        }

        chain.doFilter(req, res);
    }
}