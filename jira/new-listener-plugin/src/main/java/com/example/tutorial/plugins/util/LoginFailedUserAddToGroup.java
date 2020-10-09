package com.example.tutorial.plugins.util;

import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.exception.OperationNotPermittedException;
import com.atlassian.jira.bc.security.login.LoginReason;
import com.atlassian.jira.bc.security.login.LoginResult;
import com.atlassian.jira.bc.security.login.LoginService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.license.LicenseCountService;
import com.atlassian.jira.bc.user.search.UserSearchService;
import com.atlassian.jira.bc.user.search.UserSearchParams;
import com.atlassian.jira.user.ApplicationUser;
import com.example.tutorial.plugins.entity.GroupInfo;
import com.example.tutorial.plugins.entity.LoginInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.atlassian.jira.user.UserFilter;

import javax.servlet.ServletRequest;
import java.io.InputStream;
import java.util.*;

public class LoginFailedUserAddToGroup {
    private  int maxNumberOfUsers;
    private  int deleteNumberOfUsers;
    CrowdService crowdService = null;
    LicenseCountService licenseCountService = null;
    UserSearchService userSearchService = null;
    LoginService loginService = null;
    private Properties properties = null;
    private static final Logger log = LoggerFactory.getLogger(LoginFailedUserAddToGroup.class);

    public LoginFailedUserAddToGroup()  {
        crowdService = ComponentAccessor.getCrowdService();
        licenseCountService = ComponentAccessor.getComponentOfType(LicenseCountService.class);
        userSearchService = ComponentAccessor.getComponentOfType(UserSearchService.class);
        loginService = ComponentAccessor.getComponentOfType(LoginService.class);
        properties = new Properties();
        try{
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("new-listener-plugin.properties");
            if(inputStream != null){
                properties.load(inputStream);
                maxNumberOfUsers = Integer.parseInt(properties.getProperty("maxNumberOfUsers"));
                deleteNumberOfUsers = Integer.parseInt(properties.getProperty("deleteNumberOfUsers"));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    /**
     * 判断是否处理完毕
     */
    public boolean hasHandled(ServletRequest request){
        if(request != null){
            String name = request.getParameter("os_username");
            if(hasNoAuthorityToLogin(request)){
                if(isUserInGroup(name)){
                    return false;
                }else{
                    if(isNeedCleanUser()){
                        deleteUsers(getDeleteUserList());
                    }
                    addUserToGroup(name);
                    return true;
                }
            }else{
                return  false;
            }
        }
        return false;
    }
    /**
     * 当用户首次登录的时候，如果需要清理用户，则开始清理
     */
    public boolean cleanUserWhenUserFirstLogin(ServletRequest request){
        boolean cleanUser = false;
        if(request != null){
            if(hasSuccessLogin(request)){
                log.warn("用户首次登录");
                if(isNeedCleanUser()){
                    deleteUsers(getDeleteUserList());
                    cleanUser = true;
                }
            }
        }
        return cleanUser;
    }
    /**
     * 判断用户是否是密码正确但是是无权限登录
     */
    private boolean hasNoAuthorityToLogin(ServletRequest request){
        if(request != null){
            LoginResult loginResult = (LoginResult) request.getAttribute(LoginService.LOGIN_RESULT);
            boolean failedAuthorisation = loginResult != null && loginResult.getReason() == LoginReason.AUTHORISATION_FAILED;
            if(failedAuthorisation){
                return true;
            }
        }
        return false;
    }
    /**
     * 判断用户是否是成功登录的
     */
    private boolean hasSuccessLogin(ServletRequest request){
        boolean successLogin = false;
        if(request != null) {
            if(request.getAttribute("com.atlassian.seraph.auth.LoginReason").toString().equals("OK")){
                successLogin = true;
            }
        }
        return  successLogin;
    }
    /**
     * 判断用户是否在组里
     */
    private boolean isUserInGroup(String userName){
        boolean isUserinGroup = false;
        if(crowdService.isUserMemberOfGroup(crowdService.getUser(userName), crowdService.getGroup("jira-software-users"))){
            isUserinGroup = true;
        }
        return isUserinGroup;
    }
    /**
     * 添加用户到jira-software-users 用户组
     */
    private void addUserToGroup(String name){
        log.warn("添加的用户是："+name);
        try{
            crowdService.addUserToGroup(crowdService.getUser(name), crowdService.getGroup("jira-software-users"));
        } catch (OperationNotPermittedException e) {
            e.printStackTrace();
        }
    }
    /**
     * 判断当前是否需要清理用户
     */
    private boolean isNeedCleanUser(){
        int licenseCount = licenseCountService.totalBillableUsers();
        if(licenseCount >= maxNumberOfUsers){
            return true;
        }
        return false;
    }
    /**
     * 获取当前LIcense数
     */
    private int getLicenseNumbers(){
        return licenseCountService.totalBillableUsers();
    }
    /**
     * 获取当前需要删除的用户数
     */
    private int  needCleanUserNumbers(){
        GroupInfo groupInfo = initLoginInformation("jira-administrators");
        int jira_admin_count = groupInfo.getGroupMemberCount();
        int cntLiicense = getLicenseNumbers();
        if(cntLiicense - jira_admin_count < deleteNumberOfUsers){
            return cntLiicense - jira_admin_count;
        }else{
            return deleteNumberOfUsers;
        }
    }
    /**
     * 初始化用户组的信息
     * @return TreeSet<LoginInformation> 已经排好序
     */
    private GroupInfo  initLoginInformation(String group_name){
        GroupInfo groupInfo ;
        TreeSet<LoginInformation> logininfos = new TreeSet<>();
        Collection<String> groups = new ArrayList<>(Arrays.asList(group_name));
        Collection<Long> roles = new ArrayList<>(Arrays.asList());
        UserFilter userFilter = new UserFilter(true, roles,groups);
        UserSearchParams userSearchParams= (new UserSearchParams.Builder()).allowEmptyQuery(true).includeActive(true).includeInactive(false).filter(userFilter).build();
        List<ApplicationUser> users= userSearchService.findUsers("",userSearchParams);
        for(ApplicationUser user : users){
            String userName = user.getUsername();
            Long lastLoginTime;
             if(loginService.getLoginInfo(userName).getLastLoginTime() == null){
                 lastLoginTime = 0L;
             }else{
                 lastLoginTime = loginService.getLoginInfo(userName).getLastLoginTime();
             }
            LoginInformation loginInformation = new LoginInformation(userName,lastLoginTime);
            logininfos.add(loginInformation);
        }
        groupInfo = new GroupInfo(logininfos, logininfos.size());
        return  groupInfo;
    }

    /**
     * 获取需要删除的用户列表
     * @return 用户名列表
     */
    private  ArrayList<String> getDeleteUserList(){
        GroupInfo jira_users_group = initLoginInformation("jira-software-users");
        GroupInfo jira_admin_group = initLoginInformation("jira-administrators");
        RemoveUsersFromGroup removeUsersFromGroup  = new RemoveUsersFromGroup();
        TreeSet<LoginInformation> jira_users_List = jira_users_group.getUserList();
        TreeSet<LoginInformation> jira_admin_List = jira_admin_group.getUserList();
        ArrayList<String> deleteUserNamesList = new ArrayList<>();
        int i = needCleanUserNumbers();
        log.warn("需要删除的用户数："+i);
        if(removeUsersFromGroup.removeUsersFromGroup(jira_users_List,jira_admin_List)){
            for(LoginInformation user: jira_users_List){
                if(i <= 0){
                    break;
                }
                deleteUserNamesList.add(user.getUserName());
                i--;
            }
        }
        return deleteUserNamesList;
    }
    /**
     * 功能：删除deleteUserNumbers个用户
     * @param userNames：需要删除的用户的userName
     */
    private void deleteUsers(ArrayList<String> userNames){
        for(String userName: userNames){
            try {
                log.warn("要删除的用户名是："+userName);
                crowdService.removeUserFromGroup(crowdService.getUser(userName), crowdService.getGroup("jira-software-users"));
            }catch(Exception e){
                e.getMessage();
            }
        }
    }
}
