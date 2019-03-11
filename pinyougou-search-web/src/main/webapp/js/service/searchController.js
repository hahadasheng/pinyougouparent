app.controller("searchController", function ($scope, searchService) {

    // 搜索
    $scope.search = function () {
        searchService.search($scope.searchMap).success(function (response) {
            // 返回搜索的结果
            $scope.resultMap = response;
        })
    }

});

