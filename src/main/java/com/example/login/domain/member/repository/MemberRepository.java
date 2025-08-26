package com.example.login.domain.member.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.login.domain.member.entity.MemberEntity;
import com.example.login.global.oauth2.entity.SocialType;

@Repository
public interface MemberRepository extends JpaRepository<MemberEntity, Long> {

    Optional<MemberEntity> findByMemberEmail(String memberEmail);


    Boolean existsByMemberEmail(String memberEmail);

    Optional<MemberEntity> findBySocialTypeAndSocialId(SocialType socialType, String socialId);
}
