<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <title>迁移数据</title>
    <jsp:include page="/WEB-INF/include/head.jsp"/>
    <script type="text/javascript" src="/resources/js/myPopover.js"></script>
    <script type="text/javascript">
        $(function () {
            $('#migrateTool').change(function () {
                if ($(this).val() == 0) {
                    $("#sourceRedisMigrateIndex").hide();
                    $("#sourceRedisShakeIndex").show();
                    $("#targetRedisMigrateIndex").hide();
                    $("#targetRedisShakeIndex").show();
                    $('#redis_shake_config').show();
                } else {
                    $("#sourceRedisShakeIndex").hide();
                    $("#sourceRedisMigrateIndex").show();
                    $("#targetRedisMigrateIndex").show();
                    $("#targetRedisShakeIndex").hide();
                    $('#redis_shake_config').hide();
                }
            });

            var targetAppId = document.getElementById('targetAppId').value;
            if (targetAppId != '') {
                fillAppInstanceList('targetRedisShakeIndex', 'redisTargetPass', 'redisTargetVersion', 'targetServers', 'targetRedisMigrateIndex', 'targetAppName', 'targetAppId');
            }

        });

        function changeDataType(appIdId, serversId, choose) {
            var dataType = choose.options[choose.selectedIndex].value;
            var appId = document.getElementById(appIdId);
            var servers = document.getElementById(serversId);
            if (dataType == 0) {
                appId.disabled = true;
                servers.disabled = false;
            } else if (dataType == 1) {
                appId.disabled = false;
                servers.disabled = true;
            }
        }

        function fillAppInstanceList(redisShakeIndexId, redisPassId, redisVersionId, instanceDetailId, redisMigrateIndexId, appName, appIdInputId) {
            var appId = document.getElementById(appIdInputId).value;
            if (appId == "") {
                //不能为空
                return;
            }
            var redisPass = document.getElementById(redisPassId);
            var instanceDetail = document.getElementById(instanceDetailId);
            var migrateTool = document.getElementById("migrateTool").value;
            var appNameId = document.getElementById(appName);
            var redisVersion = document.getElementById(redisVersionId);
            $.get(
                '/data/migrate/appInstanceList.json',
                {
                    appId: appId,
                    migrateTool: migrateTool
                },
                function (data) {
                    var instances = data.instances;
                    instanceDetail.value = instances;

                    var password = data.password;
                    redisPass.value = password;

                    var version = data.redisVersion;
                    redisVersion.value = version;

                    appNameId.innerHTML = data.appName;
                    $('#' + appName).attr("style", "display:'';");

                    var appType = data.appType;
                    var redisMigrateIndex = document.getElementById(migrateTool == 1 ? redisMigrateIndexId : redisShakeIndexId);
                    var options = redisMigrateIndex.options;
                    //修改select
                    if (appType == 2) {
                        for (var i = 0; i < options.length; i++) {
                            if (migrateTool == 0 && 7 == options[i].value) {
                                options[i].selected = 'selected';
                                break;
                            }
                            if (migrateTool == 1 && 1 == options[i].value) {
                                options[i].selected = 'selected';
                                break;
                            }
                        }
                    } else if (appType == 5) {
                        for (var i = 0; i < options.length; i++) {
                            if (migrateTool == 0 && 6 == options[i].value) {
                                options[i].selected = 'selected';
                                break;
                            }
                            if (migrateTool == 1 && 0 == options[i].value) {
                                options[i].selected = 'selected';
                                break;
                            }
                        }
                    } else if (appType == 6) {
                        for (var i = 0; i < options.length; i++) {
                            if (migrateTool == 0 && 5 == options[i].value) {
                                options[i].selected = 'selected';
                                break;
                            }
                            if (migrateTool == 1 && 0 == options[i].value) {
                                options[i].selected = 'selected';
                                break;
                            }
                        }
                    }
                }
            );
        }

        function checkMigrateFormat() {
            var sourceRedisMigrateIndex;
            var targetRedisMigrateIndex;
            if ($('#migrateTool').val() == 0) {
                sourceRedisMigrateIndex = document.getElementById("sourceRedisShakeIndex").value;
                targetRedisMigrateIndex = document.getElementById("targetRedisShakeIndex").value;
            } else {
                sourceRedisMigrateIndex = document.getElementById("sourceRedisMigrateIndex").value;
                targetRedisMigrateIndex = document.getElementById("targetRedisMigrateIndex").value;
            }
            var sourceServers = document.getElementById("sourceServers");
            var sourceAppId = document.getElementById("sourceAppId");
            var sourceDataType = document.getElementById("sourceDataType").value;
            var migrateMachineIp = document.getElementById("migrateMachineIp").value;
            var redisSourcePass = document.getElementById("redisSourcePass");
            var redisTargetPass = document.getElementById("redisTargetPass");
            var migrateTool = document.getElementById("migrateTool");


            //非cachecloud
            if (sourceDataType == 0 && sourceServers.value == "") {
                alert("源实例信息不能为空!");
                sourceServers.focus();
                return false;
                //cachecloud
            } else if (sourceDataType == 1 && sourceAppId.value == "") {
                alert("源appId不能为空!");
                sourceAppId.focus();
                return false;
            } else if (sourceDataType == 1 && sourceServers.value == "") {
                alert("请确保appId=" + sourceAppId.value + "下有实例信息");
                sourceAppId.focus();
                return false;
            }

            var targetAppId = document.getElementById("targetAppId");
            var targetServers = document.getElementById("targetServers");
            var targetDataType = document.getElementById("targetDataType").value;
            //非cachecloud
            if (targetDataType == 1 && targetAppId.value == "") {
                alert("目标appId不能为空!");
                targetAppId.focus();
                return false;
            } else if (targetDataType == 1 && targetServers.value == "") {
                alert("请确保appId=" + targetAppId.value + "下有实例信息");
                targetAppId.focus();
                return false;
            }

            $.get(
                '/data/migrate/check.json',
                {
                    sourceRedisMigrateIndex: sourceRedisMigrateIndex,
                    targetRedisMigrateIndex: targetRedisMigrateIndex,
                    sourceServers: sourceServers.value,
                    targetServers: targetServers.value,
                    migrateMachineIp: migrateMachineIp,
                    redisSourcePass: redisSourcePass.value,
                    redisTargetPass: redisTargetPass.value,
                    migrateTool: migrateTool.value,
                    versionid: $('#version option:selected').attr("versionid")
                },
                function (data) {
                    var status = data.status;
                    alert(data.message);
                    if (status == 1) {
                        var submitButton = document.getElementById("submitButton");
                        submitButton.disabled = false;

                        var checkButton = document.getElementById("checkButton");
                        checkButton.disabled = true;
                    }
                }
            );
        }

        function startMigrate() {
            var migrateTool = document.getElementById("migrateTool").value;
            var sourceRedisMigrateIndex = document.getElementById(migrateTool == 1 ? "sourceRedisMigrateIndex" : "sourceRedisShakeIndex").value;
            var targetRedisMigrateIndex = document.getElementById(migrateTool == 1 ? "targetRedisMigrateIndex" : "targetRedisShakeIndex").value;
            var sourceServers = document.getElementById("sourceServers");
            var targetServers = document.getElementById("targetServers");
            var migrateMachineIp = document.getElementById("migrateMachineIp").value;
            var sourceAppId = document.getElementById("sourceAppId");
            var targetAppId = document.getElementById("targetAppId");
            var redisSourcePass = document.getElementById("redisSourcePass");
            var redisTargetPass = document.getElementById("redisTargetPass");
            var redisSourceVersion = document.getElementById("redisSourceVersion");
            var redisTargetVersion = document.getElementById("redisTargetVersion");
            var source_rdb_parallel = document.getElementById('source_rdb_parallel');
            var parallel = document.getElementById('parallel');


            $.get(
                '/data/migrate/start.json',
                {
                    sourceRedisMigrateIndex: sourceRedisMigrateIndex,
                    targetRedisMigrateIndex: targetRedisMigrateIndex,
                    sourceServers: sourceServers.value,
                    targetServers: targetServers.value,
                    migrateMachineIp: migrateMachineIp,
                    versionid: $('#version option:selected').attr("versionid"),
                    sourceAppId: sourceAppId.value,
                    targetAppId: targetAppId.value,
                    redisSourcePass: redisSourcePass.value,
                    redisTargetPass: redisTargetPass.value,
                    redisSourceVersion: redisSourceVersion.value,
                    redisTargetVersion: redisTargetVersion.value,
                    migrateTool: migrateTool,
                    source_rdb_parallel: source_rdb_parallel.value,
                    parallel: parallel.value
                },
                function (data) {
                    var status = data.status;
                    if (status == 1) {
                        updateForImport(data.migrateId);
                        alert("迁移程序已经启动，请返回迁移列表关注迁移进度!");
                        location.href = "/data/migrate/index?status=-2";
                    } else {
                        alert("迁移失败,请查看日志分析原因!");
                    }
                    var checkButton = document.getElementById("checkButton");
                    checkButton.disabled = true;
                }
            );
        }

        function updateForImport(migrateId) {
            console.log("updateForImport migrateId:" + migrateId);
            var importId = document.getElementById("importId");
            if (importId != null && importId.value != '') {
                console.log("updateForImport importId:" + importId.value);
                $.get(
                    '/import/app/goOn.json',
                    {
                        importId: importId.value,
                        migrateId: migrateId
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
    </script>
</head>
<body role="document">
<div class="container">
    <jsp:include page="/WEB-INF/include/headMenu.jsp"/>
    <div id="systemAlert">
    </div>
    <div class="page-content">
        <div class="portlet box light-grey">
            <div class="portlet-body">
                <div class="form">
                    <form action="" method="post"
                          class="form-horizontal form-bordered form-row-stripped">
                        <div class="form-body">
                            <div class="row">
                                <div class="col-md-12">
                                    <h4 class="page-header">
                                        迁移工具配置
                                        <button class="btn btn-success btn-sm"
                                                data-container="body" data-toggle="popover" data-placement="top"
                                                data-content="<a href='http://cachecloud.github.io/2016/06/28/1.2.%20%E8%BF%81%E7%A7%BB%E5%B7%A5%E5%85%B7%E4%BD%BF%E7%94%A8%E8%AF%B4%E6%98%8E/'>使用文档</a>"
                                                style="border-radius:100%">
                                            ?
                                        </button>
                                    </h4>
                                </div>
                            </div>
                            <div class="row">
                                <div class="col-md-12">
                                    <div class="col-md-6">
                                        <div class="form-group">
                                            <label class="control-label col-md-3">
                                                迁移工具:
                                            </label>
                                            <div class="col-md-5">
                                                <select id="migrateTool" name="migrateTool"
                                                        class="form-control select2_category">
                                                    <option value="0">redis-shake</option>
                                                    <option value="1">redis-migrate-tool</option>
                                                </select>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label class="control-label col-md-3">
                                                迁移工具机器:
                                            </label>
                                            <div class="col-md-5">
                                                <select id="migrateMachineIp" name="migrateMachineIp"
                                                        class="form-control select2_category">
                                                    <c:forEach items="${machineInfoMap}" var="machineInfo">
                                                        <option value="${machineInfo.key}">
                                                            ip：${machineInfo.key}&nbsp;&nbsp;任务数：${machineInfo.value}
                                                        </option>
                                                    </c:forEach>
                                                </select>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label class="control-label col-md-3">
                                                版本:
                                            </label>
                                            <div class="col-md-5">
                                                <select name="type" id="version" class="form-control select2_category">
                                                    <c:forEach items="${resourcelist}" var="resource">
                                                        <c:if test="${resource.status == 1}">
                                                            <option
                                                                    <c:if test="${resource.id == currentVersion.id}">selected</c:if>
                                                                    versionid="${resource.id}">${resource.name}</option>
                                                        </c:if>
                                                    </c:forEach>
                                                </select>
                                            </div>
                                        </div>
                                        <div class="form-group" id="redis_shake_config">
                                            <label class="control-label col-md-3">
                                                参数配置:
                                            </label>
                                            <div class="col-md-4">
                                                <label class="control-label"
                                                       title="源RDB文件同步并发数">source.rdb.parallel (?):</label>
                                                <input type="text" id="source_rdb_parallel" class="form-control"
                                                       placeholder="源RDB文件同步并发数" value="8"/>
                                            </div>
                                            <div class="col-md-4">
                                                <label class="control-label" title="目标节点数据同步线程数">parallel (?):</label>
                                                <input type="text" id="parallel" class="form-control"
                                                       placeholder="目标节点数据同步线程数" value="16"/>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <div class="row">
                                <div class="col-md-12">
                                    <h4 class="page-header">
                                        源和目标配置
                                    </h4>
                                </div>
                            </div>

                            <div class="row">
                                <div class="col-md-12">
                                    <div class="col-md-6">
                                        <div class="form-group">
                                            <label class="control-label col-md-3">
                                                源类型:
                                            </label>
                                            <div class="col-md-5">
                                                <select id="sourceRedisMigrateIndex" name="sourceRedisMigrateIndex"
                                                        class="form-control select2_category" style="display: none">
                                                    <option value="0">
                                                        Redis普通节点
                                                    </option>
                                                    <option value="1">
                                                        Redis-cluster
                                                    </option>
                                                    <option value="2">
                                                        RDB-file
                                                    </option>
                                                    <option value="4">
                                                        AOF-file
                                                    </option>
                                                </select>
                                                <select id="sourceRedisShakeIndex" name="sourceRedisMigrateIndex"
                                                        class="form-control select2_category">
                                                    <option value="0">
                                                        Redis普通节点
                                                    </option>
                                                    <option value="5" <c:if test="${sourceType==5}">selected="selected"</c:if>>
                                                        Redis-standalone
                                                    </option>
                                                    <option value="6" <c:if test="${sourceType==6}">selected="selected"</c:if>>
                                                        Redis-sentinel
                                                    </option>
                                                    <option value="7" <c:if test="${sourceType==7}">selected="selected"</c:if>>
                                                        Redis-cluster
                                                    </option>
                                                </select>
                                            </div>
                                        </div>
                                    </div>

                                    <div class="col-md-6">
                                        <div class="form-group">
                                            <label class="control-label col-md-3">
                                                目标类型:
                                            </label>
                                            <div class="col-md-5">
                                                <select id="targetRedisMigrateIndex" name="targetRedisMigrateIndex"
                                                        class="form-control select2_category" style="display: none">
                                                    <option value="0">
                                                        Redis普通节点
                                                    </option>
                                                    <option value="1">
                                                        Redis-cluster
                                                    </option>
                                                    <option value="2">
                                                        RDB-file
                                                    </option>
                                                </select>
                                                <select id="targetRedisShakeIndex" name="sourceRedisMigrateIndex"
                                                        class="form-control select2_category">
                                                    <option value="0">
                                                        Redis普通节点
                                                    </option>
                                                    <option value="5">
                                                        Redis-standalone
                                                    </option>
                                                    <option value="6">
                                                        Redis-sentinel
                                                    </option>
                                                    <option value="7">
                                                        Redis-cluster
                                                    </option>
                                                </select>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <div class="row">
                                <div class="col-md-12">
                                    <div class="col-md-6">
                                        <div class="form-group">
                                            <label class="control-label col-md-3">
                                                源redis版本:
                                            </label>
                                            <div class="col-md-5">
                                                <input type="text" id="redisSourceVersion" name="redisSourceVersion"
                                                       value="${redisSourceVersion}"
                                                       placeholder="redis-x.x.x" class="form-control"/>
                                            </div>
                                        </div>
                                    </div>

                                    <div class="col-md-6">
                                        <div class="form-group">
                                            <label class="control-label col-md-3">
                                                目标redis版本:
                                            </label>
                                            <div class="col-md-5">
                                                <input type="text" id="redisTargetVersion" name="redisTargetVersion"
                                                       placeholder="redis-x.x.x" class="form-control"/>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <div class="row">
                                <div class="col-md-12">
                                    <div class="col-md-6">
                                        <div class="form-group">
                                            <label class="control-label col-md-3">
                                                数据源:
                                            </label>
                                            <div class="col-md-5">
                                                <select id="sourceDataType" name="sourceDataType"
                                                        class="form-control select2_category"
                                                        onchange="changeDataType('sourceAppId','sourceServers',this)">
                                                    <option value="1" selected="selected">
                                                        cachecloud
                                                    </option>
                                                    <option value="0"
                                                            <c:if test="${sourceDataType==0}">selected="selected"</c:if> >
                                                        非cachecloud
                                                    </option>
                                                </select>
                                            </div>
                                        </div>
                                    </div>

                                    <div class="col-md-6">
                                        <div class="form-group">
                                            <label class="control-label col-md-3">
                                                数据源:
                                            </label>
                                            <div class="col-md-5">
                                                <select id="targetDataType" name="targetDataType"
                                                        class="form-control select2_category"
                                                        onchange="changeDataType('targetAppId','targetServers',this)">
                                                    <option value="1" selected="selected">
                                                        cachecloud
                                                    </option>
                                                    <option value="0">
                                                        非cachecloud
                                                    </option>
                                                </select>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <div class="row">
                                <div class="col-md-12">
                                    <div class="col-md-6">
                                        <div class="form-group" id="sourceAppIdDiv">
                                            <label class="control-label col-md-3">
                                                源appId:
                                            </label>
                                            <div class="col-md-5">
                                                <input type="text" id="sourceAppId"
                                                       class="form-control"
                                                       onchange="fillAppInstanceList('sourceRedisShakeIndex', 'redisSourcePass','redisSourceVersion', 'sourceServers', 'sourceRedisMigrateIndex', 'sourceAppName', this.id)"/>
                                                <label id="sourceAppName" class="control-label"
                                                       style="display:none"></label>
                                            </div>
                                        </div>
                                    </div>

                                    <div class="col-md-6">
                                        <div class="form-group" id="targetAppIdDiv">
                                            <label class="control-label col-md-3">
                                                目标appId:
                                            </label>
                                            <div class="col-md-5">
                                                <input type="text" id="targetAppId" class="form-control"
                                                       value="${targetAppId}"
                                                       onchange="fillAppInstanceList('targetRedisShakeIndex', 'redisTargetPass','redisTargetVersion', 'targetServers', 'targetRedisMigrateIndex', 'targetAppName',this.id)"/>
                                                <label id="targetAppName" class="control-label"
                                                       style="display:none"></label>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <div class="row">
                                <div class="col-md-12">
                                    <div class="col-md-6">
                                        <div class="form-group">
                                            <label class="control-label col-md-3">
                                                源密码:
                                            </label>
                                            <div class="col-md-5">
                                                <input type="text" id="redisSourcePass" name="redisSourcePass"
                                                       value="${redisSourcePass}"
                                                       placeholder="没有无需填写" class="form-control"/>
                                            </div>
                                        </div>
                                    </div>

                                    <div class="col-md-6">
                                        <div class="form-group">
                                            <label class="control-label col-md-3">
                                                目标密码:
                                            </label>
                                            <div class="col-md-5">
                                                <input type="text" id="redisTargetPass" name="redisTargetPass"
                                                       placeholder="没有无需填写" class="form-control"/>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <div class="row">
                                <div class="col-md-12">
                                    <div class="col-md-6">
                                        <div class="form-group" id="sourceServersDiv">
                                            <label class="control-label col-md-3">
                                                源实例详情:
                                            </label>
                                            <div class="col-md-8">
                                                <textarea
                                                        <c:if test="${sourceServers==''}">disabled="disabled"</c:if>
                                                        rows="10" name="sourceServers"
                                                        id="sourceServers"
                                                        placeholder="节点详情"
                                                        class="form-control">${sourceServers}</textarea>
                                            </div>
                                        </div>
                                    </div>

                                    <div class="col-md-6">
                                        <div class="form-group" id="targetServersDiv">
                                            <label class="control-label col-md-3">
                                                目标实例详情:
                                            </label>
                                            <div class="col-md-8">
                                                <textarea disabled="disabled" rows="10" name="targetServers"
                                                          id="targetServers" placeholder="节点详情"
                                                          class="form-control"></textarea>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <div class="row">
                                <div class="col-md-12" style="border: 1px dashed #348f27" id="help">
                                    <div class="col-md-6">
                                        <span class="help-block">
                                            			<strong>redis-shake迁移工具实例样式说明</strong><br/>
                                            			每行格式都是:&nbsp;&nbsp;ip:port(例如：10.10.xx.xx:6379)<br/>
														1. standalone类型：<br/>
														&nbsp;&nbsp;&nbsp;&nbsp;masterIp:masterPort<br/>
														2. sentinel类型：<br/>
														&nbsp;&nbsp;&nbsp;master_name:master/slave<br/>
														&nbsp;&nbsp;&nbsp;sentinelIp1:sentinelPort1<br/>
														&nbsp;&nbsp;&nbsp;sentinelIp2:sentinelPort2<br/>
														&nbsp;&nbsp;&nbsp;sentinelIp3:sentinelPort3<br/>
                                            			3. cluster类型：<br/>
														&nbsp;&nbsp;&nbsp;&nbsp;masterIp1:masterPort1<br/>
														&nbsp;&nbsp;&nbsp;&nbsp;masterIp2:masterPort2<br/>
														&nbsp;&nbsp;&nbsp;&nbsp;masterIp3:masterPort3<br/>

                                        </span>
                                    </div>
                                    <div class="col-md-6">
                                        <span class="help-block">
                                            			<strong>redis-migrate-tool迁移工具实例样式说明</strong><br/>
														每行格式都是:&nbsp;&nbsp;ip:port(例如：10.10.xx.xx:6379)<br/>
														1. standalone类型：<br/>
														&nbsp;&nbsp;&nbsp;&nbsp;masterIp:masterPort<br/>
														2. sentinel类型：<br/>
														&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;masterIp:masterPort<br/>
														3. cluster类型：<br/>
														&nbsp;&nbsp;&nbsp;&nbsp;masterIp1:masterPort1<br/>
														&nbsp;&nbsp;&nbsp;&nbsp;slaveIp1:slavePort1<br/>
														&nbsp;&nbsp;&nbsp;&nbsp;masterIp2:masterPort2<br/>
														&nbsp;&nbsp;&nbsp;&nbsp;slaveIp2:slavePort2<br/>
														(可以是多对主从，只要把所有的cluster节点都按照格式写就可以，程序会自动判断)<br/>
                                    </span>
                                    </div>
                                </div>
                            </div>
                            <br/><br/>
                            <div class="form-actions fluid">
                                <div class="row">
                                    <div class="col-md-12">
                                        <div class="col-md-offset-5 col-md-9">
                                            <button id="submitButton" type="button" onclick="startMigrate()"
                                                    class="btn green" disabled="disabled">
                                                <i class="fa fa-check"></i>
                                                开始迁移
                                            </button>
                                            <button id="checkButton" type="button" class="btn green"
                                                    onclick="checkMigrateFormat()">
                                                <i class="fa fa-check"></i>
                                                检查格式
                                            </button>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <input id="importId" type="hidden" value="${importId}">
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </div>
</div>
<br/><br/><br/><br/><br/><br/><br/>
<jsp:include page="/WEB-INF/include/foot.jsp"/>
</body>
</html>

