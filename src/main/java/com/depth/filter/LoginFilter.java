package com.depth.filter;

import com.depth.bean.User;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

public class LoginFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String requestUri = httpRequest.getRequestURI();
        if (requestUri.contains("login.jsp") || requestUri.contains("register.jsp") ||
            requestUri.contains("LoginServlet") || requestUri.contains("RegisterServlet") ||
            requestUri.contains("TestServlet") ||
            requestUri.contains(".css") || requestUri.contains(".js") || requestUri.contains(".png") ||
            requestUri.contains(".jpg") || requestUri.contains(".jpeg") || requestUri.contains(".webp")) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = httpRequest.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            httpRequest.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(request, response);
            return;
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}