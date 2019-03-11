package com.pinyougou.solrutil;

import com.alibaba.fastjson.JSON;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class SolrUtil {

    @Autowired
    private TbItemMapper itemMapper;

    @Autowired
    private SolrTemplate solrTemplate;

    /** 导入上商品数据 */
    public void importItemData() {
        TbItemExample example = new TbItemExample();
        TbItemExample.Criteria criteria = example.createCriteria();

        // 过滤出已经审核通过的商品信息
        criteria.andStatusEqualTo("1");

        List<TbItem> itemList = itemMapper.selectByExample(example);

        System.out.println("==== 商品列表 ====");
        for (TbItem item : itemList) {
            // 将spec字段中的json字符串转换为map
            Map specMap = JSON.parseObject(item.getSpec(), Map.class);

            // 为创建的动态域字段赋值
            item.setSpecMap(specMap);

            System.out.println(item.getId() + " " + item.getTitle());
        }

        solrTemplate.saveBeans(itemList);
        solrTemplate.commit();
        System.out.println("--- 结束 ---");
    }

    // 测试打印类
    public static void main(String[] args) {
        /* classpath* 会让程序扫描依赖jar包下的配置文件,我需要扫描
           依赖jar包下 dao 下的配置文件，所以这里得写 * !
          */
        ApplicationContext context = new ClassPathXmlApplicationContext("classpath*:spring/applicationContext*.xml");

        // 下面这用方式只是测试
        SolrUtil solrUtil = (SolrUtil)context.getBean("solrUtil");
        solrUtil.importItemData();
    }
}
