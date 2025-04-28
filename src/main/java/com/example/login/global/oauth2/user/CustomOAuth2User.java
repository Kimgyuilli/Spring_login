package com.example.login.global.oauth2.user;

import com.example.login.domain.member.entity.Role;
import com.example.login.global.oauth2.entity.SocialType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

import java.util.Collection;
import java.util.Map;

/**
 * DefaultOAuth2User를 상속하고, email과 role 필드를 추가로 가진다.
 */
public class CustomOAuth2User extends DefaultOAuth2User {

    private final String email;
    private final Role role;
    private final SocialType socialType;

    public CustomOAuth2User(Collection<? extends GrantedAuthority> authorities,
                            Map<String, Object> attributes,
                            String nameAttributeKey,
                            String email, Role role, SocialType socialType) {
        super(authorities, attributes, nameAttributeKey);
        this.email = email;
        this.role = role;
        this.socialType = socialType;
    }

    @Override
    public String getName() {
        return super.getName(); // nameAttributeKey 기반으로 반환
    }

    public String getEmail() {
        return email;
    }

    public Role getRole() {
        return role;
    }

    public SocialType getSocialType() {
        return socialType;
    }
}
