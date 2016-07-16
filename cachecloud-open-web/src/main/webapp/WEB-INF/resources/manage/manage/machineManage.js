function saveOrUpdateMachine(machineId){
	var ip = document.getElementById("ip" + machineId);
	var room = document.getElementById("room" + machineId);
	var mem = document.getElementById("mem" + machineId);
	var cpu = document.getElementById("cpu" + machineId);
	var virtual = document.getElementById("virtual" + machineId);
    var realIp = document.getElementById("realIp" + machineId);
    var type = document.getElementById("type" + machineId);
    var extraDesc = document.getElementById("extraDesc" + machineId);

	if(ip.value == ""){
    	alert("IP不能为空!");
        ip.focus();
		return false;
    }
    if(room.value == ""){
        alert("机房不能为空!");
        room.focus();
        return false;
    }
    if(mem.value == ""){
        alert("内存不能为空!");
        mem.focus();
        return false;
    }
    if(cpu.value == ""){
        alert("CPU不能为空!");
        cpu.focus();
        return false;
    }
    if(virtual.value == ""){
        alert("是否虚机为空!");
        virtual.focus();
        return false;
    }
    var addMachineBtn = document.getElementById("addMachineBtn" + machineId);
    addMachineBtn.disabled = true;
    
	$.post(
		'/manage/machine/add.json',
		{
            ip: ip.value,
            room: room.value,
            mem: mem.value,
            cpu: cpu.value,
            virtual: virtual.value,
            realIp: realIp.value,
            id:machineId,
            type: type.value,
            extraDesc: extraDesc.value
		},
        function(data){
            if(data.result){
                $("#machineInfo" + machineId).html("<div class='alert alert-error' ><button class='close' data-dismiss='alert'>×</button><strong>Success!</strong>更新成功，窗口会自动关闭</div>");
                var targetId = "#addMachineModal" + machineId;
                setTimeout("$('" + targetId +"').modal('hide');window.location.reload();",1000);
            }else{
                addMachineBtn.disabled = false;
                $("#machineInfo" + machineId).html("<div class='alert alert-error' ><button class='close' data-dismiss='alert'>×</button><strong>Error!</strong>更新失败！</div>");
            }
        }
     );
}
