package com.example.tutorial.plugins.entity;

import java.io.Serializable;

public class LoginInformation implements Comparable<LoginInformation>, Serializable {
    private static final long serialVersionUID = 1L;
    private String username;
    private Long lastLoginTime;

    public LoginInformation(String username, Long lastLoginTime){
        this.username = username;
        this.lastLoginTime = lastLoginTime;
    }
    public String getUserName(){
        return this.username;
    }
    public Long getLastLoginTime(){
        return this.lastLoginTime;
    }

    @Override
    public int compareTo(LoginInformation loginInformation) {
        return this.lastLoginTime.compareTo(loginInformation.getLastLoginTime()) == 0 ? 1:this.lastLoginTime.compareTo(loginInformation.getLastLoginTime());
    }
}
