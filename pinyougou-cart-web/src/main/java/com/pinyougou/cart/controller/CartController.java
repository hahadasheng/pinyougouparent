package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.pojogroup.Cart;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import utils.CookieUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Reference(timeout = 6000)
    private CartService cartService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private HttpServletResponse response;

    /**
     * 获取购物车列表
     * @return
     */
    @RequestMapping("/findCartList")
    public List<Cart> findCarList() {

        // 获取登录人账号，判断是否登陆
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        String cartListString = CookieUtil.getCookieValue(request, "cartList", "UTF-8");

        // 初始化时购物车为空！提高容错性
        if (cartListString == null || "".equals(cartListString)) {
            cartListString = "[]";
        }

        List<Cart> cartListFromCookie = JSON.parseArray(cartListString, Cart.class);

        if ("anonymousUser".equals(username)) {
            // 当前访问没有登陆
            return cartListFromCookie;

        } else {
            // 已经登陆; 从Redis中获取
            List<Cart> cartListFromRedis = cartService.findCartListFromRedis(username);

            // 如果本地存在购物车; 需要合并购物车
            if (cartListFromCookie.size() > 0) {
                // 合并购物车
                cartListFromRedis = cartService.mergeCartList(cartListFromRedis, cartListFromCookie);

                // 清除本次cookie的数据
                CookieUtil.deleteCookie(request, response, "cartList");

                // 将合并后的数据放入redis;
                cartService.saveCartListToRedis(username, cartListFromRedis);
            }
            return cartListFromRedis;
        }
    }

    /**
     * 添加商品到购物车
     * @param itemId
     * @param num
     * @return
     */
    @RequestMapping("/addGoodsToCartList")
    private Result addGoodsToCartList(Long itemId, Integer num) {
        // 获取登录人账号，判断是否登陆
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        try {
            // 获取购物车列表
            List<Cart> carList = findCarList();

            // 将商品添加到购物车
            carList = cartService.addGoodsToCartList(carList, itemId, num);

            if ("anonymousUser".equals(username)) {
                // 将购物车存入Cookie
                CookieUtil.setCookie(request, response,
                        "cartList", JSON.toJSONString(carList), 3600 * 24, "utf-8");

            } else {
                // 将数据存放到Redis缓存中
                cartService.saveCartListToRedis(username, carList);

            }

            return new Result(true, "添加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "添加失败");
        }
    }
}