 //控制层 
app.controller('itemCatController' ,function($scope,$controller ,itemCatService, typeTemplateService){
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		itemCatService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	};
	
	//分页
	$scope.findPage=function(page,rows){			
		itemCatService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	};
	
	//查询实体 
	$scope.findOne=function(id){				
		itemCatService.findOne(id).success(
			function(response){
				$scope.entity= response;					
			}
		);				
	};

	$scope.reset = function() {
        $scope.entity={};
	};

	$scope.saveId = function(id) {
        $scope.entity.id = id;
        $scope.findOne(id);
	};

	//保存 
	$scope.save=function(){
		var serviceObject;//服务层对象
        $scope.entity.parentId = $scope.crumbs[$scope.crumbs.length - 1].parentId;
		if($scope.entity.id != null){//如果有ID
			serviceObject=itemCatService.update( $scope.entity ); // 修改
		}else{
			serviceObject=itemCatService.add($scope.entity);   // 增加
		}				
		serviceObject.success(
			function(response){
				if(response.success){
					//重新查询
                    $scope.findByParentId($scope.crumbs[$scope.crumbs.length - 1].parentId);
				}else{
					alert(response.message);
				}
			}		
		);				
	};

	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		itemCatService.dele($scope.selectIds).success(
			function(response){
				if(!response.success){
					alert(response.message);
				}
                $scope.findByParentId($scope.crumbs[$scope.crumbs.length - 1].parentId);

            }
		);
	};

	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){			
		itemCatService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	};

    //分级查询
    $scope.findByParentId=function(parentId){
        itemCatService.findByParentId(parentId).success(
            function(response){
                $scope.list= response;
            }
        );
    };

    // @@@@@@@@@@@@@@@@@@ 面包屑功能;这个版本更牛逼；扩展性更好 @@@@@@@@@@@@@@@@@
	// 初始化 面包屑 菜单
	$scope.crumbs = [{index: 0, parentId: 0,parentName: "顶级分类列表"}];

	// 点击查询下级触发的功能
	$scope.seekForward = function (entity) {
        var index = $scope.crumbs.length;
		var parentId = entity.id;
		var parentName = entity.name;
        $scope.crumbs.push({index:index, parentId: parentId, parentName: parentName});

        $scope.findByParentId(entity.id);
    };
    
    // 点击面包屑对应的标签，触发的事件
	$scope.seekRollBack = function (index) {
		// 将面包屑列表进行截取
        $scope.crumbs.splice(index + 1, $scope.crumbs.length - (index + 1));

        // 重新进行查询
        $scope.findByParentId($scope.crumbs[index].parentId);
    };

    // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@


	//查询 类型模板列表 typeTemplateService
	$scope.findAllTypeTemplate = function () {
        typeTemplateService.findAll().success(function (response) {
            $scope.templateList = response;
        })
    };

    $scope.findAllTypeTemplate();
});	
