package com.haieros.plugin.entity;

import java.util.TreeSet;

public class GroupInfo {
    private TreeSet<UserInformation> userList;
    private int groupMemberCount;

    public GroupInfo(TreeSet<UserInformation> userList, int groupMemberCount){
        this.userList = userList;
        this.groupMemberCount = groupMemberCount;
    }
    public int getGroupMemberCount(){
        return groupMemberCount;
    }
    public TreeSet<UserInformation> getUserList(){
        return userList;
    }
}
