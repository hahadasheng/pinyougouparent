app.service("itemService", function ($http) {
    // 分页查询品牌列表
    this.findPage = function (page, size) {
        return $http.get("../brand/findBrandPage.do?page=" + page + "&size=" + size);
    };

});