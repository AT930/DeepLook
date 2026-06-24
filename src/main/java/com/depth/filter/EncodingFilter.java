package com.depth.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;

public class EncodingFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String requestUri = httpRequest.getRequestURI();
        
        if (!requestUri.contains(".css") && !requestUri.contains(".js") && 
            !requestUri.contains(".png") && !requestUri.contains(".jpg") && 
            !requestUri.contains(".jpeg") && !requestUri.contains(".webp") &&
            !requestUri.contains(".tiff") && !requestUri.contains(".raw")) {
            response.setContentType("text/html;charset=UTF-8");
        }
        
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}