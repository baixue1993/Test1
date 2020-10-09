package com.haieros.plugin.servlet;

import com.haieros.plugin.util.LoginCleanUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class UserLoginFilter  implements Filter {
    private static final Logger LOG = LoggerFactory.getLogger(UserLoginFilter.class);
    private LoginCleanUser loginCleanUser;

    public UserLoginFilter() {

    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
        String baseUrl = httpServletRequest.getRequestURI().toString();
        if (baseUrl.matches(".*(css|jpg|png|gif|js|dev-toolbar|svg|woff|eot)")) {
            filterChain.doFilter(httpServletRequest, httpServletResponse);
        } else if (baseUrl.contains("logout.action")) {
            filterChain.doFilter(httpServletRequest, httpServletResponse);
        } else if(baseUrl.equals("/confluence/")) {
            if(httpServletRequest != null){
                loginCleanUser = new LoginCleanUser();
                if(loginCleanUser.hasHandled(httpServletRequest)){
                    LOG.warn("添加用户成功");
                }
            }
            filterChain.doFilter(httpServletRequest, httpServletResponse);
        }else{
            filterChain.doFilter(httpServletRequest, httpServletResponse);
        }
    }

    @Override
    public void destroy() {

    }
}
