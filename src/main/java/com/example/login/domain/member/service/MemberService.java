package com.example.login.domain.member.service;


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


    public void save(MemberSaveReq req) {
        // 중복 이메일 체크
        boolean exists = memberRepository.findByMemberEmail(req.getMemberEmail()).isPresent();
        if (exists) {
            throw new BaseException(ErrorCode.DUPLICATE_EMAIL);
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(req.getMemberPassword());

        log.info("[회원가입] 이메일: {}, 이름: {}", req.getMemberEmail(), req.getMemberName());


        MemberEntity memberEntity = MemberEntity.toMemberEntity(
                req.getMemberEmail(),
                req.getMemberName(),
                encodedPassword, // 암호화된 비밀번호로 저장
                Role.ADMIN // 개발 중이니까 일단 ADMIN
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
                .orElseThrow(() -> new BaseException(ErrorCode.MEMBER_NOT_FOUND));
    }

    public MemberRes updateForm(String myEmail) {
        return memberRepository.findByMemberEmail(myEmail)
                .map(MemberRes::toMemberDTO)
                .orElseThrow(() -> new BaseException(ErrorCode.MEMBER_NOT_FOUND));
    }

    public void update(MemberUpdateReq req) {
        MemberEntity entity = memberRepository.findById(req.getId())
                .orElseThrow(() -> new BaseException(ErrorCode.MEMBER_NOT_FOUND));

        String encodedPassword = passwordEncoder.encode(req.getMemberPassword());

        MemberEntity updated = MemberEntity.toUpdateMemberEntity(
                req.getId(),
                req.getMemberEmail(),
                req.getMemberName(),
                encodedPassword,
                entity.getRole() // 기존 역할 유지
        );
        memberRepository.save(updated);
    }

    public void deleteById(Long id) {
        if (!memberRepository.existsById(id)) {
            throw new BaseException(ErrorCode.MEMBER_NOT_FOUND);
        }
        memberRepository.deleteById(id);
    }

    // 개선
    public boolean emailCheck(String memberEmail) {
        return !memberRepository.existsByMemberEmail(memberEmail);
    }
}
