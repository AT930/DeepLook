package com.depth.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;

public class TestServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        HttpSession session = request.getSession(false);
        out.println("<html><body>");
        out.println("<h1>Session Test</h1>");
        if (session != null) {
            out.println("<p>Session ID: " + session.getId() + "</p>");
            out.println("<p>User: " + session.getAttribute("user") + "</p>");
        } else {
            out.println("<p>No session</p>");
        }
        out.println("</body></html>");
    }
}