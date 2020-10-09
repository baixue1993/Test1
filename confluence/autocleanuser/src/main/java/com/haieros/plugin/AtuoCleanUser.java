package com.haieros.plugin;

import com.atlassian.confluence.security.login.LoginInfo;
import com.atlassian.confluence.security.login.LoginManager;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.sal.api.component.ComponentLocator;
import com.atlassian.user.Group;
import com.haieros.plugin.entity.GroupInfo;
import com.haieros.plugin.entity.UserInformation;
import com.haieros.plugin.util.RemoveUsersFromGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.Serializable;
import java.util.*;

public class AtuoCleanUser implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(AtuoCleanUser.class);
    private UserAccessor userAccessor;
    private int minNumberOfUsers;
    private Properties properties = null;
    private LoginManager loginManager;
    private CrowdService crowdService;
    public AtuoCleanUser(){
        LOG.warn("开始清理用户");
        this.userAccessor = ComponentLocator.getComponent(UserAccessor.class);
        this.loginManager = ComponentLocator.getComponent(LoginManager.class);
        this.crowdService = ComponentLocator.getComponent(CrowdService.class);
        try{
            properties = new Properties();
            InputStream properFiles = getClass().getClassLoader().getResourceAsStream("autocleanuser.properties");
            if(properFiles != null) {
                properties.load(properFiles);
            }
        }catch (Exception e){
            e.getMessage();
        }
        this.minNumberOfUsers = Integer.parseInt(properties.getProperty("minNumberOfUsers"));
    }

    /**
     * 清理用户，外部可以调用
     * @return
     */
    public boolean cleanUsers(){
        if(isNeedToCLeanUser()){
            deleteUsers(getUserListNeedDelete());
            return true;
        }
        return false;
    }

    /**
     * 获得当前License用户数
     * @return
     */
    private int getLicenseUserNumber(){
        int cntLicense = 0;
        cntLicense = userAccessor.countLicenseConsumingUsers();
        LOG.warn("当前License的个数："+cntLicense);
        return cntLicense;
    }
    /**
     * 判断当前是否需要删除用户
     */
    private boolean isNeedToCLeanUser(){
        if(getLicenseUserNumber() - minNumberOfUsers > 0){
            return true;
        }
        return false;
    }

    /**
     * 初始化给定的用户组
     */
    private GroupInfo  initGroups (String groupName){
        TreeSet<UserInformation> userList = new TreeSet<>();
        Group group = userAccessor.getGroup(groupName);
        List<String> users = userAccessor.getMemberNamesAsList(group);
        for(String userName : users){

            String name = userName;
            Long lastLoginTime;
            LoginInfo loginInfo = loginManager.getLoginInfo(userName);
            Date loginDate = loginInfo.getLastSuccessfulLoginDate();
            if(loginDate != null){
                lastLoginTime = loginDate.getTime();
            }else{
                lastLoginTime = 0L;
            }
            UserInformation userInformation = new UserInformation(name, lastLoginTime);
            userList.add(userInformation);
        }
        GroupInfo groupInfo = new GroupInfo(userList, userList.size());
        return groupInfo;
    }

    /**
     * 获取当前需要删除的用户数
     * @return
     */
    private int getUserNumberNeedToDelete(){
        GroupInfo groupInfo = initGroups("confluence-administrators");
        GroupInfo confluence_user_group = initGroups("confluence-users");
        int cntLicense = getLicenseUserNumber();
        int admin_users_count = groupInfo.getGroupMemberCount();
        if(cntLicense - minNumberOfUsers <= cntLicense - admin_users_count){
            return cntLicense - minNumberOfUsers;
        }else{
            return cntLicense - admin_users_count;
        }
    }

    /**
     * 获取需要删除的用户列表
     */
    private ArrayList<String> getUserListNeedDelete(){
        ArrayList<String> userList = new ArrayList<>();
        int count = getUserNumberNeedToDelete();
        LOG.warn("拿到的需求要删除的用户数："+count);
        GroupInfo confluence_user_info = initGroups("confluence-users");
        GroupInfo confluence_admin_info = initGroups("confluence-administrators");
        TreeSet<UserInformation> confluence_user_list = confluence_user_info.getUserList();
        TreeSet<UserInformation> confluence_admin_list = confluence_admin_info.getUserList();
        RemoveUsersFromGroup removeUsersFromGroup = new RemoveUsersFromGroup();
        boolean removeFlag = removeUsersFromGroup.removeAll(confluence_user_list, confluence_admin_list);
        if(removeFlag){
            for(UserInformation user: confluence_user_list){
                if(count <= 0){
                    break;
                }
                userList.add(user.getUserName());
                count --;
            }
        }
        return userList;
    }

    /**
     * 删除需要删除的用户列表中的用户
     * @param userNamesList
     * @return
     */
   private void deleteUsers(ArrayList<String> userNamesList ){
        for(String userName: userNamesList){
            try {
                LOG.warn("删除用户："+userName);
                crowdService.removeUserFromGroup(crowdService.getUser(userName), crowdService.getGroup("confluence-users"));
            }catch(Exception e){
                e.getMessage();
            }
        }
    }
}

