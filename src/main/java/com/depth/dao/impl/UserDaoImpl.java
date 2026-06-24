package com.depth.dao.impl;

import com.depth.bean.User;
import com.depth.dao.UserDao;
import com.depth.util.JDBCUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDaoImpl implements UserDao {

    @Override
    public User findByUsername(String username) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = JDBCUtil.getConnection();
            String sql = "SELECT uid, username, password, create_time FROM tb_user WHERE username = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            rs = stmt.executeQuery();
            if (rs.next()) {
                User user = new User();
                user.setUid(rs.getInt("uid"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setCreateTime(rs.getDate("create_time"));
                return user;
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("查询用户失败", e);
        } finally {
            JDBCUtil.close(conn, stmt, rs);
        }
    }

    @Override
    public int insert(User user) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = JDBCUtil.getConnection();
            String sql = "INSERT INTO tb_user (username, password) VALUES (?, ?)";
            stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    user.setUid(rs.getInt(1));
                }
            }
            return rows;
        } catch (SQLException e) {
            throw new RuntimeException("插入用户失败", e);
        } finally {
            JDBCUtil.close(conn, stmt, rs);
        }
    }

    @Override
    public User findById(Integer uid) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = JDBCUtil.getConnection();
            String sql = "SELECT uid, username, password, create_time FROM tb_user WHERE uid = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, uid);
            rs = stmt.executeQuery();
            if (rs.next()) {
                User user = new User();
                user.setUid(rs.getInt("uid"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setCreateTime(rs.getDate("create_time"));
                return user;
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("查询用户失败", e);
        } finally {
            JDBCUtil.close(conn, stmt, rs);
        }
    }
}