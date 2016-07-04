//驳回应用请求
function appRefuse(appAuditId, type){
	//驳回
	var status = -1;
	//驳回理由
	var refuseReason = document.getElementById("refuseReason" + appAuditId);
	if(refuseReason.value == ""){
		alert("驳回理由不能为空");
		refuseReason.focus();
		return false;
	}
	
	var appRefuseBtn = document.getElementById("appRefuseBtn" + appAuditId);
	appRefuseBtn.disabled = true;
	
	var url = "";
	if(type == 0 || type == 1 || type == 2){
		url = "/manage/app/addAuditStatus.do";
	//用户申请
	}else if(type == 3){
		url = "/manage/user/addAuditStatus.do";
	}
	$.post(
		url,
		{
			appAuditId: appAuditId,
			refuseReason: refuseReason.value,
			status: status
		},
        function(data){
            if(data==1){
            	$("#appRefuseInfo"+appAuditId).html("<div class='alert alert-error' ><button class='close' data-dismiss='alert'>×</button><strong>Success!</strong>更新成功，窗口会自动关闭</div>");
                $('appRefuseModal'+appAuditId).modal('hide');
            	setTimeout("window.location.reload()",1000);
            }else{
            	appRefuseBtn.disabled = false;
                $("#appRefuseInfo"+appAuditId).html("<div class='alert alert-error' ><button class='close' data-dismiss='alert'>×</button><strong>Error!</strong>更新失败！</div>");
            }
        }
     );
}

//检查配置项
function checkAppConfig(){
	//配置项
	var appConfigKey = document.getElementById("appConfigKey");
	if(appConfigKey.value == ""){
		alert("配置项不能为空");
		appConfigKey.focus();
		return false;
	}
	
	//配置值
	var appConfigValue = document.getElementById("appConfigValue");
	if(appConfigValue.value == ""){
		alert("配置值不能为空");
		appConfigValue.focus();
		return false;
	}
	return true;
}

//检查配置项
function checkInstanceConfig(){
	//配置项
	var instanceConfigKey = document.getElementById("instanceConfigKey");
	if(instanceConfigKey.value == ""){
		alert("配置项不能为空");
		instanceConfigKey.focus();
		return false;
	}
	
	//配置值
	var instanceConfigValue = document.getElementById("instanceConfigValue");
	if(instanceConfigValue.value == ""){
		alert("配置值不能为空");
		instanceConfigValue.focus();
		return false;
	}
	return true;
}


//检查扩容配置
function checkAppScaleText(){
	var appScaleText = document.getElementById("appScaleText");
	if(appScaleText.value == ""){
		alert("配置不能为空");
		appScaleText.focus();
		return false;
	}
	return true;
}

function startShowDeployLabel(){
	var startDeployLabel = document.getElementById("startDeployLabel");
	startDeployLabel.innerHTML += '.';
}

//检查应用部署配置
function checkAppDeployText(){
	var appDeployText = document.getElementById("appDeployText");
	if(appDeployText.value == ""){
		alert("应用部署信息不能为空");
		appDeployText.focus();
		return false;
	}
	var appAuditId = document.getElementById("appAuditId");
	$.get(
		'/manage/app/appDeployCheck.json',
		{
			appAuditId: appAuditId.value,
			appDeployText: appDeployText.value
		},
        function(data){
			var status = data.status;
			alert(data.message);
			if (status == 1) {
				var appDeployBtn = document.getElementById("appDeployBtn");
				appDeployBtn.disabled = false;
	    		
	    		var appCheckBtn = document.getElementById("appCheckBtn");
	    		appCheckBtn.disabled = true;
	    		
	    		appDeployText.disabled = true;
			} else {
				appDeployText.focus();
			}
        }
     );
}

function addAppDeployText() {
	var appDeployBtn = document.getElementById("appDeployBtn");
	appDeployBtn.disabled = true;
	
	var appDeployText = document.getElementById("appDeployText");
	var appAuditId = document.getElementById("appAuditId");
	
	var startDeployLabel = document.getElementById("startDeployLabel");
	startDeployLabel.innerHTML = '正在部署,请等待.';
	
	$.get(
		'/manage/app/addAppDeploy.json',
		{
			appAuditId: appAuditId.value,
			appDeployText: appDeployText.value
		},
        function(data){
			var status = data.status;
			if (status == 1) {
				alert("应用部署成功,确认后将跳转到审核界面,点击[通过]按钮即可!");
			} else {
				alert("应用部署失败,请查看系统日志确认相关原因!");
			}
			window.location.href="/manage/app/auditList";
        }
     );
	//展示简单的进度条
	setInterval(startShowDeployLabel,500);
}

//添加分片验证
function checkAddShardParam(){
	var masterSizeSlave = document.getElementById("masterSizeSlave");
	if(masterSizeSlave.value == ""){
		alert("主从分片配置不能为空");
		masterSizeSlave.focus();
		return false;
	}
	
	return true;
}

//添加水平扩容验证
function checkHorizontalScaleParam(){
	var ip = document.getElementById("ip");
	if(ip.value == ""){
		alert("ip不能为空");
		ip.focus();
		return false;
	}
	
	var port = document.getElementById("port");
	if(port.value == ""){
		alert("port不能为空");
		port.focus();
		return false;
	}
	
	return true;
}
//添加下线分片验证
function checkOffLineInstanceParam(){
	var ip = document.getElementById("dropIp");
	if(ip.value == ""){
		alert("ip不能为空");
		ip.focus();
		return false;
	}
	
	var port = document.getElementById("dropPort");
	if(port.value == ""){
		alert("port不能为空");
		port.focus();
		return false;
	}
	return true;
}




