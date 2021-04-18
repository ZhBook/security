package com.example.security.service.impl;

import com.example.security.mapper.UserMapper;
import com.example.security.pojo.User;
import com.example.security.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public User getUser(String username) {
        return userMapper.getUserInfoByUsername(username);
    }

    @Override
    public String getRole(String username) {
        return userMapper.getUserRoleByUsername(username);
    }

    @Override
    public int insertUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userMapper.insertUser(user);
    }

}
