package com.pinyougou.shop.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/login")
public class LoginController {

    /**
     * 获取登陆的用户名
     */
    @RequestMapping("/name")
    public Map<String, String> getUserName() {
        String loginName = SecurityContextHolder.getContext()
                .getAuthentication().getName();

        Map<String, String> map = new HashMap<String, String>();
        map.put("loginName", loginName);
        return map;
    }
}
