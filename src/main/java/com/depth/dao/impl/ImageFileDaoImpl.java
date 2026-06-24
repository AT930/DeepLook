package com.depth.dao.impl;

import com.depth.bean.ImageFile;
import com.depth.dao.ImageFileDao;
import com.depth.util.JDBCUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ImageFileDaoImpl implements ImageFileDao {

    @Override
    public int insert(ImageFile imageFile) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = JDBCUtil.getConnection();
            String sql = "INSERT INTO tb_image_file (uid, origin_name, save_name, file_path, file_size) VALUES (?, ?, ?, ?, ?)";
            stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, imageFile.getUid());
            stmt.setString(2, imageFile.getOriginName());
            stmt.setString(3, imageFile.getSaveName());
            stmt.setString(4, imageFile.getFilePath());
            stmt.setInt(5, imageFile.getFileSize());
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    imageFile.setImgId(rs.getInt(1));
                }
            }
            return rows;
        } catch (SQLException e) {
            throw new RuntimeException("插入图片记录失败", e);
        } finally {
            JDBCUtil.close(conn, stmt, rs);
        }
    }

    @Override
    public ImageFile findById(Integer imgId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = JDBCUtil.getConnection();
            String sql = "SELECT img_id, uid, origin_name, save_name, file_path, file_size, upload_time FROM tb_image_file WHERE img_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, imgId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                ImageFile imageFile = new ImageFile();
                imageFile.setImgId(rs.getInt("img_id"));
                imageFile.setUid(rs.getInt("uid"));
                imageFile.setOriginName(rs.getString("origin_name"));
                imageFile.setSaveName(rs.getString("save_name"));
                imageFile.setFilePath(rs.getString("file_path"));
                imageFile.setFileSize(rs.getInt("file_size"));
                imageFile.setUploadTime(rs.getDate("upload_time"));
                return imageFile;
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("查询图片记录失败", e);
        } finally {
            JDBCUtil.close(conn, stmt, rs);
        }
    }

    @Override
    public List<ImageFile> findByUid(Integer uid, Integer start, Integer pageSize) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = JDBCUtil.getConnection();
            String sql = "SELECT img_id, uid, origin_name, save_name, file_path, file_size, upload_time FROM tb_image_file WHERE uid = ? ORDER BY upload_time DESC LIMIT ?, ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, uid);
            stmt.setInt(2, start);
            stmt.setInt(3, pageSize);
            rs = stmt.executeQuery();
            List<ImageFile> list = new ArrayList<>();
            while (rs.next()) {
                ImageFile imageFile = new ImageFile();
                imageFile.setImgId(rs.getInt("img_id"));
                imageFile.setUid(rs.getInt("uid"));
                imageFile.setOriginName(rs.getString("origin_name"));
                imageFile.setSaveName(rs.getString("save_name"));
                imageFile.setFilePath(rs.getString("file_path"));
                imageFile.setFileSize(rs.getInt("file_size"));
                imageFile.setUploadTime(rs.getDate("upload_time"));
                list.add(imageFile);
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("查询图片记录列表失败", e);
        } finally {
            JDBCUtil.close(conn, stmt, rs);
        }
    }

    @Override
    public int countByUid(Integer uid) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = JDBCUtil.getConnection();
            String sql = "SELECT COUNT(*) FROM tb_image_file WHERE uid = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, uid);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException("统计图片数量失败", e);
        } finally {
            JDBCUtil.close(conn, stmt, rs);
        }
    }

    @Override
    public int deleteById(Integer imgId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = JDBCUtil.getConnection();
            String sql = "DELETE FROM tb_image_file WHERE img_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, imgId);
            return stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("删除图片记录失败", e);
        } finally {
            JDBCUtil.close(conn, stmt);
        }
    }
}