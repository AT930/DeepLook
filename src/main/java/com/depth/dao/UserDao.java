package com.depth.dao;

import com.depth.bean.User;

public interface UserDao {

    User findByUsername(String username);

    int insert(User user);

    User findById(Integer uid);
}