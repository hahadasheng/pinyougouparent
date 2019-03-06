 //控制层 
app.controller('goodsController' ,function($scope, $controller ,goodsService, uploadService, itemCatService, typeTemplateService){
	
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
	
	//查询实体 
	$scope.findOne=function(id){				
		goodsService.findOne(id).success(
			function(response){
				$scope.entity= response;					
			}
		);				
	}
	
	//保存 
	$scope.add=function(){
		// 获取富文本编辑器中的内容！
		$scope.entity.goodsDesc.introduction = editor.html();
        goodsService.add( $scope.entity  ).success(
			function(response){
				if(response.success){
					//重新查询 
		        	alert("保存成功!");
					$scope.entity = {};
					// 清空富文本编辑器
					editor.html("");
				}else{
					alert(response.message);
				}
			}		
		);				
	};
	
	 
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
	};

	// 上传文件服务
	$scope.uploadFile = function () {
        uploadService.uploadFile().success(function (response) {
			if (response.success) // 如果上传成功,取出url
				$scope.image_entity.url = response.message; // 设置文件地址
        	else {
        		alert(response.message)
			}

        }).error(function () {
			alert("上传失败！")
        })
    };

    // 初始化 定义页面实体结构
	$scope.entity={goods:{}, goodsDesc:{itemImages:[]}};

	// 添加图片列表
	$scope.add_image_entity = function () {
		$scope.entity.goodsDesc.itemImages.push(($scope.image_entity));
    };

    // 列表中移除图片
	$scope.remove_image_entity = function (index) {
        $scope.entity.goodsDesc.itemImages.splice(index, 1)
    };

	// 读取一级分类  语法很像 Python!
	/* ng-option 指令表达式 介绍 在 select 标签中使用
	   ng-option="item.id as item.name for item in itemList "
	    对应select  value    标签文本框内容    对象     容量列表
	    标签中的属性
	*/
	$scope.selectItemCat1List = function () {
        itemCatService.findByParentId(0).success(function (response) {
			$scope.itemCat1List = response;
        })
    };

    // 读取二级分类
	// angularJs提供的变量监听器，思想和 vue中的 钩子函数一样
	// 一切操作围绕 变量进行！
	$scope.$watch("entity.goods.category1Id", function (newValue, oldValue) {
		if (newValue !== undefined) {
            itemCatService.findByParentId(newValue).success(function (response) {
                $scope.itemCat2List = response;
            })
		}
    });

	// 读取三级分类
	$scope.$watch("entity.goods.category2Id", function (newValue, oldValue) {
		if (newValue !== undefined) {
            itemCatService.findByParentId(newValue).success(function (response) {
                $scope.itemCat3List = response;
            })
		}
    });

	// 读取模板id 监听三级分类
	$scope.$watch("entity.goods.category3Id", function (newValue, oldValue) {
		itemCatService.findOne(newValue).success(function (response) {
			// 更新模板ID
			$scope.entity.goods.typeTemplateId = response.typeId;
        })
    });

	// 模板ID选择后 跟新品牌列表
	$scope.$watch('entity.goods.typeTemplateId', function (newValue, oldValue) {
		if (newValue !== undefined) {
            typeTemplateService.findOne(newValue).success(function (response) {
				// 获取模板类型
				$scope.typeTemplate = response;

				// 后台传递的字符串转换为JSON  渲染品牌列表
				$scope.typeTemplate.brandIds = JSON.parse($scope.typeTemplate.brandIds)

				// 将模板中的扩展属性渲染到商品的扩展属性！
				$scope.entity.goodsDesc.customAttributeItems =
					JSON.parse($scope.typeTemplate.customAttributeItems)
            })
		}
    })
	



    
});	
