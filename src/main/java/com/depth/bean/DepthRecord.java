package com.depth.bean;

import java.util.Date;

public class DepthRecord {

    private Integer recordId;
    private Integer imgId;
    private String depthOutputPath;
    private Integer nearThreshold;
    private Integer farThreshold;
    private Double maskOpacity;
    private Date analyzeTime;

    public DepthRecord() {
    }

    public Integer getRecordId() {
        return recordId;
    }

    public void setRecordId(Integer recordId) {
        this.recordId = recordId;
    }

    public Integer getImgId() {
        return imgId;
    }

    public void setImgId(Integer imgId) {
        this.imgId = imgId;
    }

    public String getDepthOutputPath() {
        return depthOutputPath;
    }

    public void setDepthOutputPath(String depthOutputPath) {
        this.depthOutputPath = depthOutputPath;
    }

    public Integer getNearThreshold() {
        return nearThreshold;
    }

    public void setNearThreshold(Integer nearThreshold) {
        this.nearThreshold = nearThreshold;
    }

    public Integer getFarThreshold() {
        return farThreshold;
    }

    public void setFarThreshold(Integer farThreshold) {
        this.farThreshold = farThreshold;
    }

    public Double getMaskOpacity() {
        return maskOpacity;
    }

    public void setMaskOpacity(Double maskOpacity) {
        this.maskOpacity = maskOpacity;
    }

    public Date getAnalyzeTime() {
        return analyzeTime;
    }

    public void setAnalyzeTime(Date analyzeTime) {
        this.analyzeTime = analyzeTime;
    }
}