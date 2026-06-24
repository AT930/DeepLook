package com.depth.service.impl;

import com.depth.bean.User;
import com.depth.dao.UserDao;
import com.depth.dao.impl.UserDaoImpl;
import com.depth.service.UserService;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class UserServiceImpl implements UserService {

    private UserDao userDao = new UserDaoImpl();

    @Override
    public User login(String username, String password) {
        User user = userDao.findByUsername(username);
        if (user != null && user.getPassword().equals(encryptPassword(password))) {
            return user;
        }
        return null;
    }

    @Override
    public boolean register(String username, String password) {
        User existing = userDao.findByUsername(username);
        if (existing != null) {
            return false;
        }
        User user = new User(username, encryptPassword(password));
        return userDao.insert(user) > 0;
    }

    @Override
    public User findById(Integer uid) {
        return userDao.findById(uid);
    }

    private String encryptPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("密码加密失败", e);
        }
    }
}