package com.pinyougou.page.service;

/**
 * 商品详细页接口
 */
public interface ItemPageService {

    /**
     * 生成商品详情页
     */
    public boolean genIteHtml(Long goodsId);

    /**
     * 删除商品详细页
     * */
    public boolean deleteItemHtml(Long[] goodsIds);
}
