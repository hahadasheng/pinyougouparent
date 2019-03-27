package com.pinyougou.seckill.service.impl;
import java.util.Date;
import java.util.List;

import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbSeckillOrderMapper;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.pojo.TbSeckillOrderExample;
import com.pinyougou.pojo.TbSeckillOrderExample.Criteria;
import com.pinyougou.seckill.service.SeckillOrderService;

import entity.PageResult;
import org.springframework.data.redis.core.RedisTemplate;
import utils.IdWorker;


/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class SeckillOrderServiceImpl implements SeckillOrderService {

	@Autowired
	private TbSeckillOrderMapper seckillOrderMapper;

	@Autowired
	private TbSeckillGoodsMapper seckillGoodsMapper;

	@Autowired
	private RedisTemplate redisTemplate;

	@Autowired
	private IdWorker idWorker;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbSeckillOrder> findAll() {
		return seckillOrderMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbSeckillOrder> page=   (Page<TbSeckillOrder>) seckillOrderMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbSeckillOrder seckillOrder) {
		seckillOrderMapper.insert(seckillOrder);		
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbSeckillOrder seckillOrder){
		seckillOrderMapper.updateByPrimaryKey(seckillOrder);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbSeckillOrder findOne(Long id){
		return seckillOrderMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			seckillOrderMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbSeckillOrder seckillOrder, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbSeckillOrderExample example=new TbSeckillOrderExample();
		Criteria criteria = example.createCriteria();
		
		if(seckillOrder!=null){			
						if(seckillOrder.getUserId()!=null && seckillOrder.getUserId().length()>0){
				criteria.andUserIdLike("%"+seckillOrder.getUserId()+"%");
			}
			if(seckillOrder.getSellerId()!=null && seckillOrder.getSellerId().length()>0){
				criteria.andSellerIdLike("%"+seckillOrder.getSellerId()+"%");
			}
			if(seckillOrder.getStatus()!=null && seckillOrder.getStatus().length()>0){
				criteria.andStatusLike("%"+seckillOrder.getStatus()+"%");
			}
			if(seckillOrder.getReceiverAddress()!=null && seckillOrder.getReceiverAddress().length()>0){
				criteria.andReceiverAddressLike("%"+seckillOrder.getReceiverAddress()+"%");
			}
			if(seckillOrder.getReceiverMobile()!=null && seckillOrder.getReceiverMobile().length()>0){
				criteria.andReceiverMobileLike("%"+seckillOrder.getReceiverMobile()+"%");
			}
			if(seckillOrder.getReceiver()!=null && seckillOrder.getReceiver().length()>0){
				criteria.andReceiverLike("%"+seckillOrder.getReceiver()+"%");
			}
			if(seckillOrder.getTransactionId()!=null && seckillOrder.getTransactionId().length()>0){
				criteria.andTransactionIdLike("%"+seckillOrder.getTransactionId()+"%");
			}
	
		}
		
		Page<TbSeckillOrder> page= (Page<TbSeckillOrder>)seckillOrderMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 提交秒杀订单
	 * @param seckillId
	 * @param userId
	 */
	@Override
	public void submitOrder(Long seckillId, String userId) {

		// ~~~~~~~ 感觉需要锁【得深入理解Redis】 ~~~~~~
		// 从缓存中查询秒杀商品
		TbSeckillGoods seckillGoods = (TbSeckillGoods)redisTemplate.boundHashOps("seckillGoods").get(seckillId);

		if (seckillGoods == null) {
			throw new RuntimeException("商品不存在");
		}

		if (seckillGoods.getStockCount() <= 0) {
			throw new RuntimeException("商品已经一枪而空");
		}

		// 扣减(redis) 库存
		seckillGoods.setStockCount(seckillGoods.getStockCount() - 1);
		// 放回缓存
		redisTemplate.boundHashOps("seckillGoods").put(seckillId, seckillGoods);
		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

		// 如果已经秒光，
		if (seckillGoods.getStockCount() == 0) {
			// 同步到数据库
			seckillGoodsMapper.updateByPrimaryKey(seckillGoods);
			redisTemplate.boundHashOps("seckillGoods").delete(seckillId);
		}

		// 保存(redis) 订单
		long orderId = idWorker.nextId();
		TbSeckillOrder seckillOrder = new TbSeckillOrder();
		seckillOrder.setId(orderId);
		seckillOrder.setCreateTime(new Date());
		// 秒杀价格
		seckillOrder.setMoney(seckillGoods.getCostPrice());
		seckillOrder.setSeckillId(seckillId);
		seckillOrder.setSellerId(seckillGoods.getSellerId());
		// 设置用户ID
		seckillOrder.setStatus("0");
		redisTemplate.boundHashOps("seckillOrder").put(userId, seckillOrder);
	}

	/**
	 * 根据用户名查询秒杀订单
	 * @param userId
	 * @return
	 */
	@Override
	public TbSeckillOrder searchOrderFromRedisByUserId(String userId) {

		return (TbSeckillOrder)redisTemplate.boundHashOps("seckillOrder").get(userId);
	}

	/**
	 * 支付成功保存订单
	 *    将Redis中的订单保存到数据库中
	 * @param userId
	 * @param orderId
	 * @param transactionId
	 */
	@Override
	public void saveOrderFromRedisToDb(String userId, Long orderId, String transactionId) {
		System.out.println("saveOrderFromRedisToDb:" + userId);

		// 根据用户ID查询日志 Redis中的订单
		TbSeckillOrder seckillOrder = (TbSeckillOrder)redisTemplate.boundHashOps("seckillOrder").get(userId);

		if (seckillOrder == null) {
			throw new RuntimeException("订单不存在");
		}

		// 如果与传递过来的订单号不符
		if (seckillOrder.getId().longValue() != orderId.longValue()) {
			throw new RuntimeException("订单不相符");
		}

		// 交易流水号
		seckillOrder.setTransactionId(transactionId);

		//支付时间
		seckillOrder.setPayTime(new Date());

		// 状态
		seckillOrder.setStatus("1");

		// 保存到数据库
		seckillOrderMapper.insert(seckillOrder);

		// 从redis中清除
		redisTemplate.boundHashOps("seckillOrder").delete(userId);
	}

	/**
	 * 从缓存中删除订单
	 * @param userId
	 * @param orderId
	 */
	@Override
	public void deleteOrderFromRedis(String userId, Long orderId) {
		// 根据用户ID查询日志
		TbSeckillOrder seckillOrder = (TbSeckillOrder)
				redisTemplate.boundHashOps("seckillOrder").get(userId);

		if (seckillOrder != null && seckillOrder.getId().longValue() == orderId.longValue()) {
			// 从缓存中删除订单
			redisTemplate.boundHashOps("seckillOrder").delete(userId);

			// 恢复库存
			// 1. 从缓存中提取秒杀商品
			TbSeckillGoods seckillGoods = (TbSeckillGoods)
					redisTemplate.boundHashOps("seckillGoods")
					.get(seckillOrder.getSeckillId());

			if (seckillGoods != null) {
				seckillGoods.setStockCount(seckillGoods.getStockCount() + 1);
			}

			// 存入缓存
			redisTemplate.boundHashOps("seckillGoods").put(seckillOrder.getSeckillId(), seckillGoods);
		}
	}
}
