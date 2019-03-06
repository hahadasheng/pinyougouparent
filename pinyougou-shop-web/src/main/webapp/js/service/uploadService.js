app.service("uploadService", function ($http) {

    // 上传文件服务
    this.uploadFile = function () {

        // formDate是H5提供的负责文件上传的相关类
        var formData = new FormData();

        // ???? file变量表示文件上传框中的name属性！必须以这种 对象数组 的形式上传文件
        // 这好像是 input[type='file'] 默认的name属性
        formData.append("file", file.files[0]);

        return $http({
            method: "POST",
            url: "../upload.do",
            data: formData, // 指定上传的为H5文件对象类
            headers: {"Content-Type":undefined}, // 覆盖默认的Content-Type，等同于设置为 multipart/form-data
            transformRequest: angular.identity // anjularjs transformRequest function 将二进制序列化 formdata object
        });
    }


});
