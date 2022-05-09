<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp" %>
<link rel="stylesheet" type="text/css" href="/resources/select/bootstrap-select.css"/>
<script src="/resources/manage/plugins/jquery-1.10.2.min.js"></script>
<script type="text/javascript">var jQuery_1_10_2 = $;</script>
<script src="/resources/manage/plugins/bootstrap/js/bootstrap.min.js" type="text/javascript"></script>
<script type="text/javascript" src="/resources/select/bootstrap-select.js"></script>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <title>CacheCloud应用运维</title>
    <jsp:include page="/WEB-INF/include/head.jsp"/>
    <script type="text/javascript" src="/resources/js/jquery-console.js"></script>

</head>
<body role="document">
    <%@include file="/WEB-INF/jsp/manage/include/cache_cloud_paginator_js.jsp" %>
    <div class="page-container">
        <div class="page-content">
            <div class="row">
                <div class="col-md-12">
                    <h3 class="page-title">
                        应用滚动重启/修改配置记录
                    </h3>
                </div>
            </div>
            <div class="row">
                <div class="col-md-12">
                    <div style="float:right">
                        <form class="form-inline" role="form" action="/manage/app/restart/getRestartRecord" method="get" id="appList">
                            <div class="form-group">
                                <input type="text" class="form-control" id="appId" name="appId" value="${appId}"
                                       placeholder="应用ID">
                                <input type="hidden" name="pageNo" id="pageNo">
                            </div>
                            <button type="submit" class="btn btn-info">查询</button>
                        </form>
                    </div>
                    <br/><br/>
                    <div class="portlet box light-grey">
                        <div class="portlet-title">
                            <div class="caption"><i class="fa fa-globe"></i>应用重启记录列表</div>
                            <div class="tools">
                                <a href="javascript:;" class="collapse"></a>
                            </div>
                        </div>
                        <div class="table-scrollable">
                            <table class="table table-striped table-bordered table-hover" id="tableDataList">
                                <thead>
                                <tr>
                                    <td>记录ID</td>
                                    <td>操作人</td>
                                    <td>应用ID</td>
                                    <td>应用名称</td>
                                    <td>涉及实例<br><font size="1" style="font-weight: bold">点击实例查看日志</font></td>
                                    <td>重启类型</td>
                                    <td>日志</td>
                                    <td>开始时间</td>
                                    <td>结束时间</td>
                                    <td>状态</td>
                                </tr>
                                </thead>
                                <tbody>
                                <c:forEach items="${restartRecordList}" var="configRestartRecord">
                                    <tr class="odd gradeX">
                                        <td>
                                            ${configRestartRecord.id}
                                        </td>
                                        <td>
                                            ${configRestartRecord.userName}
                                        </td>
                                        <td>
                                            ${configRestartRecord.appId}
                                        </td>
                                        <td>
                                            ${configRestartRecord.appName}
                                        </td>
                                        <td>
                                            <c:forEach items="${configRestartRecord.instanceIdList}" var="instance_id">
                                                <c:set var="hostPort" value="${instanceInfoMap[instance_id].hostPort}"></c:set>
                                                <a href="/manage/instance/log?instanceId=${instance_id}" target="_blank">${instance_id}</a>(${hostPort})
                                                <br/>
                                            </c:forEach>
                                        </td>
                                        <td>
                                            <c:if test="${configRestartRecord.operateType != null && configRestartRecord.operateType == 0}">
                                                滚动重启
                                            </c:if>
                                            <c:if test="${configRestartRecord.operateType != null && configRestartRecord.operateType == 1}">
                                                修改冷配置并重启
                                            </c:if>
                                            <c:if test="${configRestartRecord.operateType != null && configRestartRecord.operateType == 2}">
                                                修改热配置
                                            </c:if>
                                        </td>
                                        <td escapeXml="false">
                                            <c:forEach items="${configRestartRecord.logList}" var="log">
                                                <c:out value="${log}" escapeXml="false"></c:out>
                                                <br/>
                                            </c:forEach>
                                        </td>
                                        <td>
                                            <fmt:formatDate pattern="yyyy-MM-dd HH:mm:ss" value="${configRestartRecord.startTime}"/>
                                        </td>
                                        <td>
                                            <fmt:formatDate pattern="yyyy-MM-dd HH:mm:ss" value="${configRestartRecord.endTime}"/>
                                        </td>
                                        <td>
                                            <c:if test="${configRestartRecord.status != null && configRestartRecord.status == 0}">
                                                等待中
                                            </c:if>
                                            <c:if test="${configRestartRecord.status != null && configRestartRecord.status == 1}">
                                                运行中
                                            </c:if>
                                            <c:if test="${configRestartRecord.status != null && configRestartRecord.status == 2}">
                                                成功
                                            </c:if>
                                            <c:if test="${configRestartRecord.status != null && configRestartRecord.status == 3}">
                                                失败
                                            </c:if>
                                            <c:if test="${configRestartRecord.status != null && configRestartRecord.status == 4}">
                                                <font style="color: red;">配置已改待重启</font>
                                            </c:if>
                                        </td>
                                    </tr>
                                </c:forEach>
                                </tbody>
                            </table>
                        </div>
                        <div style="margin-bottom: 10px;float: right;margin-right: 15px">
                                <span>
                                    <ul id='ccPagenitor' style="margin-bottom: 0px;margin-top: 0px"></ul>
                                    <div id="pageDetail"
                                         style="float:right;padding-top:7px;padding-left:8px;color:#4A64A4;display: none">共${page.totalPages}页,${page.totalCount}条</div>
                                </span>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</body>

<script>
    var installId = "";
    var method = "";

    /**
     * 一键执行
     */
    function install(appId) {
        installId = appId;
        if (!$('#versionSelect' + installId).val()) {
            alert("请先选择升级版本");
            return;
        }
        if ($('#install' + installId).html() == "继续") {
            // step forward
            setTimeout(method + '()', 1000);
        } else if ($('#install' + installId).html() == "升级完成") {
            // step complete
            window.location.reload();
        } else {
            instanceCheck(appId);
        }
    }

    /**
     * 继续 step forward
     */
    function goOn(m) {
        $("#install" + installId).html("继续");
        method = m;
    }

    /**
     * 1.实例检查
     */
    function instanceCheck() {
        var instanceId = "instanceCheck" + installId;
        active(instanceId);
        $("#install" + installId).html("配置检查中...");
        $("#versionSelect" + installId).attr("disabled", "disabled");//不能修改版本
        $.post('/manage/redis/upgrade/check/instance',
            {
                "appId": installId,
                "upgradeVersionId": $('#versionSelect' + installId + " option:selected").attr('versionid'),
                "upgradeVersionName": $('#versionSelect' + installId + " option:selected").val()
            },
            function (data) {
                if (data.status == 1) {
                    $("#instanceInfo" + installId).html(data.instanceInfo);
                    if (data.machineInstallInfo != "") {
                        toastr.success(data.machineInstallInfo);
                    }
                    $("#div" + installId).removeAttr("hidden");
                    complete(instanceId);
                    // Slave更新配置&重启
                    goOn("slaveUpdate");
                } else if (data.status == -1) {
                    $("#instanceInfo" + installId).html(data.instanceInfo);
                    $("#div" + installId).removeAttr("hidden");
                    toastr.error("异常信息：" + data.message);
                    warn(instanceId);
                    goOn("instanceCheck");
                } else {
                    toastr.error("操作失败,请查看日志!");
                    warn(instanceId);
                    goOn("instanceCheck");
                }
            }
        );
    }

    /**
     * 2.Slave配置替换重启
     */
    function slaveUpdate() {
        var slaveUpdateId = "slaveUpdate" + installId;
        active(slaveUpdateId);
        $("#install" + installId).html("配置更新中...");
        $.post('/manage/redis/upgrade/slave/update/config',
            {
                "appId": installId,
                "upgradeVersionId": $('#versionSelect' + installId + " option:selected").attr('versionid'),
                "upgradeVersionName": $('#versionSelect' + installId + " option:selected").val()
            },
            function (data) {
                if (data.status == 1) {
                    $("#instanceInfo" + installId).html(data.instanceInfo);
                    $("#instanceLog" + installId).html(data.instanceLog);
                    complete(slaveUpdateId);
                    // 下一步主从failover
                    goOn("msFailover");
                } else if (data.status == -1) {
                    toastr.error("异常信息：" + data.message);
                    warn(slaveUpdateId);
                    goOn("slaveUpdate");
                } else {
                    toastr.error("操作失败,请查看日志!");
                    warn(slaveUpdateId);
                    goOn("slaveUpdate");
                }
            });
    }

    /**
     * 3.主从Failover
     */
    function msFailover() {
        var failoverId = "msFailover" + installId;
        active(failoverId);
        $("#install" + installId).html("主从切换中...");
        $.post('/manage/redis/upgrade/slave/failover',
            {
                "appId": installId,
                "upgradeVersionId": $('#versionSelect' + installId + " option:selected").attr('versionid')
            },
            function (data) {
                if (data.status == 1) {
                    $("#instanceInfo" + installId).html(data.instanceInfo);
                    $("#instanceLog" + installId).html(data.instanceLog);
                    complete(failoverId);
                    // 下一步新的从节点替换配置
                    goOn("newSlaveUpdate");
                } else if (data.status == -1) {
                    toastr.error("异常信息：" + data.message);
                    warn(failoverId);
                    goOn("msFailover");
                } else {
                    toastr.error("操作失败,请查看日志!");
                    warn(failoverId);
                    goOn("msFailover");
                }
            });
    }

    /**
     * 4.new Slave配置替换重启
     */
    function newSlaveUpdate() {
        var newSlaveUpdateId = "newSlaveUpdate" + installId;
        active(newSlaveUpdateId);
        $("#install" + installId).html("配置更新中...");
        $.post('/manage/redis/upgrade/slave/update/config',
            {
                "appId": installId,
                "upgradeVersionId": $('#versionSelect' + installId + " option:selected").attr('versionid'),
                "upgradeVersionName": $('#versionSelect' + installId + " option:selected").val()
            },
            function (data) {
                if (data.status == 1) {
                    $("#instanceInfo" + installId).html(data.instanceInfo);
                    $("#instanceLog" + installId).html(data.instanceLog);
                    complete(newSlaveUpdateId);
                    goOn("upgradeComplete");
                } else if (data.status == -1) {
                    toastr.error("异常信息：" + data.message);
                    warn(newSlaveUpdateId);
                    goOn("newSlaveUpdate");
                } else {
                    toastr.error("操作失败,请查看日志!");
                    warn(newSlaveUpdateId);
                    goOn("newSlaveUpdate");
                }
            });
    }

    /**
     * 5.升级完成
     */
    function upgradeComplete() {
        var upgradeCompleteId = "upgradeComplete" + installId;
        $.post('/manage/redis/upgrade/complete/check',
            {
                "appId": installId,
                "upgradeVersionId": $('#versionSelect' + installId + " option:selected").attr('versionid')
            },
            function (data) {
                if (data.status == 1) {
                    $("#instanceInfo" + installId).html(data.instanceInfo);
                    $("#instanceLog" + installId).html(data.instanceLog);
                    active(upgradeCompleteId);
                    $("#install" + installId).html("升级完成");
                } else if (data.status == -1) {
                    toastr.error("异常信息：" + data.message);
                    warn(upgradeCompleteId);
                } else {
                    toastr.error("操作失败,请查看日志!");
                    warn(upgradeCompleteId);
                }
            });
    }

    function warn(id) {
        $("#" + id).addClass("warn");
    }

    function disable(id) {
        $("#" + id).removeClass("active").addClass("disabled");
    }

    function active(id) {
        $("#" + id).removeClass("disabled").removeClass("warn").addClass("active");
    }

    function complete(id) {
        $("#" + id).removeClass("active").removeClass("warn").addClass("complete");
    }
</script>