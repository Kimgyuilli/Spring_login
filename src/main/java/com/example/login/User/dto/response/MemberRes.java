package com.example.login.User.dto.response;

import com.example.login.User.domain.MemberEntity;
import lombok.*;

@Getter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberRes {
    private Long id;
    private String memberName;
    private String memberEmail;
    private String memberPassword;

    public static MemberRes toMemberDTO(MemberEntity memberEntity) {
        return MemberRes.builder()
                .id(memberEntity.getId())
                .memberName(memberEntity.getMemberName())
                .memberEmail(memberEntity.getMemberEmail())
                .memberPassword(memberEntity.getMemberPassword())
                .build();

    }
}
