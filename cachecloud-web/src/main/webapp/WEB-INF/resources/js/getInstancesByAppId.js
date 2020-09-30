function changeAppIdSelect(appId, instance_select) {
	console.log('instance_select:' + instance_select);
	console.log(appId);

	document.getElementById(instance_select).options.length = 0;

	$.post('/manage/app/tool/diagnostic/appInstances',
		{
			appId: appId,
		},
		function (data) {
			var status = data.status;
			if (status == 1) {
				var appInstanceList = data.appInstanceList;
				for (var i = 0; i < appInstanceList.length; i++) {
					var val = appInstanceList[i].hostPort;
					var term = appInstanceList[i].hostPort + '（角色：' + appInstanceList[i].roleDesc + '）'
					$('#' + instance_select).append("<option value='" + val + "'>" + term + "</option>");
				}
				$('#' + instance_select).selectpicker('refresh');
				$('#' + instance_select).selectpicker('render');
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