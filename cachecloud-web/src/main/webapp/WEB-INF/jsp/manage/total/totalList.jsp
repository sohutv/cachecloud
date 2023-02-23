<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp" %>
<div class="page-container">
    <div class="page-content">
        <div class="row">
            <div class="col-md-12">
                <h3 class="page-title">
                    应用运维
                </h3>
            </div>
        </div>
        <div class="row">
            <div class="col-md-12">
                <div style="float:right">
                    <form class="form-inline" role="form" action="/manage/total/list" method="get" id="appList">
                        <div class="form-group">
                            <input type="text" class="form-control" id="appParam" name="appParam" value="${appParam}"
                                   placeholder="应用ID/应用名">
                            <input type="hidden" name="pageNo" id="pageNo">
                        </div>
                        <button type="submit" class="btn btn-info">查询</button>
                    </form>
                </div>
                <br/><br/>
                <div class="portlet box light-grey">
                    <div class="portlet-title">
                        <div class="caption"><i class="fa fa-globe"></i>应用列表</div>
                        <div class="tools">
                            <a href="javascript:;" class="collapse"></a>
                        </div>
                    </div>
                    <div class="table-scrollable">
                        <table class="table table-striped table-bordered table-hover" id="tableDataList">
                            <thead>
                            <tr>
                                <td>应用ID</td>
                                <td>应用名</td>
                                <td>版本</td>
                                <td>应用类型</td>
                                <td>内存详情</td>
                                <td>最大碎片率</td>
                                <td>命中率</td>
                                <td>天数</td>
                                <td>申请状态</td>
                                <td>淘汰策略</td>
                                <td>操作</td>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach items="${appDetailList}" var="appDetail">
                                <tr class="odd gradeX">
                                    <td>
                                        <c:choose>
                                            <c:when test="${appDetail.appDesc.status == 0 or appDetail.appDesc.status == 1}">
                                                ${appDetail.appDesc.appId}
                                            </c:when>
                                            <c:when test="${appDetail.appDesc.status == 2 or appDetail.appDesc.status == 3 or appDetail.appDesc.status == 4}">
                                                <a target="_blank"
                                                   href="/manage/app/index?appId=${appDetail.appDesc.appId}">${appDetail.appDesc.appId}</a>
                                            </c:when>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${appDetail.appDesc.status == 0 or appDetail.appDesc.status == 1}">
                                                ${appDetail.appDesc.name}
                                            </c:when>
                                            <c:when test="${appDetail.appDesc.status == 2 or appDetail.appDesc.status == 3 or appDetail.appDesc.status == 4}">
                                                <a target="_blank"
                                                   href="/admin/app/index?appId=${appDetail.appDesc.appId}"
                                                   title="${appDetail.appDesc.name}">
                                                        ${fn:substring(appDetail.appDesc.name,0,20)}
                                                        <c:if test="${fn:length(appDetail.appDesc.name)>=20}">...</c:if>
                                                </a>
                                            </c:when>
                                        </c:choose>
                                    </td>
                                    <td>${appDetail.appDesc.versionName}</td>
                                    <td>
                                            ${appDetail.appDesc.typeDesc}
                                    </td>
                                    <td>
                                        <span style="display:none"><fmt:formatNumber
                                                value="${appDetail.memUsePercent / 100}" pattern="0.00"/></span>
                                        <div class="progress margin-custom-bottom0">
                                            <c:choose>
                                                <c:when test="${appDetail.memUsePercent >= 80}">
                                                    <c:set var="progressBarStatus" value="progress-bar-danger"/>
                                                </c:when>
                                                <c:otherwise>
                                                    <c:set var="progressBarStatus" value="progress-bar-success"/>
                                                </c:otherwise>
                                            </c:choose>
                                            <div class="progress-bar ${progressBarStatus}"
                                                 role="progressbar" aria-valuenow="${appDetail.memUsePercent}"
                                                 aria-valuemax="100"
                                                 aria-valuemin="0" style="width: ${appDetail.memUsePercent}%">
                                                <label style="color: #000000">
                                                    <fmt:formatNumber
                                                            value="${appDetail.mem  * appDetail.memUsePercent / 100 / 1024}"
                                                            pattern="0.00"/>G&nbsp;&nbsp;Used/<fmt:formatNumber
                                                        value="${appDetail.mem / 1024 * 1.0}" pattern="0.00"/>G&nbsp;&nbsp;Total
                                                </label>
                                            </div>
                                        </div>
                                    </td>
                                    <td>
                                        <a target="_blank"
                                           href="/admin/instance/index?instanceId=${appDetail.instIdWithHighestMemFragRatio}">${appDetail.highestMemFragRatio}</a>
                                    </td>
                                    <td>
                                        <span style="display:none"><fmt:formatNumber
                                                value="${appDetail.hitPercent / 100}" pattern="0.00"/></span>
                                        <c:choose>
                                            <c:when test="${appDetail.hitPercent <= 0}">
                                                无
                                            </c:when>
                                            <c:when test="${appDetail.hitPercent <= 30}">
                                                <label class="label label-danger">${appDetail.hitPercent}%</label>
                                            </c:when>
                                            <c:when test="${appDetail.hitPercent >= 30 && appDetail.hitPercent < 50}">
                                                <label class="label label-warning">${appDetail.hitPercent}%</label>
                                            </c:when>
                                            <c:when test="${appDetail.hitPercent >= 50 && appDetail.hitPercent < 90}">
                                                <label class="label label-info">${appDetail.hitPercent}%</label>
                                            </c:when>
                                            <c:otherwise>
                                                <label class="label label-success">${appDetail.hitPercent}%</label>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>${appDetail.appDesc.appRunDays}</td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${appDetail.appDesc.status == 0}">
                                                <font color="red">未申请</font>
                                            </c:when>
                                            <c:when test="${appDetail.appDesc.status == 1}">
                                                <font color="red">申请中</font>
                                            </c:when>
                                            <c:when test="${appDetail.appDesc.status == 2}">
                                                运行中
                                            </c:when>
                                            <c:when test="${appDetail.appDesc.status == 3}">
                                                <font color="red">已下线</font>
                                            </c:when>
                                            <c:when test="${appDetail.appDesc.status == 4}">
                                                <font color="red">驳回</font>
                                            </c:when>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${appDetail.appDesc.maxmemoryPolicyDesc == null}">
                                                默认
                                            </c:when>
                                            <c:otherwise>
                                                ${appDetail.appDesc.maxmemoryPolicyDesc}
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${appDetail.appDesc.status == 2}">
                                                <a target="_blank" type="button" class="btn btn-small btn-success"
                                                   href="/manage/app/index?appId=${appDetail.appDesc.appId}">应用运维</a>

                                                <a target="_blank" type="button" class="btn btn-small btn-info"
                                                   href="/manage/app/migrate/init?appId=${appDetail.appDesc.appId}">应用迁移</a>

                                                <button type="button" class="btn btn-small btn-danger"
                                                        id="offline${appDetail.appDesc.appId}"
                                                        onclick="offLine(${appDetail.appDesc.appId})">应用下线
                                                </button>

                                                <c:if test="${appDetail.appDesc.isVersionUpgrade==0}">
                                                    <button id="install" class="btn btn-info" data-toggle="modal"
                                                            disabled="disabled" style="background:#CCCCCC">
                                                        最新版本
                                                    </button>
                                                </c:if>
                                                <c:if test="${appDetail.appDesc.isVersionUpgrade==1}">
                                                    <button type="button" class="btn btn-small btn-primary"
                                                            data-target="#upgradeRedisVersionModal${appDetail.appDesc.appId}"
                                                            data-toggle="modal">
                                                        版本升级
                                                    </button>
                                                </c:if>
                                            </c:when>
                                        </c:choose>
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

<c:forEach items="${appDetailList}" var="appDetail">
    <c:if test="${appDetail.appDesc.isVersionUpgrade == 1}">
        <%@include file="upgradeRedis.jsp" %>
    </c:if>
</c:forEach>

<script type="text/javascript">
    function offLine(appId,appAuditId) {
        if (confirm("确认要下线该应用？应用id=" + appId)) {
            $.ajax({
                type: "get",
                url: "/manage/app/offLine.json",
                data: {appId: appId,appAuditId: appAuditId,},
                success: function (result) {
                    alert(result.message);
                    setTimeout("reloadPage(" + result.taskId + ");", 0);
                }
            });
        }
    }

    function reloadPage(taskid) {
        location.href = "/manage/task/flow?taskId=" + taskid;
    }
</script>

<script>
    // 配置预览
    function configPreview(appId) {
        window.open("/manage/redisConfig/init?resourceId=" + $('#versionSelect' + appId + " option:selected").attr('versionid'));
    }

    // 升级配置对比
    function configContrast(currentVerisonId, appId) {
        window.open("/manage/redisConfig/contrast?currentVersionId=" + currentVerisonId + "&upgradeVersionId=" + $('#versionSelect' + appId + " option:selected").attr('versionid'));
    }
</script>

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