app.controller("baseController", function ($scope) {
    // 控制器的伪继承，将公共的功能抽离出来，注意要将此js文件
    // 引入到实际的html页面上，否则不会被加载，浏览器就找不到！

    // 分页查询订单对象配置
    $scope.paginationConf = {
        currentPage: 1,// 当前页,当点击事件触发时，模块会自动更新此值
        totalItems: 10, // 总记录数
        itemsPerPage: 10, // 每页记录数
        perPageOptions: [10, 20, 30, 40, 50], // 分页选项
        onChange: function(){ // 当页面变更后自动触发的的方法,初始化时会被调用一次！所以不用写ng-init方法
            $scope.reloadList();//重新加载
        }
    };

    // 重新请求并渲染页面列表
    $scope.reloadList = function () {
        $scope.search($scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);
    };

    // 保存用户触发方法
    $scope.isAddNew = true;

    // 点击新建触发的方法
    $scope.clickNew = function() {
        $scope.entity = {};
        $scope.way = "新建";
        $scope.isAddNew = true;
    };

    // 存放待删除的 品牌的 id
    $scope.selectIds = [];

    // 更新复选框;$event代表的触发事件对象，封装了触发事件的原生dom对象
    $scope.updateSelection = function ($event, id) {
        if ($event.target.checked) {
            // 此种情况是复选框被选中了，需要添加
            $scope.selectIds.push(id);
        } else {
            var index = $scope.selectIds.indexOf(id);
            $scope.selectIds.splice(index, 1); // 根据索引位置删除指定个数元素
        }
    };

    // 将字符串转化为json并获取指定的值
    $scope.jsonToString = function(string, key) {
        var json = JSON.parse(string);
        var value = "";
        for (var i = 0; i < json.length; i++) {
            if (i > 0) {
                value += ","
            }

            value += json[i][key];
        }

        return value;
    }

});