package com.example.login.User.domain;

public enum Role {
    USER, ADMIN, GUEST;

    public String getKey() {
        return "ROLE_" + this.name();
    }
}
