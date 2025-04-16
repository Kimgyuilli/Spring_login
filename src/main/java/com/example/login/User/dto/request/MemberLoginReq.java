package com.example.login.User.dto.request;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberLoginReq {
    private String memberEmail;
    private String memberPassword;

}