
//重新加载appDetail页面
function reloadAppStatPage(appId){
	location.href = "/admin/app/index.do?appId=" + appId + "&tabTag=app_stat";
}


//申请应用扩容
function appScaleApply(appId){
	var applyMemSize = document.getElementById("applyMemSize");
	if(applyMemSize.value == ""){
		alert("请填写要扩容的容量!");
		applyMemSize.focus();
		return false;
	}
	
	var appScaleReason = document.getElementById("appScaleReason");
	if(appScaleReason.value == ""){
		alert("请填写申请扩容的原因!");
		appScaleReason.focus();
		return false;
	}
	var appScaleApplyBtn = document.getElementById("appScaleApplyBtn");
	appScaleApplyBtn.disabled = true;
	
	$.post(
		'/admin/app/scale.do',
		{
			appId: appId,
			applyMemSize: applyMemSize.value,
			appScaleReason: appScaleReason.value
		},
        function(data){
            if(data==1){
                alert("申请成功，请在邮件中关注申请状况.");
            	$("#appScaleApplyInfo").html("<div class='alert alert-error' ><button class='close' data-dismiss='alert'>×</button><strong>Success!</strong>更新成功，窗口会自动关闭</div>");
                setTimeout("$('appConfigChangeModal').modal('hide');reloadAppStatPage("+appId+");",1000);
            }else{
            	appScaleApplyBtn.disabled = false;
                $("#appScaleApplyInfo").html("<div class='alert alert-error' ><button class='close' data-dismiss='alert'>×</button><strong>Error!</strong>更新失败！</div>");
            }
        }
     );
}