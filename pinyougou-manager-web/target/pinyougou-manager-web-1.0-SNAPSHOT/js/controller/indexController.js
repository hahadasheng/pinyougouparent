app.controller("indexController", function ($scope, $controller, loginService) {
    $controller('baseController',{$scope:$scope}); //继承父类的controller 伪继承

    // 读取当前登陆人信息
    $scope.showLoginName = function () {
        loginService.loginName().success(function (response) {
            $scope.loginName = response.loginName;
        })
    }
});