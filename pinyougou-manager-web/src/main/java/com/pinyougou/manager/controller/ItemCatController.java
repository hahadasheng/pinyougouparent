package com.pinyougou.manager.controller;
import java.util.ArrayList;
import java.util.List;

import com.pinyougou.pojogroup.ItemCatRecursion;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbItemCat;
import com.pinyougou.sellergoods.service.ItemCatService;

import entity.PageResult;
import entity.Result;
/**
 * com.lingting.controller
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/itemCat")
public class ItemCatController {

	@Reference
	private ItemCatService itemCatService;
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findAll")
	public List<TbItemCat> findAll(){			
		return itemCatService.findAll();
	}
	
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findPage")
	public PageResult  findPage(int page,int rows){			
		return itemCatService.findPage(page, rows);
	}
	
	/**
	 * 增加
	 * @param itemCat
	 * @return
	 */
	@RequestMapping("/add")
	public Result add(@RequestBody TbItemCat itemCat){
		try {
			itemCatService.add(itemCat);
			return new Result(true, "增加成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "增加失败");
		}
	}
	
	/**
	 * 修改
	 * @param itemCat
	 * @return
	 */
	@RequestMapping("/update")
	public Result update(@RequestBody TbItemCat itemCat){
		try {
			itemCatService.update(itemCat);
			return new Result(true, "修改成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "修改失败");
		}
	}	
	
	/**
	 * 获取实体
	 * @param id
	 * @return
	 */
	@RequestMapping("/findOne")
	public TbItemCat findOne(Long id){
		return itemCatService.findOne(id);		
	}
	
	/**
	 * 批量删除
	 * @param ids
	 * @return
	 */
	@RequestMapping("/delete")
	public Result delete(Long [] ids){
		try {
			return itemCatService.delete(ids);
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "删除失败");
		}
	}
	
		/**
	 * 查询+分页
	 * @param itemCat
	 * @param page
	 * @param rows
	 * @return
	 */
	@RequestMapping("/search")
	public PageResult search(@RequestBody TbItemCat itemCat, int page, int rows  ){
		return itemCatService.findPage(itemCat, page, rows);		
	}

	/** 查询商品分类，不用分页 */
	@RequestMapping("/findByParentId")
	public List<TbItemCat> findByParentId(Long parentId) {
		return itemCatService.findByParentId(parentId);
	}



	/** 【练习】递归查询 item_cat 数结构表  */
	@RequestMapping("/findAllRecursion")
	public ItemCatRecursion findAllRecursion(Long id) {
		ItemCatRecursion god = new ItemCatRecursion();
		god.setChildern(findAllChildren(id));
		return god;
	}

	private List<ItemCatRecursion> findAllChildren(Long parentId) {
		// 【根据parentId获取所有的子节点】
		List<TbItemCat> byParentId = itemCatService.findByParentId(parentId);

		// 递归结束条件
		if (byParentId.size() == 0) { return null; }

		// 封装子节点
		List<ItemCatRecursion> childrenItems = new ArrayList<>();

		// 遍历每一个子节点
		for (TbItemCat itemCat : byParentId) {

			// 创建当前子节点的对象容器
			ItemCatRecursion itemCatRecursion = new ItemCatRecursion();

			// 【递归】查询当前子节点对象 下面的 子节点
			itemCatRecursion.setChildern(findAllChildren(itemCat.getId()));
			childrenItems.add(itemCatRecursion);
		}
		return childrenItems;
	}
}
