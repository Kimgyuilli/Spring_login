package com.example.login.domain.member.service;


import com.example.login.global.config.properties.SecurityProperties;
import com.example.login.global.exception.BaseException;
import com.example.login.global.response.ErrorType.ErrorCode;
import com.example.login.domain.member.entity.MemberEntity;
import com.example.login.domain.member.entity.Role;
import com.example.login.domain.member.dto.request.MemberSaveReq;
import com.example.login.domain.member.dto.request.MemberUpdateReq;
import com.example.login.domain.member.dto.response.MemberRes;
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


    public void save(MemberSaveReq req) {
        // 중복 이메일 체크
        boolean exists = memberRepository.findByMemberEmail(req.getMemberEmail()).isPresent();
        if (exists) {
            throw new BaseException(ErrorCode.DUPLICATE_EMAIL);
        }

        // 비밀번호 암호화
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


    public List<MemberRes> findAll() {
        return memberRepository.findAll()
                .stream()
                .map(MemberRes::toMemberDTO)
                .toList();
    }

    public MemberRes findById(Long id) {
        return memberRepository.findById(id)
                .map(MemberRes::toMemberDTO)
                .orElseThrow(() -> memberNotFound());
    }

    public MemberRes updateForm(String myEmail) {
        return memberRepository.findByMemberEmail(myEmail)
                .map(MemberRes::toMemberDTO)
                .orElseThrow(() -> memberNotFound());
    }

    public void update(MemberUpdateReq req) {
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

    public boolean emailCheck(String memberEmail) {
        return !memberRepository.existsByMemberEmail(memberEmail);
    }
    
    private BaseException memberNotFound() {
        return new BaseException(ErrorCode.MEMBER_NOT_FOUND);
    }
}
