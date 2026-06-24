package com.depth.service;

import com.depth.bean.ImageFile;

import java.util.List;
import java.util.Map;

public interface ImageService {

    ImageFile upload(Integer uid, String originName, String saveName, String filePath, Integer fileSize);

    ImageFile findById(Integer imgId);

    Map<String, Object> findByUid(Integer uid, Integer pageNum, Integer pageSize);

    boolean deleteById(Integer imgId);
}