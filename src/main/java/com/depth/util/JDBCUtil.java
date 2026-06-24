package com.depth.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

public class JDBCUtil {

    private static String driver;
    private static String url;
    private static String username;
    private static String password;

    static {
        try {
            Properties props = new Properties();
            props.load(JDBCUtil.class.getClassLoader().getResourceAsStream("db.properties"));
            driver = props.getProperty("jdbc.driver");
            url = props.getProperty("jdbc.url");
            username = props.getProperty("jdbc.username");
            password = props.getProperty("jdbc.password");
            Class.forName(driver);
        } catch (Exception e) {
            throw new RuntimeException("加载数据库配置失败", e);
        }
    }

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            throw new RuntimeException("获取数据库连接失败", e);
        }
    }

    public static void close(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void close(Connection conn, PreparedStatement stmt) {
        close(stmt);
        close(conn);
    }

    public static void close(Connection conn, PreparedStatement stmt, ResultSet rs) {
        close(rs);
        close(stmt);
        close(conn);
    }

    public static void close(PreparedStatement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void close(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}