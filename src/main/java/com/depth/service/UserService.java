package com.depth.service;

import com.depth.bean.User;

public interface UserService {

    User login(String username, String password);

    boolean register(String username, String password);

    User findById(Integer uid);
}