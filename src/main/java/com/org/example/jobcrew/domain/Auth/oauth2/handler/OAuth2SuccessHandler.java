package com.org.example.jobcrew.domain.Auth.oauth2.handler;

import com.org.example.jobcrew.domain.Auth.dto.TokenBundle;
import com.org.example.jobcrew.domain.Auth.entity.Provider;
import com.org.example.jobcrew.domain.Auth.oauth2.processor.OAuth2LoginProcessor;
import com.org.example.jobcrew.global.exception.CustomException;
import com.org.example.jobcrew.global.exception.ErrorCode;
import com.org.example.jobcrew.global.util.CookieUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 소셜 로그인 성공 시, 공급자별 {@link OAuth2LoginProcessor} 로 후처리하고
 * <p>Access·Refresh 토큰을 내려준 뒤 <b>프런트엔드로 리다이렉트</b>한다.</p>
 *
 * <p><b>리다이렉트 주소</b>는 <code>application.yml</code> 의
 * <code>front.url</code> 프로퍼티(@Value 주입)로 외부 설정 가능.</p>
 */
@Slf4j
@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    /** 공급자 → 후처리기 Map (생성자에서 List → Map 변환) */
    private final Map<Provider, OAuth2LoginProcessor> processorMap;

    /** front.url → 없으면 FRONT_URL env → 없으면 기본 */
    @Value("${front.url}")
    private String frontUrl;
    
    /** 개발 환경 여부 */
    @Value("${spring.profiles.active:dev}")
    private String activeProfile;


    public OAuth2SuccessHandler(List<OAuth2LoginProcessor> list) {
        this.processorMap = list.stream()
                .collect(Collectors.toMap(OAuth2LoginProcessor::provider, p -> p));
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest req,
                                        HttpServletResponse res,
                                        Authentication auth) throws IOException {

        /* 1) registrationId → Provider enum */
        String id = ((OAuth2AuthenticationToken) auth).getAuthorizedClientRegistrationId();
        Provider provider = Provider.valueOf(id.toUpperCase());

        /* 2) 프로세서 실행 */
        OAuth2LoginProcessor proc = processorMap.get(provider);
        if (proc == null) {
            throw new CustomException(ErrorCode.UNSUPPORTED_PROVIDER,
                    "지원하지 않는 OAuth2 공급자입니다: " + provider);
        }

        // 2) 토큰 발급
        TokenBundle t = proc.process((OAuth2User) auth.getPrincipal(), req);

        // 3-1) Refresh Token → HttpOnly Cookie에만 저장
        boolean isSecure = !"dev".equals(activeProfile);
        res.addCookie(CookieUtils.refresh(t.refresh(), isSecure));

        // 3-2) 리다이렉트 대상 URL (쿼리스트링은 최소화)
        String redirectUrl = frontUrl + "/test-login.html";

        log.info("🔄 OAuth2 리다이렉트: {}", redirectUrl);

        // 프론트에서 Access Token은 /refresh API or localStorage 처리
        getRedirectStrategy().sendRedirect(req, res, redirectUrl);
    }
}