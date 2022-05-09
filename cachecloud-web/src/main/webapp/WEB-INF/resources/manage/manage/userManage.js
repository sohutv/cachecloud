//验证手机号格式
var valPhones=/^((13[0-9])|(14[5,7])|(15[0-3,5-9])|(17[0,3,5-8])|(18[0-9])|166|198|199|(147))\d{8}$/;
//验证邮箱格式
var valEmails=/^(([a-zA-Z0-9]+[_|\_|\.]?)*[a-zA-Z0-9]+@([a-zA-Z0-9]+[_|\_|\.|\-]?)*[a-zA-Z0-9]+\.[a-zA-Z]{2,3};){0,6}([a-zA-Z0-9]+[_|\_|\.]?)*[a-zA-Z0-9]+@([a-zA-Z0-9]+[_|\_|\.|\-]?)*[a-zA-Z0-9]+\.[a-zA-Z]{2,3}$/;
function saveOrUpdateUser(userId, openFlag){
	var name = document.getElementById("name" + userId);
	var chName = document.getElementById("chName" + userId);
	var email = document.getElementById("email" + userId);
	var mobile = document.getElementById("mobile" + userId);
    var weChat = document.getElementById("weChat" + userId);
	var type = document.getElementById("type" + userId);
	var isAlert = document.getElementById("isAlert" + userId);
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

	if(openFlag){
		var companyDoc = document.getElementById("company" + userId);
		var purposeDoc = document.getElementById("purpose" + userId);
		if(companyDoc.value == ""){
			alert("公司名称不能为空!");
			companyDoc.focus();
			return false;
		}
		if(purposeDoc.value == ""){
			alert("使用目的不能为空!");
			purposeDoc.focus();
			return false;
		}
		company = companyDoc.value;
		purpose = purposeDoc.value;
	}

	var userBtn = document.getElementById("userBtn" + userId);
	userBtn.disabled = true;
	
	$.post(
		'/manage/user/add',
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

function updateUserPwd(userId){
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
		'/manage/user/updatePwd',
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

