package com.example.login.User.domain;

import com.example.login.Common.entity.BaseTimeEntity;
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

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;


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
}
