package com.depth.dao;

import com.depth.bean.ImageFile;

import java.util.List;

public interface ImageFileDao {

    int insert(ImageFile imageFile);

    ImageFile findById(Integer imgId);

    List<ImageFile> findByUid(Integer uid, Integer start, Integer pageSize);

    int countByUid(Integer uid);

    int deleteById(Integer imgId);
}