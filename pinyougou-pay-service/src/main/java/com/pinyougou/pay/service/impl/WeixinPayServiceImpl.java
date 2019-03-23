package com.pinyougou.pay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import com.pinyougou.pay.service.WeixinPayService;
import org.springframework.beans.factory.annotation.Value;
import utils.HttpClient;

import java.util.HashMap;
import java.util.Map;


/**
 * 微信支付
 */
@Service
public class WeixinPayServiceImpl implements WeixinPayService {

    @Value("${appid}")
    private String appid;

    @Value("${partner}")
    private String partner;

    @Value("${partnerkey}")
    private String partnerKey;

    /**
     * 生成支付二维码链接
     * @param out_trade_no
     * @param total_fee
     * @return
     */
    @Override
    public Map createNative(String out_trade_no, String total_fee) {

        // 1. 创建参数
        Map<String, String> param = new HashMap<>();

        // 公众号
        param.put("appid", appid);

        // 商户号
        param.put("mch_id", partner);

        // 随机字符串
        param.put("nonce_str", WXPayUtil.generateNonceStr());

        // 商品描述
        param.put("body", "pinyougou");

        // 商户订单号
        param.put("out_trade_no", out_trade_no);

        // 总金额(分)
        param.put("total_fee", total_fee);

        // IP
        param.put("spbill_create_ip", "127.0.0.1");

        // 回调地址
        param.put("notify_url", "http://www.lingting.com");

        // 交易类型
        param.put("trade_type", "NATIVE");

        try {
            // 2. 生成要发送的xml
            String xmlParam = WXPayUtil.generateSignedXml(param, partnerKey);
            System.out.println(xmlParam);

            HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
            client.setHttps(true);
            client.setXmlParam(xmlParam);
            client.post();

            // 3. 获得结果
            String result = client.getContent();

            // 封装返回地址
            Map<String, String> resultMap = WXPayUtil.xmlToMap(result);
            System.out.println("返回的结果： \n" + resultMap);

            Map<String, String> map = new HashMap<>();

            // 微信返回的信息
            map.put("return_msg", resultMap.get("return_msg"));

            // 支付地址
            map.put("code_url", resultMap.get("code_url"));

            // 总金额
            map.put("total_fee", total_fee);

            // 订单号
            map.put("out_trade_no", out_trade_no);

            return map;

        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap();
        }
    }

    /**
     * 查询支付状态：每隔3秒轮询
     * @param out_trade_no
     * @return
     */
    @Override
    public Map queryPayStatus(String out_trade_no) {

        Map param = new HashMap();
        // 公众账号ID
        param.put("appid", appid);

        // 商户号
        param.put("mch_id", partner);

        // 订单号
        param.put("out_trade_no", out_trade_no);

        // 随机字符串
        param.put("nonce_str", WXPayUtil.generateNonceStr());

        String url = "https://api.mch.weixin.qq.com/pay/orderquery";

        try {

            String xmlParam = WXPayUtil.generateSignedXml(param, partnerKey);
            HttpClient client = new HttpClient(url);

            client.setHttps(true);
            client.setXmlParam(xmlParam);
            client.post();

            String result = client.getContent();

            Map<String, String> map = WXPayUtil.xmlToMap(result);
            System.out.println(map);

            return map;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
