
//验证是数字
function testisNum(id){
   var value =document.getElementById(id).value;
   if(value != "" && isNaN(value)){
      alert("请输入数字类型!");
      document.getElementById(id).value="";
      document.getElementById(id).focus();
   }
}

//保存应用
function saveAppDesc(){
	//应用名
	var appName = document.getElementById("appName");
	if(appName.value == ""){
		alert("应用名不能为空");
		appName.focus();
		return false;
	}
	
	var appExist = document.getElementById("appExist");
	if(appExist.value == 1){
		alert("应用名已经存在,请修改!");
		appName.focus();
		return false;
	}
	
	//应用描述
	var appIntro = document.getElementById("appIntro");
	if(appIntro.value == ""){
		alert("应用描述不能为空");
		appIntro.focus();
		return false;
	}
	
	//应用描述
	var memSize = document.getElementById("memSize");
	if(memSize.value == ""){
		alert("内容容量不能为空");
		memSize.focus();
		return false;
	}
	
	//项目负责人
	var officer = document.getElementById("officer");
	if(officer.value == ""){
		alert("项目负责人不能为空");
		officer.focus();
		return false;
	}
	
	//预估QPS
	var forecaseQps = document.getElementById("forecaseQps");
	if(forecaseQps.value == ""){
		alert("预估QPS不能为空");
		forecaseQps.focus();
		return false;
	}
	
	//预估条目数量
	var forecastObjNum = document.getElementById("forecastObjNum");
	if(forecastObjNum.value == ""){
		alert("预估条目数量不能为空");
		forecastObjNum.focus();
		return false;
	}
	
	//客户端机房信息
	var clientMachineRoom = document.getElementById("clientMachineRoom");
	if(clientMachineRoom.value == ""){
		alert("客户端机房信息不能为空");
		clientMachineRoom.focus();
		return false;
	}
	
	//内存报警阀值
	var memAlertValue = document.getElementById("memAlertValue");
	if(memAlertValue.value == ""){
		alert("内存报警阀值不能为空");
		memAlertValue.focus();
		return false;
	}
	
	//客户端连接数报警阀值
	var clientConnAlertValue = document.getElementById("clientConnAlertValue");
	if(clientConnAlertValue.value == ""){
		alert("客户端连接数报警阀值不能为空");
		clientConnAlertValue.focus();
		return false;
	}
	
	return true;	
}

//查看应用名是否已经存在
function checkAppNameExist(){
	var appName = document.getElementById("appName").value;
	if(appName != ''){
		$.post(
			'/admin/app/checkAppNameExist.do',
			{
				appName: appName,
			},
	        function(data){
	            if(data==1){
	            	alert("应用名已经存在，请修改");
	            	document.getElementById("appName").focus();
	            	document.getElementById("appExist").value = 1;
	            }else{
	            	document.getElementById("appExist").value = 0;
	            }
	        }
	     );
	}
}