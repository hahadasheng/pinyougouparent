package com.pinyougou.task;

import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillGoodsExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class SeckillTask {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private TbSeckillGoodsMapper seckillGoodsMapper;

    /**
     * 刷新秒杀商品
     *  @Scheduled 注解为注册任务调度
     *  cron 参数 ： cron 表达式: 执行的时间规则！
     Cron表达式是一个字符串，字符串以5或6个空格隔开，分为6或7个域，每一个域代表一个含义，
     Cron有如下两种语法格式：【如果不指定年，推荐使用第二个表达式】
        （1）Seconds Minutes Hours DayofMonth Month DayofWeek Year
        （2）Seconds Minutes Hours DayofMonth Month DayofWeek
    每一个域可出现的字符如下：
        Seconds: 可出现", - * /"四个字符，有效范围为0-59的整数
        Minutes: 可出现", - * /"四个字符，有效范围为0-59的整数
        Hours: 可出现", - * /"四个字符，有效范围为0-23的整数
        DayofMonth: 可出现", - * / ? L W C"八个字符，有效范围为1-31的整数
        Month: 可出现", - * /"四个字符，有效范围为1-12的整数或JAN-DEc
        DayofWeek: 可出现", - * / ? L C #"四个字符，有效范围为1-7的整数或SUN-SAT两个范围。1表示星期天，2表示星期一， 依次类推
        Year: 可出现", - * /"四个字符，有效范围为1970-2099年

    每一个域都使用数字，但还可以出现如下特殊字符，它们的含义是：
        (1)*：表示匹配该域的任意值，假如在Minutes域使用*, 即表示每分钟都会触发事件。
        (2)?:只能用在DayofMonth和DayofWeek两个域【互斥，表示放弃当前位置的匹配】。比如DayofMonth和 DayofWeek会相互影响。例如想在每月的20日触发调度，不管20日到底是星期几，则只能使用如下写法： 13 13 15 20 * ?, 其中最后一位只能用？，而不能使用*，如果使用*表示不管星期几都会触发，实际上并不是这样。
        (3)-:表示范围，例如在Minutes域使用5-20，表示从5分到20分钟每分钟触发一次
        (4)/：表示起始时间开始触发，然后每隔固定时间触发一次，例如在Minutes域使用5/20,则意味着5分钟触发一次，而25，45等分别触发一次.
        (5),:表示列出枚举值值。例如：在Minutes域使用5,20，则意味着在5和20分每分钟触发一次。
        (6)L:表示最后，只能出现在DayofWeek和DayofMonth域，如果在DayofWeek域使用5L,意味着在最后的一个星期四触发。
        (7)W: 表示有效工作日(周一到周五),只能出现在DayofMonth域，系统将在离指定日期的最近的有效工作日触发事件。例如：在 DayofMonth使用5W，如果5日是星期六，则将在最近的工作日：星期五，即4日触发。如果5日是星期天，则在6日(周一)触发；如果5日在星期一 到星期五中的一天，则就在5日触发。另外一点，W的最近寻找不会跨过月份
        (8)LW:这两个字符可以连用，表示在某个月最后一个工作日，即最后一个星期五。
        (9)#:用于确定每个月第几个星期几，只能出现在DayofMonth域。例如在4#2，表示某月的第二个星期三。

    Cron表达式例子：
        0 0 10,14,16 * * ? 每天上午10点，下午2点，4点
        0 0/30 9-17 * * ? 朝九晚五工作时间内每半小时
        0 0 12 ? * WED 表示每个星期三中午12点
        "0 0 12 * * ?" 每天中午12点触发
        "0 15 10 ? * *" 每天上午10:15触发
        "0 15 10 * * ?" 每天上午10:15触发
        "0 15 10 * * ? *" 每天上午10:15触发
        "0 15 10 * * ? 2005" 2005年的每天上午10:15触发
        "0 * 14 * * ?" 在每天下午2点到下午2:59期间的每1分钟触发
        "0 0/5 14 * * ?" 在每天下午2点到下午2:55期间的每5分钟触发
        "0 0/5 14,18 * * ?" 在每天下午2点到2:55期间和下午6点到6:55期间的每5分钟触发
        "0 0-5 14 * * ?" 在每天下午2点到下午2:05期间的每1分钟触发
        "0 10,44 14 ? 3 WED" 每年三月的星期三的下午2:10和2:44触发
        "0 15 10 ? * MON-FRI" 周一至周五的上午10:15触发
        "0 15 10 15 * ?" 每月15日上午10:15触发
        "0 15 10 L * ?" 每月最后一日的上午10:15触发
        "0 15 10 ? * 6L" 每月的最后一个星期五上午10:15触发
        "0 15 10 ? * 6L 2002-2005" 2002年至2005年的每月的最后一个星期五上午10:15触发
        "0 15 10 ? * 6#3" 每月的第三个星期五上午10:15触发
     */
    @Scheduled(cron = "0/10 * * * * ?")
    public void refreshSeckillGoods() {
        System.out.println("execute task: " + new Date());

        // 查询所有的秒杀商品建的集合
        List ids = new ArrayList(redisTemplate.boundHashOps("seckillGoods").keys());
        System.out.println(ids);

        // 查询正在秒杀的商品列表
        TbSeckillGoodsExample example = new TbSeckillGoodsExample();
        TbSeckillGoodsExample.Criteria criteria = example.createCriteria();
        // 审核通过的商品
        criteria.andStatusEqualTo("1");
        // 剩余库存大于0
        criteria.andStockCountGreaterThan(0);
        // 开始时间小于等于当前时间
        criteria.andStartTimeLessThan(new Date());
        // 结束时间大于当前时间
        criteria.andEndTimeGreaterThan(new Date());

        // 当缓存中存在商品时！排除缓存中已经有的商品
        if (ids.size() > 0 ) {
            criteria.andIdNotIn(ids);
        }

        List<TbSeckillGoods> seckillGoodsList = seckillGoodsMapper.selectByExample(example);
        // 将商品放入缓存

        for (TbSeckillGoods seckillGoods : seckillGoodsList) {
            redisTemplate.boundHashOps("seckillGoods").put(seckillGoods.getId(), seckillGoods);
        }
        System.out.println("put " + seckillGoodsList.size() + " items to redis!");
    }


    /**
     * 移除过期的秒杀商品
     */
    @Scheduled(cron = "0/5 * * * * ?")
    public void removeSeckillGoods() {
        List<TbSeckillGoods> seckillGoods = redisTemplate.boundHashOps("seckillGoods").values();
        for (TbSeckillGoods seckillGood : seckillGoods) {
            // 如果结束日期小于当前日期，表示过期，需要从Redis中移除
            if (seckillGood.getEndTime().getTime() < (new Date()).getTime()) {
                // 跟新数据库对应的商品
                seckillGoodsMapper.updateByPrimaryKey(seckillGood);
                // 从Redis中移除缓存数据
                redisTemplate.boundHashOps("seckillGoods").delete(seckillGood.getId());
                System.out.println("~~~~~~ remove seckillGoods from Redis + " + seckillGood.getId());
            }
        }

    }

}
