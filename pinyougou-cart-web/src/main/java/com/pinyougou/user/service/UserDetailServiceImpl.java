package com.pinyougou.user.service;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;

/**
 * 认证类
 * 这个类的主要作用是在登陆后得到用户名，可以根据
 * 用户名查询角色或者执行一些逻辑！
 */
public class UserDetailServiceImpl implements UserDetailsService {


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // 构建角色集合;互联网网应用，角色权限很单一，不复杂！
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        // 此处，密码是一个虚壳(设计上的问题)，在执行者这话时，单点登陆系统已经认证通过了！
        return new User(username, "", authorities);
    }
}

