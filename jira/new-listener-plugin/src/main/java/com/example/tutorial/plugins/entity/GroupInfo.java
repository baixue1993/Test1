package com.example.tutorial.plugins.entity;

import java.io.Serializable;
import java.util.TreeSet;

public class GroupInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    private TreeSet<LoginInformation> userList;
    private int groupMemberCount;
    public GroupInfo(TreeSet<LoginInformation> userList, int groupMemberCount){
        this.userList = userList;
        this.groupMemberCount = groupMemberCount;
    }
    public int getGroupMemberCount(){
        return groupMemberCount;
    }
    public TreeSet<LoginInformation> getUserList(){
        return userList;
    }
}
