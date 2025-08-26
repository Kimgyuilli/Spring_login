package com.example.login.global.oauth2.service;

import com.example.login.global.oauth2.user.CustomOAuth2User;
import com.example.login.global.oauth2.dto.OAuthAttributes;
import com.example.login.global.oauth2.entity.SocialType;
import com.example.login.global.oauth2.strategy.SocialLoginStrategyManager;
import com.example.login.domain.member.entity.MemberEntity;
import com.example.login.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final MemberRepository userRepository;
    private final SocialLoginStrategyManager strategyManager;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        log.info("CustomOAuth2UserService.loadUser() 실행 - OAuth2 로그인 요청 진입");

        OAuth2User oAuth2User = new DefaultOAuth2UserService().loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // Strategy 패턴을 사용하여 소셜별 속성 추출
        OAuthAttributes extractAttributes = strategyManager.extractAttributes(
                registrationId, userNameAttributeName, attributes);

        SocialType socialType = strategyManager.getSocialType(registrationId);
        MemberEntity user = findOrCreateUser(extractAttributes, socialType);

        return new CustomOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(user.getRole().getKey())),
                attributes,
                extractAttributes.getNameAttributeKey(),
                String.valueOf(user.getId()),
                user.getMemberEmail(),
                user.getRole(),
                user.getSocialType()
        );
    }


    private MemberEntity findOrCreateUser(OAuthAttributes attributes, SocialType socialType) {
        String socialId = attributes.getOauth2UserInfo().getId();
        String email = attributes.getOauth2UserInfo().getEmail();
        
        // 1. 먼저 같은 소셜 타입과 소셜 ID로 찾기
        return userRepository.findBySocialTypeAndSocialId(socialType, socialId)
                .orElseGet(() -> {
                    // 2. 소셜 ID로 찾지 못한 경우, 이메일로 기존 사용자 확인
                    return userRepository.findByMemberEmail(email)
                            .map(existingUser -> linkSocialAccount(existingUser, socialType, socialId))
                            .orElseGet(() -> saveUser(attributes, socialType));
                });
    }
    
    /**
     * 기존 사용자에게 소셜 계정 연결
     * 이미 일반 회원가입으로 가입된 사용자가 같은 이메일로 소셜 로그인하는 경우
     */
    private MemberEntity linkSocialAccount(MemberEntity existingUser, SocialType socialType, String socialId) {
        log.info("기존 사용자에게 소셜 계정 연결: email={}, socialType={}", existingUser.getMemberEmail(), socialType);
        
        // 기존 사용자가 이미 다른 소셜 계정이 연결된 경우 경고 로그
        if (existingUser.getSocialType() != null && !existingUser.getSocialType().equals(socialType)) {
            log.warn("사용자가 이미 다른 소셜 계정을 연결하고 있음: existing={}, new={}", 
                    existingUser.getSocialType(), socialType);
        }
        
        // 소셜 정보 업데이트
        existingUser.updateSocialInfo(socialType, socialId);
        return userRepository.save(existingUser);
    }

    private MemberEntity saveUser(OAuthAttributes attributes, SocialType socialType) {
        MemberEntity user = attributes.toEntity(socialType);
        return userRepository.save(user);
    }
}