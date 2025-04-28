package com.example.login.domain.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "회원 정보 수정 요청 DTO")
public class MemberUpdateReq {

    @Schema(description = "회원 ID (경로에서 전달되며 내부 설정용)", example = "1")
    private Long id;

    @Schema(description = "회원 이메일", example = "updateduser@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "이메일 형식이 아닙니다.")
    private String memberEmail;

    @Schema(description = "회원 이름", example = "김수정", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "이름은 필수입니다.")
    private String memberName;

    @Schema(description = "회원 비밀번호", example = "newPassword456!", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "비밀번호는 필수입니다.")
    private String memberPassword;
}
