package com.depth.service;

import com.depth.bean.DepthRecord;

import java.util.List;
import java.util.Map;

public interface DepthService {

    DepthRecord analyze(Integer imgId, Integer nearThreshold, Integer farThreshold, Double maskOpacity);

    DepthRecord findByImgId(Integer imgId);

    Map<String, Object> findByUid(Integer uid, Integer pageNum, Integer pageSize);
}