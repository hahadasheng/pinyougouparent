app.controller("searchController", function ($scope, searchService) {

    // 搜索
    $scope.search = function () {
        searchService.search($scope.searchMap).success(function (response) {
            // 返回搜索的结果
            $scope.resultMap = response;
        })
    };

    // 定义 搜索对象 的 数据结构
    $scope.searchMap = {'keywords':'', 'category':'', 'brand':'', 'price':'', 'spec':{}};

    // 添加搜索项
    $scope.addSearchItem = function (key, value) {
        // 点击的为分类或者品牌的处理方式，要求key指定方式
        if (key === 'category' || key === 'brand' || key === 'price') {
            $scope.searchMap[key] = value;
        } else {
            // 处理规格
            $scope.searchMap.spec[key] = value;
        }

        // 每次删选条件就进行查询
        $scope.search();
    };

    // 移除复合搜索条件
    $scope.removeSearchItem = function (key) {
        // 点击的为分类或者品牌的处理方式，要求key指定方式
        if (key === 'category' || key === 'brand' || key === 'price') {
            $scope.searchMap[key] = "";
        } else {
            // 处理规格
            delete $scope.searchMap.spec[key];
        }
        // 每次删选条件就进行查询
        $scope.search();
    }

});

