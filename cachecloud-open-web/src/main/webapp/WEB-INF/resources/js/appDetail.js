
//重新加载appDetail页面
function reloadAppDetailPage(appId){
	location.href = "/admin/app/index.do?appId=" + appId + "&tabTag=app_detail";
}

//appDetail页面删除用户
function deleteAppUser(userId,appId){
	if(window.confirm('确认要删除该用户吗?!')){
		var url = "/admin/app/deleteAppToUser.do?userId="+userId+"&appId="+appId;
		$.ajax({
			type : "get",
			url : url,
			async : false,
			success : function(data) {
				alert("删除成功");
				reloadAppDetailPage(appId);
			}
		});
	}
	return false;
}


//改变应用信息
function updateAppDetailChange(appId){
	var appDescName = document.getElementById("appDescName");
	if(appDescName.value == ""){
		alert("应用名不能为空");
		appDescName.focus();
		return false;
	}
	var appDescIntro = document.getElementById("appDescIntro");
	if(appDescIntro.value == ""){
		alert("应用描述不能为空");
		appDescIntro.focus();
		return false;
	}
	var officer = document.getElementById("officer");
	if(officer.value == ""){
		alert("负责人不能为空");
		officer.focus();
		return false;
	}
	var updateAppDetailBtn = document.getElementById("updateAppDetailBtn");
	updateAppDetailBtn.disabled = true;
	$.post(
		'/admin/app/updateAppDetail.do',
		{
			appId: appId,
			appDescName: appDescName.value,
			appDescIntro: appDescIntro.value,
			officer: officer.value
		},
        function(data){
            if(data==1){
                alert("修改成功！");
            	$("#updateAppDetailInfo").html("<div class='alert alert-error' ><button class='close' data-dismiss='alert'>×</button><strong>Success!</strong>更新成功，窗口会自动关闭</div>");
                setTimeout("$('updateAppDetailModal').modal('hide');reloadAppDetailPage("+appId+");",1000);
            }else{
            	updateAppDetailBtn.disabled = false;
                $("#updateAppDetailInfo").html("<div class='alert alert-error' ><button class='close' data-dismiss='alert'>×</button><strong>Error!</strong>更新失败！</div>");
            }
        }
     );
}


//改变内存阀值
function appAlertConfigChange(appId){
	var memAlertValue = document.getElementById("memAlertValue");
	if(memAlertValue.value == ""){
		alert("内存报警阀值不能为空");
		memAlertValue.focus();
		return false;
	}
	var clientConnAlertValue = document.getElementById("clientConnAlertValue");
	if(clientConnAlertValue.value == ""){
		alert("客户端连接数报警阀值不能为空");
		clientConnAlertValue.focus();
		return false;
	}
	var appConfigChangeBtn = document.getElementById("appConfigChangeBtn");
	appConfigChangeBtn.disabled = true;
	$.post(
		'/admin/app/changeAppAlertConfig.do',
		{
			appId: appId,
			memAlertValue: memAlertValue.value,
			clientConnAlertValue: clientConnAlertValue.value
		},
        function(data){
            if(data==1){
                alert("修改成功！");
            	$("#appConfigChangeInfo").html("<div class='alert alert-error' ><button class='close' data-dismiss='alert'>×</button><strong>Success!</strong>更新成功，窗口会自动关闭</div>");
                setTimeout("$('appScaleApplyModal').modal('hide');reloadAppDetailPage("+appId+");",1000);
            }else{
            	appConfigChangeBtn.disabled = false;
                $("#appConfigChangeInfo").html("<div class='alert alert-error' ><button class='close' data-dismiss='alert'>×</button><strong>Error!</strong>更新失败！</div>");
            }
        }
     );
}

//添加应用用户
function appAddUser(appId){
	var userName = document.getElementById("userName").value;
	if(userName == ""){
		alert("用户名不能为空");
		return false;
	}
	var appAddUserBtn = document.getElementById("appAddUserBtn");
	appAddUserBtn.disabled = true;
	$.post(
		'/admin/app/addAppToUser.do',
		{
			appId: appId,
			userName: userName
		},
        function(data){
            if(data==1){
                alert("用户添加成功!");
            	$("#appAddUserInfo").html("<div class='alert alert-error' ><button class='close' data-dismiss='alert'>×</button><strong>Success!</strong>更新成功，窗口会自动关闭</div>");
                setTimeout("$('appAddUserModal').modal('hide');reloadAppDetailPage("+appId+");",1000);
            }else{
            	appAddUserBtn.disabled = false;
            	alert("cachecloud中不存在该用户，只能添加有cachecloud权限的用户");
                $("#appAddUserInfo").html("<div class='alert alert-error' ><button class='close' data-dismiss='alert'>×</button><strong>Error!</strong>添加失败,cachecloud中不存在该用户，只能添加有cachecloud权限的用户！</div>");
            }
        }
     );
}

//验证手机号格式
var valPhones=/^(1[3|5|8][0-9]\d{4,8};){0,6}(1[3|5|8][0-9]\d{4,8})$/; 
//验证邮箱格式
var valEmails=/^(([a-zA-Z0-9]+[_|\_|\.]?)*[a-zA-Z0-9]+@([a-zA-Z0-9]+[_|\_|\.|\-]?)*[a-zA-Z0-9]+\.[a-zA-Z]{2,3};){0,6}([a-zA-Z0-9]+[_|\_|\.]?)*[a-zA-Z0-9]+@([a-zA-Z0-9]+[_|\_|\.|\-]?)*[a-zA-Z0-9]+\.[a-zA-Z]{2,3}$/;
function saveOrUpdateUser(userId){
	var name = document.getElementById("name" + userId);
	var chName = document.getElementById("chName" + userId);
	var email = document.getElementById("email" + userId);
	var mobile = document.getElementById("mobile" + userId);
	var type = document.getElementById("type" + userId);
	if(name.value == ""){
    	alert("域账户名不能为空!");
		name.focus();
		return false;
    }
	if(chName.value == ""){
    	alert("中文名不能为空!");
		chName.focus();
		return false;
    }
	if(email.value == ""){
		alert("邮箱不能为空!");
		email.focus();
		return false;
	}
	if(!valEmails.test(email.value)){
		alert("邮箱格式错误!");
		email.focus();
		return false;
	}
	if(mobile.value == ""){
		alert("手机号不能为空!");
		mobile.focus();
		return false;
	}
	if(!valPhones.test(mobile.value)){
		alert("手机号格式错误!");
		mobile.focus();
		return false;
	}
	$.post(
		'/admin/app/changeAppUserInfo.do',
		{
			name: name.value,
			chName: chName.value,
			email: email.value,
			mobile: mobile.value,
			type: type.value,
			userId: userId
		},
        function(data){
            if(data==1){
                $("#info" + userId).html("<div class='alert alert-error' ><button class='close' data-dismiss='alert'>×</button><strong>Success!</strong>更新成功，窗口会自动关闭</div>");
                var targetId = "#addUserModal" + userId;
                setTimeout("$('" + targetId +"').modal('hide');reloadAppDetailPage("+appId+")",1000);
            }else{
                $("#info" + userId).html("<div class='alert alert-error' ><button class='close' data-dismiss='alert'>×</button><strong>Error!</strong>更新失败！</div>");
            }
        }
     );
}




