// angularJs模块
var app = angular.module("pinyougou",[]);
// 配置 过滤器,$sce 是安全策略服务
app.filter('trustHtml', ['$sce',function ($sce) {
    return function (data) {
        // 返回被过滤的数据
        return $sce.trustAsHtml(data)
    }
}]);