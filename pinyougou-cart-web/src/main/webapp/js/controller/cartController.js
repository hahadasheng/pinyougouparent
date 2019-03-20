app.controller("cartController", function ($scope, cartService) {
    // 查询购物车列表
    $scope.findCartList = function () {
        cartService.findCarList().success(function (response) {
            $scope.cartList = response;

            // 求合计数【@@@@@@@ 有待优化！@@@@@@@@@】
            $scope.totalValue = cartService.sum($scope.cartList);
        })
    };

    // 添加商品到购物车
    $scope.addGoodsToCartList = function (itemId, num) {
        cartService.addGoodsToCartList(itemId, num).success(
            function (response) {

                if (response.success) {
                    // 刷新列表
                    $scope.findCartList();
                } else {
                    // 弹出错误信息
                    alert(response.message);
                }
            }
        )
    }

    // 合计数

});