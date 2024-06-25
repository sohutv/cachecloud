//验证手机号格式
var valPhones=/^((13[0-9])|(14[5,7])|(15[0-3,5-9])|(17[0,3,5-8])|(18[0-9])|166|198|199|(147))\d{8}$/;
//验证邮箱格式
var valEmails=/^(([a-zA-Z0-9]+[_|\_|\.]?)*[a-zA-Z0-9]+@([a-zA-Z0-9]+[_|\_|\.|\-]?)*[a-zA-Z0-9]+\.[a-zA-Z]{2,3};){0,6}([a-zA-Z0-9]+[_|\_|\.]?)*[a-zA-Z0-9]+@([a-zA-Z0-9]+[_|\_|\.|\-]?)*[a-zA-Z0-9]+\.[a-zA-Z]{2,3}$/;
function saveOrUpdateUser(userId, contextPath){
	var name = document.getElementById("user_name" + userId);
	var chName = document.getElementById("chName" + userId);
	var email = document.getElementById("email" + userId);
	var mobile = document.getElementById("mobile" + userId);
    var weChat = document.getElementById("weChat" + userId);
	var type = document.getElementById("type" + userId);
	var isAlert = document.getElementById("isAlert" + userId);
	var bizId = document.getElementById("bizId" + userId);
	var company = "";
	var purpose = "";
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
    if(weChat.value == ""){
        alert("微信不能为空!");
        weChat.focus();
        return false;
    }

	var userBtn = document.getElementById("userBtn" + userId);
	userBtn.disabled = true;
	
	$.post(
		contextPath + '/manage/user/add',
		{
			name: name.value,
			chName: chName.value,
			email: email.value,
			mobile: mobile.value,
            weChat: weChat.value,
			type: type.value,
            isAlert: isAlert.value,
            company: company,
            purpose: purpose,
			bizId: bizId.value,
			userId: userId
		},
        function(data){
            if(data==1){
                $("#info" + userId).html("<div class='alert alert-error' ><button class='close' data-dismiss='alert'>×</button><strong>Success!</strong>更新成功，窗口会自动关闭</div>");
                var targetId = "#addUserModal" + userId;
                setTimeout("$('" + targetId +"').modal('hide');window.location.reload();",1000);
            }else{
            	userBtn.disabled = false;
                $("#info" + userId).html("<div class='alert alert-error' ><button class='close' data-dismiss='alert'>×</button><strong>Error!</strong>更新失败！</div>");
            }
        }
     );
}

//验证密码强度
var pwdRegex = new RegExp('(?=.*[0-9])(?=.*[a-zA-Z]).{8,30}');
function checkPassword(userId) {
	var password = $('#updateUserPwdPassword' + userId).val();
	if (!pwdRegex.test(password)) {
		alert("您的密码复杂度太低，密码中必须包含字母、数字、特殊字符至少8个字符，请修改密码！");
		$('#password').focus();
		return false;
	}
	return true;
}

function checkConfirmPassword(userId) {
	var password = $('#updateUserPwdPassword' + userId).val();
	var password1 = $('#updateUserPwdPassword0' + userId).val();
	if (password != password1) {
		alert("两次密码输入不一致，请确认密码！")
		$('#updateUserPwdPassword' + userId).focus();
		return false;
	}
	return true;
}

function updateUserPwd(userId, contextPath){
	var password = document.getElementById("updateUserPwdPassword" + userId);
	var password0 = document.getElementById("updateUserPwdPassword0" + userId);
	if(password.value == ""){
		alert("密码不能为空!");
		password.focus();
		return false;
	}

	var userBtn = document.getElementById("updateUserPwdBtn" + userId);
	userBtn.disabled = true;

	$.post(
		contextPath + '/manage/user/updatePwd',
		{
			password: $.md5(password.value),
			userId: userId
		},
		function(data){
			if(data==1){
				$("#info" + userId).html("<div class='alert alert-error' ><button class='close' data-dismiss='alert'>×</button><strong>Success!</strong>更新成功，窗口会自动关闭</div>");
				var targetId = "#updateUserPwdModal" + userId;
				setTimeout("$('" + targetId +"').modal('hide');window.location.reload();",1000);
			}else{
				userBtn.disabled = false;
				$("#info" + userId).html("<div class='alert alert-error' ><button class='close' data-dismiss='alert'>×</button><strong>Error!</strong>更新失败！</div>");
			}
		}
	);
}

function takeOverUser(contextPath){
	var toRemoveUser = document.getElementById("toRemoveUser");
	var toChargeUser = document.getElementById("toChargeUser");
	if(toRemoveUser.value == ""){
		alert("待移交用户，域账户名不能为空!");
		toRemoveUser.focus();
		return false;
	}
	if(toChargeUser.value == ""){
		alert("接手用户，域账户名不能为空!");
		toChargeUser.focus();
		return false;
	}
	if(toRemoveUser.value == toChargeUser.value){
		alert("待移交用户与接收用户不能为同一人!");
		toRemoveUser.focus();
		return false;
	}

	var overUserBtn = document.getElementById("overUserBtn");
	overUserBtn.disabled = true;

	$.post(
		contextPath + '/manage/user/takeover',
		{
			toRemoverUserName: toRemoveUser.value,
			toChargeUserName: toChargeUser.value
		},
		function(data){
			if(data==1){
				$("#takeoverInfo").html("<div class='alert alert-error' ><button class='close' data-dismiss='alert'>×</button><strong>Success!</strong>接手成功，窗口会自动关闭</div>");
				var targetId = "#overUserModal";
				setTimeout("$('" + targetId +"').modal('hide');window.location.reload();",1000);
			}else{
				overUserBtn.disabled = false;
				$("#takeoverInfo").html("<div class='alert alert-error' ><button class='close' data-dismiss='alert'>×</button><strong>Error!</strong>接手失败！</div>");
			}
		}
	);
}

function saveOrUpdateBiz(bizId, contextPath){
	var bizName = document.getElementById("biz_name" + bizId);
	var bizDesc = document.getElementById("biz_desc" + bizId);
	if(bizName.value == ""){
		alert("业务组名称不能为空!");
		bizName.focus();
		return false;
	}

	var bizBtn = document.getElementById("bizBtn" + bizId);
	bizBtn.disabled = true;

	$.post(
		contextPath + '/manage/user/biz/add',
		{
			name: bizName.value,
			bizDesc: bizDesc.value,
			bizId: bizId
		},
		function(data){
			if(data==1){
				$("#info" + bizId).html("<div class='alert alert-error' ><button class='close' data-dismiss='alert'>×</button><strong>Success!</strong>更新成功，窗口会自动关闭</div>");
				var targetId = "#addBizModal" + bizId;
				setTimeout("$('" + targetId +"').modal('hide');window.location.reload();",1000);
			}else{
				bizBtn.disabled = false;
				$("#info" + bizId).html("<div class='alert alert-error' ><button class='close' data-dismiss='alert'>×</button><strong>Error!</strong>更新失败！</div>");
			}
		}
	);
}


