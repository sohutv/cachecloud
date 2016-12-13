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

function testisNum(id){
   var value =document.getElementById(id).value;
   if(value != "" && isNaN(value)){
      alert("请输入数字类型!");
      document.getElementById(id).value="";
      document.getElementById(id).focus();
   }
}

//添加水平扩容验证
function checkHorizontalScale(){
	var sourceId = document.getElementById("sourceId");
	if(sourceId.value == ""){
		alert("源实例ID不能为空");
		sourceId.focus();
		return false;
	}
	
	var targetId = document.getElementById("targetId");
	if(targetId.value == ""){
		alert("目标实例ID不能为空");
		targetId.focus();
		return false;
	}
	
	var startSlot = document.getElementById("startSlot");
	if(startSlot.value == ""){
		alert("开始slot不能为空");
		startSlot.focus();
		return false;
	}
	
	var endSlot = document.getElementById("endSlot");
	if(endSlot.value == ""){
		alert("结束slot不能为空");
		endSlot.focus();
		return false;
	}
	
	var migrateType = document.getElementById("migrateType");
	
	var appId = document.getElementById("appId");
	var appAuditId = document.getElementById("appAuditId");
	$.get(
		'/manage/app/checkHorizontalScale.json',
		{
			sourceId: sourceId.value,
			targetId: targetId.value,
			startSlot: startSlot.value,
			endSlot: endSlot.value,
			appId: appId.value,
			appAuditId: appAuditId.value,
			migrateType: migrateType.value
		},
        function(data){
			var status = data.status;
			alert(data.message);
			if (status == 1) {
				var submitButton = document.getElementById("submitButton");
	    		submitButton.disabled = false;
	    		
	    		var checkButton = document.getElementById("checkButton");
	    		checkButton.disabled = true;
	    		sourceId.disabled = true;
	    		targetId.disabled = true;
	    		startSlot.disabled = true;
	    		endSlot.disabled = true;
	    		migrateType.disabled = true;
			}
        }
     );
	return true;
}


//开始水平扩容
function startHorizontalScale(){
	var sourceId = document.getElementById("sourceId");
	var targetId = document.getElementById("targetId");
	var startSlot = document.getElementById("startSlot");
	var endSlot = document.getElementById("endSlot");
	var appId = document.getElementById("appId");
	var appAuditId = document.getElementById("appAuditId");
	var migrateType = document.getElementById("migrateType");
	
	var submitButton = document.getElementById("submitButton");
    submitButton.disabled = true;
	
	$.get(
		'/manage/app/startHorizontalScale.json',
		{
			sourceId: sourceId.value,
			targetId: targetId.value,
			startSlot: startSlot.value,
			endSlot: endSlot.value,
			appId: appId.value,
			appAuditId: appAuditId.value,
			migrateType: migrateType.value
		},
        function(data){
			var status = data.status;
			alert(data.message);
			if (status == 1) {
				window.location.href="/manage/app/handleHorizontalScale?appAuditId=" + appAuditId.value;
			}
        }
     );
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




