package com.haieros.plugin.entity;

public class UserInformation implements Comparable<UserInformation> {
    private String userName;
    private Long  lastLoginTime;

    public UserInformation(String userName, Long lastLoginTime){
        this.userName = userName;
        this.lastLoginTime = lastLoginTime;
    }

    public String getUserName(){
        return userName;
    }
    private void setUserName(String userName){
        this.userName = userName;
    }

    public Long getLastLoginTime(){
        return lastLoginTime;
    }

    private void setLastLoginTime(Long lastLoginTime){
        this.lastLoginTime = lastLoginTime;
    }

    @Override
    public int compareTo(UserInformation userInformation) {
        return this.lastLoginTime.compareTo(userInformation.lastLoginTime) == 0 ? 1: this.lastLoginTime.compareTo(userInformation.lastLoginTime);
    }
}
