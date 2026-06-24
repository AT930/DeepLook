package com.depth.controller;

import com.depth.bean.User;
import com.depth.service.DepthService;
import com.depth.service.ImageService;
import com.depth.service.impl.DepthServiceImpl;
import com.depth.service.impl.ImageServiceImpl;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Map;

public class HistoryServlet extends HttpServlet {

    private ImageService imageService = new ImageServiceImpl();
    private DepthService depthService = new DepthServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User user = (User) session.getAttribute("user");
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/LoginServlet");
            return;
        }

        String pageNumStr = request.getParameter("pageNum");
        String pageSizeStr = request.getParameter("pageSize");

        int pageNum = 1;
        int pageSize = 10;

        try {
            if (pageNumStr != null && !pageNumStr.isEmpty()) {
                pageNum = Integer.parseInt(pageNumStr);
            }
            if (pageSizeStr != null && !pageSizeStr.isEmpty()) {
                pageSize = Integer.parseInt(pageSizeStr);
            }
        } catch (NumberFormatException e) {
            pageNum = 1;
            pageSize = 10;
        }

        Map<String, Object> imagePage = imageService.findByUid(user.getUid(), pageNum, pageSize);
        Map<String, Object> depthPage = depthService.findByUid(user.getUid(), pageNum, pageSize);

        request.setAttribute("images", imagePage.get("list"));
        request.setAttribute("depthRecords", depthPage.get("list"));
        request.setAttribute("total", imagePage.get("total"));
        request.setAttribute("pageNum", pageNum);
        request.setAttribute("pageSize", pageSize);
        request.setAttribute("totalPages", imagePage.get("totalPages"));

        request.getRequestDispatcher("/WEB-INF/jsp/history.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        String imgIdStr = request.getParameter("imgId");

        if ("delete".equals(action) && imgIdStr != null) {
            try {
                Integer imgId = Integer.parseInt(imgIdStr);
                boolean success = imageService.deleteById(imgId);

                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"success\": " + success + "}");
            } catch (NumberFormatException e) {
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"success\": false, \"message\": \"参数格式错误\"}");
            }
        }
    }
}