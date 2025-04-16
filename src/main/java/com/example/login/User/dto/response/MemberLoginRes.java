package com.example.login.User.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MemberLoginRes {
    private Long id;
    private String memberEmail;
    private String memberName;
}
