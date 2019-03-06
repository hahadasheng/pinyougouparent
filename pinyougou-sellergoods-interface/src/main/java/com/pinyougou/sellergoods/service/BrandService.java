package com.pinyougou.sellergoods.service;

import com.pinyougou.pojo.TbBrand;
import entity.PageResult;
import entity.Result;

import java.util.List;
import java.util.Map;

/**
 * 品牌接口*/
public interface BrandService {

    public List<TbBrand> findAll();

    /** 分页查询品牌列表 */
    public PageResult findBrandPage(Integer pageNum, Integer pageSize);

    /** 分页调条件查询品牌列表 */
    public PageResult findBrandPage(TbBrand tbBrand, Integer pageNum, Integer pageSize);

    /** 新增品牌 */
    public Integer addBrand(TbBrand tbBrand);

    /** 根据id查询一个品牌 */
    public TbBrand findBrandById(Long id);

    /** 修改品牌 */
    public Integer updateBrand(TbBrand tbBrand);

    /** 删除品牌 */
    public Integer deleteBrandByIds(Long[] ids);

    /** 品牌下拉框数据 */
    public List<Map> selectOptionList();
}
