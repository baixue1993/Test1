package com.example.tutorial.plugins.filter;

import com.example.tutorial.plugins.util.LoginFailedUserAddToGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Enumeration;

public class LoginFilter implements Filter {
    private static final Logger log = LoggerFactory.getLogger(LoginFilter.class);
    private LoginFailedUserAddToGroup loginFailedUserAddToGroup;
    public void init(FilterConfig config) throws ServletException{

    }
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws ServletException, IOException {
        //从请求中获取username和password
        String name = request.getParameter("os_username");
        String password = request.getParameter("os_password");
        HttpServletRequest request1 =(HttpServletRequest) request;
        String baseUrl = request1.getRequestURI().toString();
        loginFailedUserAddToGroup = new LoginFailedUserAddToGroup();
        if(baseUrl.equals("/jira/secure/WelcomeToJIRA.jspa") && name == null){
            if(loginFailedUserAddToGroup.cleanUserWhenUserFirstLogin(request)){
                log.warn("清理用户成功");
            }
        }
        if(name != null && password != null){

            if(loginFailedUserAddToGroup.hasHandled(request)){
                log.warn("添加成功");
            }
        }
        chain.doFilter(request, response);
    }

    public void destroy(){

    }
}
