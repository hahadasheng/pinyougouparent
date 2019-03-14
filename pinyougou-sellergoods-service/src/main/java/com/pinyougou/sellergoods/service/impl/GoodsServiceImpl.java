package com.pinyougou.sellergoods.service.impl;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.pinyougou.mapper.*;
import com.pinyougou.pojo.*;
import com.pinyougou.pojogroup.Goods;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.pojo.TbGoodsExample.Criteria;
import com.pinyougou.sellergoods.service.GoodsService;

import entity.PageResult;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class GoodsServiceImpl implements GoodsService {

	@Autowired
	private TbGoodsMapper goodsMapper;

	@Autowired
	private TbGoodsDescMapper goodsDescMapper;
	
	@Autowired
	private TbItemMapper itemMapper;

	@Autowired
	private TbBrandMapper brandMapper;

	@Autowired
	private TbItemCatMapper itemCatMapper;

	@Autowired
	private TbSellerMapper sellerMapper;

	/**
	 * 查询全部
	 */
	@Override
	public List<TbGoods> findAll() {
		return goodsMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbGoods> page=   (Page<TbGoods>) goodsMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(Goods goods) {

		// 设置未审核状态
		goods.getGoods().setAuditStatus("0");
		// 将商品信息插入 商品表中 goods
		goodsMapper.insert(goods.getGoods());

		// 设置商品描述中关联的 商品表的id
		goods.getGoodsDesc().setGoodsId(goods.getGoods().getId());

		// 将商品描述插入到商品描述表中
		goodsDescMapper.insert(goods.getGoodsDesc());

		// 插入商品SKU列表数据
		saveItemList(goods);
	}

	private void setItemValuesPublic(Goods goods, TbItem item) {
		// 商品SPU编号
		item.setGoodsId(goods.getGoods().getId());

		// 商家编号
		item.setSellerId(goods.getGoods().getSellerId());

		// 商品分类
		item.setCategoryid(goods.getGoods().getCategory3Id());

		// 创建日期
		item.setCreateTime(new Date());

		// 修改日期
		item.setUpdateTime(new Date());

		// 品牌名称
		TbBrand brand = brandMapper.selectByPrimaryKey(goods.getGoods().getBrandId());
		item.setBrand(brand.getName());

		// 分类名称
		TbItemCat itemCat = itemCatMapper.selectByPrimaryKey(goods.getGoods().getCategory3Id());
		item.setCategory(itemCat.getName());

		// 商家名称
		TbSeller seller = sellerMapper.selectByPrimaryKey(goods.getGoods().getSellerId());
		item.setSeller(seller.getNickName());

		// 图片地址（去spu的第一个图片）
		List<Map> imageList = JSON.parseArray(goods.getGoodsDesc().getItemImages(), Map.class);
		if (imageList.size() > 0) {
			item.setImage((String)(imageList.get(0).get("url")));
		}

	}

	/** 插入SKU列表数据 */
	private void saveItemList(Goods goods) {
		if ("1".equals(goods.getGoods().getIsEnableSpec())) {
			for (TbItem item : goods.getItemList()) {
				// 标题 =  spu 规格内容1 规格内容2 规格内容3
				StringBuilder title = new StringBuilder(goods.getGoods().getGoodsName());

				Map<String, Object> specMap = JSON.parseObject(item.getSpec());
				for (String key : specMap.keySet()) {
					title.append(" ").append(specMap.get(key));
				}

				item.setTitle(title.toString());

				setItemValuesPublic(goods, item);

				itemMapper.insert(item);
			}
		} else {
			TbItem item = new TbItem();
			// 商品title = SPU
			item.setTitle(goods.getGoods().getGoodsName());

			// 价格
			item.setPrice(goods.getGoods().getPrice());

			// 状态
			item.setStatus("1");

			// 是否默认
			item.setIsDefault("1");

			// 库存数量
			item.setNum(999999);

			// 规格
			item.setSpec("{}");

			setItemValuesPublic(goods, item);
			itemMapper.insert(item);
		}
	}


	/**
	 * 修改
	 */
	@Override
	public void update(Goods goods){
		// 设置为申请状态，如果是经过修改的商品，需要重新设置状态
		goods.getGoods().setAuditStatus("0");

		// 保存商品表
		goodsMapper.updateByPrimaryKey(goods.getGoods());

		// 保存商品扩展表
		goodsDescMapper.updateByPrimaryKey(goods.getGoodsDesc());

		// 删除原有的sku列表数据
		TbItemExample example = new TbItemExample();
		TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andGoodsIdEqualTo(goods.getGoods().getId());
		itemMapper.deleteByExample(example);

		// 添加新的sku列表数据
		saveItemList(goods);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public Goods findOne(Long id){
		Goods goods = new Goods();

		TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
		goods.setGoods(tbGoods);

		TbGoodsDesc goodsDesc = goodsDescMapper.selectByPrimaryKey(id);
		goods.setGoodsDesc(goodsDesc);

		TbItemExample example = new TbItemExample();
		TbItemExample.Criteria criteria = example.createCriteria();
		// 查询条件：商品ID
		criteria.andGoodsIdEqualTo(id);

		List<TbItem> itemList = itemMapper.selectByExample(example);
		goods.setItemList(itemList);
		return goods;
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			// 删除功能：不能讲商品进行物理上的删除，要进行
			// 逻辑上的删除，如果只为!null 就代表删除
			TbGoods goods = goodsMapper.selectByPrimaryKey(id);
			goods.setIsDelete("1");
			goodsMapper.updateByPrimaryKey(goods);
		}		
	}

		@Override
	public PageResult findPage(TbGoods goods, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbGoodsExample example=new TbGoodsExample();
		Criteria criteria = example.createCriteria();

		// 过滤逻辑上被删除的商品 不进行查询: is_delete 字段为!null表示被删除了
		criteria.andIsDeleteIsNull();

		if(goods!=null){			
			if(goods.getSellerId()!=null && goods.getSellerId().length()>0){
				// 此处不能进行模糊查询
				// criteria.andSellerIdLike("%"+goods.getSellerId()+"%");
				criteria.andSellerIdEqualTo(goods.getSellerId());
			}
			if(goods.getGoodsName()!=null && goods.getGoodsName().length()>0){
				criteria.andGoodsNameLike("%"+goods.getGoodsName()+"%");
			}
			if(goods.getAuditStatus()!=null && goods.getAuditStatus().length()>0){
				criteria.andAuditStatusLike("%"+goods.getAuditStatus()+"%");
			}
			if(goods.getIsMarketable()!=null && goods.getIsMarketable().length()>0){
				criteria.andIsMarketableLike("%"+goods.getIsMarketable()+"%");
			}
			if(goods.getCaption()!=null && goods.getCaption().length()>0){
				criteria.andCaptionLike("%"+goods.getCaption()+"%");
			}
			if(goods.getSmallPic()!=null && goods.getSmallPic().length()>0){
				criteria.andSmallPicLike("%"+goods.getSmallPic()+"%");
			}
			if(goods.getIsEnableSpec()!=null && goods.getIsEnableSpec().length()>0){
				criteria.andIsEnableSpecLike("%"+goods.getIsEnableSpec()+"%");
			}
			if(goods.getIsDelete()!=null && goods.getIsDelete().length()>0){
				criteria.andIsDeleteLike("%"+goods.getIsDelete()+"%");
			}
	
		}
		
		Page<TbGoods> page= (Page<TbGoods>)goodsMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	/** 批量审核 */
	@Override
	public void updateStatus(Long[] ids, String status) {

		for (Long id: ids) {
			TbGoods goods = goodsMapper.selectByPrimaryKey(id);
			goods.setAuditStatus(status);
			goodsMapper.updateByPrimaryKey(goods);
		}
	}

	/** 根据商品ID和状态查询Item表信息 */
	@Override
	public List<TbItem> findItemListByGoodsIdAndStatus(Long[] goodsIds, String status) {
		TbItemExample example = new TbItemExample();
        TbItemExample.Criteria criteria = example.createCriteria();
        criteria.andGoodsIdIn(Arrays.asList(goodsIds));
        criteria.andStatusEqualTo(status);
        return itemMapper.selectByExample(example);
	}

}
