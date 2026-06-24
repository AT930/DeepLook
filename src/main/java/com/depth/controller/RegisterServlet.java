package com.depth.controller;

import com.depth.service.UserService;
import com.depth.service.impl.UserServiceImpl;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class RegisterServlet extends HttpServlet {

    private UserService userService = new UserServiceImpl();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            out.write("{\"success\": false, \"message\": \"用户名和密码不能为空\"}");
            out.flush();
            out.close();
            return;
        }

        boolean success = userService.register(username, password);

        if (success) {
            out.write("{\"success\": true, \"message\": \"注册成功\"}");
        } else {
            out.write("{\"success\": false, \"message\": \"用户名已存在\"}");
        }
        out.flush();
        out.close();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/jsp/register.jsp").forward(request, response);
    }
}