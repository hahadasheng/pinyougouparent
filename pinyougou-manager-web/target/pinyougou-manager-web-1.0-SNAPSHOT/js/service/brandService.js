// 模块下的自定义服务，这里主要与后台进行 ajax 交互的抽离
app.service("brandService", function ($http) {
    // 分页查询品牌列表
    this.findPage = function (page, size) {
        return $http.get("../brand/findBrandPage.do?page=" + page + "&size=" + size);
    };

    // 条件查询 混合请求，post必须有请求体
    this.search = function (page, size, searchEntity) {
        return $http.post("../brand/findBrandPageOnCondition.do?page=" + page + "&size=" + size, searchEntity);
    };

    // 新增/修改品牌 $scope.entity 自定义上传的数据！
    this.saveBrand = function (method, entity) {
        return $http.post("../brand/" + method, entity);
    };

    // 根据品牌id 查询品牌信息
    this.findBrandById = function (id) {
        return $http.get("../brand/findBrandById.do?id=" + id);
    };

    // 删除 品牌请求
    this.deleteBrandByIds = function (selectIds) {
        return $http.get("../brand/deleteBrandByIds.do?ids=" + selectIds);
    };

    // 下拉数据列表
    this.selectOptionList = function () {
        return $http.get("../brand/selectOptionList.do")
    };

});