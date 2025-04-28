package com.example.login.domain.member.repository;

import com.example.login.global.oauth2.entity.SocialType;
import com.example.login.domain.member.entity.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<MemberEntity, Long> {
    //    이메일로 회원 정보 조회(select * from member_table)
    Optional<MemberEntity> findByMemberEmail(String memberEmail);


    Boolean existsByMemberEmail(String memberEmail);

    MemberEntity findByMemberEmailIgnoreCase(String memberEmail);

    Optional<MemberEntity> findBySocialTypeAndSocialId(SocialType socialType, String socialId);
}
