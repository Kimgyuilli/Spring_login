package com.example.login.domain.member.entity;

public enum Role {
    USER, ADMIN, GUEST;

    public String getKey() {
        return "ROLE_" + this.name();
    }
}
