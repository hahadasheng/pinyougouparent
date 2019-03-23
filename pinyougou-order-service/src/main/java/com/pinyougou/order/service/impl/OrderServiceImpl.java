package com.pinyougou.order.service.impl;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.pinyougou.mapper.TbOrderItemMapper;
import com.pinyougou.mapper.TbPayLogMapper;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojo.TbPayLog;
import com.pinyougou.pojogroup.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbOrderMapper;
import com.pinyougou.pojo.TbOrder;
import com.pinyougou.pojo.TbOrderExample;
import com.pinyougou.pojo.TbOrderExample.Criteria;
import com.pinyougou.order.service.OrderService;

import entity.PageResult;
import org.springframework.data.redis.core.RedisTemplate;
import utils.IdWorker;
import java.util.Date;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class OrderServiceImpl implements OrderService {

	@Autowired
	private TbOrderMapper orderMapper;

	@Autowired
	private RedisTemplate<String, Object> redisTemplate;

	@Autowired
	private TbOrderItemMapper orderItemMapper;

	@Autowired
	private IdWorker idWorker;

	@Autowired
	private TbPayLogMapper payLogMapper;

	/**
	 * 查询全部
	 */
	@Override
	public List<TbOrder> findAll() {
		return orderMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbOrder> page=   (Page<TbOrder>) orderMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbOrder order) {

		// 查询购物车的数据；只有登录后才此功能才能实现，所以得从Redis中获取
		List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(order.getUserId());

		// 订单IP列表
		List<String> orderIdList = new ArrayList<>();

		// 总金额
		double total_money = 0;

		// 遍历每一个商家；每个商家生成一个 大订单
		for (Cart cart : cartList) {

			// 雪花算法 分布式ID解决方案 商家对应的订单
			long orderId = idWorker.nextId();

			// 创建订单对象
			TbOrder tbOrder = new TbOrder();

			// 订单ID
			tbOrder.setOrderId(orderId);

			// 用户名
			tbOrder.setUserId(order.getUserId());

			// 支付类型
			tbOrder.setPaymentType(order.getPaymentType());

			// 状态：未付款
			tbOrder.setStatus("1");

			// 订单创建日期
			tbOrder.setCreateTime(new Date());

			// 订单更新日期
			tbOrder.setUpdateTime(new Date());

			// 地址
			tbOrder.setReceiverAreaName(order.getReceiverAreaName());

			// 手机号
			tbOrder.setReceiverMobile(order.getReceiverMobile());

			// 收货人
			tbOrder.setReceiver(order.getReceiver());

			// 订单来源
			tbOrder.setSourceType(order.getSourceType());

			// 商家ID
			tbOrder.setSellerId(cart.getSellerId());

			// 循环购物车明细
			double money = 0;

			// 循环当前商家下所有的商品！
			for (TbOrderItem orderItem : cart.getOrderItemList()) {
				// 设置商品明细的 ID
				orderItem.setId(idWorker.nextId());

				// 记录对应的订单ID
				orderItem.setOrderId(orderId);

				// 设置商家 ID
				orderItem.setSellerId(cart.getSellerId());

				// 金额累加
				money += orderItem.getTotalFee().doubleValue();
				orderItemMapper.insert(orderItem);
			}

			// 设置当前商家支付的总额
			tbOrder.setPayment(new BigDecimal(money));

			// 保存订单
			orderMapper.insert(tbOrder);

			// 将生成的orderId【商家订单ID】添加到订单列表
			orderIdList.add(orderId + "");

			// 累加到总金额
			total_money += money;

		}

		// 如果是微信支付，写入日志
		if ("1".equals(order.getPaymentType())) {
			TbPayLog payLog = new TbPayLog();

			// 【支付订单号】-- 一个支付订单号代表一次微信支付，多个商家订单，多个商品订单！
			String outTradeNo = idWorker.nextId() + "";

			// 支付订单号
			payLog.setOutTradeNo(outTradeNo);

			// 创建时间
			payLog.setCreateTime(new Date());

			// 订单号列表，逗号分隔【商家订单ID】
			String ids = orderIdList.toString().replace("[", "").replace("]", "").replace(" ","");

			// 订单号列表，逗号分隔【商家订单ID】
			payLog.setOrderList(ids);

			// 设置支付类型，统一用微信吧
			payLog.setPayType("1");

			// 总金额(分)
			payLog.setTotalFee((long)(total_money * 100));

			// 支付状态 未支付
			payLog.setTradeState("0");

			// 用户ID
			payLog.setUserId(order.getUserId());

			// 插入到支付日志表
			payLogMapper.insert(payLog);

			// 将订单日志信息放入缓存，方便读取
			redisTemplate.boundHashOps("payLog")
					.put(order.getUserId(), payLog);
		}

		// 清除缓存中购物车的数据
		redisTemplate.boundHashOps("cartList").delete(order.getUserId());


	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbOrder order){
		orderMapper.updateByPrimaryKey(order);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbOrder findOne(Long id){
		return orderMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			orderMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
	@Override
	public PageResult findPage(TbOrder order, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbOrderExample example=new TbOrderExample();
		Criteria criteria = example.createCriteria();
		
		if(order!=null){			
						if(order.getPaymentType()!=null && order.getPaymentType().length()>0){
				criteria.andPaymentTypeLike("%"+order.getPaymentType()+"%");
			}
			if(order.getPostFee()!=null && order.getPostFee().length()>0){
				criteria.andPostFeeLike("%"+order.getPostFee()+"%");
			}
			if(order.getStatus()!=null && order.getStatus().length()>0){
				criteria.andStatusLike("%"+order.getStatus()+"%");
			}
			if(order.getShippingName()!=null && order.getShippingName().length()>0){
				criteria.andShippingNameLike("%"+order.getShippingName()+"%");
			}
			if(order.getShippingCode()!=null && order.getShippingCode().length()>0){
				criteria.andShippingCodeLike("%"+order.getShippingCode()+"%");
			}
			if(order.getUserId()!=null && order.getUserId().length()>0){
				criteria.andUserIdLike("%"+order.getUserId()+"%");
			}
			if(order.getBuyerMessage()!=null && order.getBuyerMessage().length()>0){
				criteria.andBuyerMessageLike("%"+order.getBuyerMessage()+"%");
			}
			if(order.getBuyerNick()!=null && order.getBuyerNick().length()>0){
				criteria.andBuyerNickLike("%"+order.getBuyerNick()+"%");
			}
			if(order.getBuyerRate()!=null && order.getBuyerRate().length()>0){
				criteria.andBuyerRateLike("%"+order.getBuyerRate()+"%");
			}
			if(order.getReceiverAreaName()!=null && order.getReceiverAreaName().length()>0){
				criteria.andReceiverAreaNameLike("%"+order.getReceiverAreaName()+"%");
			}
			if(order.getReceiverMobile()!=null && order.getReceiverMobile().length()>0){
				criteria.andReceiverMobileLike("%"+order.getReceiverMobile()+"%");
			}
			if(order.getReceiverZipCode()!=null && order.getReceiverZipCode().length()>0){
				criteria.andReceiverZipCodeLike("%"+order.getReceiverZipCode()+"%");
			}
			if(order.getReceiver()!=null && order.getReceiver().length()>0){
				criteria.andReceiverLike("%"+order.getReceiver()+"%");
			}
			if(order.getInvoiceType()!=null && order.getInvoiceType().length()>0){
				criteria.andInvoiceTypeLike("%"+order.getInvoiceType()+"%");
			}
			if(order.getSourceType()!=null && order.getSourceType().length()>0){
				criteria.andSourceTypeLike("%"+order.getSourceType()+"%");
			}
			if(order.getSellerId()!=null && order.getSellerId().length()>0){
				criteria.andSellerIdLike("%"+order.getSellerId()+"%");
			}
	
		}
		
		Page<TbOrder> page= (Page<TbOrder>)orderMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 根据用户查询 订单日志信息 payLog
	 * @param userId
	 * @return
	 */
	@Override
	public TbPayLog searchPayLogFromRedis(String userId) {

		return (TbPayLog) redisTemplate.boundHashOps("payLog").get(userId);
	}

	/**
	 * 修改订单状态：【支付成功】
	 * 	1. 修改支付日志支付状态
	 * 	2. 修改关联的订单的状态
	 * 	3. 清除缓存中的支付日志对象
	 * @param out_trade_no 支付订单号
	 * @param transaction_id 微型返回的交易流水号
	 */
	@Override
	public void updateOrderStatus(String out_trade_no, String transaction_id) {
		// 1. 修改支付日志状态
		TbPayLog payLog = payLogMapper.selectByPrimaryKey(out_trade_no);

		// 支付日期
		payLog.setPayTime(new Date());

		// 已支付【针对于日志表】
		payLog.setTradeState("1");

		// 交易号
		payLog.setTransactionId(transaction_id);

		// 更新
		payLogMapper.updateByPrimaryKey(payLog);

		// 2. 修改订单状态
		// 获取订单号列表
		String orderList = payLog.getOrderList();

		// 获取订单号数组
		String[] orderIds = orderList.split(",");

		for (String orderId : orderIds) {
			TbOrder order = orderMapper.selectByPrimaryKey(Long.parseLong(orderId));

			if (order != null) {
				// 将状态设置为已经付款【针对于订单表】
				order.setStatus("2");
				orderMapper.updateByPrimaryKey(order);
			}
		}

		// 清除redis缓存数据
		redisTemplate.boundHashOps("payLog").delete(payLog.getUserId());
	}

}
