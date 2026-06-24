package com.depth.controller;

import com.depth.bean.ImageFile;
import com.depth.bean.User;
import com.depth.service.ImageService;
import com.depth.service.impl.ImageServiceImpl;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

public class AnalyzeServlet extends HttpServlet {

    private ImageService imageService = new ImageServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User user = (User) session.getAttribute("user");
        if (user == null) {
            request.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(request, response);
            return;
        }

        String imgIdStr = request.getParameter("imgId");
        if (imgIdStr == null || imgIdStr.isEmpty()) {
            request.setAttribute("message", "缺少图片ID");
            request.getRequestDispatcher("/WEB-INF/jsp/index.jsp").forward(request, response);
            return;
        }

        int imgId;
        try {
            imgId = Integer.parseInt(imgIdStr);
        } catch (NumberFormatException e) {
            request.setAttribute("message", "无效的图片ID");
            request.getRequestDispatcher("/WEB-INF/jsp/index.jsp").forward(request, response);
            return;
        }

        ImageFile imageFile = imageService.findById(imgId);
        if (imageFile == null) {
            request.setAttribute("message", "图片不存在");
            request.getRequestDispatcher("/WEB-INF/jsp/index.jsp").forward(request, response);
            return;
        }

        request.setAttribute("imageFile", imageFile);
        request.setAttribute("imgId", imgIdStr);
        request.setAttribute("saveName", imageFile.getSaveName());
        request.getRequestDispatcher("/WEB-INF/jsp/analyze.jsp").forward(request, response);
    }
}