package com.depth.service.impl;

import com.depth.bean.ImageFile;
import com.depth.dao.DepthRecordDao;
import com.depth.dao.ImageFileDao;
import com.depth.dao.impl.DepthRecordDaoImpl;
import com.depth.dao.impl.ImageFileDaoImpl;
import com.depth.service.ImageService;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImageServiceImpl implements ImageService {

    private ImageFileDao imageFileDao = new ImageFileDaoImpl();
    private DepthRecordDao depthRecordDao = new DepthRecordDaoImpl();

    @Override
    public ImageFile upload(Integer uid, String originName, String saveName, String filePath, Integer fileSize) {
        ImageFile imageFile = new ImageFile();
        imageFile.setUid(uid);
        imageFile.setOriginName(originName);
        imageFile.setSaveName(saveName);
        imageFile.setFilePath(filePath);
        imageFile.setFileSize(fileSize);
        imageFileDao.insert(imageFile);
        return imageFile;
    }

    @Override
    public ImageFile findById(Integer imgId) {
        return imageFileDao.findById(imgId);
    }

    @Override
    public Map<String, Object> findByUid(Integer uid, Integer pageNum, Integer pageSize) {
        int start = (pageNum - 1) * pageSize;
        List<ImageFile> list = imageFileDao.findByUid(uid, start, pageSize);
        int total = imageFileDao.countByUid(uid);
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);
        result.put("totalPages", (int) Math.ceil((double) total / pageSize));
        return result;
    }

    @Override
    public boolean deleteById(Integer imgId) {
        ImageFile imageFile = imageFileDao.findById(imgId);
        if (imageFile != null) {
            File originalFile = new File(imageFile.getFilePath());
            if (originalFile.exists()) {
                originalFile.delete();
            }
            DepthRecordDaoImpl depthDao = new DepthRecordDaoImpl();
            DepthRecordDao depthRecordDao = new DepthRecordDaoImpl();
            com.depth.bean.DepthRecord record = depthRecordDao.findByImgId(imgId);
            if (record != null) {
                File depthFile = new File(record.getDepthOutputPath());
                if (depthFile.exists()) {
                    depthFile.delete();
                }
                depthRecordDao.deleteByImgId(imgId);
            }
            imageFileDao.deleteById(imgId);
            return true;
        }
        return false;
    }
}