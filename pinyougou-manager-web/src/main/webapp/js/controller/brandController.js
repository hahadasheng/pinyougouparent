// 相关模块下的控制器
app.controller("brandController", function ($scope, $controller, brandService) {

    /*
    加载公共的 com.lingting.controller,
    {$scope:$scope} 表示将两个控制器的作用域联通起来，
    让此作用域能直接访问公共的 com.lingting.controller!
    【注意】：在html页面上要引用 公共的controller,否则找不到资源！
    */
    $controller("baseController", {$scope:$scope});


    /* 查询所有订单
    $scope.findAll = function () {
        $http.get("../brand/findAll.do").success(function (response) {
            $scope.list = response;
        })
    }; */


    // 分页查询品牌列表
    $scope.findPage = function (page, size) {
        brandService.findPage(page, size).success(function (response) {
            $scope.list = response.rows;
            $scope.paginationConf.totalItems = response.total;
        })
    };

    // 条件查询 混合请求，post必须有请求体
    $scope.searchEntity = {}; // 初始化
    $scope.search = function (page, size) {
        brandService.search(page, size, $scope.searchEntity).success(function (response) {
            $scope.list = response.rows;
            $scope.paginationConf.totalItems = response.total;
        })
    };


    // 新增/修改品牌 $scope.entity 自定义上传的数据！
    $scope.saveBrand = function () {
        var method = "";
        if ($scope.isAddNew) {
            // 新增 品牌
            method = "addBrand.do";
        } else {
            // 修改 品牌
            method = "updateBrand.do";
        }

        brandService.saveBrand(method, $scope.entity).success(function (response) {
            if (response.success) {
                $scope.reloadList(); //重新渲染当前页面
            } else {
                alert(response.message)
            }
        });
    };

    // 根据品牌id 查询品牌信息
    $scope.findBrandById = function (id) {
        $scope.way = "更新";
        $scope.isAddNew = false;
        brandService.findBrandById(id).success(function (response) {
            $scope.entity = response;
        });
    };


    // 删除 品牌请求
    $scope.deleteBrandByIds = function () {
        // confirm 要写在 if判断语句中才生效？写在外边不会生效？
        if (confirm('确定要删除？')) {
            brandService.deleteBrandByIds($scope.selectIds).success(function (response) {
                if (response.success) {
                    $scope.reloadList(); //重新渲染当前页面
                } else {
                    alert(response.message);
                }
            })
        }
    }
})