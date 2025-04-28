package com.example.login.global.oauth2.userInfo;

import java.util.Map;

public class KakaoOAuth2UserInfo extends OAuth2UserInfo {

    public KakaoOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getId() {
        return String.valueOf(attributes.get("id"));
    }

    @Override
    public String getNickname() {
        Map<String, Object> profile = getProfile();
        return profile != null ? (String) profile.get("nickname") : null;
    }

    @Override
    public String getImageUrl() {
        Map<String, Object> profile = getProfile();
        return profile != null ? (String) profile.get("thumbnail_image_url") : null;
    }

    private Map<String, Object> getProfile() {
        Map<String, Object> account = (Map<String, Object>) attributes.get("kakao_account");
        if (account == null) {
            return null;
        }
        return (Map<String, Object>) account.get("profile");
    }
}
