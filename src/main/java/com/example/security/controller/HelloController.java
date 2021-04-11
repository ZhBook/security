package com.example.security.controller;

import com.example.security.pojo.User;
import com.example.security.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * FileName:com.example.security.controller
 * Author:70968
 * Date:2021-04-11 11:46
 * History
 */
@RestController
public class HelloController {
    @Autowired
    private UserService userService;

    @GetMapping("/getUser")
    public User getUser(@RequestParam String username){
        return userService.getUser(username);
    }
}
