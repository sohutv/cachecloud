/**
 * 生成部署实例列表
 */
function generateDeployInfo() {

    /**
     *  1.获取机器信息
     */
    var redisMachines = "";
    var sentinelMachines = "";
    var twemproxyMachines = "";
    var pikaMachines = "";
    // 1.获取不同类型的机器信息
    if (redisMachines == '') {
        $("#redisMachineList option:selected").each(function () {
            if (this.value != '') {
                redisMachines += this.value + ";";
            }
        });
    }
    if (sentinelMachines == '') {
        $("#sentinelMachineList option:selected").each(function () {
            if (this.value != '') {
                sentinelMachines += this.value + ";";
            }
        });
    }
    if (twemproxyMachines == '') {
        $("#twemproxyMachineList option:selected").each(function () {
            if (this.value != '') {
                twemproxyMachines += this.value + ";";
            }
        });
    }
    if (pikaMachines == '') {
        $("#pikaMachineList option:selected").each(function () {
            if (this.value != '') {
                pikaMachines += this.value + ";";
            }
        });
    }
    /**
     * 2.信息验证:
     *    2.1 部署机器信息
     *    2.2 资源验证: maxmeory验证
     */
    var maxMemory = $.trim($("#maxMemory").val());
    if (maxMemory == '' || maxMemory == null) {
        toastr.error("请填写maxMemory内存大小!");
        $("#maxMemory").focus();
        return;
    }

    if ($("#appType").val() == '2' || $("#appType").val() == '6') {  //rediscluster /redis standalone
        if (redisMachines == '') {
            toastr.error("请选择Redis部署机器!");
            $("#redisMachineList").focus();
            return;
        }
    } else if ($("#appType").val() == '5') {  // redis sentinel
        if (redisMachines == '') {
            toastr.error("请选择Redis部署机器!");
            $("#redisMachineList").focus();
            return;
        }
        if (sentinelMachines == '') {
            toastr.error("请选择Sentinel机器机器!");
            $("#sentinelMachineList").focus();
            return;
        }
    } else if ($("#appType").val() == '7') { // redis twemproxy
        if (redisMachines == '') {
            toastr.error("请选择Redis部署机器!");
            $("#redisMachineList").focus();
            return;
        }
        if (sentinelMachines == '') {
            toastr.error("请选择Sentinel机器机器!");
            $("#sentinelMachineList").focus();
            return;
        }
        if (twemproxyMachines == '') {
            toastr.error("请选择Twemproxy机器机器!");
            $("#twemproxyMachineList").focus();
            return;
        }
    } else if ($("#appType").val() == '8') { // pika sentinel
        if (pikaMachines == '') {
            toastr.error("请选择Pika部署机器!");
            $("#redisMachineList").focus();
            return;
        }
        if (sentinelMachines == '') {
            toastr.error("请选择Sentinel机器机器!");
            $("#sentinelMachineList").focus();
            return;
        }
    } else if ($("#appType").val() == '9') { // pika twemproxy
        if (pikaMachines == '') {
            toastr.error("请选择Pika部署机器!");
            $("#redisMachineList").focus();
            return;
        }
        if (sentinelMachines == '') {
            toastr.error("请选择Sentinel机器机器!");
            $("#sentinelMachineList").focus();
            return;
        }
        if (twemproxyMachines == '') {
            toastr.error("请选择Twemproxy机器机器");
            $("#twemproxyMachineList").focus();
            return;
        }
    }

    //清除上次记录
    $('#tableList tbody tr').empty();
    $("#appDeployText").val("");
    $("#appDeployInfo").val("");

    $.post(
        '/manage/app/generateDeployInfo.json',
        {
            type: $("#appType").val(),
            hasSalve: 1,
            maxMemory: maxMemory,
            redisNum: $("#redisNum option:selected").val(),
            sentinelNum: $("#sentinelNum option:selected").val(),
            pikaNum: $("#pikaNum option:selected").val(),
            twemproxyNum: $("#twemproxyNum option:selected").val(),
            redisMachines: redisMachines,
            sentinelMachines: sentinelMachines,
            pikaMachines: pikaMachines,
            twemproxyMachines: twemproxyMachines
        },
        function (data) {
            $("#startDeployInfoLabel").val("");
            if (data.result != 'success') {
                toastr.error("实例部署异常:" + data.result);
            } else {
                /**
                 * 显示机器部署详情
                 */
                $("#selectMachineId").removeAttr("style");
                $("#clearInfo").attr("style", "background:#CCCCCC");
                var resMachines = data.resMachines;
                var machineDeployStatMap = data.machineDeployStatMap;
                resMachines.forEach(function (machine) {
                    var machineDeployStat = machineDeployStatMap[machine.ip];
                    $("#tableList tbody").prepend(
                        '<tr class="odd gradeX">\n' +
                        "   <td> <a target='_blank' href='/manage/machine/index?tabTag=machine&ipLike=" + machine.ip + " '>" + machine.ip + "</a>\n" +
                        '   <td>' + machine.instanceNum + "&nbsp;&nbsp;/&nbsp;&nbsp;" + machine.cpu + '</td>\n' +
                        '   <td> ' + (machine.usedMem / 1024 / 1024 / 1024).toFixed(1) + 'G / <font color="#FF0000">' + (machine.mem - (machine.usedMem / 1024 / 1024 / 1024)).toFixed(1) + 'G </font> / ' + machine.mem + 'G</td>\n' +
                        '   <td> - G / - G / - G</td>\n' +
                        '   <td>' + machineDeployStat.masterNum + "&nbsp;&nbsp;/&nbsp;&nbsp;" + machineDeployStat.slaveNum + "&nbsp;&nbsp;/&nbsp;&nbsp;" + machineDeployStat.sentinelNum + "&nbsp;&nbsp;/&nbsp;&nbsp;" + machineDeployStat.twemproxyNum + '</td>\n' +
                        '   <td>' + machine.realIp + "/&nbsp;&nbsp;" + machine.rack + '</td>\n' +
                        '</tr>'
                    )
                })
                /**
                 * 展示部署详情
                 */
                var deployInfos = data.deployInfoList;
                var appType = $("#appType").val();
                if (appType == 2) {//Redis-cluster
                    deployInfos.forEach(function (deployInfo) {
                        var masterIp = deployInfo.masterIp;
                        var memSize = deployInfo.memSize;
                        var slaveIp = deployInfo.slaveIp;
                        appDeployInfo.value = appDeployInfo.value + masterIp + ":" + memSize + (slaveIp == '' ? "\n" : ":" + slaveIp + "\n");
                    });
                } else if (appType == 5) {//Redis-sentinel
                    deployInfos.forEach(function (deployInfo) {
                        if (deployInfo.masterIp != null && deployInfo.masterIp != '') {
                            var masterIp = deployInfo.masterIp;
                            var memSize = deployInfo.memSize;
                            var slaveIp = deployInfo.slaveIp;
                            appDeployInfo.value = appDeployInfo.value + masterIp + ":" + memSize + (slaveIp == '' ? "\n" : ":" + slaveIp + "\n");
                        } else {
                            var sentinelIp = deployInfo.sentinelIp;
                            appDeployInfo.value = appDeployInfo.value + "sentinel:" + sentinelIp + "\n";
                        }
                    });
                } else if (appType == 6) {//Redis-standalone
                    deployInfos.forEach(function (deployInfo) {
                        var masterIp = deployInfo.masterIp;
                        var memSize = deployInfo.memSize;
                        appDeployInfo.value = masterIp + ":" + memSize;
                    });
                } else if (appType == 7) {//Redis twemproxy
                    deployInfos.forEach(function (deployInfo) {
                        if (deployInfo.masterIp != null && deployInfo.masterIp != '') {
                            var masterIp = deployInfo.masterIp;
                            var memSize = deployInfo.memSize;
                            var slaveIp = deployInfo.slaveIp;
                            appDeployInfo.value = appDeployInfo.value + masterIp + ":" + memSize + (slaveIp == '' ? "\n" : ":" + slaveIp + "\n");
                        } else if (deployInfo.sentinelIp != null && deployInfo.sentinelIp != '') {
                            var sentinelIp = deployInfo.sentinelIp;
                            appDeployInfo.value = appDeployInfo.value + "sentinel:" + sentinelIp + "\n";
                        } else if (deployInfo.twemproxyIp != null && deployInfo.twemproxyIp != '') {
                            var twemproxyIp = deployInfo.twemproxyIp;
                            appDeployInfo.value = appDeployInfo.value + "twemproxy:" + twemproxyIp + "\n";
                        }
                    });
                } else if (appType == 8) {//pika sentinel
                    deployInfos.forEach(function (deployInfo) {
                        if (deployInfo.masterPikaIp != null && deployInfo.masterPikaIp != '') {
                            var masterPikaIp = deployInfo.masterPikaIp;
                            var memSize = deployInfo.memSize;
                            var slavePikaIp = deployInfo.slavePikaIp;
                            appDeployInfo.value = appDeployInfo.value + masterPikaIp + ":" + memSize + (slavePikaIp == '' ? "\n" : ":" + slavePikaIp + "\n");
                        } else {
                            var sentinelIp = deployInfo.sentinelIp;
                            appDeployInfo.value = appDeployInfo.value + sentinelIp + "\n";
                        }
                    });
                } else if (appType == 9) {//pika twemproxy
                    deployInfos.forEach(function (deployInfo) {
                        if (deployInfo.masterPikaIp != null && deployInfo.masterPikaIp != '') {
                            var masterPikaIp = deployInfo.masterPikaIp;
                            var memSize = deployInfo.memSize;
                            var slavePikaIp = deployInfo.slavePikaIp;
                            appDeployInfo.value = appDeployInfo.value + masterPikaIp + ":" + memSize + (slavePikaIp == '' ? "\n" : ":" + slavePikaIp + "\n");
                        } else if (deployInfo.sentinelIp != null && deployInfo.sentinelIp != '') {
                            var sentinelIp = deployInfo.sentinelIp;
                            appDeployInfo.value = appDeployInfo.value + "sentinel:" + sentinelIp + "\n";
                        } else if (deployInfo.twemproxyIp != null && deployInfo.twemproxyIp != '') {
                            var twemproxyIp = deployInfo.twemproxyIp;
                            appDeployInfo.value = appDeployInfo.value + "twemproxy:" + twemproxyIp + "\n";
                        }
                    });
                }
            }
        }
    );
}

/**
 * 置空相关信息
 */
function clearinfo() {
    //清除上次记录
    $('#tableList tbody tr').empty();
    $("#selectMachineId").attr("style", "display:none");
    $("#appDeployText").val("");
    $("#appDeployInfo").val("");
}

/**
 * 添加应用部署任务
 */
function addAppDeployTask() {

    // 是否设置密码
    var isSetPasswd = $("#isSetPasswd").attr("checked") == 'checked' ? 1 : 0;
    var maxMemory = $.trim($("#maxMemory").val());
    if (maxMemory == '' || maxMemory == null) {
        toastr.error("请填写maxMemory内存大小!");
        $("#maxMemory").focus();
        return;
    }
    // 检查部署预览是否为空
    // $("#appDeployBtn").attr("disabled",true);
    if ($("#appDeployInfo").val() == '') {
        toastr.error('请先生成部署预览!');
        return;
    }

    var redisMachines = "";
    var sentinelMachines = "";
    var twemproxyMachines = "";
    var pikaMachines = "";
    // 1.获取不同类型的机器信息
    if (redisMachines == '') {
        $("#redisMachineList option:selected").each(function () {
            if (this.value != '') {
                redisMachines += this.value + ";";
            }
        });
    }
    if (sentinelMachines == '') {
        $("#sentinelMachineList option:selected").each(function () {
            if (this.value != '') {
                sentinelMachines += this.value + ";";
            }
        });
    }
    if (twemproxyMachines == '') {
        $("#twemproxyMachineList option:selected").each(function () {
            if (this.value != '') {
                twemproxyMachines += this.value + ";";
            }
        });
    }
    if (pikaMachines == '') {
        $("#pikaMachineList option:selected").each(function () {
            if (this.value != '') {
                pikaMachines += this.value + ";";
            }
        });
    }

    $.post(
        '/manage/app/addAppDeployTask.json',
        {
            appid: $("#hiddenAppId").val(),
            appAuditId: $("#appAuditId").val(),
            isSetPasswd: isSetPasswd,
            versionId: $("#versionId option:selected").attr("versionid"),
            importantLevel: $("#importantLevel option:selected").val(),
            type: $("#appType").val(), // 根据实际选用类型来确认最终部署类型
            maxMemory: maxMemory,
            redisNum: $("#redisNum option:selected").val(),
            sentinelNum: $("#sentinelNum option:selected").val(),
            pikaNum: $("#pikaNum option:selected").val(),
            twemproxyNum: $("#twemproxyNum option:selected").val(),
            redisMachines: redisMachines,
            sentinelMachines: sentinelMachines,
            pikaMachines: pikaMachines,
            twemproxyMachines: twemproxyMachines,
        },
        function (data) {
            var status = data.status;
            if (status == 'success') {
                $('#appDeployBtn').attr("disabled", true);
                toastr.success(data.message);
                console.log("updateForImport");
                updateForImport(data.taskid);
                setTimeout("reloadAppStatPage(" + data.taskid + ");", 2000);
            } else {
                toastr.error("应用部署失败,请查看系统日志确认相关原因!");
            }
        }
    );
}

//重新加载appDetail页面
function reloadAppStatPage(taskid) {
    location.href = "/manage/task/flow?taskId=" + taskid;
}

function updateForImport(taskId) {
    var importId = document.getElementById("importId");
    if (importId != null && importId.value != '') {
        $.get(
            '/import/app/goOn.json',
            {
                importId: importId.value,
                appBuildTaskId: taskId,
            },
            function (data) {
                var success = data.success;
                if (success == 1) {
                    console.log("updateForImport success");
                }
            }
        );
    }
}


