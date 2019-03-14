 //控制层 
app.controller('goodsController' ,function($scope, $controller, $location ,goodsService, uploadService, itemCatService, typeTemplateService){
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		goodsService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	};
	
	//分页
	$scope.findPage=function(page,rows){			
		goodsService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}
		);
	};
	
	//查询实体 
	$scope.findOne=function(){
		// 根据页面的跳转，获取angularJs封装的 $location获取哈希路由中的值
		var id = $location.search()['id'];

		if (id == null || id === undefined) {
			return;
		}

		goodsService.findOne(id).success(
			function(response){
				$scope.entity= response;

				// 富文本编辑器
				editor.html($scope.entity.goodsDesc.introduction);

				// 商品图片
				$scope.entity.goodsDesc.itemImages =
					JSON.parse($scope.entity.goodsDesc.itemImages);

				// 显示拓展属性
				$scope.entity.goodsDesc.customAttributeItems =
					JSON.parse($scope.entity.goodsDesc.customAttributeItems);

				// SKU 规格列表转换
				for (var i = 0; i < $scope.entity.itemList.length; i ++) {
					$scope.entity.itemList[i].spec =
						JSON.parse($scope.entity.itemList[i].spec);
				}
			}
		);				
	};
	
	//保存
	$scope.save=function(){
		// 获取富文本编辑器中的内容！
		$scope.entity.goodsDesc.introduction = editor.html();

		// 服务层对象
		var serviceObject;

		if ($scope.entity.goods.id != null) {
			// 有id为修改方法
            serviceObject = goodsService.update( $scope.entity);
		} else {
			// 添加商品
            serviceObject = goodsService.add( $scope.entity);
        }
        serviceObject.success(
			function(response){
				if(response.success){
					//重新查询
                    if ($scope.entity.goods.id != null) {
                        alert("修改成功!");
					} else {
                        alert("添加成功!");
					}

					// 跳转到商品列表页
					location.href = "goods.html";
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
	$scope.entity={goods:{}, goodsDesc:{itemImages:[],specificationItems:[]}};

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
				$scope.typeTemplate.brandIds = JSON.parse($scope.typeTemplate.brandIds);

				// 将模板中的扩展属性渲染到商品的扩展属性！
				// 确定是新增时执行，在修改时要防止此语句覆盖从后台读取的数据
				if ($location.search()["id"] === undefined || $location.search()["id"] == null ) {
                    $scope.entity.goodsDesc.customAttributeItems =
                        JSON.parse($scope.typeTemplate.customAttributeItems);
				}

				// 将规格反序列化成json对象
				$scope.entity.goodsDesc.specificationItems =
					JSON.parse($scope.entity.goodsDesc.specificationItems);

				//

            })
		}

		// 查询规格列表
		if (newValue !== undefined) {
            typeTemplateService.findSpecList(newValue).success(function (response) {
				$scope.specList = response;
            })
		};
    });




	/* 集合列表勾选对应的增删改！
	   $scope.entity.goodsDesc.specificationItems 对应大的 list  格式如下
	   [ {"attributeName":"网络制式","attributeValue":["移动3G","移动4G"]}, -- 大集合中的一个小对象为一个 object
	     {"attributeName":"屏幕尺寸","attributeValue":["6寸","5寸"]} ]
	  name 对应 attributeName 标识的值
	  value 对应 attributeValue 中的集合
	*/
	$scope.updateSpecAttribute = function ($event, name, value) {
		var object = $scope.searchObjectByKey(
            $scope.entity.goodsDesc.specificationItems, "attributeName", name
		);

		if (object != null) {
			// 根据事件的是否选中判断操作
			if ($event.target.checked) {
				object.attributeValue.push(value);
			} else {
				// 取消勾选
				object.attributeValue.splice(object.attributeValue.indexOf(value), 1);

				// 如果勾选后，列表为空，将此对象从大列表中删除
				if (object.attributeValue.length === 0) {
                    $scope.entity.goodsDesc.specificationItems.splice(
                        $scope.entity.goodsDesc.specificationItems.indexOf(object), 1
					);
				}
			}

		} else {
			// 原始列表 中不存在这个 对象
            $scope.entity.goodsDesc.specificationItems.push(
                {"attributeName":name,"attributeValue":[value]}
			)
		}

    };

	// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	// @@@@@@@@@@@@@@@@@@@@@@@@                              @@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ 【归零滚雪球思想】  @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	// @@@@@@@@@@@@@@@@@@@@@@@@                              @@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	/*
		启动时初始化 itemList sku列表；[{spec:{}, price: 0, num: 0, status:"0", isDefault:"0"}]
		遍历 items 属性列表，例如 [{"attributeName":"网络","attributeValue":["移动3G", "移动4G", "联通3G"]},{"attributeName":"机身内存","attributeValue":["16G"]}]
	    每遍历一个 item；遍历 itemList 列表，深克隆当前元素，遍历item下的attributeValue 列表，
	    向深克隆的列表spec属性中添加 attributeName：attributeValue

	*/
    // 创建SKU列表【难点】 @@@###@@@###@@@###@@@###@@@###@@@###@@@###@@@###@@@###@@@###@@@###
	$scope.createItemList = function () {
		/*  【！！初始重置SKU数据结构: @@@注意，每一次 启动都 会将 $scope.entity.itemList 清空重置！
		    然后根据 items 进行重新计算！这样可以避免重复！@@】

		    重置列表：[{spec:{}, price: 0, num: 0, status:"0", isDefault:"0"}]  */

		$scope.entity.itemList = [{spec:{}, price: 0, num: 0, status:"0", isDefault:"0"}];

		/* 属性选项表 items
		   [{"attributeName":"网络","attributeValue":["移动3G", "移动4G", "联通3G"]},
		    {"attributeName":"机身内存","attributeValue":["16G"]}
		   ]
		*/
		var items = $scope.entity.goodsDesc.specificationItems;

		/* 循环遍历 items 每遍历一个节点，创建该节点 */
		for (var i = 0; i < items.length; i ++) {
            $scope.entity.itemList = addColumn(
                $scope.entity.itemList, items[i].attributeName, items[i].attributeValue
			)
		}
	};

	/* 向列表中添加列值 {"attributeName":name,"attributeValue":[value]});
			columnName                columnValues
	 [ {"attributeName":"网络","attributeValue":["移动3G", "移动4G" "联通3G"}]

	 list: [
	       {spec:{"网络": "移动3G", "机身内存": "16G"}, price: 0, num: 0, status:"0", isDefault:"0"},
	       {spec:{"网络": "移动3G", "机身内存": "32G"}, price: 0, num: 0, status:"0", isDefault:"0"},
	       {spec:{"网络": "移动4G", "机身内存": "16G"}, price: 0, num: 0, status:"0", isDefault:"0"},
	       {spec:{"网络": "移动4G", "机身内存": "32G"}, price: 0, num: 0, status:"0", isDefault:"0"},
	       {spec:{"网络": "联通3G", "机身内存": "16G"}, price: 0, num: 0, status:"0", isDefault:"0"},
	       {spec:{"网络": "联通3G", "机身内存": "32G"}, price: 0, num: 0, status:"0", isDefault:"0"},
	 ]
	 */
	addColumn = function (list, columnName, columnValues) {
		// 新的集合
		var newList = [];

		for (var i = 0; i < list.length; i ++) {
			var oldRow = list[i];

			for (var j = 0; j < columnValues.length; j ++) {
				// 深克隆； 将对象转换为字符串 然后再转换为 对象，就是一个新的 内容
				// 一模一样的对象了
				var newRow = JSON.parse(JSON.stringify(oldRow));
				newRow.spec[columnName] = columnValues[j];
				newList.push(newRow);
			}
		}
		return newList;
    };

    // @@@###@@@###@@@###@@@###@@@###@@@###@@@###@@@###@@@###@@@###@@@###
    // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	// 补充 ；<input ng-model="pojo.status" ng-true-value="1" ng-false-value="1" type="checkbox" > 设置选中值！

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

    // 根据规格名称和选项名称返回是否被勾选 ng-checked属性，布尔值，判断
	$scope.checkAttributeValue = function (specName, optionName) {
		var items = $scope.entity.goodsDesc.specificationItems;

		var object = $scope.searchObjectByKey(items, 'attributeName', specName);
		if (object === null) {
			return false;
		} else {
			if (object.attributeValue.indexOf(optionName) >= 0) {
				return true;
			} else {
				return false;
			}
		}
    }


});	
