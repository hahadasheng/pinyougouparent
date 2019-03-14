package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** dubbox 默认超时时间，这里设置大一点；推荐设置在服务端
 * 也可以在web端的@Reference中设置【如果同时配置，消费端为主】 */
@Service(timeout = 3000)
public class ItemSearchServiceImpl implements ItemSearchService {

    @Autowired
    private SolrTemplate solrTemplate;

    @Autowired
    private RedisTemplate redisTemplate;

    /** 搜索 */
    @Override
    public Map<String, Object> search(Map searchMap) {
        // 去掉关键字的空格
        String keywords = (String)searchMap.get("keywords");
        searchMap.put("keywords", keywords.replace(" ", ""));

        Map<String, Object> map = new HashMap<>();

        // 1. 按照关键字查询(高亮显示)
        map.putAll(searchList(searchMap));

        // 2. 根据关键字查询 商品分类
        List categoryList = searchCategoryList(searchMap);
        map.put("categoryList", categoryList);

        // 3. 根据分类名称 在缓存中 【查询品牌】 和 【规格列表】，为了提高容错性，添加一个判断
        String categoryName = (String)searchMap.get("category");
        if (!"".equals(categoryName)) {
            map.putAll(searchBrandAndSpecList(categoryName));
        } else {
            // 如果没有分类名称，默认是根据分组的第一个值进行查询，默认功能！
            if (categoryList.size() > 0) {
                map.putAll(searchBrandAndSpecList((String)categoryList.get(0)));
            }
        }

        return map;
    }

    /**
     * 向solr中导入数据
     */
    @Override
    public void importList(List list) {
        solrTemplate.saveBeans(list);
        solrTemplate.commit();
    }

    /**
     * 在solr中删除数据
     */
    @Override
    public void deleteByGoodsIds(List goodsIdList) {
        System.out.println("删除商品ID" + goodsIdList);
        Query query = new SimpleQuery();
        Criteria criteria = new Criteria("item_goodsid").in(goodsIdList);
        query.addCriteria(criteria);
        solrTemplate.delete(query);
        solrTemplate.commit();
    }

    /** 【很繁琐】按照关键字查询 复制域item_keywords 并 高亮显示 标题域item_title */
    private Map searchList (Map searchMap) {

        // 创建结果集容器
        Map map = new HashMap<>();

        // 1.创建对象高亮查询
        HighlightQuery query = new SimpleHighlightQuery();

        // 2. 配置查询的域【复制域，有很多的域】并将配置赋予给 查询对象
        Criteria criteria = new Criteria("item_keywords")
                .is(searchMap.get("keywords"));
        query.addCriteria(criteria);

        /*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        ~~~~~~~~~~~~~~~ 按照条件进行删选过滤 ~~~~~~~~~~~~~~~~~~~
        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */

        // 【a.】 根据 分类 进行过滤
        if (!"".equals(searchMap.get("category"))) {

            Criteria filterCriteria =  new Criteria("item_category").is(searchMap.get("category"));
            FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
            query.addFilterQuery(filterQuery);
        }

        // 【b.】 根据 品牌 进行过滤
        if (!"".equals(searchMap.get("brand"))) {

            Criteria filterCriteria =  new Criteria("item_brand").is(searchMap.get("brand"));
            FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
            query.addFilterQuery(filterQuery);
        }

        // 【c.】 根据 规格 进行过滤, 有多个规格，需要遍历操作！
        if (searchMap.get("spec") != null) {
            Map<String, String> specMap = (Map)(searchMap.get("spec"));

            for (String key : specMap.keySet()) {
                Criteria filterCriteria =  new Criteria("item_spec_" + key).is(specMap.get(key));
                FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
                query.addFilterQuery(filterQuery);
            }
        }

        // 【d.】 根据价格进行过滤，尽量让搜索结果多
        if (!"".equals(searchMap.get("price"))) {
            String[] price = ((String)searchMap.get("price")).split("-");
            if (!"0".equals(price[0])) {
                Criteria filterCriteria =  new Criteria("item_price").greaterThan(price[0]);
                FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
                query.addFilterQuery(filterQuery);
            }
            if (!"*".equals(price[1])) {
                Criteria filterCriteria =  new Criteria("item_price").lessThan(price[1]);
                FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
                query.addFilterQuery(filterQuery);
            }
        }

        // 【e.】 分页查询 需要提高容错性
        // 提取页码
        Integer pageNo = (Integer) searchMap.get("pageNo");
        if (pageNo == null || pageNo <= 0) {
            pageNo = 1;
        }

        // 每页记录数，根据前端排版美观的角度设计
        Integer pageSize = (Integer) searchMap.get("pageSize");
        if (pageSize == null || pageSize <= 0) {
            pageSize = 20;
        }

        // 根据查询的页数与每页显示条数计算开始索引
        query.setOffset((pageNo - 1) * pageSize);
        query.setRows(pageSize);


        /*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        ~~~~~~~~~~~~~~~~~~~~  排序处理  ~~~~~~~~~~~~~~~~~~~~~
        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */
        // 【a、】获取前端传入的排序规则 ASC DESC; 排序的字段【价格/跟新时间...】
        String sortValue = (String) searchMap.get("sort");
        String sortField = (String) searchMap.get("sortField");

        if (sortValue != null && !"".equals(sortValue) && sortField != null && !"".equals(sortField)) {
            if (sortValue.equals("ASC")) {
                // 升序排序， 第一个参数是枚举类型；第二个为被排序的域
                Sort sort = new Sort(Sort.Direction.ASC, "item_" + sortField);
                query.addSort(sort);
            }

            if (sortValue.equals("DESC")) {
                // 升序排序， 第一个参数是枚举类型；第二个为被排序的域
                Sort sort = new Sort(Sort.Direction.DESC, "item_" + sortField);
                query.addSort(sort);
            }
        }






        /*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */



        // 3. 设置高亮的域，可能有很多个域。可以通过链式编程添加域
        HighlightOptions highlightOptions = new HighlightOptions().addField("item_title");
        // 高亮前缀
        highlightOptions.setSimplePrefix("<em style='color:red'>");
        // 高亮后缀
        highlightOptions.setSimplePostfix("</em>");
        // 将高亮配置 赋予 给查询对象
        query.setHighlightOptions(highlightOptions);


        // 4. 按照关键字进行查询 获取返回结果
        HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query, TbItem.class);

        // 5. 循环【高亮入口】集合，获取每个【文档集合 对应 Items 实体类集合】
        // 注意：page.getContent() 获得的是原始没有高亮的内容
        for (HighlightEntry<TbItem> h : page.getHighlighted()) {

             /*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            // @@@@ ~:~API设计原理解释及演示~:~ @@@@ 继续上面的步骤！
            // 遍历【文档 -> Items 实体类 】，获取【域集合 -> 字段/属性】
            List<HighlightEntry.Highlight> h2 = h.getHighlights();

            // 遍历【域集合】，比如 复制域 ，与中有多个原始域【Map -> (key:value)】 ，所以需要集合存储！
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
        // 返回总页数
        map.put("totalPages", page.getTotalPages());
        // 返回总记录数
        map.put("total", page.getTotalElements());
        return map;
    }

    /** 【很繁琐】按照关键字查询 复制域item_keywords 并 根据 分类域item_category 查询 分类列表
     *  */
    private List searchCategoryList(Map searchMap) {
        // 创建结果集
        List<String> list = new ArrayList<>();

        // 1、创建查询对象
        Query query = new SimpleQuery();

        // 2、按照关键字进行查询
        Criteria criteria = new Criteria("item_keywords")
                .is(searchMap.get("keywords"));
        query.addCriteria(criteria);

        // 3、设置分组选项；支持链式编程，可以添加 多个分组 的条件，得到多个分组的结果
        GroupOptions groupOptions = new GroupOptions()
                .addGroupByField("item_category");
        query.setGroupOptions(groupOptions);

        // 4、得到查询分组页
        GroupPage<TbItem> page = solrTemplate.queryForGroupPage(query, TbItem.class);

        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~ 复杂设计导致的繁琐 ~~~~~~~~~~~~~~~~~~~~~~~~~~
        // 5~~~、获得指定 分组选项 的结果集，指定的分组条件是 上面第【3、】步中指定的分组条件之一！
        // 注意！((GroupPage<TbItem>)page).getContent() 是空内容，接口设计的不足问题导致的，必须实现的空方法！
        GroupResult<TbItem> groupResult = page.getGroupResult("item_category");

        // 6~~~、得到分组结果入口页
        Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();

        // 7~~~、得到分组入口集合
        List<GroupEntry<TbItem>> entryList = groupEntries.getContent();
        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

        // 8、遍历入口，将分组结果的名称封装到返回值中
        for (GroupEntry<TbItem> entry : entryList) {
            list.add(entry.getGroupValue());
        }

        return list;
    }

    /** 从Redis中 根据 商品分类 查询 模板id -> 品牌/规格 列表 */
    private Map searchBrandAndSpecList(String category) {
        // 存放查询结果的集合
        Map map = new HashMap();

        // 获取模板ID
        Long typeId = (Long)redisTemplate.boundHashOps("itemCat").get(category);

        // 为了提高容错性，这里加一个判断
        if (typeId != null) {

            // 根据模板ID查询品牌列表
            List brandList = (List) redisTemplate.boundHashOps("brandList").get(typeId);
            map.put("brandList", brandList);

            // 根据模板ID查询规格列表
            List specList = (List) redisTemplate.boundHashOps("specList").get(typeId);

            map.put("specList", specList);
        }

        return map;
    }
}
