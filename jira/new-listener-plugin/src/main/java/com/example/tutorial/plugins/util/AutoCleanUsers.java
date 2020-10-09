package com.example.tutorial.plugins.util;

import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.exception.OperationNotPermittedException;
import com.atlassian.jira.bc.security.login.LoginService;
import com.atlassian.jira.bc.user.search.UserSearchParams;
import com.atlassian.jira.bc.user.search.UserSearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.license.LicenseCountService;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.UserFilter;
import com.example.tutorial.plugins.entity.GroupInfo;
import com.example.tutorial.plugins.entity.LoginInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.*;

public class AutoCleanUsers {
    private LicenseCountService licenseCountService;
    private Properties properties;
    private int minNumberOfUsers;
    private UserSearchService userSearchService;
    private LoginService loginService;
    private CrowdService crowdService;
    private static final Logger LOG =LoggerFactory.getLogger(AutoCleanUsers.class);

    public AutoCleanUsers(){
        LOG.warn("开始清理");
        licenseCountService = ComponentAccessor.getComponentOfType(LicenseCountService.class);
        userSearchService = ComponentAccessor.getComponentOfType(UserSearchService.class);
        loginService = ComponentAccessor.getComponentOfType(LoginService.class);
        crowdService = ComponentAccessor.getCrowdService();
        try{
            properties = new Properties();
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("new-listener-plugin.properties");
            if(inputStream != null){
                properties.load(inputStream);
                minNumberOfUsers = Integer.parseInt(properties.getProperty("minNumberOfUsers"));
            }
        }catch(Exception e){
            e.getMessage();
        }
    }
    /**
     * 处理用户调度的工作
     */
    public  boolean hasHandled(){
        if(isNeedToCleanUser()){
            deleteUser(getNeedDeleteUserList());
            return true;
        }
        return false;
    }
    /**
     * 是否需要清理用户
     */
    private boolean isNeedToCleanUser(){
        if(getLicenseNumbers() > minNumberOfUsers){
            return true;
        }
        return false;
    }
    /**
     *获取当前的license数
     */
    private int getLicenseNumbers(){
        return licenseCountService.totalBillableUsers();
    }
    /**
     * 获取需要删除的用户数
     */
    private int getUserDeleteNumbers(){
        GroupInfo groupInfo = initGroupInfo("jira-administrators");
        int groupCount = groupInfo.getGroupMemberCount();
        if(getLicenseNumbers() - groupCount < getLicenseNumbers() - minNumberOfUsers){
            return getLicenseNumbers() - groupCount;
        }else{
            return getLicenseNumbers() - minNumberOfUsers;
        }
    }
    /**
     * 获取需要删除用户的列表
     */
    private ArrayList<String> getNeedDeleteUserList(){
        ArrayList<String> userList = new ArrayList<>();
        GroupInfo jira_admin_groupinfo =initGroupInfo("jira-administrators");
        GroupInfo jira_users_groupinfo = initGroupInfo("jira-software-users");
        TreeSet<LoginInformation> jira_admin_userList = jira_admin_groupinfo.getUserList();
        TreeSet<LoginInformation> jira_users_userList = jira_users_groupinfo.getUserList();
        int count = getUserDeleteNumbers();
        LOG.warn("需要删除的用户数是："+count);
        RemoveUsersFromGroup removeUsersFromGroup = new RemoveUsersFromGroup();
        if(removeUsersFromGroup.removeUsersFromGroup(jira_users_userList, jira_admin_userList)){
            for(LoginInformation user: jira_users_userList){
                if(count <= 0){
                    break;
                }
                userList.add(user.getUserName());
            }
            count --;
        }
        return userList;
    }
    /**
     * 初始化列表中的用户
     */
    private GroupInfo initGroupInfo(String groupName){
        GroupInfo groupInfo ;
        TreeSet<LoginInformation> userList = new TreeSet<>();
        Collection<String> group = new ArrayList<>(Arrays.asList(groupName));
        Collection<Long> roles = new ArrayList<>(Arrays.asList());
        UserFilter userFilter = new UserFilter(true,roles,group);
        UserSearchParams userSearchParams = (new UserSearchParams.Builder()).allowEmptyQuery(true).includeActive(true).includeInactive(false).filter(userFilter).build();
        List<ApplicationUser> users = userSearchService.findUsers("", userSearchParams);
        for(ApplicationUser user: users){
            String name = user.getUsername();
            Long lastLoginTime ;
            if(loginService.getLoginInfo(name).getLastLoginTime() == null){
                lastLoginTime = 0L;
            }else{
                lastLoginTime = loginService.getLoginInfo(name).getLastLoginTime();
            }
            LoginInformation loginInformation = new LoginInformation(name, lastLoginTime);
            userList.add(loginInformation);
        }
        groupInfo = new GroupInfo(userList, userList.size());
        return groupInfo;
    }
    /**
     * 删除用户
     */
    private boolean deleteUser(ArrayList<String> userList) {
        boolean modifyed = false;
        if(userList != null){
            for(String userName : userList){
                LOG.warn("删除用户："+userName);
                try{
                crowdService.removeUserFromGroup(crowdService.getUser(userName), crowdService.getGroup("jira-software-users"));
                modifyed = true;
                }catch (OperationNotPermittedException e){
                    e.printStackTrace();
                }
            }
        }
        return modifyed;
    }
}
