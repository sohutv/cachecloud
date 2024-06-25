//驳回应用请求
function appRefuse(appAuditId, type, contextPath){
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
	if(type == 3){
		url = contextPath + "/manage/user/addAuditStatus";
	}else {
		url = contextPath + "/manage/app/addAuditStatus";
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

function testisNum(id){
   var value =document.getElementById(id).value;
   if(value != "" && isNaN(value)){
      alert("请输入数字类型!");
      document.getElementById(id).value="";
      document.getElementById(id).focus();
   }
}

//添加水平扩容验证
function checkHorizontalScale(contextPath){
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
		contextPath + '/manage/app/checkHorizontalScale.json',
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
function startHorizontalScale(contextPath){
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
		contextPath + '/manage/app/startHorizontalScale.json',
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
				window.location.href= contextPath + "/manage/app/handleHorizontalScale?appAuditId=" + appAuditId.value;
			}
        }
     );
	return true;
}

//重试水平扩容
function retryHorizontalScale(instanceReshardProcessId, contextPath){
	var appAuditId = document.getElementById("appAuditId");
	var retryButton = document.getElementById("retryBtn" + instanceReshardProcessId);
	instanceReshardProcessId.disabled = true;
	$.get(
		contextPath + '/manage/app/retryHorizontalScale.json',
		{
			instanceReshardProcessId: instanceReshardProcessId
		},
        function(data){
			var status = data.status;
			alert(data.message);
			if (status == 1) {
				window.location.href= contextPath + "/manage/app/handleHorizontalScale?appAuditId=" + appAuditId.value;
			}
        }
     );
	return true;
}

// step forward method
var method = "appLevel";
/**
 * 继续/下一步 step forward
 */
function goOn(m){
    //$("#install").html("继续");
    method = m;
}
/**
 *  跳过步骤
 */
function skip(currentMethod,nextMethod){
    active(currentMethod);
    complete(currentMethod);
    goOn(nextMethod);
    $("#skipbutton").attr("style","display:none;");
}

function warn(id){
    $("#"+id).addClass("warn");
}
function disable(id){
    $("#"+id).removeClass("active").addClass("disabled");
}
function active(id){
    $("#"+id).removeClass("disabled").removeClass("warn").addClass("active");
}
function complete(id){
    $("#"+id).removeClass("active").removeClass("warn").addClass("complete");
}

/**
 * 1.应用评级
 */
function appLevel(){
    complete("appLevel");
    $('#appLevelDiv').attr("style","display:none;");
    $('#redisVersionDiv').attr("style","display:'';");
    active('redisVersion');
    goOn("redisVersion");
}


/**
 * 2.redis版本
 */
function redisVersion() {
    complete("redisVersion");
    $('#redisVersionDiv').attr("style","display:none;");
    $('#appDeployDiv').attr("style","display:'';");
    active('appDeploy');
    goOn("appDeploy");
}

/**
 * 3.应用部署
 */
function appDeploy() {
	if($('#appDeployBtn').html()!='部署完成'){
        toastr.error("请完成应用部署操作");
		warn('appDeploy');
		return;
	}
    complete("appDeploy");
    $('#appDeployDiv').attr("style","display:none;");
    $('#appPasswordDiv').attr("style","display:'';");
    active('appPassword');
    goOn("appPassword");
}

/**
 * 4.应用密码
 */
function appPassword() {
    if($('#passUpdateBtn').html()!='设置成功'){
        toastr.error("请设置redis密码");
        warn('appPassword');
        return;
    }
    complete("appPassword");
    $('#appPasswordDiv').attr("style","display:none;");
    $('#installBtn').attr("style","display:none;");
    $('#appConfirmDiv').attr("style","display:'';");
    active('appConfirm');
    goOn("appConfirm");
}
/**
 * 5.应用审核
 */
function appConfirm() {
    if($('#confirmBtn').html()!='已通过'){
        toastr.error("请审核应用部署");
        warn('appConfirm');
        return;
    }
    complete("appConfirm");
    $('#appConfirmDiv').attr("style","display:none;");
    $('#deployCompleteDiv').attr("style","display:'';");
    active('deployComplete');
    $("#install").html("部署完成");

    goOn("deployComplete");
}
