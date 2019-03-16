app.controller("itemController", function ($scope) {

    // 数量操作
    $scope.addNum = function (x) {
        $scope.num = $scope.num + x;

        if ($scope.num < 1) {
            $scope.num = 1;
        }
    };

    // 记录用户选择的规格
    $scope.specificationItems = {};

    // 用户选择规格
    $scope.selectSpecifiction = function (name, value) {
        if ($scope.specificationItems[name] == value) {
            $scope.specificationItems[name] = "";
            // 回归默认
            $scope.loadSku();
        } else {
            $scope.specificationItems[name] = value;
        }

        // 读取sku
        searchSku();
    }

    // 判断某规格选项是否被用户选中
    $scope.isSelected = function (name, value) {
        if ($scope.specificationItems[name] == value) {
            return true;
        } else {
            return false;
        }
    }

    // 加载默认SKU信息
    $scope.loadSku = function () {
        $scope.sku = skuList[0];
        // 避免指向同一个对象，原始数据与用户选择的数据应该是各自独立的
        $scope.specificationItems = JSON.parse(JSON.stringify($scope.sku.spec));
    }

    // 选择规格更新SKU
    // 匹配两个对象【方法很通用】
    matchObject = function (map1, map2) {
        for (var k in map1) {
            if (map1[k] !== map2[k]) {
                return false;
            }
        }
        for (var k in map2) {
            if (map2[k] !== map1[k]) {
                return false;
            }
        }

        return true;
    }

    // 在sku列表中查询当前用户选择的SKU
    searchSku = function () {
        for (var i = 0; i < skuList.length; i ++) {
            if (matchObject(skuList[i].spec, $scope.specificationItems)) {
                $scope.sku = skuList[i];
                return;
            }
        }

        // 如果没有匹配的
        $scope.sku = {id:0,title: "---",price: 0};
    }

    // 添加商品到购物车【后期完善】
    $scope.addToCart = function () {
        alert("skuid:" + $scope.sku.id);
    }



});