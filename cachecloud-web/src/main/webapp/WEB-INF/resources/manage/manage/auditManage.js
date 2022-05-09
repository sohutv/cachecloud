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
	if(type == 3){
		url = "/manage/user/addAuditStatus";
	}else {
		url = "/manage/app/addAuditStatus";
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

//生成部署详情
function getDeployInfo() {
    var flag=1;

	var appType=document.getElementById("appType");
	var isSlave=document.getElementById("isSalve");
	var size=document.getElementById("size");
	var machineNum=document.getElementById("machineNum");
    var instanceNum=document.getElementById("instanceNum");
	var useType=document.getElementById("useType");
	var room=document.getElementById("room");
    var machines=$('#machines').val();
    var excludeMachines=$('#excludeMachines').val();
    var sentinelMachineList=$('#sentinelMachineList').val();

	var notemachines=document.getElementById("notemachines");
	var notesize=document.getElementById("notesize");
	var noteMachineNum=document.getElementById("noteMachineNum");
	var noteSentinelMachines=document.getElementById('noteSentinelMachines');
	var notenum=document.getElementById("notenum");
	notemachines.style.display='none';
	notesize.style.display='none';
    noteMachineNum.style.display='none';
    noteSentinelMachines.style.display='none';
	notenum.style.display='none';

	if(size.value==''){
        notesize.style.display='';
        flag=0;
	}
    if(machineNum.value==''){
        noteMachineNum.style.display='';
        flag=0;
    }
	if(instanceNum.value==''){
        notenum.style.display='';
		flag=0;
	}
	if(useType.value==0){//专用
		if(machines==null){
            notemachines.style.display='';
            flag=0;
		}
	}
	if(appType.value==5){//sentinel
		var count=sentinelMachineList==null?0:sentinelMachineList.length;
        // alert("count: "+count);
        if(count<3||count%2==0){
            noteSentinelMachines.style.display='';
            flag=0;
        }

    }

    if(appType.value!=6&&isSlave.value==1&&machineNum.value<2){
		alert("需要生成从节点，则机器数需大于1")
		flag=0;
	}

	if(flag==0) return;

	//清除上次记录
	$('#tableList tbody tr').empty();
    var appDeployText = document.getElementById("appDeployText");
    appDeployText.value="";
    var appDeployInfo = document.getElementById("appDeployInfo");
    appDeployInfo.value="";

    var startDeployInfoLabel = document.getElementById("startDeployInfoLabel");
    startDeployInfoLabel.innerHTML = '<br/>正在生成部署预览,请等待...';
	$.post(
        '/manage/app/getDeployInfo.json',
		{
			type: appType.value,
			isSalve: isSlave.value,
            room: room.value,
            size: size.value,
            machineNum: machineNum.value,
			instanceNum: instanceNum.value,
            useType: useType.value,
            machines: machines==null?null:machines.toString(),
            excludeMachines: excludeMachines==null?null:excludeMachines.toString(),
            sentinelMachines: sentinelMachineList==null?null:sentinelMachineList.toString()
		},
		function (data) {
            startDeployInfoLabel.innerHTML='';
        	if(data.result!='success'){
        		alert("生成部署预览发生错误："+data.result);
			}
			else {
                /**
				 * 展示机器详情
                 */
                var resMachines=data.resMachines;
                var machineDeployStatMap=data.machineDeployStatMap;
                resMachines.forEach(function (machine) {
                    var machineDeployStat=machineDeployStatMap[machine.ip];
                    $("#tableList tbody").prepend(
                        '<tr class="odd gradeX">\n' +
                        "   <td> <a target='_blank' href='/manage/machine/machineInstances?ip="+machine.ip+" '>"+machine.ip+"</a>\n" +
                        '   <td>'+machine.mem+'G</td>\n' +
                        '   <td ><font color="#FF0000">'+(machine.mem-(machine.applyMem/1024/1024/1024)).toFixed(2)+'G </font> </td>\n' +
                        '   <td>'+machine.instanceNum+"&nbsp;&nbsp;/&nbsp;&nbsp;"+machine.cpu+'</td>\n' +
                        '   <td>'+machineDeployStat.masterNum+"&nbsp;&nbsp;/&nbsp;&nbsp;"+machineDeployStat.slaveNum+"&nbsp;&nbsp;/&nbsp;&nbsp;"+machineDeployStat.sentinelNum+'</td>\n' +
                        '   <td>'+machine.realIp+"&nbsp;&nbsp;/&nbsp;&nbsp;"+machine.rack+'</td>\n' +
                        "   <td> <a target='_blank' href='/manage/machine/index?tabTag=machine&ipLike="+machine.ip+" '>查看</a>\n" +
                        '</tr>'
                    )
                })
                /**
				 * 展示部署详情
                 */
                var deployInfos=data.deployInfoList;
                if(deployInfos.length==0){
                    alert('可用机器数不足，请重新部署生成预览');
                }
                if(appType.value==2){//Redis-cluster
                    deployInfos.forEach(function (deployInfo) {
                        var masterIp=deployInfo.masterIp;
                        var memSize=deployInfo.memSize;
                        var slaveIp=deployInfo.slaveIp;
                        appDeployInfo.value=appDeployInfo.value+masterIp+":"+memSize+(slaveIp==''?"\n":":"+slaveIp+"\n");
                    });
                }else if(appType.value==5){//Redis-sentinel
                    deployInfos.forEach(function (deployInfo) {
                        if(deployInfo.masterIp!=null&&deployInfo.masterIp!=''){
                            var masterIp=deployInfo.masterIp;
                            var memSize=deployInfo.memSize;
                            var slaveIp=deployInfo.slaveIp;
                            appDeployInfo.value=appDeployInfo.value+masterIp+":"+memSize+(slaveIp==''?"\n":":"+slaveIp+"\n");
                        }else {
                            var sentinelIp=deployInfo.sentinelIp;
                            appDeployInfo.value=appDeployInfo.value+sentinelIp+"\n";
                        }
                    });
                }else if(appType.value==6){//Redis-standalone
                    deployInfos.forEach(function (deployInfo) {
                        var masterIp=deployInfo.masterIp;
                        var memSize=deployInfo.memSize;
                        appDeployInfo.value=masterIp+":"+memSize;
                    });
                }
			}
        }
	);
}

//确认部署信息
function generatePreview() {
    var appDeployInfo = document.getElementById("appDeployInfo");
    var appDeployText = document.getElementById("appDeployText");
    if(appDeployInfo.value==''){
    	alert('暂无部署信息,请先部署生成预览');
	}else {
        appDeployText.value=appDeployInfo.value;
        $('#assignRedisModal').modal('hide');

	}
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
			appDeployText: appDeployText.value,
            versionId: $('#versionId option:selected').attr("versionid")
		},
        function(data){
			var status = data.status;
            toastr.success(data.message);
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
	startDeployLabel.innerHTML = '正在部署,请等待...';
	
	$.get(
		'/manage/app/addAppDeploy.json',
		{
			appAuditId: appAuditId.value,
			appDeployText: appDeployText.value
		},
        function(data){
			var status = data.status;
			if (status == 1) {
                $('#appDeployBtn').html('部署完成');
				$('#appDeployBtn').hide();
				$('#appCheckBtn').hide();
                startDeployLabel.innerHTML = '';
                $('#appDeployBtn').attr("disabled",true);
                toastr.success("应用部署成功,请点击下一步进行审核");
			} else {
                toastr.error("应用部署失败,请查看系统日志确认相关原因!");
			}
        }
     );
	//展示简单的进度条
	//setInterval(startShowDeployLabel,500);
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

//重试水平扩容
function retryHorizontalScale(instanceReshardProcessId){
	var appAuditId = document.getElementById("appAuditId");
	var retryButton = document.getElementById("retryBtn" + instanceReshardProcessId);
	instanceReshardProcessId.disabled = true;
	$.get(
		'/manage/app/retryHorizontalScale.json',
		{
			instanceReshardProcessId: instanceReshardProcessId
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
function deployBegin() {
    if($('#install').html() == "下一步"){
        // step forward
        setTimeout(method+'()', 500);
    }else if($('#install').html() == "部署完成"){
        // step complete
        window.location.href="/manage/total/list";
    }
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


function confirmApplication(appAuditId) {
    $.get(
        '/manage/app/addAuditStatus.json',
        {
            status: 1,
            appAuditId: appAuditId,
			type: 0
        },
        function(data){
            var status = data;
            if (status == 1) {
                $('#confirmBtn').html('已通过');
                $('#confirmBtn').attr("disabled",true);

                //审核通过直接跳转到部署成功part
                complete("appConfirm");
                $('#appConfirmDiv').attr("style","display:none;");
                $('#deployCompleteDiv').attr("style","display:'';");
                active('deployComplete');
                $("#install").html("部署完成");
                $('#installBtn').attr("style","display:'';");
                goOn("deployComplete");
            } else {
                alert("审核失败，请从新确认!");
            }
        }
    );
}