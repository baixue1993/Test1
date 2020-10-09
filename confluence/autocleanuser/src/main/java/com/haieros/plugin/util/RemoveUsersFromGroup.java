package com.haieros.plugin.util;

import com.haieros.plugin.entity.UserInformation;

import java.util.Iterator;
import java.util.Objects;
import java.util.TreeSet;

public class RemoveUsersFromGroup {
    public boolean removeAll(TreeSet<UserInformation> userList1, TreeSet<UserInformation> userList2){
        Objects.requireNonNull(userList1);
        Objects.requireNonNull(userList2);
        boolean modifyed = false;
        Iterator iterator_users = userList1.iterator();
        while(iterator_users.hasNext()){
            UserInformation u1 = (UserInformation) iterator_users.next();
            Iterator iterator_admins = userList2.iterator();
            while(iterator_admins.hasNext()){
                UserInformation u2 = (UserInformation) iterator_admins.next();
                if(u1.getUserName() == u2.getUserName()){
                    iterator_users.remove();
                    iterator_admins.remove();
                    modifyed = true;
                }
            }
        }
        return modifyed;
    }
}
