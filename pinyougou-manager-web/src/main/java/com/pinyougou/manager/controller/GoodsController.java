package com.pinyougou.manager.controller;
import java.util.Arrays;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojogroup.Goods;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.sellergoods.service.GoodsService;

import entity.PageResult;
import entity.Result;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

/**
 * com.lingting.controller
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {

	@Reference
	private GoodsService goodsService;

	/** 发送solr导入的消息 */
	@Autowired
	private Destination queueSolrDestination;

	/** 发送solr删除的消息 */
	@Autowired
	private Destination queueSolrDeleteDestination;

	/** 页面生成的 topic */
	@Autowired
	private Destination topicPageDestination;

	/** 页面删除的 topic */
	@Autowired
	private Destination topicPageDeleteDestination;

	/** spring 发送消息的工具模板 */
	@Autowired
	private JmsTemplate jmsTemplate;

	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findAll")
	public List<TbGoods> findAll(){			
		return goodsService.findAll();
	}
	
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findPage")
	public PageResult  findPage(int page,int rows){			
		return goodsService.findPage(page, rows);
	}
	
//	/**
//	 * 增加
//	 * @param goods
//	 * @return
//	 */
//	@RequestMapping("/add")
//	public Result add(@RequestBody TbGoods goods){
//		try {
//			goodsService.add(goods);
//			return new Result(true, "增加成功");
//		} catch (Exception e) {
//			e.printStackTrace();
//			return new Result(false, "增加失败");
//		}
//	}
	
	/**
	 * 修改
	 * @param goods
	 * @return
	 */
	@RequestMapping("/update")
	public Result update(@RequestBody Goods goods){
		try {
			goodsService.update(goods);
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
	public Goods findOne(Long id){
		return goodsService.findOne(id);		
	}
	
	/**
	 * 批量删除
	 * @param ids
	 * @return
	 */
	@RequestMapping("/delete")
	public Result delete(final Long [] ids){
		try {
			goodsService.delete(ids);
			// 删除solr中的数据
			jmsTemplate.send(queueSolrDeleteDestination,
					new MessageCreator() {
						@Override
						public Message createMessage(Session session) throws JMSException {
							return session.createObjectMessage(ids);
						}
					});


			// 删除页面的服务的静态化页面
			jmsTemplate.send(topicPageDeleteDestination,
					new MessageCreator() {
						@Override
						public Message createMessage(Session session) throws JMSException {
							return session.createObjectMessage(ids);
						}
					});


			return new Result(true, "删除成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "删除失败");
		}
	}
	
	 /**
	 * 查询+分页
	 * @param
	 * @param page
	 * @param rows
	 * @return
	 */
	@RequestMapping("/search")
	public PageResult search(@RequestBody TbGoods goods, int page, int rows  ){
		return goodsService.findPage(goods, page, rows);		
	}

	/** 批量修改状态 */
	@RequestMapping("/updateStatus")
	public Result updateStatus(Long[] ids, String status) {

		try {
			goodsService.updateStatus(ids, status);

			// 按照SPU ID 查询 SKU列表，(状态为1)
			if ("1".equals(status)) {
				// 审核通过
				List<TbItem> itemList = goodsService.findItemListByGoodsIdAndStatus(ids, status);

				/*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/
				// *** 调用 ActiveMQ消息队列 实现数据批量导入到 solr
				if (itemList.size() > 0) {
					final String jsonString = JSON.toJSONString(itemList);
					jmsTemplate.send(queueSolrDestination, new MessageCreator() {
						@Override
						public Message createMessage(Session session) throws JMSException {
							return session.createTextMessage(jsonString);
						}
					});

				} else {
					System.out.println("没有明确数据");
				}
				/*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/


				// *** 生成商品静态详情页
				for (final Long goodsId : ids) {
					jmsTemplate.send(topicPageDestination,
							new MessageCreator() {

								@Override
								public Message createMessage(Session session) throws JMSException {
									return session.createTextMessage(goodsId + "");
								}
							});
				}
			}


			return new Result(true, "成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "失败");
		}
	}

	/**
	 * 静态生成页 (测试)

	@RequestMapping("/genHtml")
	public String genHtml(Long goodsId) {
        boolean flag = itemPageService.genIteHtml(goodsId);

        if (flag) {
            return "200 generate success!";
        } else {
            return "500 generate failure!";
        }
	}
	 */

}
