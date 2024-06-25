function removeMachine(id, ip, contextPath) {
    var removeMachineBtn = document.getElementById(id);
    removeMachineBtn.disabled = true;
    $.get(
        contextPath + '/manage/machine/checkMachineInstances.json',
        {
            ip: ip,
        },
        function (data) {
            var machineHasInstance = data.machineHasInstance;
            var alertMsg;
            if (machineHasInstance == true) {
                alertMsg = "该机器ip=" + ip + "还有运行中的Redis节点,确认要删除吗？";
            } else {
                alertMsg = "确认要删除ip=" + ip + "吗?";
            }
            if (confirm(alertMsg)) {
                location.href = contextPath + "/manage/machine/delete?machineIp=" + ip;
            } else {
                removeMachineBtn.disabled = false;
            }
        }
    );
}

function saveOrUpdateMachine(machineId, contextPath) {
    var ip = document.getElementById("ip" + machineId);
    var room = document.getElementById("machineRoom" + machineId);
    var mem = document.getElementById("mem" + machineId);
    var cpu = document.getElementById("cpu" + machineId);
    var disk = document.getElementById("disk" + machineId);
    var virtual = document.getElementById("virtual" + machineId);
    var disType = document.getElementById("disType" + machineId);
    var realIp = document.getElementById("realIp" + machineId);
    var machineType = document.getElementById("machineType" + machineId);
    var useType = document.getElementById("useType1" + machineId);
    var extraDesc = document.getElementById("extraDesc" + machineId);
    var collect = document.getElementById("collect" + machineId);
    var versionInfo = document.getElementById("versionInfo" + machineId);
    var k8sType = document.getElementById("k8sType" + machineId);
    var rack = document.getElementById("rack" + machineId);

    if (ip.value == "") {
        alert("IP不能为空!");
        ip.focus();
        return false;
    }
    if (room.value == "") {
        alert("机房不能为空!");
        room.focus();
        return false;
    }
    if (mem.value == "") {
        alert("内存不能为空!");
        mem.focus();
        return false;
    }
    if (cpu.value == "") {
        alert("CPU不能为空!");
        cpu.focus();
        return false;
    }
    if (disk.value == "") {
        alert("磁盘不能为空!");
        disk.focus();
        return false;
    }
    if (virtual.value == "") {
        alert("是否虚机为空!");
        virtual.focus();
        return false;
    }
    var addMachineBtn = document.getElementById("addMachineBtn" + machineId);
    addMachineBtn.disabled = true;

    $.post(
        contextPath + '/manage/machine/addMultiple.json',
        {
            ip: ip.value,
            room: room.value,
            mem: mem.value,
            cpu: cpu.value,
            disk: disk.value,
            virtual: virtual.value,
            disType: disType.value,
            realIp: realIp.value,
            id: machineId,
            machineType: machineType.value,
            useType: useType.value,
            k8sType: k8sType.value,
            extraDesc: extraDesc.value,
            rack: rack.value,
            collect: collect.value,
            versionInfo: versionInfo.value
        },
        function (data) {
            if (data.result) {
                $("#machineInfo" + machineId).html("<div class='alert alert-error' ><button class='close' data-dismiss='alert'>×</button><strong>Success!</strong>更新成功，窗口会自动关闭</div>");
                var targetId = "#addMachineModal" + machineId;
                setTimeout("$('" + targetId + "').modal('hide');window.location.reload();", 1000);
            } else {
                addMachineBtn.disabled = false;
                $("#machineInfo" + machineId).html("<div class='alert alert-error' ><button class='close' data-dismiss='alert'>×</button><strong>Error!</strong>更新失败！</div>");
            }
        }
    );
}

function downCrashMachineAddSlave(contextPath) {
    var ip = document.getElementById("downMachineIp");
    if (ip.value == "") {
        alert("IP不能为空!");
        ip.focus();
        return false;
    }
    var availableMachineIps = "";
    $("#availablePodIp option:selected").each(function () {
        if (this.value != '') {
            availableMachineIps += this.value + ",";
        }
    });
    if(availableMachineIps == "" || availableMachineIps.length <= 0) {
        alert("请选择可用pod!");
        return false;
    }
    var downCrashMachineModalBtn = document.getElementById("downCrashMachineModalBtn");
    downCrashMachineModalBtn.disabled = true;
    $.post(
        contextPath + '/manage/machine/downCrashAddSlave.json',
        {
            ip: ip.value,
            availableMachineIps: availableMachineIps
        },
        function (data) {
            if (data.result) {
                $("#downMachineInfo").html("<div class='alert alert-error' ><button class='close' data-dismiss='alert'>×</button><strong>Success!</strong>创建自动补全app缺失slave任务成功</div>");
                window.open(contextPath + '/manage/machine/taskInfo?taskId=' + data.taskId, "_blank");
            } else {
                downCrashMachineModalBtn.disabled = false;
                $("#downMachineInfo").html("<div class='alert alert-error' ><button class='close' data-dismiss='alert'>×</button><strong>Error!</strong>创建自动补全app缺失slave任务，失败！</div>");
            }
        }
    );
}

function removeRoom(id, roomId, contextPath) {
    var removeBtn = document.getElementById(id);
    removeBtn.disabled = true;
    var alertMsg = "确认要删除该机房吗?";
    console.log("roomid:"+roomId);
    if (confirm(alertMsg)) {
        location.href = contextPath + "/manage/machine/room/delete?id=" + roomId;
    } else {
        removeBtn.disabled = false;
    }
}

function saveOrUpdateRoom(roomId, contextPath) {
    var id = document.getElementById("roomId" + roomId);
    var name = document.getElementById("name" + roomId);
    var status = document.getElementById("status" + roomId);
    var desc = document.getElementById("desc" + roomId);
    var ipNetwork = document.getElementById("ipNetwork" + roomId);
    var operator = document.getElementById("operator" + roomId);


    if (name.value == "") {
        alert("机房名称不能为空!");
        ip.focus();
        return false;
    }
    var addRoomBtn = document.getElementById("addRoomBtn" + roomId);
    addRoomBtn.disabled = true;

    $.post(
        contextPath + '/manage/machine/room/add.json',
        {
            id: id == null ? null : id.value,
            name: name.value,
            status: status.value,
            desc: desc.value,
            ipNetwork: ipNetwork.value,
            operator: operator.value,
        },
        function (data) {
            if (data.result) {
                $("#machineRoom" + roomId).html("<div class='alert alert-error' ><button class='close' data-dismiss='alert'>×</button><strong>Success!</strong>更新成功，窗口会自动关闭</div>");
                var targetId = "#addRoomModal" + roomId;
                setTimeout("$('" + targetId + "').modal('hide');window.location.reload();", 1000);
            } else {
                addRoomBtn.disabled = false;
                $("#machineRoom" + roomId).html("<div class='alert alert-error' ><button class='close' data-dismiss='alert'>×</button><strong>Error!</strong>更新失败！</div>");
            }
        }
    );
}

function prepareMachinedownResource(contextPath) {
    $('#availablePodIp').selectpicker('destroy');
    $('#availablePodIp').selectpicker();
    var availablePodIpElement = document.getElementById('availablePodIp');
    availablePodIpElement.innerHTML = '';
    $("#selectedResourceId").html("");
  var downMachineIp = document.getElementById("downMachineIp").value;
  if (downMachineIp == "" || downMachineIp == undefined) {
      alert("机器ip不能为空");
      return false;
  }
  var downCrashMachineModalBtn = document.getElementById("downCrashMachineModalBtn");
  var downMachineIpInput = document.getElementById("downMachineIp");
  $.post(
      contextPath + '/manage/machine/prepareMachinedownResource.json',
      {
          ip: downMachineIp
      },
      function (data) {
          if (data.result) {
            var podResourceUsedMap = new Map(Object.entries(data.podResourceUsedMap));
            var deployDetailId = document.getElementById("deployDetailId");
            var totalUsedCpu = 0;
            var totalUsedMemRss = 0.00;
            var totalUsedDisk = 0.00;
            podResourceUsedMap.forEach((value, key) => {
                if(value.type == 0 || value.type == 6){
                    totalUsedCpu += value.instanceNum;
                    totalUsedMemRss += (value.usedMemRss / 1024 / 1024 / 1024);
                    totalUsedDisk += (value.usedDisk / 1024 / 1024 / 1024);
                }
            });
            var html = '<p><label style="color:red">汇总部署信息：【cpu：' + totalUsedCpu + '核】 【使用内存：' + totalUsedMemRss.toFixed(2) + 'G】 【使用SSD：' + totalUsedDisk.toFixed(2) + 'G】<label></p>';
            html += '<table class="table table-striped table-bordered table-hover table-sm" style="white-space: nowrap">' +
                        '<thead><th scope="col">ip</th>' +
                        '<th scope="col">实例数/核数</th>' +
                        '<th scope="col">内存使用</th>' +
                        '<th scope="col">rss内存使用</th>' +
                        '<th scope="col">SSD使用</th>' +
                        '</thead>' +
                        '<tbody>';
            podResourceUsedMap.forEach((value, key) => {
                var migrateTip = "";
                if(value.type != 0 && value.type != 6){
                    migrateTip ="--该pod不支持迁移，请手动处理";
                }
                html += "<tr><td>" + key + migrateTip + "</td>"
                    + "<td>" + value.instanceNum + "/" + value.cpu + "核</td>"
                    + "<td>" + (value.usedMem / 1024 / 1024 / 1024).toFixed(2) + "/" + value.mem + "G</td>"
                    + "<td>" + (value.usedMemRss / 1024 / 1024 / 1024).toFixed(2) + "/" + value.mem + "G</td>"
                    + "<td>" + (value.usedDisk / 1024 / 1024 / 1024).toFixed(2) + "/" + value.disk + "G</td></tr>";
            });
            html += "</tbody></table>";
            deployDetailId.innerHTML = html;

            var tipId = document.getElementById("tipId");
            var tipHtml = "";
            var optionHtml = "";
            var machineNeedInfoMap = new Map(Object.entries(data.machineNeedInfoMap));
            var podMemStatInfoMap = new Map(Object.entries(data.podMemStatInfoMap));
            tipHtml += "共有" + machineNeedInfoMap.size + "种类型pod，请选择合适的数量";
            $('#tipId').html(tipHtml);
            $('#availablePodIp').selectpicker('destroy');
            machineNeedInfoMap.forEach((value, key) => {
                $('#availablePodIp').append("<option value='' disabled='true'>————机器类型分割线:" + key.replace("MachineBasicInfo", "") + "————————</option>");
                var ips = value.availableMachineIps;
                for(var i = 0; i < ips.length; i++) {
                    var memStatInfo = podMemStatInfoMap.get(ips[i]);
                    var text = ": 【" + memStatInfo.instanceNum + "/" + memStatInfo.cpu + "核】 【"
                                + (memStatInfo.usedMemRss / 1024 / 1024 / 1024).toFixed(2) + "/" + memStatInfo.mem + "G】 【"
                                + (memStatInfo.usedDisk / 1024 / 1024 / 1024).toFixed(2)  + "/" + memStatInfo.disk + "G】 【"
                                + memStatInfo.realIp + "】";
                    var leftCpu = memStatInfo.cpu - memStatInfo.instanceNum;
                    if(leftCpu < 0){
                        leftCpu = 0;
                    }
                    var leftMem = (memStatInfo.mem - (memStatInfo.usedMemRss / 1024 / 1024 / 1024)).toFixed(2);
                    var leftDisk = (memStatInfo.disk - (memStatInfo.usedDisk / 1024 / 1024 / 1024)).toFixed(2);
                    $('#availablePodIp').append("<option value='" + memStatInfo.ip + "' leftCpu='" + leftCpu + "' leftMem='" + leftMem + "' leftDisk='" + leftDisk + "'>" + memStatInfo.ip + text + "</option>");
                }
            });
            $('#availablePodIp').selectpicker();

            $('#availableMachineId').show();
            downCrashMachineModalBtn.disabled = false;
            downMachineIpInput.disabled = true;
          } else {
              $("#downMachineInfo").html("<div class='alert alert-error' ><strong>Error!</strong>执行失败，请查找原因！<button class='close' data-bs-dismiss='alert'></button></div>");
          }
      }
  );
}

// 计算迁移实例需要内存
function availableMachineChange(){
    var totalPodCount = 0;
    var totalLeftCpu = 0;
    var totalLeftMem = 0.00;
    var totalLeftDisk = 0.00;
    $('#availablePodIp option:selected').each(function(){
        if($(this).attr("value") == ''){
            return;
        }
        totalPodCount += 1;
        totalLeftCpu += parseFloat($(this).attr("leftCpu"));
        totalLeftMem += parseFloat($(this).attr("leftMem"));
        totalLeftDisk += parseFloat($(this).attr("leftDisk"));
    });
    totalLeftMem = totalLeftMem.toFixed(2);
    totalLeftDisk = totalLeftDisk.toFixed(2);
    $("#selectedResourceId").html("<label style=\"color:green\">已选可用信息:【pod数量：" + totalPodCount + "个】 【cpu：" + totalLeftCpu + "核】 【内存：" + totalLeftMem + "G】 【SSD：" + totalLeftDisk + "G】<label>");
}
