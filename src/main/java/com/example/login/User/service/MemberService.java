package com.example.login.User.service;


import com.example.login.Common.exception.BaseException;
import com.example.login.Common.response.ErrorType.ErrorCode;
import com.example.login.User.domain.MemberEntity;
import com.example.login.User.domain.Role;
import com.example.login.User.dto.request.MemberSaveReq;
import com.example.login.User.dto.request.MemberUpdateReq;
import com.example.login.User.dto.response.MemberRes;
import com.example.login.User.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

        System.out.println("[회원가입] 이메일: " + req.getMemberEmail());
        System.out.println("[회원가입] 인코딩된 비밀번호: " + encodedPassword);
        System.out.println("[회원가입] 이름: " + req.getMemberName());
        System.out.println("[회원가입] 역할: " + Role.ADMIN);


        MemberEntity memberEntity = MemberEntity.toMemberEntity(
                req.getMemberEmail(),
                req.getMemberName(),
                encodedPassword, // 암호화된 비밀번호로 저장
                Role.ADMIN // 개발 중이니까 일단 ADMIN
        );

        memberRepository.save(memberEntity);
    }


    public List<MemberRes> findAll() {
        List<MemberEntity> memberEntityList = memberRepository.findAll();
        List<MemberRes> memberDTOList = new ArrayList<>();
        for (MemberEntity memberEntity : memberEntityList) {
            memberDTOList.add(MemberRes.toMemberDTO(memberEntity));
        }
        return memberDTOList;
    }

    public MemberRes findById(Long id) {
        Optional<MemberEntity> optionalMemberEntity = memberRepository.findById(id);
        return optionalMemberEntity.map(MemberRes::toMemberDTO).orElse(null);
    }

    public MemberRes updateForm(String myEmail) {
        Optional<MemberEntity> optionalMemberEntity = memberRepository.findByMemberEmail(myEmail);
        return optionalMemberEntity.map(MemberRes::toMemberDTO).orElse(null);
    }

    public void update(MemberUpdateReq req) {
        String encodedPassword = passwordEncoder.encode(req.getMemberPassword());
        MemberEntity updated = MemberEntity.toUpdateMemberEntity(
                req.getId(), req.getMemberEmail(), req.getMemberName(), encodedPassword, Role.ADMIN
        );
        memberRepository.save(updated);
    }

    public void deleteById(Long id) {
        memberRepository.deleteById(id);
    }

    public String emailCheck(String memberEmail) {
        Optional<MemberEntity> optionalMemberEntity = memberRepository.findByMemberEmail(memberEmail);
        if (optionalMemberEntity.isPresent()) {
            // 조회결과 존재 -> 사용 불가
            return null;
        } else {
            // 사용 가능
            return "ok";
        }
    }
}
