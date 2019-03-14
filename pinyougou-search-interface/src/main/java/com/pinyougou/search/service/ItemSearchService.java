package com.pinyougou.search.service;

import java.util.List;
import java.util.Map;

public interface ItemSearchService {

    /**
     * 搜索
     */
    public Map<String, Object> search(Map searchMap);

    /**
     * 向solr中导入数据
     */
    public void importList(List list);

    /**
     * 在solr中删除数据
     */
    public void deleteByGoodsIds(List goodsIdList);

}
