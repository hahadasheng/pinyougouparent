package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbBrandMapper;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.pojo.TbBrandExample;
import com.pinyougou.pojo.TbBrandExample.Criteria;
import com.pinyougou.sellergoods.service.BrandService;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

/** 远程注入 */
@Service
public class BrandServiceImpl implements BrandService {

    @Autowired
    private TbBrandMapper brandMapper;

    @Override
    public List<TbBrand> findAll() {
        return brandMapper.selectByExample(null);
    }

    /** 分页查询品牌列表 */
    @Override
    public PageResult findBrandPage(Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        // Page<T> 应该实现了 List<T> 接口 实现了 ArrayList 接口
        // 我推测，当使用了PageHelper，在切面时进行了 封装实现 ！
        Page<TbBrand> page = (Page<TbBrand>) brandMapper.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /** 分页条件查询品牌列表 */
    @Override
    public PageResult findBrandPage(TbBrand tbBrand, Integer pageNum, Integer pageSize) {

        PageHelper.startPage(pageNum, pageSize);

        // 条件查询封装类,框架底层需要封装的example配动态拼接 sql
        TbBrandExample example = new TbBrandExample();
        Criteria criteria = example.createCriteria();
        if (tbBrand != null) {
            if (tbBrand.getName() != null && tbBrand.getName().length() > 0) {
                criteria.andNameLike("%" + tbBrand.getName() + "%");
            }

            if (tbBrand.getFirstChar() != null && tbBrand.getFirstChar().length() > 0) {
                criteria.andFirstCharEqualTo(tbBrand.getFirstChar());
            }
        }

        Page<TbBrand> page = (Page<TbBrand>)brandMapper.selectByExample(example);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /** 新增品牌 */
    @Override
    public Integer addBrand(TbBrand tbBrand) {
        return brandMapper.insert(tbBrand);
    }

    /** 根据id查询一个品牌 */
    @Override
    public TbBrand findBrandById(Long id) {
        return brandMapper.selectByPrimaryKey(id);
    }

    /** 修改品牌 */
    @Override
    public Integer updateBrand(TbBrand tbBrand) {
        return brandMapper.updateByPrimaryKey(tbBrand);
    }

    /** 删除品牌 */
    @Override
    public Integer deleteBrandByIds(Long[] ids) {
        Integer res = 0;
        for (Long id : ids) {
            res += brandMapper.deleteByPrimaryKey(id);
        }
        return res;
    }

    /** 品牌下拉框数据 */
    @Override
    public List<Map> selectOptionList() {
        return brandMapper.selectOptionList();
    }
}
