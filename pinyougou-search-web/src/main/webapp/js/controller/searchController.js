app.controller("searchController", function ($scope, $location, searchService) {

    // 定义 搜索对象 的 数据结构
    $scope.searchMap = {
        'keywords':'', 'category':'', 'brand':'', 'price':'','pageNo':1,'pageSize':40,
        'sortField':'', 'sort':'',
        'spec':{}
    };

    // 搜索
    $scope.search = function () {
        $scope.searchMap.pageNo = parseInt($scope.searchMap.pageNo);
        searchService.search($scope.searchMap).success(function (response) {
            // 返回搜索的结果
            $scope.resultMap = response;
            // 构建分页渲染
            buildPageLable();
        })
    };
    /*
    * $scope.resultMap = {
    *       brandList:[],
    *       categoryList:[],
    *       rows:[],
    *       specList:[],
    *       total: 100,
    *       totalPages: 4
    * }
    * */

    // 根据返回结构构建分页渲染数据
    buildPageLable = function() {
        // 新增分页栏属性
        $scope.pageLable = [];

        // 得到最终页码
        $scope.maxPageNo = $scope.resultMap.totalPages;

        // 初始化起始页码
        $scope.firstPage = 1;

        // 初始化截止页码
        $scope.lastPage = $scope.maxPageNo;

        // 如果总页数大于5页，显示部分页码，否则就是使用默认的1~<=5
        if ($scope.resultMap.totalPages > 5) {
            // 如果当前页小于等于三
            if ($scope.searchMap.pageNo <= 3)  {
                $scope.lastPage = 5;
            } else if ($scope.searchMap.pageNo >= $scope.lastPage - 2) {
                // 如果当前页大于等于最大页码减去2
                // 取后5页
                $scope.firstPage = $scope.maxPageNo - 4;
            } else  {
                // 显示当前页为中心的5页
                $scope.firstPage = $scope.searchMap.pageNo - 2;
                $scope.lastPage = $scope.searchMap.pageNo + 2;
            }
        }

        // 循环产生页码标签
        for (var i = $scope.firstPage; i <= $scope.lastPage; i ++) {
            $scope.pageLable.push(i);
        }

        $scope.searchMap.pageNo = 1;
    };

    // 根据页码进行查询
    $scope.queryByPage =function(pageNo) {
        // 页码验证
        if (pageNo < 1 || pageNo > $scope.resultMap.totalPages) {
            return;
        }
        
        $scope.searchMap.pageNo = pageNo;
        $scope.search();
        
    };

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

    // 设置排序规则
    $scope.sortSearch = function (sortField, sort) {
        $scope.searchMap.sortField = sortField;
        $scope.searchMap.sort = sort;
        $scope.search();
    };

    // 品牌选项处理
    $scope.keywordsContainBrand = function () {
        for (var i = 0; i < $scope.resultMap.brandList.length; i ++) {
            if ($scope.searchMap.keywords.indexOf($scope.resultMap.brandList[i].text) >= 0) {
                // 如果包含
                return true;
            }
        }

        return false;
    }

    // 接收参数查询字符串
    $scope.loadkeywords = function () {
        $scope.searchMap.keywords = $location.search()["keywords"];
        $scope.search();
    }

});

