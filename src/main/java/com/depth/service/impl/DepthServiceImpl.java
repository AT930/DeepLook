package com.depth.service.impl;

import com.depth.bean.DepthRecord;
import com.depth.bean.ImageFile;
import com.depth.dao.DepthRecordDao;
import com.depth.dao.ImageFileDao;
import com.depth.dao.impl.DepthRecordDaoImpl;
import com.depth.dao.impl.ImageFileDaoImpl;
import com.depth.service.DepthService;
import com.depth.util.DepthApiClient;
import com.depth.util.DepthVisualUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DepthServiceImpl implements DepthService {

    private DepthRecordDao depthRecordDao = new DepthRecordDaoImpl();
    private ImageFileDao imageFileDao = new ImageFileDaoImpl();

    @Override
    public DepthRecord analyze(Integer imgId, Integer nearThreshold, Integer farThreshold, Double maskOpacity) {
        ImageFile imageFile = imageFileDao.findById(imgId);
        if (imageFile == null) {
            throw new RuntimeException("图片不存在");
        }

        String depthMapPath = null;
        try {
            if (DepthApiClient.isServiceAvailable()) {
                byte[] depthImageBytes = DepthApiClient.predictDepthImage(imageFile.getFilePath());
                
                File outputDir = new File("webapp/depthOutput");
                if (!outputDir.exists()) {
                    outputDir.mkdirs();
                }
                
                String extension = imageFile.getFilePath().substring(imageFile.getFilePath().lastIndexOf('.') + 1);
                String depthFileName = UUID.randomUUID().toString().replace("-", "") + "_depth_map.png";
                depthMapPath = "webapp/depthOutput" + File.separator + depthFileName;
                
                FileOutputStream fos = new FileOutputStream(depthMapPath);
                fos.write(depthImageBytes);
                fos.close();
            }
        } catch (IOException e) {
            System.err.println("Failed to call depth API, falling back to local algorithm: " + e.getMessage());
        }

        String outputPath;
        if (depthMapPath != null && new File(depthMapPath).exists()) {
            outputPath = generateDepthVisualWithMap(imageFile.getFilePath(), depthMapPath, nearThreshold, farThreshold, maskOpacity);
        } else {
            outputPath = DepthVisualUtil.generateDepthVisual(imageFile.getFilePath(), nearThreshold, farThreshold, maskOpacity);
        }

        DepthRecord existingRecord = depthRecordDao.findByImgId(imgId);
        if (existingRecord != null) {
            existingRecord.setDepthOutputPath(outputPath);
            existingRecord.setNearThreshold(nearThreshold);
            existingRecord.setFarThreshold(farThreshold);
            existingRecord.setMaskOpacity(maskOpacity);
            depthRecordDao.update(existingRecord);
            return existingRecord;
        }

        DepthRecord depthRecord = new DepthRecord();
        depthRecord.setImgId(imgId);
        depthRecord.setDepthOutputPath(outputPath);
        depthRecord.setNearThreshold(nearThreshold);
        depthRecord.setFarThreshold(farThreshold);
        depthRecord.setMaskOpacity(maskOpacity);
        depthRecordDao.insert(depthRecord);
        return depthRecord;
    }

    private String generateDepthVisualWithMap(String inputPath, String depthMapPath, Integer nearThreshold, Integer farThreshold, Double maskOpacity) {
        try {
            File inputFile = new File(inputPath);
            File depthFile = new File(depthMapPath);
            
            javax.imageio.ImageIO.read(inputFile);
            java.awt.image.BufferedImage depthMap = javax.imageio.ImageIO.read(depthFile);
            
            java.awt.image.BufferedImage originalImage = javax.imageio.ImageIO.read(inputFile);
            int width = originalImage.getWidth();
            int height = originalImage.getHeight();
            
            java.awt.image.BufferedImage resizedDepth = new java.awt.image.BufferedImage(width, height, java.awt.image.BufferedImage.TYPE_BYTE_GRAY);
            java.awt.Graphics2D g2d = resizedDepth.createGraphics();
            g2d.drawImage(depthMap, 0, 0, width, height, null);
            g2d.dispose();
            
            java.awt.image.BufferedImage result = new java.awt.image.BufferedImage(width, height, java.awt.image.BufferedImage.TYPE_INT_RGB);
            g2d = result.createGraphics();
            g2d.drawImage(originalImage, 0, 0, null);
            
            float opacity = maskOpacity.floatValue();
            
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int depthValue = resizedDepth.getRGB(x, y) & 0xFF;
                    java.awt.Color maskColor;
                    
                    if (depthValue <= nearThreshold) {
                        maskColor = new java.awt.Color(255, 100, 100, (int) (opacity * 255));
                    } else if (depthValue <= farThreshold) {
                        maskColor = new java.awt.Color(100, 255, 100, (int) (opacity * 255));
                    } else {
                        maskColor = new java.awt.Color(100, 150, 255, (int) (opacity * 255));
                    }
                    
                    int originalRGB = originalImage.getRGB(x, y);
                    java.awt.Color originalColor = new java.awt.Color(originalRGB, true);
                    
                    int blendedRGB = blendColors(originalColor, maskColor);
                    result.setRGB(x, y, blendedRGB);
                }
            }
            
            g2d.dispose();
            
            File outputDir = new File("webapp/depthOutput");
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }
            
            String extension = inputPath.substring(inputPath.lastIndexOf('.') + 1);
            String outputFileName = UUID.randomUUID().toString().replace("-", "") + "_depth." + extension;
            String outputPath = "webapp/depthOutput" + File.separator + outputFileName;
            
            File outputFile = new File(outputPath);
            javax.imageio.ImageIO.write(result, extension.toUpperCase(), outputFile);
            
            return outputPath;
        } catch (Exception e) {
            throw new RuntimeException("生成景深可视化图片失败", e);
        }
    }

    private int blendColors(java.awt.Color original, java.awt.Color mask) {
        float maskAlpha = mask.getAlpha() / 255.0f;
        int r = (int) (original.getRed() * (1 - maskAlpha) + mask.getRed() * maskAlpha);
        int g = (int) (original.getGreen() * (1 - maskAlpha) + mask.getGreen() * maskAlpha);
        int b = (int) (original.getBlue() * (1 - maskAlpha) + mask.getBlue() * maskAlpha);
        return (255 << 24) | (r << 16) | (g << 8) | b;
    }

    @Override
    public DepthRecord findByImgId(Integer imgId) {
        return depthRecordDao.findByImgId(imgId);
    }

    @Override
    public Map<String, Object> findByUid(Integer uid, Integer pageNum, Integer pageSize) {
        int start = (pageNum - 1) * pageSize;
        List<DepthRecord> list = depthRecordDao.findByUid(uid, start, pageSize);
        int total = depthRecordDao.countByUid(uid);
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);
        result.put("totalPages", (int) Math.ceil((double) total / pageSize));
        return result;
    }
}