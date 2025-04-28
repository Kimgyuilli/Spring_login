package com.example.login.domain.member.security;

import com.example.login.domain.member.entity.MemberEntity;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Getter
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final MemberEntity member;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Role.USER or Role.ADMIN → ROLE_USER / ROLE_ADMIN 형식으로 반환
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + member.getRole().name()));
    }

    @Override
    public String getPassword() {
        return member.getMemberPassword();
    }

    @Override
    public String getUsername() {
        return member.getMemberEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // 만료 안됨
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // 잠금 안됨
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // 인증 만료 안됨
    }

    @Override
    public boolean isEnabled() {
        return true; // 활성화 상태
    }
}
