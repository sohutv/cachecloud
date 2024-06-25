function changeAppIdSelect(appId, instance_select, contextPath) {
	document.getElementById(instance_select).options.length = 0;
	$('#' + instance_select).selectpicker('destroy');
	$('#' + instance_select).selectpicker();

	$.post(contextPath + '/manage/app/tool/diagnostic/appInstances',
		{
			appId: appId,
		},
		function (data) {
			var status = data.status;
			if (status == 1) {
				$('#' + instance_select).selectpicker('destroy');
				var appInstanceList = data.appInstanceList;
				for (var i = 0; i < appInstanceList.length; i++) {
					var val = appInstanceList[i].hostPort;
					var term = appInstanceList[i].hostPort + '（角色：' + appInstanceList[i].roleDesc + '）'
					$('#' + instance_select).append("<option value='" + val + "'>" + term + "</option>");
				}
				$('#' + instance_select).selectpicker();
			} else {
				console.log('data.status:' + status);
			}
		}
	);
}

function testisNum(id){
	var value =document.getElementById(id).value;
	if(value != "" && isNaN(value)){
		alert("请输入数字类型!");
		document.getElementById(id).value="";
		document.getElementById(id).focus();
	}
}