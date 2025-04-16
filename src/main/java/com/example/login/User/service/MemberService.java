package com.example.login.User.service;


import com.example.login.User.domain.MemberEntity;
import com.example.login.User.dto.MemberDTO;
import com.example.login.User.dto.request.MemberLoginReq;
import com.example.login.User.dto.request.MemberSaveReq;
import com.example.login.User.dto.response.MemberLoginRes;
import com.example.login.User.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;

    public void save(MemberSaveReq req) {
        MemberEntity memberEntity = MemberEntity.toMemberEntity(req.getMemberEmail(), req.getMemberName(), req.getMemberPassword());
        memberRepository.save(memberEntity);

    }

    public MemberLoginRes login(MemberLoginReq request) {
        Optional<MemberEntity> memberOpt = memberRepository.findByMemberEmail(request.getMemberEmail());

        if (memberOpt.isPresent()) {
            MemberEntity entity = memberOpt.get();
            if (entity.getMemberPassword().equals(request.getMemberPassword())) {
                return new MemberLoginRes(
                        entity.getId(),
                        entity.getMemberEmail(),
                        entity.getMemberName()
                );
            }
        }

        return null; // or throw exception
    }

    public List<MemberDTO> findAll() {
        List<MemberEntity> memberEntityList = memberRepository.findAll();
        List<MemberDTO> memberDTOList = new ArrayList<>();
        for (MemberEntity memberEntity : memberEntityList) {
            memberDTOList.add(MemberDTO.toMemberDTO(memberEntity));
        }
        return memberDTOList;
    }

    public MemberDTO findById(Long id) {
        Optional<MemberEntity> optionalMemberEntity = memberRepository.findById(id);
        return optionalMemberEntity.map(MemberDTO::toMemberDTO).orElse(null);
    }

    public MemberDTO updateForm(String myEmail) {
        Optional<MemberEntity> optionalMemberEntity = memberRepository.findByMemberEmail(myEmail);
        return optionalMemberEntity.map(MemberDTO::toMemberDTO).orElse(null);
    }

    public void update(MemberDTO memberDTO) {
        memberRepository.save(MemberEntity.toUpdateMemberEntity(memberDTO));
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
