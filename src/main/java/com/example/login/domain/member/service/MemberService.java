package com.example.login.domain.member.service;


import com.example.login.global.config.properties.SecurityProperties;
import com.example.login.global.exception.BaseException;
import com.example.login.global.response.ErrorCode;
import com.example.login.domain.member.entity.MemberEntity;
import com.example.login.domain.member.entity.Role;
import com.example.login.domain.member.dto.request.MemberSaveRequest;
import com.example.login.domain.member.dto.request.MemberUpdateRequest;
import com.example.login.domain.member.dto.response.MemberResponse;
import com.example.login.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityProperties securityProperties;


    public void save(MemberSaveRequest req) {
        boolean exists = memberRepository.findByMemberEmail(req.getMemberEmail()).isPresent();
        if (exists) {
            throw new BaseException(ErrorCode.DUPLICATE_EMAIL);
        }

        String encodedPassword = passwordEncoder.encode(req.getMemberPassword());

        log.info("[회원가입] 이메일: {}, 이름: {}", req.getMemberEmail(), req.getMemberName());


        Role defaultRole = Role.valueOf(securityProperties.getDefaultRole());
        
        MemberEntity memberEntity = MemberEntity.createRegularMember(
                req.getMemberEmail(),
                req.getMemberName(),
                encodedPassword,
                defaultRole
        );

        memberRepository.save(memberEntity);
    }


    public List<MemberResponse> findAll() {
        return memberRepository.findAll()
                .stream()
                .map(MemberResponse::fromEntity)
                .toList();
    }

    public MemberResponse findById(Long id) {
        return memberRepository.findById(id)
                .map(MemberResponse::fromEntity)
                .orElseThrow(() -> memberNotFound());
    }

    public MemberResponse findMemberForUpdate(String myEmail) {
        return memberRepository.findByMemberEmail(myEmail)
                .map(MemberResponse::fromEntity)
                .orElseThrow(() -> memberNotFound());
    }

    public void update(MemberUpdateRequest req) {
        MemberEntity entity = memberRepository.findById(req.getId())
                .orElseThrow(() -> memberNotFound());

        String encodedPassword = passwordEncoder.encode(req.getMemberPassword());
        
        entity.updateMemberInfo(req.getMemberName(), encodedPassword);
        memberRepository.save(entity);
    }

    public void deleteById(Long id) {
        if (!memberRepository.existsById(id)) {
            throw memberNotFound();
        }
        memberRepository.deleteById(id);
    }

    public boolean isEmailAvailable(String memberEmail) {
        return !memberRepository.existsByMemberEmail(memberEmail);
    }
    
    private BaseException memberNotFound() {
        return new BaseException(ErrorCode.MEMBER_NOT_FOUND);
    }
}
