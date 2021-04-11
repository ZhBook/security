package com.example.security.mapper;

import com.example.security.pojo.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;

/**
 * FileName:com.example.security.mapper
 * Author:70968
 * Date:2021-04-11 11:34
 * History
 */
@Component
@Mapper
public interface UserMapper {
    @Select("select * from user where username = #{username}")
    User getUserInfoByUsername(String username);
}

