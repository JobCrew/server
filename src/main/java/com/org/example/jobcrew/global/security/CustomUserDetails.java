package com.org.example.jobcrew.global.security;

import com.org.example.jobcrew.domain.user.entity.Role;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/** SecurityContext 에 저장·주입되는 사용자 객체 */
@RequiredArgsConstructor
@Getter
@com.fasterxml.jackson.annotation.JsonIgnoreType // Jackson 역직렬화 완전 차단
public class CustomUserDetails implements UserDetails {

    private final Long id;
    private final String email;
    private final String nickname;        // 닉네임, 필수
    private final boolean active;     // 활성화 여부
    private final Role role ;
    private final List<GrantedAuthority> authorities;

    public CustomUserDetails(Long id, String email, String nickname, boolean active, Role role) {
        this.id = id;
        this.email = email;
        this.nickname = nickname;
        this.active = active;
        this.role = role;
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }
    /* ===== UserDetails 인터페이스 ===== */

    @Override public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    /** 비밀번호 기반 로그인 안 쓰므로 빈 문자열 반환 */
    @Override public String getPassword() { return ""; }

    /** Spring 내부 username → nickname 스프링 내부 username 을 nickname 으로 사용. */
    @Override public String getUsername() { return nickname; }

    @Override public boolean isAccountNonExpired()    { return true; }
    @Override public boolean isAccountNonLocked()     { return true; }
    @Override public boolean isCredentialsNonExpired(){ return true; }

    /** 회원 활성 플래그와 연결 */
    @Override public boolean isEnabled() { return active; }
}
