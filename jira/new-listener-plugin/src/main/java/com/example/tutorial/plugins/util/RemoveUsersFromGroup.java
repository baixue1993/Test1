package com.example.tutorial.plugins.util;

import com.example.tutorial.plugins.entity.LoginInformation;

import java.util.Iterator;
import java.util.TreeSet;

public class RemoveUsersFromGroup {
    public boolean removeUsersFromGroup(TreeSet<LoginInformation> userList1, TreeSet<LoginInformation> userList2){
        boolean modifyed = false;
        if(userList1 != null && userList2 != null){
            if(userList1.size() >= userList2.size()){
                Iterator iterator_userList1 = userList1.iterator();
                while(iterator_userList1.hasNext()){
                    LoginInformation  u1 = (LoginInformation) iterator_userList1.next();
                    Iterator iterator_userList2 = userList2.iterator();
                    while(iterator_userList2.hasNext()){
                        LoginInformation u2 = (LoginInformation) iterator_userList2.next();
                        if(u1.getUserName() == u2.getUserName()){
                            iterator_userList1.remove();
                            iterator_userList2.remove();
                            modifyed = true;
                        }
                    }
                }
            }
        }
        return modifyed;
    }
}
