package com.depth.dao;

import com.depth.bean.DepthRecord;

import java.util.List;

public interface DepthRecordDao {

    int insert(DepthRecord depthRecord);

    DepthRecord findByImgId(Integer imgId);

    List<DepthRecord> findByUid(Integer uid, Integer start, Integer pageSize);

    int deleteByImgId(Integer imgId);

    int update(DepthRecord depthRecord);

    int countByUid(Integer uid);
}