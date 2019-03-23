package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pay.service.WeixinPayService;
import com.pinyougou.pojo.TbPayLog;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import utils.IdWorker;

import java.util.HashMap;
import java.util.Map;

/**
 * 支付控制层
 */
@RestController
@RequestMapping("/pay")
public class PayController {

    @Reference
    private WeixinPayService weixinPayService;

    @Reference(timeout = 3000)
    private OrderService orderService;

    /**
     * 返回生成二维码的链接信息
     * @return
     */
    @RequestMapping("/createNative")
    public Map createNative() {
        // 获取当前用户
        String userId = SecurityContextHolder.getContext()
                .getAuthentication().getName();

        // 从Redis中查询支付日志
        TbPayLog payLog = orderService.searchPayLogFromRedis(userId);

        // 判断支付日志是否存在
        if (payLog != null) {
//            return  weixinPayService.createNative(payLog.getOutTradeNo(),
//                    payLog.getTotalFee() + "");
            return  weixinPayService.createNative(payLog.getOutTradeNo(),
                    "1");
        } else {
            return new HashMap();
        }
    }

    /**
     * 【查询支付状态】
     *     后台轮询查询！
     *     同时要考虑用户 迟迟不肯支付导致的 程序一直循环！
     * @param out_trade_no
     * @return
     */
    @RequestMapping("/queryPayStatus")
    public Result queryPayStatus(String out_trade_no) {
        Result result = null;

        // 记录循环调用次数
        int x = 0;

        // 轮询开始
        while (true) {
            // 调用查询接口
            Map<String, String> map = weixinPayService.queryPayStatus(out_trade_no);

            if (map == null) {
                // 查询出错，一定是没有支付成功
                result = new Result(false, "支付出错");
                break;
            }

            if ("SUCCESS".equals(map.get("trade_state"))) {
                // 支付成功
                result = new Result(true, "支付成功");

                // 修改订单状态
                orderService.updateOrderStatus(out_trade_no, map.get("transaction_id"));
                break;
            }

            try {
                // 3秒 轮询
                Thread.sleep(3000);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // 为了不让循环无休止的运行，定义了一个循环变零，如果这个变量超过这个值，跳出循环
            x ++;
            if (x >= 100) {
                result =  new Result(false, "二维码超时");

                break;
            }
        }

        return result;
    }
}
