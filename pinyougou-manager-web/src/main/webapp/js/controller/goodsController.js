 //控制层 
app.controller('goodsController' ,function($scope,$controller   ,goodsService, itemCatService){
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		goodsService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		goodsService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//保存 
	$scope.save=function(){				
		var serviceObject;//服务层对象  				
		if($scope.entity.id!=null){//如果有ID
			serviceObject=goodsService.update( $scope.entity ); //修改  
		}else{
			serviceObject=goodsService.add( $scope.entity  );//增加 
		}				
		serviceObject.success(
			function(response){
				if(response.success){
					//重新查询 
		        	$scope.reloadList();//重新加载
				}else{
					alert(response.message);
				}
			}		
		);				
	}
	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		goodsService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
					$scope.reloadList();//刷新列表
				}						
			}		
		);				
	}
	
	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){			
		goodsService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}

    // 商品状态对应值
    $scope.status=['未审核', '已审核', '审核未通过', '关闭'];

    // 显示分类名称的方案
    /*
    方案1：在后端代码中写关联查询，返回的数据中直接有分类名称；效率低
    方案2：分类列表的数据量很小，可以将数据全部拿到，存到前端，以id为key,name为value的形式进行存储；
    */
    $scope.itemCatList = {};
    $scope.findItemCatList = function () {
        itemCatService.findAll().success(function (response) {
            for (var i = 0; i <response.length; i++) {
                $scope.itemCatList[response["" + i].id] = response["" + i].name;
            }
        })
    };

    // 查询商品详情，我难得写样式：
	// 想当初，写css，html 写吐了！我就不渲染，我就展示json数据！
    $scope.findOne=function(id){

        goodsService.findOne(id).success(
            function(response){
                $scope.entity= response;
                // 富文本编辑器
                // editor.html($scope.entity.goodsDesc.introduction);
				alert("富文本编辑：" + $scope.entity.goodsDesc.introduction)

                // 商品图片
                alert("图片地址：" + $scope.entity.goodsDesc.itemImages);

                // $scope.entity.goodsDesc.itemImages =
                //     JSON.parse($scope.entity.goodsDesc.itemImages);

                // 显示拓展属性
                alert("拓展属性：" + $scope.entity.goodsDesc.customAttributeItems);
                // $scope.entity.goodsDesc.customAttributeItems =
                //     JSON.parse($scope.entity.goodsDesc.customAttributeItems);

                // SKU 规格列表转换
				var skus;
                for (var i = 0; i < $scope.entity.itemList.length; i ++) {
                    skus += $scope.entity.itemList[i].spec;
                        $scope.entity.itemList[i].spec =
                        JSON.parse($scope.entity.itemList[i].spec);
                }
                alert("sku:" + skus)
            }
        );
    };

    // 更改状态
	$scope.updateStatus = function (status) {
		goodsService.updateStatus($scope.selectIds,status).success(function (response) {
			if (response.success) {
				// 成功之后，刷新列表
				$scope.reloadList();

				// 清空id集合
				$scope.selectIds = [];
			} else {
				alert(response.message);
			}
        })
    }
    
});	
