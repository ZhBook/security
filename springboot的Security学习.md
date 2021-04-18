# SpringBoot Security学习

## 一、创建环境

### 1.创建SpringBoot项目，导入依赖

![](C:\Users\70968\Desktop\微信截图_20210411111739.png)

如果这里选了security的依赖，在访问时要先通过security的默认验证，用户名user，密码可以在控制台找到。

记得添加mybatis的依赖哦

```java
 <dependency>
            <groupId>org.mybatis.spring.boot</groupId>
            <artifactId>mybatis-spring-boot-starter</artifactId>
        </dependency>
```



### 2.创建数据库

#### 2.1建库

```mysql
create table user
(
	id int auto_increment,
	username varchar(255) null,
	password varchar(255) null,
	role varchar(255) null,
	constraint user_pk
		primary key (id)
);
```

![](C:\Users\70968\Desktop\微信截图_20210411112820.png)

#### 2.3添加配置信息

```java
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/security
    username: root
    password: 123
logging:
  level: 
    com.example.security.mapper: debug
```

### 3.创建基础类

#### 3.1 创建User

```java
package com.example.security.pojo;

import lombok.Data;

/**
 * FileName:com.example.security.domain
 * Author:70968
 * Date:2021-04-11 11:29
 * History
 */
@Data
public class User {
    private int id;
    private String username;
    private String password;
    private String role;
}

```

#### 3.2 创建mapper

```java
package com.example.security.mapper;

import com.example.security.pojo.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

/**
 * FileName:com.example.security.mapper
 * Author:70968
 * Date:2021-04-11 11:34
 * History
 */
@Mapper
@Repository
public interface UserMapper {
    @Select("select * from user where username = #{username}")
    User getUserInfoByUsername(String username);
    
    @Select("select role from user where username= #{username}")
    String getUserRoleByUsername(String username);
}

```



#### 3.3 创建service

```java
package com.example.security.service;

import com.example.security.mapper.UserMapper;
import com.example.security.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * FileName:com.example.security.service
 * Author:70968
 * Date:2021-04-11 11:43
 * History
 */
public class UserService {
    @Autowired
    private UserMapper userMapper;

    public User getUser(String username){
        return userMapper.getUserInfoByUsername(username);
    }
    
    public String getRole(String username) {
        return userMapper.getUserRoleByUsername(username);
    }

}

```

#### 3.3 创建Controller

```java
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

    @GetMapping("getUser")
    public User getUser(@RequestParam String username){
        return userService.getUser(username);
    }
}

```

### 4.环境测试

访问 http://localhost:8080/get-user?username=admin

![image-20210418102538126](C:\Users\70968\AppData\Roaming\Typora\typora-user-images\image-20210418102538126.png)

## 二、创建security配置类

测试环境完成后，接下来需要配置security，实现数据库验证

### 1.CustomUserDetailsService类

> 实现从数据库读取用户信息需要实现UserDetailService接口，并且重写他的方法loadUserByUsername()

```java
package com.example.security.service;

import com.example.security.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CustomUserDetailsService implements UserDetailsService {
    @Autowired
    private UserService userService;

    /**
     * 需新建配置类注册一个指定的加密方式Bean，或在下一步Security配置类中注册指定
     */
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        //通过用户名从数据库获取用户信息
        User user = userService.getUser(s);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在");
        }

        // 得到用户角色
        String role = userService.getRole(s);

        // 角色集合
        List<GrantedAuthority> authorities = new ArrayList<>();
        // 角色必须以`ROLE_`开头，数据库中没有，则在这里加
        authorities.add(new SimpleGrantedAuthority("ROLE_" + role));

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                // 因为数据库是明文，所以这里需加密密码
                passwordEncoder.encode(user.getPassword()),
                authorities
        );
    }
}

```

### 2.MyUserDatailService

> 该类用于获取符合security要求的用户信息

```java
package com.example.security.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class MyUserDatailService implements UserDetailsService {
    @Autowired
    private UserService userService;
    /**
     * 需新建配置类注册一个指定的加密方式Bean，或在下一步Security配置类中注册指定
     */
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        com.example.security.pojo.User user = userService.getUser(s);
        // 得到用户角色
        String role = userService.getRole(s);

        // 角色集合
        List<GrantedAuthority> authorities = new ArrayList<>();
        // 角色必须以`ROLE_`开头，数据库中没有，则在这里加
        authorities.add(new SimpleGrantedAuthority("ROLE_" + role));

        return new User(
                user.getUsername(),
                // 数据库密码已加密，不用再加密
                passwordEncoder.encode(user.getPassword()),
                authorities
        );
    }
}
```

### 3.WebSecurityConfig

> 创建Security的配置类WebSecurityConfig继承WebSecurityConfigurerAdapter，并重写configure(auth)方法

```java
package com.example.security.config;

import com.example.security.service.MyUserDatailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true) // 开启方法级安全验证
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    private MyUserDatailService userDetailService;

    /**
     * 指定加密方式
     */
    @Bean
    public PasswordEncoder passwordEncoder(){
        // 使用BCrypt加密密码
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth
                // 从数据库读取的用户进行身份认证
                .userDetailsService(userDetailService)
                .passwordEncoder(passwordEncoder());
    }
}
```

完成以上三个类后，就可以重启启动项目，通过数据库的用户名和密码进行验证了。

## 三、角色访问控制

完成通过数据库来限制用户登录后，进行角色限制访问

### 1.开启角色访问

开启方法：在WebSecurityConfig添加注解：

```java
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true) // 开启方法级安全验证
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
..
}
```

### 2.修改Controller

>添加角色访问路径

```java
package com.example.security.controller;

import com.example.security.pojo.User;
import com.example.security.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @GetMapping("/get-user")
    public User getUser(@RequestParam String username){
        return userService.getUser(username);
    }

    @PreAuthorize("hasAnyRole('user')") // 只能user角色才能访问该方法
    @GetMapping("/user")
    public String user(){
        return "user角色访问";
    }

    @PreAuthorize("hasAnyRole('admin')") // 只能admin角色才能访问该方法
    @GetMapping("/admin")
    public String admin(){
        return "admin角色访问";
    }
}
```

完成后重启测试

## 四、密码加密保存

在实际开发中用户密码是需要加密的。

### 1.简单的加密

#### 1.1向mapper中添加sql

```java
package com.example.security.mapper;

import com.example.security.pojo.User;
import org.apache.ibatis.annotations.Insert;
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

    @Select("select role from user where username= #{username}")
    String getUserRoleByUsername(String username);

    @Insert("insert into user(username,password,role) value (#{username},#{password},#{role})")
    int insertUser(User user);
}
```

#### 1.2向service中添加

```java
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
```

1.3添加添加用户的Controller

```java
package com.example.security.controller;

import com.example.security.pojo.User;
import com.example.security.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/get-user")
    public User getUser(@RequestParam String username){
        return userService.getUser(username);
    }

    @PreAuthorize("hasAnyRole('user')") // 只能user角色才能访问该方法
    @GetMapping("/user")
    public String user(){
        return "user角色访问";
    }

    @PreAuthorize("hasAnyRole('admin')") // 只能admin角色才能访问该方法
    @GetMapping("/admin")
    public String admin(){
        return "admin角色访问";
    }

    @PostMapping("/add-user")
    public int addUser(@RequestBody User user){
        return userService.insertUser(user);
    }
}
```

重启测试，用postman发送请求

![image-20210418114151670](C:\Users\70968\AppData\Roaming\Typora\typora-user-images\image-20210418114151670.png)

显示没有权限。需要重新修改WebSecurityConfig配置类

![image-20210418114753243](C:\Users\70968\AppData\Roaming\Typora\typora-user-images\image-20210418114753243.png)

重启项目发送请求

![image-20210418115206725](C:\Users\70968\AppData\Roaming\Typora\typora-user-images\image-20210418115206725.png)

### 2.使用加密密码登录

#### 2.1修改MyUserDetailService

> 删掉加密

```java
package com.example.security.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class MyUserDetailService implements UserDetailsService {
    @Autowired
    private UserService userService;
    /**
     * 需新建配置类注册一个指定的加密方式Bean，或在下一步Security配置类中注册指定
     */
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        com.example.security.pojo.User user = userService.getUser(s);
        // 得到用户角色
        String role = userService.getRole(s);

        // 角色集合
        List<GrantedAuthority> authorities = new ArrayList<>();
        // 角色必须以`ROLE_`开头，数据库中没有，则在这里加
        authorities.add(new SimpleGrantedAuthority("ROLE_" + role));

        return new User(
                user.getUsername(),
                // 数据库密码已加密，不用再加密
                user.getPassword(),
                authorities
        );
    }
}
```

## 五、修改密码

