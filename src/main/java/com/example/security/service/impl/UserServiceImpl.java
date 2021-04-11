package com.example.security.service.impl;

import com.example.security.mapper.UserMapper;
import com.example.security.pojo.User;
import com.example.security.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * FileName:com.example.security.service.impl
 * Author:70968
 * Date:2021-04-11 12:21
 * History
 */
@Service("userService")
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;

    @Override
    public User getUser(String username) {
        return userMapper.getUserInfoByUsername(username);
    }
}
