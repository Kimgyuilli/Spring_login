package com.example.login.domain.member.entity;

public enum Role {
    USER, ADMIN;

    public String getKey() {
        return "ROLE_" + this.name();
    }
}
