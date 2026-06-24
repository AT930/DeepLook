package com.depth.controller;

import com.depth.bean.ImageFile;
import com.depth.bean.User;
import com.depth.service.ImageService;
import com.depth.service.impl.ImageServiceImpl;
import com.depth.util.FileUploadUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

@MultipartConfig(maxFileSize = 50 * 1024 * 1024, maxRequestSize = 50 * 1024 * 1024)
public class UploadServlet extends HttpServlet {

    private ImageService imageService = new ImageServiceImpl();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession(false);
        User user = (User) session.getAttribute("user");
        if (user == null) {
            out.write("{\"success\": false, \"message\": \"请先登录\"}");
            out.flush();
            out.close();
            return;
        }

        String uploadDir = getServletContext().getRealPath("/uploadImg");
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        try {
            Map<String, Object> result = FileUploadUtil.parseRequest(request, uploadDir);
            String originalName = (String) result.get("originalName");
            String saveName = (String) result.get("saveName");
            Map<String, String> files = (Map<String, String>) result.get("files");

            if (files.isEmpty()) {
                out.write("{\"success\": false, \"message\": \"请选择要上传的图片\"}");
                out.flush();
                out.close();
                return;
            }

            String filePath = files.values().iterator().next();
            File file = new File(filePath);
            int fileSize = (int) (file.length() / 1024);

            ImageFile imageFile = imageService.upload(user.getUid(), originalName, saveName, filePath, fileSize);

            String redirectUrl = request.getContextPath() + "/AnalyzeServlet?imgId=" + imageFile.getImgId();
            out.write("{\"success\": true, \"imgId\": \"" + imageFile.getImgId() + "\", \"redirectUrl\": \"" + redirectUrl + "\"}");

        } catch (Exception e) {
            String message = e.getMessage();
            if (message == null) {
                message = "上传失败";
            }
            message = message.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
            out.write("{\"success\": false, \"message\": \"" + message + "\"}");
        } finally {
            out.flush();
            out.close();
        }
    }
}