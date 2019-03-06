package com.pinyougou.service;

import com.pinyougou.pojo.TbSeller;
import com.pinyougou.sellergoods.service.SellerService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;

public class UserDetailServiceImpl implements UserDetailsService {

    /**
     controller包下的Controller配置了dubbox服务
     此包没有配置，直接使用 @Reference 是无法生成 代理对象，
     由于此类特殊，可以使用 set 注入方法配合 dubbox 进行远程配置注入
     可以在spring-security.xml为此类配置一个 dubbox 服务 ！
     */
    private SellerService sellerService;

    public void setSellerService(SellerService sellerService) {
        this.sellerService = sellerService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // 构建角色列表
        List<GrantedAuthority> grantedAuths = new ArrayList<GrantedAuthority>();
        grantedAuths.add(new SimpleGrantedAuthority("ROLE_SELLER"));

        // 获取商家列表
        TbSeller seller = sellerService.findOne(username);

        if (seller != null && "1".equals(seller.getStatus())) {
            return new User(username, seller.getPassword(), grantedAuths);
        }

        return null;
    }
}
