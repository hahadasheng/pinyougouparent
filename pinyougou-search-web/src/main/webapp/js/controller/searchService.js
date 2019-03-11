app.service("searchService", function ($http) {

    // 搜索功能
    this.search = function (searchMap) {
        return $http.post("itemsearch/search.do", searchMap);
    }
});