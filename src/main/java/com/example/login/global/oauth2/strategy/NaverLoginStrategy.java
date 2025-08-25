package com.example.login.global.oauth2.strategy;

import com.example.login.global.oauth2.dto.OAuthAttributes;
import com.example.login.global.oauth2.entity.SocialType;
import com.example.login.global.oauth2.userInfo.NaverOAuth2UserInfo;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class NaverLoginStrategy implements SocialLoginStrategy {

    @Override
    public SocialType getSocialType() {
        return SocialType.NAVER;
    }

    @Override
    public OAuthAttributes extractAttributes(String userNameAttributeName, Map<String, Object> attributes) {
        return OAuthAttributes.builder()
                .nameAttributeKey(userNameAttributeName)
                .oauth2UserInfo(new NaverOAuth2UserInfo(attributes))
                .build();
    }

    @Override
    public boolean supports(String registrationId) {
        return "naver".equals(registrationId);
    }
}