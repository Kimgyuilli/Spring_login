package com.example.login.global.oauth2.dto;

import com.example.login.global.oauth2.userInfo.OAuth2UserInfo;
import com.example.login.global.oauth2.entity.SocialType;
import com.example.login.global.oauth2.userInfo.GoogleOAuth2UserInfo;
import com.example.login.global.oauth2.userInfo.KakaoOAuth2UserInfo;
import com.example.login.global.oauth2.userInfo.NaverOAuth2UserInfo;
import com.example.login.domain.member.entity.MemberEntity;
import com.example.login.domain.member.entity.Role;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;
import java.util.UUID;

/**
 * 각 소셜에서 받아오는 데이터가 다르므로
 * 소셜별로 데이터를 받는 데이터를 분기 처리하는 DTO 클래스
 */

@Getter
public class OAuthAttributes {

    private String nameAttributeKey; // OAuth2 로그인 진행 시 키가 되는 필드 값, PK와 같은 의미
    private OAuth2UserInfo oauth2UserInfo; // 소셜 타입별 로그인 유저 정보(닉네임, 이메일, 프로필 사진 등등)

    @Builder
    private OAuthAttributes(String nameAttributeKey, OAuth2UserInfo oauth2UserInfo) {
        this.nameAttributeKey = nameAttributeKey;
        this.oauth2UserInfo = oauth2UserInfo;
    }

    /**
     * SocialType에 맞는 메소드 호출하여 OAuthAttributes 객체 반환
     * 파라미터 : userNameAttributeName -> OAuth2 로그인 시 키(PK)가 되는 값 / attributes : OAuth 서비스의 유저 정보들
     * 소셜별 of 메소드(ofGoogle, ofKaKao, ofNaver)들은 각각 소셜 로그인 API에서 제공하는
     * 회원의 식별값(id), attributes, nameAttributeKey를 저장 후 build
     */
    public static OAuthAttributes of(SocialType socialType,
                                     String userNameAttributeName, Map<String, Object> attributes) {

        if (socialType == SocialType.NAVER) {
            return ofNaver(userNameAttributeName, attributes);
        }
        if (socialType == SocialType.KAKAO) {
            return ofKakao(userNameAttributeName, attributes);
        }
        return ofGoogle(userNameAttributeName, attributes);
    }

    private static OAuthAttributes ofKakao(String userNameAttributeName, Map<String, Object> attributes) {
        return OAuthAttributes.builder()
                .nameAttributeKey(userNameAttributeName)
                .oauth2UserInfo(new KakaoOAuth2UserInfo(attributes))
                .build();
    }

    public static OAuthAttributes ofGoogle(String userNameAttributeName, Map<String, Object> attributes) {
        return OAuthAttributes.builder()
                .nameAttributeKey(userNameAttributeName)
                .oauth2UserInfo(new GoogleOAuth2UserInfo(attributes))
                .build();
    }

    public static OAuthAttributes ofNaver(String userNameAttributeName, Map<String, Object> attributes) {
        return OAuthAttributes.builder()
                .nameAttributeKey(userNameAttributeName)
                .oauth2UserInfo(new NaverOAuth2UserInfo(attributes))
                .build();
    }

    /**
     * OAuth2UserInfo에서 실제 사용자 정보를 가져와서 MemberEntity 생성
     * - 실제 이메일 사용 (OAuth2 제공 이메일)
     * - 소셜 타입과 소셜 ID 저장으로 중복 가입 방지
     * - 기본 USER 권한 부여
     */
    public MemberEntity toEntity(SocialType socialType) {
        return MemberEntity.builder()
                .memberEmail(oauth2UserInfo.getEmail())
                .memberName(oauth2UserInfo.getNickname())
                .memberPassword(null) // 소셜 로그인은 패스워드 없음
                .role(Role.USER) // 기본 사용자 권한
                .socialType(socialType) // 소셜 타입 저장
                .socialId(oauth2UserInfo.getId()) // 소셜 ID 저장
                .build();
    }
}