package com.pinyougou.sellergoods.service.impl;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.pinyougou.mapper.TbSpecificationOptionMapper;
import com.pinyougou.pojo.TbSpecificationOption;
import com.pinyougou.pojo.TbSpecificationOptionExample;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbTypeTemplateMapper;
import com.pinyougou.pojo.TbTypeTemplate;
import com.pinyougou.pojo.TbTypeTemplateExample;
import com.pinyougou.pojo.TbTypeTemplateExample.Criteria;
import com.pinyougou.sellergoods.service.TypeTemplateService;

import entity.PageResult;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class TypeTemplateServiceImpl implements TypeTemplateService {

	@Autowired
	private TbTypeTemplateMapper typeTemplateMapper;

	@Autowired
	private TbSpecificationOptionMapper specificationOptionMapper;

	@Autowired
	private RedisTemplate redisTemplate;

	/**
	 * 查询全部, 下拉框
	 */
	@Override
	public List<Map> findAll() {
		return typeTemplateMapper.selectAll();
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbTypeTemplate> page=   (Page<TbTypeTemplate>) typeTemplateMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbTypeTemplate typeTemplate) {
		typeTemplateMapper.insert(typeTemplate);		
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbTypeTemplate typeTemplate){
		typeTemplateMapper.updateByPrimaryKey(typeTemplate);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbTypeTemplate findOne(Long id){
		return typeTemplateMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			typeTemplateMapper.deleteByPrimaryKey(id);
		}		
	}
	
	/** 缓存 */
	@Override
	public PageResult findPage(TbTypeTemplate typeTemplate, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbTypeTemplateExample example=new TbTypeTemplateExample();
		Criteria criteria = example.createCriteria();
		
		if(typeTemplate!=null){			
						if(typeTemplate.getName()!=null && typeTemplate.getName().length()>0){
				criteria.andNameLike("%"+typeTemplate.getName()+"%");
			}
			if(typeTemplate.getSpecIds()!=null && typeTemplate.getSpecIds().length()>0){
				criteria.andSpecIdsLike("%"+typeTemplate.getSpecIds()+"%");
			}
			if(typeTemplate.getBrandIds()!=null && typeTemplate.getBrandIds().length()>0){
				criteria.andBrandIdsLike("%"+typeTemplate.getBrandIds()+"%");
			}
			if(typeTemplate.getCustomAttributeItems()!=null && typeTemplate.getCustomAttributeItems().length()>0){
				criteria.andCustomAttributeItemsLike("%"+typeTemplate.getCustomAttributeItems()+"%");
			}
	
		}
		
		Page<TbTypeTemplate> page= (Page<TbTypeTemplate>)typeTemplateMapper.selectByExample(example);

		/** 将数据存入缓存 */
		saveToRedis ();

		return new PageResult(page.getTotal(), page.getResult());
	}


	/** 返回规格列表 */
	@Override
	public List<Map> findSpecList(Long id) {

		// 根据id查询模板记录
		TbTypeTemplate typeTemplate = typeTemplateMapper.selectByPrimaryKey(id);

		// 将模板中的 属性 Spec 列表获取出来并转换成Map数据结构
		List<Map> list = JSON.parseArray(typeTemplate.getSpecIds(), Map.class);

		for (Map map : list) {
			// 查询规格选项列表
			TbSpecificationOptionExample example = new TbSpecificationOptionExample();
			TbSpecificationOptionExample.Criteria criteria = example.createCriteria();
			criteria.andSpecIdEqualTo(new Long((Integer)map.get("id")));
			List<TbSpecificationOption> options = specificationOptionMapper.selectByExample(example);
			map.put("options", options);
		}
		return list;
	}

	/** 将模板信息全部查询出来 */
	public List<TbTypeTemplate> findAllForRedis() {
		return typeTemplateMapper.selectByExample(null);
	}

	/** 将数据存入缓存 */
	private void saveToRedis () {

		// 获取模板数据
		List<TbTypeTemplate> typeTemplateList = findAllForRedis();

		// 循环模板
		for (TbTypeTemplate typeTemplate : typeTemplateList) {
			// 存储品牌列表
			List<Map> brandList = JSON.parseArray(typeTemplate.getBrandIds(), Map.class);
			redisTemplate.boundHashOps("brandList").put(typeTemplate.getId(),brandList);

			// 存储规格列表
			// 根据id查询规格列表
			List<Map> specList = findSpecList(typeTemplate.getId());
			redisTemplate.boundHashOps("specList").put(typeTemplate.getId(),specList);
		}
        System.out.println("更新缓存：品牌列表！");
        System.out.println("更新缓存                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              ：规格列表！");


	}


}
