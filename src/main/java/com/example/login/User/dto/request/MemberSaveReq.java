package com.example.login.User.dto.request;

import com.example.login.User.domain.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberSaveReq {

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "이메일 형식이 아닙니다.")
    private String memberEmail;

    @NotBlank(message = "이름은 필수입니다.")
    private String memberName;

    @NotBlank(message = "비밀번호는 필수입니다.")
    private String memberPassword;
}
