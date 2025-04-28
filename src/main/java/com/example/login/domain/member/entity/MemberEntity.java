package com.example.login.domain.member.entity;

import com.example.login.global.entity.BaseTimeEntity;
import com.example.login.global.oauth2.entity.SocialType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "member_table")
public class MemberEntity extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String memberEmail;

    @Column
    private String memberName;

    @Column
    private String memberPassword;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private SocialType socialType;

    @Column(nullable = true)
    private String socialId;

    public static MemberEntity toMemberEntity(String email, String name, String password, Role role) {
        return MemberEntity.builder()
                .memberEmail(email)
                .memberName(name)
                .memberPassword(password)
                .role(role)
                .build();
    }

    public static MemberEntity toUpdateMemberEntity(Long id, String email, String name, String password, Role role) {
        return MemberEntity.builder()
                .id(id)
                .memberEmail(email)
                .memberName(name)
                .memberPassword(password)
                .role(role)
                .build();
    }

    public static MemberEntity socialLogin(String email, String name, String password, Role role, SocialType socialType, String socialId) {
        return MemberEntity.builder()
                .memberEmail(email)
                .memberName(name)
                .memberPassword(password)
                .role(role)
                .socialType(socialType)
                .socialId(socialId)
                .build();
    }


}
