package com.example.security.service;

import com.example.security.pojo.User;

/**
 * FileName:com.example.security.service
 * Author:70968
 * Date:2021-04-11 11:43
 * History
 */
public interface UserService {
    User getUser(String username);
    String getRole(String username);
    int insertUser(User user);
}
