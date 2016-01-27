/** 提交登录请求*/
function doLoginAuth() {
	//数据验证
	var errBeforeSubmit = "";
	if(document.getElementById("devUser").value==null || document.getElementById("devUser").value=="") {
		errBeforeSubmit = "用户名不能为空. ";
	} 
	if(document.getElementById("devPwd").value==null || document.getElementById("devPwd").value=="") {
		errBeforeSubmit += "密码不能为空. ";
	} 
	if(errBeforeSubmit != "") {
		showNotify(errBeforeSubmit);
		return ;
	}
	//构建HTTP请求参数
	var request = { "u" :  document.getElementById("devUser").value,
					"p" :  document.getElementById("devPwd").value
				};
	//构建Ajax请求提交表单信息
	$.ajax({url:"/memcloud/session-create.json",
			type: "post",//POST方式提交表单内容
			timeout : 8000,//响应超时间8秒
			async : false,//同步提交
			cache : false,//禁止浏览器缓存
			contentType : "application/x-www-form-urlencoded; charset=UTF-8",//明确告诉服务器协商编码方式，以免服务器端出现中文乱码
			beforeSend : function(xmlHttpRequest){//请求发送前的逻辑
				showNotify("正在登录...");
				document.getElementById("btnLogin").disabled = true;
            },
			dataType : "json",
			data : request,//HTTP请求数据（表单数据）
			success : function(response,httpStatus) {//HTTP响应成功后的处理逻辑
				if(response["status"]==200) {//登录成功
					location.href = "/memcloud/session.html";//登录后去当行页
				} else {
					showNotify("登录失败："+response["data"]);//返回码：response["status"]
					document.getElementById("btnLogin").disabled = false;
				}
			}
		});
}

function hideNotify() {
	document.getElementById("devNotify").style.display ='none';
}

function showNotify(notification) {
	document.getElementById("devNotify").innerHTML=notification;
	document.getElementById("devNotify").style.display ='';
}
