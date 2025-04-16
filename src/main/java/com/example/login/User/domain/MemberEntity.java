package com.example.login.User.domain;

import com.example.login.Common.entity.BaseTimeEntity;
import com.example.login.User.dto.MemberDTO;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
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

    public static MemberEntity toMemberEntity(String email, String name, String password) {
        return MemberEntity.builder()
                .memberEmail(email)
                .memberName(name)
                .memberPassword(password)
                .build();
    }

    public static MemberEntity toUpdateMemberEntity(MemberDTO memberDTO) {
        return MemberEntity.builder()
                .id(memberDTO.getId())
                .memberEmail(memberDTO.getMemberEmail())
                .memberName(memberDTO.getMemberName())
                .memberPassword(memberDTO.getMemberPassword())
                .build();
    }
}
