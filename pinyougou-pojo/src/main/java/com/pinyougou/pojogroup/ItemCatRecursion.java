package com.pinyougou.pojogroup;

import com.pinyougou.pojo.TbItemCat;

import java.io.Serializable;
import java.util.List;

/** 封装产品分类表，递归查询！ */
public class ItemCatRecursion implements Serializable {

    // 记录父节点的数据
    private TbItemCat itemCat;
    // 记录子节点的集合
    private List<ItemCatRecursion> childern;

    public ItemCatRecursion() {
    }

    public ItemCatRecursion(TbItemCat itemCat, List<ItemCatRecursion> childern) {
        this.itemCat = itemCat;
        this.childern = childern;
    }

    public TbItemCat getItemCat() {
        return itemCat;
    }

    public void setItemCat(TbItemCat itemCat) {
        this.itemCat = itemCat;
    }

    public List<ItemCatRecursion> getChildern() {
        return childern;
    }

    public void setChildern(List<ItemCatRecursion> childern) {
        this.childern = childern;
    }

    @Override
    public String toString() {
        return "ItemCatRecursion{" +
                "itemCat=" + itemCat +
                ", childern=" + childern +
                '}';
    }
}
