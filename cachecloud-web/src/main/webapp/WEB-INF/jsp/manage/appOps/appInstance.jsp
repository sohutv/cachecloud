<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp" %>
<link rel="stylesheet" type="text/css" href="/resources/select/bootstrap-select.css"/>
<script src="/resources/manage/plugins/jquery-1.10.2.min.js"></script>
<script type="text/javascript">var jQuery_1_10_2 = $;</script>
<script src="/resources/manage/plugins/bootstrap/js/bootstrap.min.js" type="text/javascript"></script>
<script type="text/javascript" src="/resources/select/bootstrap-select.js"></script>

<script type="text/javascript">
    $(window).on('load', function () {
        jQuery_1_10_2('.selectpicker').selectpicker({'selectedText': 'cat'});
        jQuery_1_10_2('.selectpicker').selectpicker('refresh');
        jQuery_1_10_2('.selectpicker').selectpicker('render');
    });
    $(function () {
        jQuery_1_10_2('.selectpicker').selectpicker();

        var slaveIp_select = document.getElementById('slaveIp');
        $("div[name='addSlave-modal']").on('shown.bs.modal', function () {
            var addSlaveModal_id = $(this).attr('id');
            var instanceId = addSlaveModal_id.split('_')[1];
            console.log(instanceId);

            if (document.getElementById('slaveIp' + instanceId).options.length == 0) {
                for (var i = 0; i < slaveIp_select.options.length; i++) {
                    var text = slaveIp_select.options[i].text;
                    var value = slaveIp_select.options[i].value;
                    $('#slaveIp' + instanceId).append("<option value='" + value + "'>" + text + "</option>");
                }
                console.log(document.getElementById('slaveIp' + instanceId).options.length);

                $('#slaveIp' + instanceId).selectpicker('refresh');
                $('#slaveIp' + instanceId).selectpicker('render');

                $('.dropdown-toggle').on('click',function(){
                    $('.dropdown-toggle').dropdown();
                });
            } else {
                console.log(document.getElementById('slaveIp' + instanceId).options.length);
            }
        });
    })
</script>
<script type="text/javascript">
    function startInstance(appId, instanceId) {
        if (confirm("确认要开启" + instanceId + "实例吗?")) {
            $.ajax({
                type: "get",
                url: "/manage/instance/startInstance.json",
                data:
                    {
                        appId: appId,
                        instanceId: instanceId
                    },
                success: function (result) {
                    if (result.success == 1) {
                        alert("开启成功!");
                    } else {
                        alert("开启失败, msg: " + result.message);
                    }
                    window.location.reload();
                }
            });
        }
    }

    function shutdownInstance(appId, instanceId) {
        if (confirm("确认要下线" + instanceId + "实例吗?")) {
            $.ajax({
                type: "get",
                url: "/manage/instance/shutdownInstance.json",
                data:
                    {
                        appId: appId,
                        instanceId: instanceId
                    },
                success: function (result) {
                    if (result.success == 1) {
                        alert("关闭成功!");
                    } else {
                        alert("关闭失败, msg: " + result.message);
                    }
                    window.location.reload();
                }
            });
        }
    }

    function forgetInstance(appId, instanceId) {
            if (confirm("确认要永久下线" + instanceId + "实例吗?")) {
                $.ajax({
                    type: "get",
                    url: "/manage/instance/forgetInstance.json",
                    data:
                        {
                            appId: appId,
                            instanceId: instanceId
                        },
                    success: function (result) {
                        if (result.success == 1) {
                            alert("关闭成功!");
                        } else {
                            alert("关闭失败, msg: " + result.message);
                        }
                        window.location.reload();
                    }
                });
            }
    }


    function redisClusterFailOverManual(appId, instanceId) {
        var redisClusterFailOverManualBtn = document.getElementById("redisClusterFailOverManualBtn" + instanceId);
        redisClusterFailOverManualBtn.disabled = true;
        $.post(
            '/manage/app/clusterSlaveFailOver',
            {
                appId: appId,
                slaveInstanceId: instanceId,
                failoverParam: ''
            },
            function (data) {
                if (data == 1) {
                    alert("执行成功!");
                    $("#redisClusterFailOverManualInfo" + instanceId).html("<div class='alert alert-error' ><button class='close' data-dismiss='alert'>×</button><strong>Success!</strong>执行成功，应用的拓扑结构要1分钟之后生效，请耐心等待</div>");
                    var targetId = "#redisClusterFailOverManualModal" + instanceId;
                    setTimeout("$('" + targetId + "').modal('hide');window.location.reload();", 1000);
                } else {
                    redisClusterFailOverManualBtn.disabled = false;
                    $("#redisClusterFailOverManualInfo" + instanceId).html("<div class='alert alert-error' ><button class='close' data-dismiss='alert'>×</button><strong>Error!</strong>执行失败，请查找原因！</div>");
                }
            }
        );
    }

    function redisClusterFailOverForce(appId, instanceId) {
        var redisClusterFailOverForceBtn = document.getElementById("redisClusterFailOverForceBtn" + instanceId);
        redisClusterFailOverForceBtn.disabled = true;
        $.post(
            '/manage/app/clusterSlaveFailOver',
            {
                appId: appId,
                slaveInstanceId: instanceId,
                failoverParam: 'force'
            },
            function (data) {
                if (data == 1) {
                    alert("执行成功!");
                    $("#redisClusterFailOverForceInfo" + instanceId).html("<div class='alert alert-error' ><button class='close' data-dismiss='alert'>×</button><strong>Success!</strong>执行成功，应用的拓扑结构要1分钟之后生效，请耐心等待</div>");
                    var targetId = "#redisClusterFailOverForceModal" + instanceId;
                    setTimeout("$('" + targetId + "').modal('hide');window.location.reload();", 1000);
                } else {
                    redisClusterFailOverForceBtn.disabled = false;
                    $("#redisClusterFailOverForceInfo" + instanceId).html("<div class='alert alert-error' ><button class='close' data-dismiss='alert'>×</button><strong>Error!</strong>执行失败，请查找原因！</div>");
                }
            }
        );
    }

    function redisClusterFailOverTakeOver(appId, instanceId) {
        var redisClusterFailOverTakeOverBtn = document.getElementById("redisClusterFailOverTakeOverBtn" + instanceId);
        redisClusterFailOverTakeOverBtn.disabled = true;
        $.post(
            '/manage/app/clusterSlaveFailOver',
            {
                appId: appId,
                slaveInstanceId: instanceId,
                failoverParam: 'takeover'
            },
            function (data) {
                if (data == 1) {
                    alert("执行成功!");
                    $("#redisClusterFailOverTakeOverInfo" + instanceId).html("<div class='alert alert-error' ><button class='close' data-dismiss='alert'>×</button><strong>Success!</strong>执行成功，应用的拓扑结构要1分钟之后生效，请耐心等待</div>");
                    var targetId = "#redisClusterFailOverTakeOverModal" + instanceId;
                    setTimeout("$('" + targetId + "').modal('hide');window.location.reload();", 1000);
                } else {
                    redisClusterFailOverTakeOverBtn.disabled = false;
                    $("#redisClusterFailOverTakeOverInfo" + instanceId).html("<div class='alert alert-error' ><button class='close' data-dismiss='alert'>×</button><strong>Error!</strong>执行失败，请查找原因！</div>");
                }
            }
        );
    }

    function redisClusterDelNode(appId, instanceId) {
        var redisClusterDelNodeBtn = document.getElementById("redisClusterDelNodeBtn" + instanceId);
        redisClusterDelNodeBtn.disabled = true;
        $.post(
            '/manage/app/clusterDelNode.json',
            {
                appId: appId,
                delNodeInstanceId: instanceId,
            },
            function (data) {
                var success = data.success;
                var message = data.message;
                if (success == 1) {
                    alert("执行成功!");
                    $("#redisClusterDelNodeInfo" + instanceId).html("<div class='alert alert-error' ><button class='close' data-dismiss='alert'>×</button><strong>Success!</strong>执行成功，应用的拓扑结构要1分钟之后生效，请耐心等待</div>");
                    var targetId = "#redisClusterDelNodeModal" + instanceId;
                    setTimeout("$('" + targetId + "').modal('hide');window.location.reload();", 1000);
                } else {
                    alert(message);
                    redisClusterDelNodeBtn.disabled = false;
                    $("#redisClusterDelNodeInfo" + instanceId).html("<div class='alert alert-error' ><button class='close' data-dismiss='alert'>×</button><strong>Error!</strong>执行失败，请查找原因！</div>");
                }
            }
        );
    }

    function genSlaveIp(appId, instanceId) {
        var genSlaveIpNote = document.getElementById('genSlaveIpNote' + instanceId);
        genSlaveIpNote.style.display = 'none';
        $.post(
            '/manage/app/genSlaveIp',
            {
                appId: appId,
                masterInstanceId: instanceId
            },
            function (data) {
                var result = data.result;
                var ip = data.machineRes;
                if (result != 'success') {
                    genSlaveIpNote.innerHTML = '自动生成slave节点ip失败: ' + result;
                    genSlaveIpNote.style.display = '';
                } else {
                    console.log(ip);
                    $('#slaveIp' + instanceId).selectpicker('val', ip);
                    $('#slaveIp' + instanceId).selectpicker('refresh');
                    console.log($('#slaveIp' + instanceId).selectpicker('val'));
                    if ($('#slaveIp' + instanceId).selectpicker('val') == '-1') {
                        genSlaveIpNote.innerHTML = '自动生成slave节点ip失败，请重试';
                        genSlaveIpNote.style.display = '';
                    }
                }
            }
        );
    }


    function redisClusterAddSlave(appId, instanceId) {
        var slaveIpObj = document.getElementById("slaveIp" + instanceId);
        var index = slaveIpObj.selectedIndex;
        var slaveIp = slaveIpObj[index].value;
        if (slaveIp == "" || slaveIp == "-1") {
            alert("从节点Ip不能为空");
            return false;
        }
        var redisClusterAddSlaveBtn = document.getElementById("redisClusterAddSlaveBtn" + instanceId);
        redisClusterAddSlaveBtn.disabled = true;
        $.post(
            '/manage/app/addSlave',
            {
                appId: appId,
                masterInstanceId: instanceId,
                slaveHost: slaveIp
            },
            function (data) {
                if (data == 1) {
                    alert("执行成功!");
                    $("#redisClusterAddSlaveInfo" + instanceId).html("<div class='alert alert-error' ><button class='close' data-dismiss='alert'>×</button><strong>Success!</strong>添加成功!</div>");
                    var targetId = "#redisClusterAddSlaveModal" + instanceId;
                    setTimeout("$('" + targetId + "').modal('hide');window.location.reload();", 1000);
                } else {
                    redisClusterAddSlaveBtn.disabled = false;
                    $("#redisClusterAddSlaveInfo" + instanceId).html("<div class='alert alert-error' ><button class='close' data-dismiss='alert'>×</button><strong>Error!</strong>执行失败，请查找原因！</div>");
                }
            }
        );
    }

    function redisSentinelAddSlave(appId, instanceId) {
        var slaveIp = document.getElementById("sentinelSlaveIp" + instanceId);
        if (slaveIp.value == "") {
            alert("从节点Ip不能为空");
            slaveIp.focus();
            return false;
        }
        var redisSentinelAddSlaveBtn = document.getElementById("redisSentinelAddSlaveBtn" + instanceId);
        redisSentinelAddSlaveBtn.disabled = true;
        $.post(
            '/manage/app/addSlave',
            {
                appId: appId,
                masterInstanceId: instanceId,
                slaveHost: slaveIp.value
            },
            function (data) {
                if (data == 1) {
                    alert("执行成功!");
                    $("#redisSentinelAddSlaveInfo" + instanceId).html("<div class='alert alert-error' ><button class='close' data-dismiss='alert'>×</button><strong>Success!</strong>添加成功!</div>");
                    var targetId = "#redisSentinelAddSlaveModal" + instanceId;
                    setTimeout("$('" + targetId + "').modal('hide');window.location.reload();", 1000);
                } else {
                    redisSentinelAddSlaveBtn.disabled = false;
                    $("#redisSentinelAddSlaveInfo" + instanceId).html("<div class='alert alert-error' ><button class='close' data-dismiss='alert'>×</button><strong>Error!</strong>执行失败，请查找原因！</div>");
                }
            }
        );
    }


    function redisSentinelFailOver(appId) {
        var redisSentinelFailOverBtn = document.getElementById("redisSentinelFailOverBtn");
        redisSentinelFailOverBtn.disabled = true;
        $.post(
            '/manage/app/sentinelFailOver',
            {
                appId: appId
            },
            function (data) {
                if (data == 1) {
                    alert("执行成功!");
                    $("#redisSentinelFailOverInfo").html("<div class='alert alert-error' ><button class='close' data-dismiss='alert'>×</button><strong>Success!</strong>执行成功，应用的拓扑结构要1分钟之后生效，请耐心等待</div>");
                    var targetId = "#redisSentinelFailOverModal";
                    setTimeout("$('" + targetId + "').modal('hide');window.location.reload();", 1000);
                } else {
                    redisSentinelFailOverBtn.disabled = false;
                    $("#redisSentinelFailOverInfo").html("<div class='alert alert-error' ><button class='close' data-dismiss='alert'>×</button><strong>Error!</strong>执行失败，请查找原因！</div>");
                }
            }
        );
    }

    function redisSentinelReset(appId) {
        var redisSentinelFailOverBtn = document.getElementById("redisSentinelResetBtn");
        redisSentinelFailOverBtn.disabled = true;
        $.post(
            '/manage/app/sentinelReset',
            {
                appId: appId
            },
            function (data) {
                if (data == 1) {
                    alert("执行成功!");
                    $("#redisSentinelResetInfo").html("<div class='alert alert-error' ><button class='close' data-dismiss='alert'>×</button><strong>Success!</strong>执行成功，应用的拓扑结构要1分钟之后生效，请耐心等待</div>");
                    var targetId = "#redisSentinelResetModal";
                    setTimeout("$('" + targetId + "').modal('hide');window.location.reload();", 1000);
                } else {
                    redisSentinelFailOverBtn.disabled = false;
                    $("#redisSentinelResetInfo").html("<div class='alert alert-error' ><button class='close' data-dismiss='alert'>×</button><strong>Error!</strong>执行失败，请查找原因！</div>");
                }
            }
        );
    }

    function redisAddFailSlotsMaster(appId, instanceId) {
        var failSlotsMasterHost = document.getElementById("failSlotsMasterHost" + instanceId);
        var redisAddFailSlotsMasterBtn = document.getElementById("redisAddFailSlotsMasterBtn" + instanceId);
        redisAddFailSlotsMasterBtn.disabled = true;
        $.post(
            '/manage/app/addFailSlotsMaster',
            {
                appId: appId,
                failSlotsMasterHost: failSlotsMasterHost.value,
                instanceId: instanceId
            },
            function (data) {
                if (data == 1 || data == 2) {
                    if (data == 1) {
                        alert("执行成功!");
                    } else {
                        alert("集群所有slots已经分配，无需补充！");
                    }
                    $("#redisAddFailSlotsMasterInfo" + instanceId).html("<div class='alert alert-error' ><button class='close' data-dismiss='alert'>×</button><strong>Success!</strong>执行成功，应用的拓扑结构要1分钟之后生效，请耐心等待</div>");
                    var targetId = "#redisAddFailSlotsMasterModal" + instanceId;
                    setTimeout("$('" + targetId + "').modal('hide');window.location.reload();", 1000);
                } else {
                    redisAddFailSlotsMasterBtn.disabled = false;
                    $("#redisAddFailSlotsMasterInfo" + instanceId).html("<div class='alert alert-error' ><button class='close' data-dismiss='alert'>×</button><strong>Error!</strong>执行失败，请查找原因！</div>");
                }
            }
        );
    }

    function redisAddSentinel(appId) {
        var sentinelIp = document.getElementById("sentinelIp");
        if (sentinelIp.value == "") {
            alert("sentinel Ip不能为空");
            slaveIp.focus();
            return false;
        }
        var redisAddSentinelBtn = document.getElementById("redisAddSentinelBtn");
        redisAddSentinelBtn.disabled = true;
        $.post(
            '/manage/app/addSentinel',
            {
                appId: appId,
                sentinelHost: sentinelIp.value
            },
            function (data) {
                if (data == 1) {
                    alert("执行成功!");
                    $("#redisAddSentinelInfo").html("<div class='alert alert-error' ><button class='close' data-dismiss='alert'>×</button><strong>Success!</strong>添加成功!</div>");
                    var targetId = "#redisAddSentinelModal";
                    setTimeout("$('" + targetId + "').modal('hide');window.location.reload();", 1000);
                } else {
                    redisAddSentinelBtn.disabled = false;
                    $("#redisAddSentinelInfo").html("<div class='alert alert-error' ><button class='close' data-dismiss='alert'>×</button><strong>Error!</strong>执行失败，请查找原因！</div>");
                }
            }
        );
    }

</script>
<div class="row">
    <div class="page-header">
        <h4>
            应用实例管理-${appDesc.name}(${appDesc.typeDesc})
            <input type="hidden" id="appId" value="${appDesc.appId}"/>
            <c:choose>
                <c:when test="${appDesc.type == 2}">
                    <c:if test="${lossSlotsSegmentMap != null && lossSlotsSegmentMap != '' && lossSlotsSegmentMap.size() > 0}">
                        <font color="red">丢失的slots:${lossSlotsSegmentMap}</font>
                    </c:if>
                </c:when>
                <c:when test="${appDesc.type == 5}">
                    <button type="button" class="btn btn-small btn-primary" data-target="#redisAddSentinelModal"
                            data-toggle="modal">添加sentinel节点
                    </button>
                    <button type="button" class="btn btn-small btn-primary" data-target="#redisSentinelFailOverModal"
                            data-toggle="modal">&nbsp;FailOver&nbsp;
                    </button>
                    <button type="button" class="btn btn-small btn-primary" data-target="#redisSentinelResetModal"
                    data-toggle="modal">&nbsp;Reset&nbsp;
                    </button>
                </c:when>
            </c:choose>
        </h4>
    </div>
    <div style="margin-top: 20px">
        <table class="table table-bordered table-striped table-hover">
            <thead>
            <tr>
                <th>ID</th>
                <th>实例</th>
                <th>k8s容器</th>
                <th>实例状态</th>
                <th>角色</th>
                <th>主实例ID</th>
                <th>内存使用</th>
                <th>对象数</th>
                <th>连接数</th>
                <th>命中率</th>
                <th>碎片率</th>
                <th>日志</th>
                <th>节点运维</th>
                <th>故障转移</th>
            </tr>
            </thead>
            <tbody>
            <select style="display:none" id="instanceList">
                <c:forEach items="${instanceList}" var="instance">
                    <option value="${instance.id}"/>
                </c:forEach>
                </optgroup>
            </select>

            <c:forEach items="${instanceListMap}" var="instanceList">
                <c:forEach var="instance" items="${instanceList.value}" varStatus="status">
                    <c:set var="instanceStatsMapKey" value="${instance.ip}:${instance.port}"></c:set>
                    <tr>
                        <td>
                            <a href="/admin/instance/index?instanceId=${instance.id}" target="_blank">${instance.id}</a>
                        </td>
                        <td>
                            <c:if test="${k8sMachineMaps.get(instance.ip)!=null}">
                                <a target="_blank" href="/manage/machine/pod/changelist?ip=${instance.ip}"
                                   title="查看pod变更记录">${instance.ip}</a>:${instance.port}
                            </c:if>
                            <c:if test="${k8sMachineMaps.get(instance.ip)==null}">
                                ${instance.ip}:${instance.port}
                            </c:if>
                        </td>
                        <td>
                            <c:if test="${k8sMachineMaps.get(instance.ip)!=null}">
                                <a target="_blank" href="/manage/machine/pod/changelist?ip=${instance.ip}"
                                   title="查看pod变更记录">是</a>
                            </c:if>
                            <c:if test="${k8sMachineMaps.get(instance.ip)==null}">
                                否
                            </c:if>
                        </td>
                        <td>${instance.statusDesc}</td>
                        <td>${instance.roleDesc}</td>
                        <c:choose>
                            <c:when test="${instance.masterInstanceId >0}">
                                <td>
                                    <a href="/admin/instance/index?instanceId=${instance.masterInstanceId}"
                                       target="_blank">${instance.masterInstanceId}</a>
                                </td>
                            </c:when>
                            <c:otherwise>
                                <td></td>
                            </c:otherwise>
                        </c:choose>
                        <td>
                            <div class="progress margin-custom-bottom0">
                                <c:choose>
                                    <c:when test="${(instanceStatsMap[instanceStatsMapKey]).memUsePercent >= 80}">
                                        <c:set var="progressBarStatus" value="progress-bar-danger"/>
                                    </c:when>
                                    <c:otherwise>
                                        <c:set var="progressBarStatus" value="progress-bar-success"/>
                                    </c:otherwise>
                                </c:choose>
                                <div class="progress-bar ${progressBarStatus}"
                                     role="progressbar"
                                     aria-valuenow="${(instanceStatsMap[instanceStatsMapKey]).memUsePercent}"
                                     aria-valuemax="100"
                                     aria-valuemin="0"
                                     style="width: ${(instanceStatsMap[instanceStatsMapKey]).memUsePercent}%">

                                    <label style="color: #000000">
                                        <fmt:formatNumber
                                                value="${(instanceStatsMap[instanceStatsMapKey]).usedMemory / 1024 / 1024 / 1024}"
                                                pattern="0.00"/>G&nbsp;&nbsp;Used/<fmt:formatNumber
                                            value="${(instanceStatsMap[instanceStatsMapKey]).maxMemory / 1024 / 1024 / 1024}"
                                            pattern="0.00"/>G&nbsp;&nbsp;Total
                                    </label>
                                </div>
                            </div>
                        </td>
                        <td>
                                ${(instanceStatsMap[instanceStatsMapKey]).currItems}
                        </td>
                        <td>
                            <a href="/admin/instance/index?instanceId=${instance.id}&tabTag=instance_clientList" target="_blank">
                                ${(instanceStatsMap[instanceStatsMapKey]).currConnections}
                            </a>
                        </td>
                        <td>${(instanceStatsMap[instanceStatsMapKey]).hitPercent}</td>
                        <td>
                            <c:set var="memFragmentationRatio"
                                   value="${(instanceStatsMap[instanceStatsMapKey]).memFragmentationRatio}"/>
                            <c:choose>
                                <c:when test="${memFragmentationRatio > 5 && (instanceStatsMap[instanceStatsMapKey]).usedMemory > 1024 * 1024 * 100}">
                                    <c:set var="memFragmentationRatioLabel" value="label-danger"/>
                                </c:when>
                                <c:when test="${memFragmentationRatio >= 3 && memFragmentationRatio < 5 && (instanceStatsMap[instanceStatsMapKey]).usedMemory > 1024 * 1024 * 100}">
                                    <c:set var="memFragmentationRatioLabel" value="label-warning"/>
                                </c:when>
                                <c:otherwise>
                                    <c:set var="memFragmentationRatioLabel" value="label-success"/>
                                </c:otherwise>
                            </c:choose>
                            <label class="label ${memFragmentationRatioLabel}">${memFragmentationRatio}</label>
                        </td>
                        <td>
                            <a target="_blank" href="/manage/instance/log?instanceId=${instance.id}">查看</a>
                        </td>
                        <td>
                            <div>
                                <c:choose>
                                    <c:when test="${instance.status ==2}">
                                        <button type="button" class="btn btn-small btn-success"
                                                onclick="startInstance('${appDesc.appId}','${instance.id}')">
                                            &nbsp;启动实例&nbsp;
                                        </button>
                                        <c:if test="${instance.type ==2}">
                                            <br/><br/>
                                            <button type="button" class="btn btn-small btn-danger"
                                                 onclick="forgetInstance('${appDesc.appId}','${instance.id}')">
                                              &nbsp;永久下线&nbsp;
                                            </button>
                                        </c:if>
                                    </c:when>
                                    <c:when test="${instance.status ==0}">
                                        <button type="button" class="btn btn-small btn-success"
                                                onclick="startInstance('${appDesc.appId}','${instance.id}')">
                                            &nbsp;启动实例&nbsp;
                                        </button>
                                        <br/><br/>
                                        <button type="button" class="btn btn-small btn-warning"
                                                onclick="shutdownInstance('${appDesc.appId}','${instance.id}')">
                                            &nbsp;下线实例&nbsp;
                                        </button>
                                        <c:choose>
                                            <c:when test="${instance.masterInstanceId == 0 && appDesc.type == 2 && lossSlotsSegmentMap[instanceStatsMapKey] != null && lossSlotsSegmentMap[instanceStatsMapKey] != ''}">
                                                <button type="button" class="btn btn-small btn-primary"
                                                        data-target="#redisAddFailSlotsMasterModal${instance.id}"
                                                        data-toggle="modal">修复slot丢失数据
                                                </button>
                                            </c:when>
                                        </c:choose>
                                    </c:when>
                                    <c:when test="${instance.status == -1}">
                                        <button type="button" class="btn btn-small btn-success"
                                                onclick="startInstance('${appDesc.appId}','${instance.id}')">
                                            &nbsp;启动实例&nbsp;
                                        </button>
                                        <br/><br/>
                                        <button type="button" class="btn btn-small btn-warning"
                                                onclick="shutdownInstance('${appDesc.appId}','${instance.id}')">
                                            &nbsp;下线实例&nbsp;
                                        </button>
                                    </c:when>
                                    <c:when test="${instance.status == 1}">
                                        <button type="button" class="btn btn-small btn-warning"
                                                onclick="shutdownInstance('${appDesc.appId}', '${instance.id}')">
                                            &nbsp;下线实例&nbsp;
                                        </button>
                                        <c:if test="${instance.masterInstanceId == 0 and instance.type != 5}">
                                            <br/><br/>
                                            <button type="button" class="btn btn-small btn-primary"
                                                    data-target="#redisClusterAddSlaveModal_${instance.id}"
                                                    data-toggle="modal">添加Slave
                                            </button>
                                        </c:if>
                                    </c:when>
                                </c:choose>

                            </div>
                        </td>
                        <td>
                            <div>
                                <c:choose>
                                    <c:when test="${instance.status == 1}">
                                        <c:if test="${instance.masterInstanceId > 0 and instance.type == 2}">
                                            <button type="button" class="btn btn-small btn-success"
                                                    data-target="#redisClusterFailOverManualModal${instance.id}"
                                                    data-toggle="modal">&nbsp;&nbsp;Manual&nbsp;
                                            </button>
                                            <br/><br/>
                                            <button type="button" class="btn btn-small btn-primary"
                                                    data-target="#redisClusterFailOverForceModal${instance.id}"
                                                    data-toggle="modal">&nbsp;&nbsp;&nbsp;Force&nbsp;&nbsp;
                                            </button>
                                            <br/><br/>
                                            <button type="button" class="btn btn-small btn-danger"
                                                    data-target="#redisClusterFailOverTakeOverModal${instance.id}"
                                                    data-toggle="modal">TakeOver
                                            </button>
                                            <br/>
                                        </c:if>
                                    </c:when>
                                </c:choose>
                            </div>
                        </td>
                    </tr>
                </c:forEach>
                <tr style="height: 10px"></tr>
            </c:forEach>
            </tbody>
        </table>
    </div>
</div>


<div id="redisAddSentinelModal" class="modal fade" tabindex="-1" data-width="400">
    <div class="modal-dialog">
        <div class="modal-content">

            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true"></button>
                <h4 class="modal-title">添加sentinel节点</h4>
            </div>

            <form class="form-horizontal form-bordered form-row-stripped">
                <div class="modal-body">
                    <div class="row">
                        <!-- 控件开始 -->
                        <div class="col-md-12">
                            <!-- form-body开始 -->
                            <div class="form-body">
                                <div class="form-group">
                                    <label class="control-label col-md-3">sentinel节点Ip:</label>
                                    <div class="col-md-7">
                                        <input type="text" name="sentinelIp" id="sentinelIp" placeholder="sentinel节点Ip"
                                               class="form-control">
                                    </div>
                                </div>
                            </div>
                            <!-- form-body 结束 -->
                            <div id="redisAddSentinelInfo"></div>
                        </div>
                    </div>
                </div>

                <div class="modal-footer">
                    <button type="button" data-dismiss="modal" class="btn">Close</button>
                    <button type="button" id="redisAddSentinelBtn" class="btn red"
                            onclick="redisAddSentinel('${appDesc.appId}')">Ok
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>


<div id="redisSentinelFailOverModal" class="modal fade" tabindex="-1" data-width="400">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true"></button>
                <h4 class="modal-title">redis-Sentinel从节点FailOver操作</h4>
            </div>

            <div class="modal-body">
                <div class="row">
                    <!-- 控件开始 -->
                    <div class="container">
                        <div class="col-md-12">
                            <div>你确定执行failOver操作?</div>
                            <div id="redisSentinelFailOverInfo"></div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="modal-footer">
                <button type="button" data-dismiss="modal" class="btn">Close</button>
                <button type="button" id="redisSentinelFailOverBtn" class="btn red"
                        onclick="redisSentinelFailOver('${appDesc.appId}')">Ok
                </button>
            </div>
        </div>
    </div>
</div>

<div id="redisSentinelResetModal" class="modal fade" tabindex="-1" data-width="400">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true"></button>
                <h4 class="modal-title">redis-Sentinel Reset操作</h4>
            </div>

            <div class="modal-body">
                <div class="row">
                    <!-- 控件开始 -->
                    <div class="container">
                        <div class="col-md-12">
                            <div>你确定重置sentinel实例状态?</div>
                            <div id="redisSentinelResetInfo"></div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="modal-footer">
                <button type="button" data-dismiss="modal" class="btn">Close</button>
                <button type="button" id="redisSentinelResetBtn" class="btn red"
                        onclick="redisSentinelReset('${appDesc.appId}')">Ok
                </button>
            </div>
        </div>
    </div>
</div>


<c:forEach var="instance" items="${instanceList}" varStatus="status">
    <div id="redisClusterFailOverManualModal${instance.id}" class="modal fade" tabindex="-1" data-width="400">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true"></button>
                    <h4 class="modal-title">redis-Cluster从节点FailOver Manual操作</h4>
                </div>

                <div class="modal-body">
                    <div class="row">
                        <!-- 控件开始 -->
                        <div class="container">
                            <div class="col-md-12">
                                <div>你确定对实例${instance.id}执行FailOver Manual操作?</div>
                                <div id="redisClusterFailOverManualInfo${instance.id}"></div>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="modal-footer">
                    <button type="button" data-dismiss="modal" class="btn">Close</button>
                    <button type="button" id="redisClusterFailOverManualBtn${instance.id}" class="btn red"
                            onclick="redisClusterFailOverManual('${appDesc.appId}', '${instance.id}')">Ok
                    </button>
                </div>
            </div>
        </div>
    </div>

    <div id="redisClusterFailOverForceModal${instance.id}" class="modal fade" tabindex="-1" data-width="400">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true"></button>
                    <h4 class="modal-title">redis-Cluster从节点FailOver Force操作</h4>
                </div>

                <div class="modal-body">
                    <div class="row">
                        <!-- 控件开始 -->
                        <div class="container">
                            <div class="col-md-12">
                                <div>你确定对实例${instance.id}执行FailOver Force操作?</div>
                                <div id="redisClusterFailOverForceInfo${instance.id}"></div>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="modal-footer">
                    <button type="button" data-dismiss="modal" class="btn">Close</button>
                    <button type="button" id="redisClusterFailOverForceBtn${instance.id}" class="btn red"
                            onclick="redisClusterFailOverForce('${appDesc.appId}', '${instance.id}')">Ok
                    </button>
                </div>
            </div>
        </div>
    </div>


    <div id="redisClusterFailOverTakeOverModal${instance.id}" class="modal fade" tabindex="-1" data-width="400">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true"></button>
                    <h4 class="modal-title">redis-Cluster从节点FailOver TakeOver操作</h4>
                </div>

                <div class="modal-body">
                    <div class="row">
                        <!-- 控件开始 -->
                        <div class="container">
                            <div class="col-md-12">
                                <div>你确定对实例${instance.id}执行FailOver TakeOver操作?</div>
                                <div id="redisClusterFailOverTakeOverInfo${instance.id}"></div>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="modal-footer">
                    <button type="button" data-dismiss="modal" class="btn">Close</button>
                    <button type="button" id="redisClusterFailOverTakeOverBtn${instance.id}" class="btn red"
                            onclick="redisClusterFailOverTakeOver('${appDesc.appId}', '${instance.id}')">Ok
                    </button>
                </div>
            </div>
        </div>
    </div>


    <div id="redisClusterDelNodeModal${instance.id}" class="modal fade" tabindex="-1" data-width="400">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true"></button>
                    <h4 class="modal-title">redis-Cluster节点删除操作</h4>
                </div>

                <div class="modal-body">
                    <div class="row">
                        <div class="container">
                            <div class="col-md-12">
                                <div>你确定对实例${instance.id}执行删除操作?</div>
                                <div id="redisClusterDelNodeInfo${instance.id}"></div>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="modal-footer">
                    <button type="button" data-dismiss="modal" class="btn">Close</button>
                    <button type="button" id="redisClusterDelNodeBtn${instance.id}" class="btn red"
                            onclick="redisClusterDelNode('${appDesc.appId}', '${instance.id}')">Ok
                    </button>
                </div>
            </div>
        </div>
    </div>

    <div id="redisClusterAddSlaveModal_${instance.id}" name="addSlave-modal" class="modal fade" tabindex="-1"
         data-width="400"
         aria-hidden="true">
        <div class="modal-dialog">
            <div class="modal-content">

                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true"></button>
                    <h4 class="modal-title">添加slave节点</h4>
                    <h5>主节点信息：</h5>
                    <c:set var="realIp" value="${machineMap[instance.ip].info.realIp}"></c:set>
                    <c:set var="rack" value="${machineMap[instance.ip].info.rack}"></c:set>
                    <c:if test="${machineMap[instance.ip].info.realIp==null}">
                        <c:set var="realIp" value="无"></c:set>
                    </c:if>
                    <c:if test="${machineMap[instance.ip].info.rack==null}">
                        <c:set var="rack" value="无"></c:set>
                    </c:if>
                    <h5>【ID】${instance.id} 【实例】${instance.ip}:${instance.port}
                        【宿主机-机架】${realIp}-${rack}</h5>
                </div>

                <form class="form-horizontal form-bordered form-row-stripped">
                    <div class="modal-body">
                        <div class="row">
                            <!-- 控件开始 -->
                            <div class="col-md-12">
                                <!-- form-body开始 -->
                                <div class="form-body">
                                    <div class="form-group">
                                        <label class="control-label col-md-2 col-md-offset-1">slave节点:</label>
                                        <div class="col-md-5" id="div_slaveIp${instance.id}">
                                            <select id="slaveIp${instance.id}" name="slaveIp"
                                                    class="selectpicker bla bla bli col-md-6" data-live-search="true">
                                            </select>
                                        </div>
                                        <button type="button" class="btn btn-small btn-success col-md-2 col-md-offset-1"
                                                onclick="genSlaveIp('${appDesc.appId}', '${instance.id}')"> 自动生成
                                        </button>
                                        <br/><br/>
                                        <p id="genSlaveIpNote${instance.id}" class="col-md-8 col-md-offset-3"
                                           style="color: red; display: none">
                                            <i class="ace-icon fa fa-exclamation-triangle bigger-120"></i>&nbsp;&nbsp;&nbsp;自动生成slave节点ip失败
                                        </p>
                                    </div>
                                </div>
                                <!-- form-body 结束 -->
                                <div id="redisClusterAddSlaveInfo${instance.id}"></div>
                            </div>
                        </div>
                    </div>

                    <div class="modal-footer">
                        <button type="button" data-dismiss="modal" class="btn">Close</button>
                        <button type="button" id="redisClusterAddSlaveBtn${instance.id}" class="btn red"
                                onclick="redisClusterAddSlave('${appDesc.appId}', '${instance.id}')">Ok
                        </button>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <div id="redisSentinelAddSlaveModal${instance.id}" class="modal fade" tabindex="-1" data-width="400">
        <div class="modal-dialog">
            <div class="modal-content">

                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true"></button>
                    <h4 class="modal-title">添加slave节点(主节点:${instance.id}, ${instance.ip}:${instance.port})</h4>
                </div>

                <form class="form-horizontal form-bordered form-row-stripped">
                    <div class="modal-body">
                        <div class="row">
                            <!-- 控件开始 -->
                            <div class="col-md-12">
                                <!-- form-body开始 -->
                                <div class="form-body">
                                    <div class="form-group">
                                        <label class="control-label col-md-3">Slave节点Ip:</label>
                                        <div class="col-md-7">
                                            <input type="text" name="sentinelSlaveIp" id="sentinelSlaveIp${instance.id}"
                                                   placeholder="Slave节点Ip" class="form-control">
                                        </div>
                                    </div>
                                </div>
                                <!-- form-body 结束 -->
                                <div id="redisSentinelAddSlaveInfo${instance.id}"></div>
                            </div>
                        </div>
                    </div>

                    <div class="modal-footer">
                        <button type="button" data-dismiss="modal" class="btn">Close</button>
                        <button type="button" id="redisSentinelAddSlaveBtn${instance.id}" class="btn red"
                                onclick="redisSentinelAddSlave('${appDesc.appId}', '${instance.id}')">Ok
                        </button>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <div id="redisAddFailSlotsMasterModal${instance.id}" class="modal fade" tabindex="-1" data-width="400">
        <div class="modal-dialog">
            <div class="modal-content">

                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true"></button>
                    <h4 class="modal-title">修复failslots</h4>
                </div>

                <form class="form-horizontal form-bordered form-row-stripped">
                    <div class="modal-body">
                        <div class="row">
                            <!-- 控件开始 -->
                            <div class="col-md-12">
                                <!-- form-body开始 -->
                                <div class="form-body">
                                    <div class="form-group">
                                        <label class="control-label col-md-3">节点Ip:</label>
                                        <div class="col-md-7">
                                            <input type="text" name="failSlotsMasterHost"
                                                   id="failSlotsMasterHost${instance.id}"
                                                   placeholder="failSlotsMasterHost" class="form-control">
                                        </div>
                                    </div>
                                </div>
                                <!-- form-body 结束 -->
                                <div id="redisAddFailSlotsMasterInfo${instance.id}"></div>
                            </div>
                        </div>
                    </div>

                    <div class="modal-footer">
                        <button type="button" data-dismiss="modal" class="btn">Close</button>
                        <button type="button" id="redisAddFailSlotsMasterBtn${instance.id}" class="btn red"
                                onclick="redisAddFailSlotsMaster('${appDesc.appId}', '${instance.id}')">Ok
                        </button>
                    </div>
                </form>
            </div>
        </div>
    </div>


</c:forEach>


<select id="slaveIp" name="slaveIp" hidden="hidden">
    <option value="-1">请选择slave</option>
    <c:forEach items="${machineMap}" var="machineEntry">
        <c:set var="machine" value="${machineEntry.value}"></c:set>
        <fmt:formatNumber var="usedCpu"
                          value="${machineInstanceCountMap[machine.info.ip]}"
                          pattern="0"/>
        <fmt:formatNumber var="cpu" value="${machine.info.cpu}"
                          pattern="0"/>
        <fmt:formatNumber var="cpuUsage" value="${usedCpu/cpu*100}"
                          pattern="0"/>
        <fmt:formatNumber var="usedMemRss"
                          value="${((machine.machineMemInfo.usedMemRss)/1024/1024/1024)}"
                          pattern="0.0"/>
        <fmt:formatNumber var="mem" value="${ machine.info.mem}"
                          pattern="0.0"/>
        <fmt:formatNumber var="memUsage" value="${usedMemRss/mem*100}"
                          pattern="0"/>
        <c:set var="realIp" value="${machine.info.realIp}"></c:set>
        <c:set var="rack" value="${machine.info.rack}"></c:set>
        <c:if test="${machine.info.realIp==''}"><c:set var="realIp" value="无realIp"></c:set></c:if>
        <c:if test="${machine.info.rack==''}"><c:set var="rack" value="无rack"></c:set></c:if>
        <c:if test="${machine.info.useType==0}">
            <c:set var="extraDesc" value="专用-${machine.info.extraDesc}"></c:set>
        </c:if>
        <c:if test="${machine.info.useType==1}">
            <c:set var="extraDesc" value="测试-${machine.info.extraDesc}"></c:set>
        </c:if>
        <c:if test="${machine.info.useType==2}">
            <c:set var="extraDesc" value="混合-${machine.info.extraDesc}"></c:set>
        </c:if>
        <option value="${machine.ip}">${machine.ip}：【${usedCpu}/${cpu}核(${cpuUsage}%)】【${usedMemRss}/${mem}G(${memUsage}%)】【${realIp}-${rack}】【${extraDesc}】</option>
    </c:forEach>
</select>








