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
	var userBtn = document.getElementById("userBtn" + userId);
	userBtn.disabled = true;
	
	$.post(
		'/manage/user/add.do',
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
                setTimeout("$('" + targetId +"').modal('hide');window.location.reload();",1000);
            }else{
            	userBtn.disabled = false;
                $("#info" + userId).html("<div class='alert alert-error' ><button class='close' data-dismiss='alert'>×</button><strong>Error!</strong>更新失败！</div>");
            }
        }
     );
}
