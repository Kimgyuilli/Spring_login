package com.example.login.User.dto.request;

import lombok.Getter;

@Getter
public class MemberSaveReq {
    private String memberEmail;
    private String memberName;
    private String memberPassword;
}