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
    };

    // 合计数

    // 获取地址列表
    $scope.findAddressList = function () {
        cartService.findAddressList().success(function (response) {
            $scope.addressList = response;
            // 默认地址显示
            for(var i = 0; i<$scope.addressList.length; i ++) {
                if ($scope.addressList[i].isDefault === "1") {
                    $scope.address = $scope.addressList[i];
                    break;
                }
            }
        })
    };

    // 选择地址
    $scope.selectAddress = function (address) {
        $scope.address = address;
    };

    // 判断是否是当前选中的地址
    $scope.isSelectedAddress = function (address) {
        if (address === $scope.address) {
            return true;
        } else {
            return false;
        }
    }

    // 新增地址
    $scope.add = function () {
        cartService.add($scope.entity).success(function (response) {
            if (response.success) {
                alert("添加成功");
                $scope.findAddressList()
            } else {
                alert("地址添加失败");
            }
        })
    }

    // 定义订单对象
    $scope.order = {paymentType: '1'};

    // 选择支付方式
    $scope.selectPayType = function (type) {
        $scope.order.paymentType = type;
    }

    // 保存订单
    $scope.submitOrder =function () {
        // 地址
        $scope.order.receiverAreaName = $scope.address.address;

        // 手机
        $scope.order.receiverMobile = $scope.address.mobile;

        cartService.submitOrder($scope.order).success(function (response) {
            if (response.success) {
                // 页面跳转 支付
                if ($scope.order.paymentType == "1") {
                    location.href = "pay.html";
                } else {
                    // 如果是货到付款，跳转到提示页面
                    location.href = "paysuccess.html"
                }
            } else {
                alert(response.message)
            }
        })
    }

});