package com.pinyougou.sellergoods.service.impl;
import java.util.List;

import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbItemCatMapper;
import com.pinyougou.pojo.TbItemCat;
import com.pinyougou.pojo.TbItemCatExample;
import com.pinyougou.pojo.TbItemCatExample.Criteria;
import com.pinyougou.sellergoods.service.ItemCatService;

import entity.PageResult;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class ItemCatServiceImpl implements ItemCatService {

	@Autowired
	private TbItemCatMapper itemCatMapper;

	@Autowired
	private RedisTemplate redisTemplate;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbItemCat> findAll() {
		return itemCatMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbItemCat> page=   (Page<TbItemCat>) itemCatMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbItemCat itemCat) {
		itemCatMapper.insert(itemCat);		
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbItemCat itemCat){
		itemCatMapper.updateByPrimaryKey(itemCat);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbItemCat findOne(Long id){
		return itemCatMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public Result delete(Long[] ids) {
		Result result = new Result();
		// 判断是否有没有删除成功的记录
		boolean flag = false;

		StringBuilder sb = new StringBuilder("以下子目录不为空，禁止删除！[");
		for(Long id:ids){
			TbItemCatExample examp = new TbItemCatExample();
			Criteria criteria = examp.createCriteria();
			criteria.andParentIdEqualTo(id);
			List<TbItemCat> tbItemCats = itemCatMapper.selectByExample(examp);
			if (tbItemCats == null || tbItemCats.size() == 0) {
				itemCatMapper.deleteByPrimaryKey(id);
			} else {
				sb.append(id).append(",");
				// 存在没有删除
				flag = true;
			}
		}
		sb.append("]：");
		if (flag) {
			result.setSuccess(false);
			result.setMessage(sb.toString());
		} else {
			result.setSuccess(true);
		}
		return result;
	}
	
	
		@Override
	public PageResult findPage(TbItemCat itemCat, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbItemCatExample example=new TbItemCatExample();
		Criteria criteria = example.createCriteria();
		
		if(itemCat!=null){			
						if(itemCat.getName()!=null && itemCat.getName().length()>0){
				criteria.andNameLike("%"+itemCat.getName()+"%");
			}
	
		}
		
		Page<TbItemCat> page= (Page<TbItemCat>)itemCatMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	/** 查询商品分类，不用分页
	 *  后台运营商在对 商品分类 进行 增删改 后
	 *  都会调用此方法重新查询列表，可以将redis更新
	 *  的逻辑写到这个里面！
	 *
	 * */
	@Override
	public List<TbItemCat> findByParentId(Long parentId) {
		TbItemCatExample examp = new TbItemCatExample();
		Criteria criteria = examp.createCriteria();
		criteria.andParentIdEqualTo(parentId);

		// 跟新缓存
		saveToRedis();

		return itemCatMapper.selectByExample(examp);
	}

	/** 缓存处理 */
	private void saveToRedis() {
		// 一次性读取缓存进行存储
		List<TbItemCat> list = findAll();
		for (TbItemCat itemCat : list) {
			redisTemplate.boundHashOps("itemCat").put(itemCat.getName(), itemCat.getTypeId());
		}
		System.out.println("更新缓存：商品分类表!");
	}

}
