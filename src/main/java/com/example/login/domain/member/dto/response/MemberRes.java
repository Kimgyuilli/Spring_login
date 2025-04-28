package com.example.login.domain.member.dto.response;

import com.example.login.domain.member.entity.MemberEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "회원 정보 응답 DTO")
public class MemberRes {

    @Schema(description = "회원 고유 ID", example = "1")
    private Long id;

    @Schema(description = "회원 이름", example = "홍길동")
    private String memberName;

    @Schema(description = "회원 이메일", example = "hong@example.com")
    private String memberEmail;


    public static MemberRes toMemberDTO(MemberEntity memberEntity) {
        return MemberRes.builder()
                .id(memberEntity.getId())
                .memberName(memberEntity.getMemberName())
                .memberEmail(memberEntity.getMemberEmail())
                .build();
    }
}