package com.depth.dao.impl;

import com.depth.bean.DepthRecord;
import com.depth.dao.DepthRecordDao;
import com.depth.util.JDBCUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DepthRecordDaoImpl implements DepthRecordDao {

    @Override
    public int insert(DepthRecord depthRecord) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = JDBCUtil.getConnection();
            String sql = "INSERT INTO tb_depth_record (img_id, depth_output_path, near_threshold, far_threshold, mask_opacity) VALUES (?, ?, ?, ?, ?)";
            stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, depthRecord.getImgId());
            stmt.setString(2, depthRecord.getDepthOutputPath());
            stmt.setInt(3, depthRecord.getNearThreshold());
            stmt.setInt(4, depthRecord.getFarThreshold());
            stmt.setDouble(5, depthRecord.getMaskOpacity());
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    depthRecord.setRecordId(rs.getInt(1));
                }
            }
            return rows;
        } catch (SQLException e) {
            throw new RuntimeException("插入景深记录失败", e);
        } finally {
            JDBCUtil.close(conn, stmt, rs);
        }
    }

    @Override
    public DepthRecord findByImgId(Integer imgId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = JDBCUtil.getConnection();
            String sql = "SELECT record_id, img_id, depth_output_path, near_threshold, far_threshold, mask_opacity, analyze_time FROM tb_depth_record WHERE img_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, imgId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                DepthRecord record = new DepthRecord();
                record.setRecordId(rs.getInt("record_id"));
                record.setImgId(rs.getInt("img_id"));
                record.setDepthOutputPath(rs.getString("depth_output_path"));
                record.setNearThreshold(rs.getInt("near_threshold"));
                record.setFarThreshold(rs.getInt("far_threshold"));
                record.setMaskOpacity(rs.getDouble("mask_opacity"));
                record.setAnalyzeTime(rs.getDate("analyze_time"));
                return record;
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("查询景深记录失败", e);
        } finally {
            JDBCUtil.close(conn, stmt, rs);
        }
    }

    @Override
    public List<DepthRecord> findByUid(Integer uid, Integer start, Integer pageSize) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = JDBCUtil.getConnection();
            String sql = "SELECT dr.record_id, dr.img_id, dr.depth_output_path, dr.near_threshold, dr.far_threshold, dr.mask_opacity, dr.analyze_time " +
                    "FROM tb_depth_record dr JOIN tb_image_file img ON dr.img_id = img.img_id WHERE img.uid = ? ORDER BY dr.analyze_time DESC LIMIT ?, ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, uid);
            stmt.setInt(2, start);
            stmt.setInt(3, pageSize);
            rs = stmt.executeQuery();
            List<DepthRecord> list = new ArrayList<>();
            while (rs.next()) {
                DepthRecord record = new DepthRecord();
                record.setRecordId(rs.getInt("record_id"));
                record.setImgId(rs.getInt("img_id"));
                record.setDepthOutputPath(rs.getString("depth_output_path"));
                record.setNearThreshold(rs.getInt("near_threshold"));
                record.setFarThreshold(rs.getInt("far_threshold"));
                record.setMaskOpacity(rs.getDouble("mask_opacity"));
                record.setAnalyzeTime(rs.getDate("analyze_time"));
                list.add(record);
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("查询景深记录列表失败", e);
        } finally {
            JDBCUtil.close(conn, stmt, rs);
        }
    }

    @Override
    public int deleteByImgId(Integer imgId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = JDBCUtil.getConnection();
            String sql = "DELETE FROM tb_depth_record WHERE img_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, imgId);
            return stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("删除景深记录失败", e);
        } finally {
            JDBCUtil.close(conn, stmt);
        }
    }

    @Override
    public int update(DepthRecord depthRecord) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = JDBCUtil.getConnection();
            String sql = "UPDATE tb_depth_record SET depth_output_path = ?, near_threshold = ?, far_threshold = ?, mask_opacity = ? WHERE record_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, depthRecord.getDepthOutputPath());
            stmt.setInt(2, depthRecord.getNearThreshold());
            stmt.setInt(3, depthRecord.getFarThreshold());
            stmt.setDouble(4, depthRecord.getMaskOpacity());
            stmt.setInt(5, depthRecord.getRecordId());
            return stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("更新景深记录失败", e);
        } finally {
            JDBCUtil.close(conn, stmt);
        }
    }

    @Override
    public int countByUid(Integer uid) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = JDBCUtil.getConnection();
            String sql = "SELECT COUNT(*) FROM tb_depth_record dr JOIN tb_image_file img ON dr.img_id = img.img_id WHERE img.uid = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, uid);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException("统计景深记录失败", e);
        } finally {
            JDBCUtil.close(conn, stmt, rs);
        }
    }
}