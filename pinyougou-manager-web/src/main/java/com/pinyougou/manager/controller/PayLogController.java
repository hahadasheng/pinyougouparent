package com.pinyougou.manager.controller;

import com.pinyougou.pojo.TbBrand;
import entity.PageResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 查询支付日志
 */
@RestController("/log")
public class PayLogController {

    /** 分页调条件查询 订单列表 */
    @RequestMapping("/findLogPageOnCondition")
    public PageResult findBrandPageOnCondition(
            @RequestBody TbBrand tbBrand,
            @RequestParam(name = "page", required = true, defaultValue = "1")Integer pageNum,
            @RequestParam(name = "size", required = true, defaultValue = "10")Integer pageSize) {

        return null;
        // return brandService.findBrandPage(tbBrand, pageNum, pageSize);
    }

}
