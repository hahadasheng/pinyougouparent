package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.HighlightEntry;
import org.springframework.data.solr.core.query.result.HighlightPage;

import java.util.HashMap;
import java.util.Map;

/** dubbox 默认超时时间，这里设置大一点；推荐设置在服务端
 * 也可以在web端的@Reference中设置【如果同时配置，消费端为主】 */
@Service(timeout = 3000)
public class ItemSearchServiceImpl implements ItemSearchService {

    @Autowired
    private SolrTemplate solrTemplate;

    /** 高亮搜索抽离 */
    private Map searchList (Map searchMap) {
        /*
        Map<String, Object> map = new HashMap<>();
        Query query = new SimpleQuery();

        // 添加查询条件,查询【复制域】，所以得用map接收
        Criteria criteria = new Criteria("item_keywords")
                .is(searchMap.get("keywords"));
        query.addCriteria(criteria);

        ScoredPage<TbItem> page = solrTemplate.queryForPage(query, TbItem.class);

        map.put("rows", page.getContent());
        return map;
        */

        // 高亮查询
        Map map = new HashMap<>();
        HighlightQuery query = new SimpleHighlightQuery();


        // 设置高亮的域，可能有很多个域。可以通过链式编程添加域
        HighlightOptions highlightOptions = new HighlightOptions().addField("item_title");
        // 高亮前缀
        highlightOptions.setSimplePrefix("<em style='color:red'>");
        // 高亮后缀
        highlightOptions.setSimplePostfix("</em>");
        // 将高亮配置 赋予 给查询对象
        query.setHighlightOptions(highlightOptions);


        // 配置查询的域并将配置赋予给 查询对象
        Criteria criteria = new Criteria("item_keywords")
                .is(searchMap.get("keywords"));
        query.addCriteria(criteria);

        // 按照关键字进行查询 获取返回结果
        HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query, TbItem.class);

        // 循环【高亮入口】集合，获取每个【文档集合】
        // 注意：page.getContent() 获得的是原始没有高亮的内容
        for (HighlightEntry<TbItem> h : page.getHighlighted()) {

             /*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            // @@@@ ~:~API设计原理解释及演示~:~ @@@@ 继续上面的步骤！
            // 遍历【文档】，获取【域集合】
            List<HighlightEntry.Highlight> h2 = h.getHighlights();

            // 遍历【域集合】，比如 复制域 ，与中有多个原始域 ，所以需要集合存储！
            for (HighlightEntry.Highlight h3 : h2) {
                List<String> finalResult = h3.getSnipplets();
                System.out.println(finalResult);
            }
             ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */


            // 获取原实体类，将高亮的的域的内容封装到 原实体类中
            // h.getEntity() 与 page.getContent() 获取的是同一个对象！
            TbItem item = h.getEntity();

            // 业务逻辑决定了如此判断！
            if (h.getHighlights().size() >0 &&
                    h.getHighlights().get(0).getSnipplets().size() > 0) {

                // 设置高亮结果
                item.setTitle(h.getHighlights().get(0).getSnipplets().get(0));
            }
        }

        map.put("rows", page.getContent());
        return map;
    }

    /** 搜索 */
    @Override
    public Map<String, Object> search(Map searchMap) {
        Map<String, Object> map = new HashMap<>();

        // 查询列表
        map.putAll(searchList(searchMap));

        return map;
    }
}
