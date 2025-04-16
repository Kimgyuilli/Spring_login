package com.example.login.User.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberUpdateReq {
    private Long id;
    private String memberEmail;
    private String memberName;
    private String memberPassword;
}
