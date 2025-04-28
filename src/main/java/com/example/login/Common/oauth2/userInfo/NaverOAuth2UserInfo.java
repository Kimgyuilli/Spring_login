package com.example.login.Common.oauth2.userInfo;

import com.example.login.Common.oauth2.OAuth2UserInfo;

import java.util.Map;

public class NaverOAuth2UserInfo extends OAuth2UserInfo {

    public NaverOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    private Map<String, Object> getResponse() {
        return (Map<String, Object>) attributes.get("response");
    }

    @Override
    public String getId() {
        Map<String, Object> response = getResponse();
        return response != null ? (String) response.get("id") : null;
    }

    @Override
    public String getNickname() {
        Map<String, Object> response = getResponse();
        return response != null ? (String) response.get("nickname") : null;
    }

    @Override
    public String getImageUrl() {
        Map<String, Object> response = getResponse();
        return response != null ? (String) response.get("profile_image") : null;
    }
}