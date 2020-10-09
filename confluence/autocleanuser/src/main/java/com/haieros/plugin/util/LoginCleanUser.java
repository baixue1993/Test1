package com.haieros.plugin.util;

import com.atlassian.confluence.security.login.LoginInfo;
import com.atlassian.confluence.security.login.LoginManager;
import com.atlassian.confluence.security.seraph.ConfluenceUserPrincipal;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.sal.api.component.ComponentLocator;
import com.atlassian.user.Group;
import com.haieros.plugin.entity.GroupInfo;
import com.haieros.plugin.entity.UserInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.util.*;

public class LoginCleanUser {
    private static final Logger LOG = LoggerFactory.getLogger(LoginCleanUser.class);
    private UserAccessor userAccessor;
    private CrowdService crowdService;
    private LoginManager loginManager;
    private Properties properties;
    private int deleteNumberOfUsers;
    private int maxNumberOfUsers;
    private ConfluenceUserPrincipal confluenceUserPrincipal;
    public LoginCleanUser(){
        this.userAccessor  = ComponentLocator.getComponent(UserAccessor.class);
        this.crowdService = ComponentLocator.getComponent(CrowdService.class);
        this.loginManager = ComponentLocator.getComponent(LoginManager.class);
        try {
            properties = new Properties();
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("autocleanuser.properties");
            if(inputStream != null){
                properties.load(inputStream);
                maxNumberOfUsers = Integer.parseInt(properties.getProperty("maxNumberOfUsers"));
                deleteNumberOfUsers = Integer.parseInt(properties.getProperty("deleteNumberOfUsers"));
            }
        }catch(Exception e){
            e.getMessage();
        }
    }
    /**
     * 处理用户（添加用户到用户组里面的流程）
     */
    public boolean hasHandled(HttpServletRequest request){
        if(request != null){
            if(request.getSession().getAttribute("seraph_defaultauthenticator_user") != null){
                confluenceUserPrincipal = (ConfluenceUserPrincipal) request.getSession().getAttribute("seraph_defaultauthenticator_user");
                if(isUserInGroup(confluenceUserPrincipal.getName())){
                    if(isFirstLogin()){
                        if(isDeleteUsers()) {
                            deleteUsers(getUserListNeedClean());
                            LOG.warn("用户清理成功");
                            return true;
                        }
                    }
                    return false;
                }else{
                    if(isDeleteUsers()){
                        deleteUsers(getUserListNeedClean());
                    }else{
                        addUserToGroup(confluenceUserPrincipal.getName());
                    }
                    return true;
            }}
        }
        return false;
    }
    /**
     * 用户是否已经在组里了
     */
    private boolean isUserInGroup(String userName){
        boolean isInGroup = false;
        if(userName != null){
            isInGroup = crowdService.isUserMemberOfGroup(crowdService.getUser(userName), crowdService.getGroup("confluence-users"));
        }
        return isInGroup;
    }
    /**
     * 判断用户是否是首次登录
     */
    private boolean isFirstLogin(){
        boolean isFirstLogin = false;
        String userName = confluenceUserPrincipal.getName();
        LoginInfo loginInfo = loginManager.getLoginInfo(userName);
        if(loginInfo.getPreviousSuccessfulLoginDate() != null){
            isFirstLogin = true;
        }
        return  isFirstLogin;
    }
    /**
     * 判断是否需要清理用户
     */
    private boolean isDeleteUsers(){
        if(getLicenseUserNumber() -  maxNumberOfUsers >  0){
            return true;
        }
        return false;
    }
    /**
     * 添加用户到用户组
     */
    private void addUserToGroup(String userName){
        try{
            crowdService.addUserToGroup(crowdService.getUser(userName), crowdService.getGroup("confluence-users"));
        }catch (Exception e){
            e.getMessage();
        }
    }
    /**
     * 获得需要清理的用户数
     */
    private int cleanUserNumbers(){
        GroupInfo groupInfo = initUserGroup("confluence-administrators");
        if(getLicenseUserNumber() - groupInfo.getGroupMemberCount() < deleteNumberOfUsers){
            LOG.warn("需要清理的用户数："+(getLicenseUserNumber() - groupInfo.getGroupMemberCount()));
            return getLicenseUserNumber() - groupInfo.getGroupMemberCount();
        }
        LOG.warn("需要清理的用户数："+deleteNumberOfUsers);
        return deleteNumberOfUsers;
    }
    /**
     * 获取当前license用户数
     */
    private int getLicenseUserNumber(){
        LOG.warn("当前Licenses数："+userAccessor.countLicenseConsumingUsers());
        return userAccessor.countLicenseConsumingUsers();
    }
    /**
     * 获取需要清理的用户的列表
     */
    private ArrayList<String> getUserListNeedClean(){
        ArrayList<String> userList = new ArrayList<>();
        GroupInfo confluence_users_group = initUserGroup("confluence-users");
        GroupInfo confluence_admin_group = initUserGroup("confluence-administrators");
        int count = cleanUserNumbers();
        RemoveUsersFromGroup removeUsersFromGroup = new RemoveUsersFromGroup();
        LOG.warn("拿到要删除的用户数："+count);

        if(removeUsersFromGroup.removeAll(confluence_users_group.getUserList(), confluence_admin_group.getUserList())){
            for(UserInformation userinfo : confluence_users_group.getUserList()){
                if(count <= 0){
                    break;
                }
                String name = userinfo.getUserName();
                userList.add(name);
                count --;
            }
        }
        return  userList;
    }
    /**
     * 初始化用户组
     * @param groupName
     */
    private GroupInfo initUserGroup(String groupName){
        GroupInfo groupInfo;
        TreeSet<UserInformation> userList = new TreeSet<>();
        Group group = userAccessor.getGroup(groupName);
        List<String> users = userAccessor.getMemberNamesAsList(group);
        for(String user : users){
            String name = user;
            LOG.warn("用户组:"+groupName+"中的用户："+name);
            Long lastLoginTime;
            LoginInfo loginInfo = loginManager.getLoginInfo(name);
            if(loginInfo.getLastSuccessfulLoginDate() != null){
                lastLoginTime = loginInfo.getLastSuccessfulLoginDate().getTime();
            }else{
                lastLoginTime = 0L;
            }
            UserInformation userInformation = new UserInformation(name, lastLoginTime);
            userList.add(userInformation);
        }
        groupInfo = new GroupInfo(userList, userList.size());
        return groupInfo;
    }
    /**
     * 清理用户列表中的用户
     * @param userList
     */
    private boolean deleteUsers(ArrayList<String> userList){
        addUserToGroup(confluenceUserPrincipal.getName());
        if(userList != null){
            for(String userName : userList){
                LOG.warn("清理用户："+userName);
                try{
                    crowdService.removeUserFromGroup(crowdService.getUser(userName), crowdService.getGroup("confluence-users"));
                }catch (Exception e){
                    e.getMessage();
                }
            }
            LOG.warn("在这里添加用户");
            return true;
        }
        return false;
    }
}
