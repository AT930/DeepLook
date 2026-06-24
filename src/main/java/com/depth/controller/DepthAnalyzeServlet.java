package com.depth.controller;

import com.depth.bean.DepthRecord;
import com.depth.bean.ImageFile;
import com.depth.service.DepthService;
import com.depth.service.ImageService;
import com.depth.service.impl.DepthServiceImpl;
import com.depth.service.impl.ImageServiceImpl;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class DepthAnalyzeServlet extends HttpServlet {

    private DepthService depthService = new DepthServiceImpl();
    private ImageService imageService = new ImageServiceImpl();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String imgIdStr = request.getParameter("imgId");
        String nearThresholdStr = request.getParameter("nearThreshold");
        String farThresholdStr = request.getParameter("farThreshold");
        String maskOpacityStr = request.getParameter("maskOpacity");

        try {
            Integer imgId = Integer.parseInt(imgIdStr);
            Integer nearThreshold = nearThresholdStr != null ? Integer.parseInt(nearThresholdStr) : 60;
            Integer farThreshold = farThresholdStr != null ? Integer.parseInt(farThresholdStr) : 180;
            Double maskOpacity = maskOpacityStr != null ? Double.parseDouble(maskOpacityStr) : 0.4;

            DepthRecord record = depthService.analyze(imgId, nearThreshold, farThreshold, maskOpacity);

            response.setContentType("application/json;charset=UTF-8");
            PrintWriter out = response.getWriter();
            out.write("{\"success\": true, \"recordId\": " + record.getRecordId() + 
                      ", \"outputPath\": \"" + record.getDepthOutputPath() + "\"}");
            out.flush();
            out.close();

        } catch (NumberFormatException e) {
            response.setContentType("application/json;charset=UTF-8");
            PrintWriter out = response.getWriter();
            out.write("{\"success\": false, \"message\": \"参数格式错误\"}");
            out.flush();
            out.close();
        } catch (Exception e) {
            response.setContentType("application/json;charset=UTF-8");
            PrintWriter out = response.getWriter();
            out.write("{\"success\": false, \"message\": \"" + e.getMessage() + "\"}");
            out.flush();
            out.close();
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String imgIdStr = request.getParameter("imgId");
        if (imgIdStr == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "缺少 imgId 参数");
            return;
        }

        try {
            Integer imgId = Integer.parseInt(imgIdStr);
            ImageFile imageFile = imageService.findById(imgId);
            DepthRecord record = depthService.findByImgId(imgId);

            if (imageFile == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "图片不存在");
                return;
            }

            request.setAttribute("imageFile", imageFile);
            request.setAttribute("depthRecord", record);
            request.getRequestDispatcher("/WEB-INF/jsp/analyze.jsp").forward(request, response);

        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "参数格式错误");
        }
    }
}