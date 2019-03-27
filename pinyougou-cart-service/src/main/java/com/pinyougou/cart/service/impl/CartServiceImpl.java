package com.pinyougou.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojogroup.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 购物车服务实现类
 */
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private TbItemMapper itemMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 添加商品到购物车
     * @param cartList
     * @param itemId
     * @param num
     * @return
     */
    @Override
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) {

        // 1. 根据商品 SKU ID 查询SKU商品信息
        TbItem item = itemMapper.selectByPrimaryKey(itemId);

        // 考虑时差操作(前台查询与后台操作操作的时差问题导致的)
        if (item == null) {
            throw new RuntimeException("商品不存在");
        }

        if (!"1".equals(item.getStatus())) {
            throw new RuntimeException("商品状态无效");
        }

        // 2. 获取商家ID
        String sellerId = item.getSellerId();

        // 3. 根据商家ID判断购物车列表中是否存在该商家的购物车
        Cart cart = searchCartBySellerId(cartList, sellerId);

        // 4. 如果购物车列表中不存在该商家的购物车
        if (cart == null) {

            // 4.1 新建购物车对象
            cart = new Cart();
            cart.setSellerId(sellerId);
            cart.setSellerName(item.getSeller());

            // 4.2 新建商品明细
            TbOrderItem orderItem = createOrderItem(item, num);

            // 4.3 购物车明细，商品明细列表容器
            List<TbOrderItem> orderItemList = new ArrayList<>();

            // 4.4 组合
            orderItemList.add(orderItem);
            cart.setOrderItemList(orderItemList);

            // 4.5 将新建的购物车对象添加到购物车列表
            cartList.add(cart);
        } else {
            // 5. 如果购物车列表中存在该商家的购物车
            // 查询购物车明细表中是否存在该商品
            TbOrderItem orderItem = searchOrderItemByItemId(cart.getOrderItemList(), itemId);

            // 5.1 如果没有，新增购物车明细
            if (orderItem == null) {
                orderItem = createOrderItem(item, num);
                cart.getOrderItemList().add(orderItem);
            } else {
                // 5.2 如果有，在原购物车明细上添加数量，更改金额
                orderItem.setNum(orderItem.getNum() + num);

                if (orderItem.getNum() > 0) {
                    orderItem.setTotalFee(new BigDecimal(
                            orderItem.getNum() * orderItem.getPrice().doubleValue()));
                }

//                // 如果数量操作后小于等于0，则删除
//                if (orderItem.getNum() <= 0) {
//                    // 移除商品明细
//                    cart.getOrderItemList().remove(orderItem);
//                }
//
//                // 如果商品明细列表为 0， 则将cart移除
//                if (cart.getOrderItemList().size() == 0) {
//                    cartList.remove(cart);
//                }
            }

            // 如果数量操作后小于等于0，则删除
            if (orderItem.getNum() <= 0) {
                // 移除商品明细
                cart.getOrderItemList().remove(orderItem);
            }

            // 如果商品明细列表为 0， 则将cart移除
            if (cart.getOrderItemList().size() == 0) {
                cartList.remove(cart);
            }
        }

        return cartList;
    }

    /**
     * 合并购物车
     * @param cartList1
     * @param cartList2
     * @return
     */
    @Override
    public List<Cart> mergeCartList(List<Cart> cartList1, List<Cart> cartList2) {

        // 1. 优化1
        if (cartList1 == null || cartList1.size() == 0) {
            return cartList2;
        }

        if (cartList2 == null || cartList2.size() == 0) {
            return cartList1;
        }

        // 2. 优化
        List<Cart> bigCart = cartList1.size() > cartList2.size() ? cartList1 : cartList2;
        List<Cart> smallCart = cartList1.size() < cartList2.size() ? cartList1 : cartList2;

        // 循环遍历小的购物车列表
        for (Cart sellerS : smallCart) {

            // 标记大购物车中是否有小的购物车
            boolean haveSeller = false;

            // 循环遍历大的购物车
            for (Cart sellerB : bigCart) {

                // 如果大购物车 中存在 小购物车 的商家购物车；
                if (sellerB.getSellerId().equals(sellerS.getSellerId())) {

                    // 标记含有该小购物车
                    haveSeller = true;

                    // 遍历 小购物车列表商家中 的商品列表
                    for (TbOrderItem orderS : sellerS.getOrderItemList()) {

                        // 标记 大购物车列表的商家 是否含有该商品
                        boolean haveOrder = false;

                        // 遍历 大购物车列表商家 中的商品
                        for (TbOrderItem orderB : sellerB.getOrderItemList()) {
                            // 如果另个列表中含有相同商品，将两个商品合并
                            if (orderB.getItemId().equals(orderS.getItemId())) {
                                haveOrder = true;
                                orderB.setNum(orderB.getNum() + orderS.getNum());
                                orderB.setTotalFee(
                                        new BigDecimal(orderB.getTotalFee().doubleValue() +
                                                orderS.getTotalFee().doubleValue())
                                        );
                            }
                        }

                        // 大购物车列表的 商家下的商品遍历完后，发现没有 小购物车列表 的 商家 的当前 商品，将
                        if (!haveOrder) {
                            sellerB.getOrderItemList().add(orderS);
                        }
                    }

                    // 不需要继续判断，因为已经有了相同的商家，不会存在一个列表中存在相同的商家
                    continue;
                }
            }

            // 大购物车遍历完之后，如果发现不存在该小购物车
            if (!haveSeller) {
                // 如果不存在，直接将小购物车加入大购物车
                bigCart.add(sellerS);
            }
        }

        return bigCart;
    }

    /**
     * 从Redis中查询购物车
     * @param username
     * @return
     */
    @Override
    public List<Cart> findCartListFromRedis(String username) {

        List<Cart> cartList = (List<Cart>)redisTemplate.boundHashOps("cartList").get(username);
        if (cartList == null) {
            cartList = new ArrayList<>();
        }
        return cartList;
    }

    /**
     * 将购物车保存到redis中
     * @param username
     * @param cartList
     */
    @Override
    public void saveCartListToRedis(String username, List<Cart> cartList) {
        redisTemplate.boundHashOps("cartList").put(username, cartList);
    }



    /**
     * 根据商家ID查询购物车对象
     * @param cartList
     * @param sellerId
     * @return
     */
    private Cart searchCartBySellerId(List<Cart> cartList, String sellerId) {

        for (Cart cart : cartList) {
            if (cart.getSellerId().equals(sellerId)) {
                return cart;
            }
        }
        return null;
    }

    /**
     * 创建订单明细
     * @param item
     * @param num
     * @return
     */
    private TbOrderItem createOrderItem(TbItem item, Integer num) {
        if (num <= 0) {
            throw new RuntimeException("数量非法");
        }

        // 封装商品明细
        TbOrderItem orderItem = new TbOrderItem();
        orderItem.setGoodsId(item.getGoodsId());
        orderItem.setItemId(item.getId());
        orderItem.setNum(num);
        orderItem.setPicPath(item.getImage());
        orderItem.setPrice(item.getPrice());
        orderItem.setSellerId(item.getSellerId());
        orderItem.setTitle(item.getTitle());
        orderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue() * num));

        return orderItem;
    }

    /**
     * 根据商品id 查询商品明细
     * @param orderItemList
     * @param itemId
     * @return
     */
    private TbOrderItem searchOrderItemByItemId(List<TbOrderItem> orderItemList, Long itemId) {
        for (TbOrderItem orderItem : orderItemList) {
            if (orderItem.getItemId().longValue() == itemId.longValue()) {
                return orderItem;
            }
        }

        return null;
    }

}
