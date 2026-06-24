package com.depth.util;

import jakarta.servlet.http.Part;
import jakarta.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FileUploadUtil {

    private static final String[] ALLOWED_EXTENSIONS = {"jpg", "jpeg", "png", "tiff", "raw", "webp"};
    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024;

    public static Map<String, Object> parseRequest(HttpServletRequest request, String uploadDir) {
        Map<String, Object> result = new HashMap<>();
        Map<String, String> params = new HashMap<>();
        Map<String, String> files = new HashMap<>();

        try {
            for (Part part : request.getParts()) {
                if (part.getContentType() == null) {
                    String value = new String(part.getInputStream().readAllBytes(), "UTF-8");
                    params.put(part.getName(), value);
                } else {
                    String fileName = getFileName(part);
                    if (fileName != null && !fileName.isEmpty()) {
                        String extension = getExtension(fileName);
                        if (!isValidExtension(extension)) {
                            throw new RuntimeException("不支持的文件格式，仅支持 jpg/jpeg/png/tiff/raw/webp");
                        }

                        if (part.getSize() > MAX_FILE_SIZE) {
                            throw new RuntimeException("文件大小超过限制（最大50MB）");
                        }

                        String saveName = UUID.randomUUID().toString().replace("-", "") + "." + extension;
                        String filePath = uploadDir + File.separator + saveName;

                        File file = new File(filePath);
                        if (!file.getParentFile().exists()) {
                            file.getParentFile().mkdirs();
                        }

                        try (InputStream is = part.getInputStream();
                             FileOutputStream fos = new FileOutputStream(file)) {
                            byte[] buffer = new byte[4096];
                            int bytesRead;
                            while ((bytesRead = is.read(buffer)) != -1) {
                                fos.write(buffer, 0, bytesRead);
                            }
                        }

                        files.put(part.getName(), filePath);
                        result.put("originalName", fileName);
                        result.put("saveName", saveName);
                    }
                }
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("文件上传解析失败: " + e.getClass().getName(), e);
        }

        result.put("params", params);
        result.put("files", files);
        return result;
    }

    private static String getFileName(Part part) {
        String contentDisposition = part.getHeader("content-disposition");
        for (String token : contentDisposition.split(";")) {
            if (token.trim().startsWith("filename")) {
                return token.substring(token.indexOf("=") + 1).trim().replace("\"", "");
            }
        }
        return null;
    }

    private static String getExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return fileName.substring(lastDotIndex + 1).toLowerCase();
    }

    private static boolean isValidExtension(String extension) {
        for (String ext : ALLOWED_EXTENSIONS) {
            if (ext.equalsIgnoreCase(extension)) {
                return true;
            }
        }
        return false;
    }

    public static String generateSaveName(String originalFileName) {
        String extension = getExtension(originalFileName);
        return UUID.randomUUID().toString().replace("-", "") + "." + extension;
    }
}