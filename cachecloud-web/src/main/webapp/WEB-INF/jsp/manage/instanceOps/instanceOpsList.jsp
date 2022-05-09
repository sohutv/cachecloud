<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp" %>

<div class="page-container">
    <div class="page-content">
        <div class="table-toolbar">
            <div class="btn-group">
                <ul class="nav nav-tabs" id="app_tabs">
                    <li <c:if test="${tabId == 1}">class="active"</c:if>><a
                            href="/manage/instance/opsList?tabId=1">配置检测</a></li>
                    <li <c:if test="${tabId == 2}">class="active"</c:if>><a
                            href="/manage/instance/opsList?tabId=2">命令检测</a></li>
                    <li <c:if test="${tabId == 3}">class="active"</c:if>><a
                            href="/manage/instance/opsList?tabId=3">重启记录</a></li>
                </ul>
            </div>
            <div class="btn-group" style="float:right">
                <form class="form-inline" role="form">
                    <input name="tabId" id="tabId" value="${tabId}" type="hidden"/>
                </form>
            </div>

        </div>

        <div class="row">
            <div class="col-md-12">
                <div class="portlet box light-grey" id="configIndex">
                    <div class="portlet-title">
                        <div class="caption">
                            <i class="fa fa-globe"></i>配置检测
                        </div>
                        <div class="tools">
                            <a href="javascript:;" class="collapse"></a>
                        </div>
                    </div>

                    <div class="form-group">
                        <form class="form-horizontal form-bordered form-row-stripped">
                            <div class="modal-contained">
                                <div class="row">
                                    <!-- 控件开始 -->
                                    <div class="col-md-12">
                                        <!-- form-body开始 -->
                                        <label class="control-label col-md-1">
                                            redis版本:
                                        </label>
                                        <div class="col-md-3">
                                            <select name="versionId" id="versionId" class="form-control select2_category">
                                                <option value="" selected>请选择</option>
                                                <option value="0">所有</option>
                                                <c:forEach items="${redisVersionList}" var="redisVersion">
                                                    <option value="${redisVersion.id}">
                                                            ${redisVersion.name}
                                                    </option>
                                                </c:forEach>
                                            </select>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <div class="modal-contained">
                                <div class="row">
                                    <!-- 控件开始 -->
                                    <div class="col-md-12">
                                        <label class="control-label col-md-1">
                                            配置项:
                                        </label>
                                        <div class="col-md-3">
                                            <input type="text" name="configName" id="configName"
                                                   class="form-control" />
                                        </div>
                                        <label class="control-label col-md-1">
                                            比较:
                                        </label>
                                        <div class="col-md-2">
                                            <select name="compareType" id="compareType" class="form-control select2_category">
                                                <c:forEach items="${compareTypeList}" var="compareTypeEnum">
                                                    <option value="${compareTypeEnum.type}">
                                                            ${compareTypeEnum.info}
                                                    </option>
                                                </c:forEach>
                                            </select>
                                        </div>
                                        <label class="control-label col-md-1">
                                            比较值:
                                        </label>
                                        <div class="col-md-3">
                                            <input type="text" name="expectValue" id="expectValue"
                                                   class="form-control" />
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <div class="modal-contained">
                                <div class="row">
                                    <div class="control-label col-md-11" >
                                        <button type="button" id="moduleBtn" class="btn green" onclick="configCheck()">Ok</button>
                                    </div>
                                </div>
                            </div>
                        </form>
                    </div>

                    <table class="table table-striped table-bordered table-hover" name="checkResultList">
                        <thead>
                        <tr>
                            <th>redis版本</th>
                            <th>操作人</th>
                            <th>检测时间</th>
                            <th>检测条件</th>
                            <th>是否异常</th>
                            <th>异常查看</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach items="${checkResultList}" var="checkResult">
                            <tr class="odd gradeX">
                                <td>
                                    <c:forEach items="${redisVersionList}" var="redisVersion">
                                        <c:if test="${redisVersion.id==checkResult.versionId}">
                                            ${redisVersion.name}
                                        </c:if>
                                    </c:forEach>
                                    <c:if test="${checkResult.versionId==null || checkResult.versionId == 0}">
                                        所有
                                    </c:if>
                                </td>
                                <td>
                                    ${checkResult.userName}
                                </td>
                                <td>
                                    ${checkResult.createTimeStr}
                                </td>
                                <td>
                                    ${checkResult.configName}
                                    &nbsp;
                                    <c:forEach items="${compareTypeList}" var="compareTypeEnum">
                                        <c:if test="${checkResult.compareType == compareTypeEnum.type}">
                                            ${compareTypeEnum.info}
                                        </c:if>
                                    </c:forEach>
                                    &nbsp;
                                    ${checkResult.expectValue}
                                </td>
                                <td>
                                    <c:if test="${checkResult.success==true}">否</c:if>
                                    <c:if test="${checkResult.success==false}">是</c:if>
                                </td>

                                <td>
                                    <c:if test="${checkResult.success==true}"></c:if>
                                    <c:if test="${checkResult.success==false}">
                                        <button class="btn btn-warning btn-sm" style="float: right;">
                                            <a href="/manage/instance/getConfigCheck?uuid=${checkResult.key}"
                                               target="_blank"><font style="color: white">查看修复</font></a>
                                        </button>
                                    </c:if>
                                </td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>

                <div class="portlet box light-grey" id="commandIndex">
                    <div class="portlet-title">
                        <div class="caption">
                            <i class="fa fa-globe"></i>命令检测
                        </div>

                    </div>
                    <div class="form-group">
                        <form class="form-horizontal form-bordered form-row-stripped">
                            <div class="modal-contained">
                                <div class="row">
                                    <!-- 控件开始 -->
                                    <div class="col-md-12">
                                        <!-- form-body开始 -->
                                        <label class="control-label col-md-1">
                                            宿主机ip:
                                        </label>
                                        <div class="col-md-5">
                                            <select id="machineIp" name="machineIp"
                                                    class="selectpicker show-tick form-control"
                                                    data-live-search="true" title="选择宿主机" data-width="31%"
                                                    data-size="8">
                                            </select>
                                        </div>
                                        <label class="control-label col-md-2">
                                            pod ip:
                                        </label>
                                        <div class="col-md-4">
                                            <select id="podIp" name="podIp"
                                                    class="selectpicker show-tick form-control"
                                                    data-live-search="true" title="选择pod" data-width="31%"
                                                    data-size="8">
                                            </select>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <div class="modal-contained">
                                <div class="row">
                                    <!-- 控件开始 -->
                                    <div class="col-md-12">
                                        <label class="control-label col-md-1">
                                            命令:
                                        </label>
                                        <div class="col-md-3">
                                            <select name="command" id="command" class="form-control select2_category">
                                                <option value="bgsave">bgsave</option>
                                                <option value="bgrewriteaof">bgrewriteaof</option>
                                            </select>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <div class="modal-contained">
                                <div class="row">
                                    <div class="control-label col-md-11" >
                                        <button type="button" id="commandCheckBtn" class="btn green" onclick="commandCheck()">Ok</button>
                                    </div>
                                </div>
                            </div>
                        </form>
                    </div>
                    <table class="table table-striped table-bordered table-hover" name="tableDataList">
                        <thead>
                        <tr>
                            <th>操作人</th>
                            <th>检测时间</th>
                            <th>宿主机ip</th>
                            <th>pod ip</th>
                            <th>执行命令</th>
                            <th>是否失败</th>
                            <th>失败详情</th>
                        </tr>
                        </thead>
                        <tbody>
                            <c:forEach items="${commandCheckResult}" var="checkResult">
                                <tr class="odd gradeX">
                                    <td>${checkResult.userName}</td>
                                    <td>${checkResult.createTimeStr}</td>
                                    <td>${checkResult.machineIps}</td>
                                    <td>${checkResult.podIp}</td>
                                    <td>${checkResult.command}</td>
                                    <td>
                                        <c:if test="${checkResult.success==true}">否</c:if>
                                        <c:if test="${checkResult.success==false}">是</c:if>
                                    </td>
                                    <td>
                                    <c:if test="${checkResult.success==true}"></c:if>
                                    <c:if test="${checkResult.success==false}">
                                        <button class="btn btn-warning btn-sm" style="float: right;">
                                            <a href="/manage/instance/getCommandCheck?uuid=${checkResult.key}"
                                               target="_blank"><font style="color: white">查看</font></a>
                                        </button>
                                    </c:if>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>


                <div class="portlet box light-grey" id="restartIndex">
                    <div class="portlet-title">
                        <div class="caption">
                            <i class="fa fa-globe"></i>应用滚动重启/修改配置记录
                        </div>
                    </div>

                    <div class="form-group">
                        <div style="float:right">
                            <form class="form-inline" role="form" action="/manage/instance/opsList" method="get" id="appList">
                                <div class="form-group">
                                    <input type="text" class="form-control" id="appId" name="appId" value="${appId}"
                                           placeholder="应用ID">
                                    <input type="hidden" name="pageNo" id="pageNo">
                                    <input type="hidden" name="tabId" value="3">
                                </div>
                                <button type="submit" class="btn btn-info">查询</button>
                            </form>
                        </div>
                    </div>
                    <br/><br/>
                    <div class="row">
                        <div class="col-md-12">
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
                                    <td>操作</td>
                                </tr>
                                </thead>
                                <tbody>
                                <c:forEach items="${restartRecordList}" var="configRestartRecord">

                                    <c:if test="${configRestartRecord.status != null && configRestartRecord.status == 3}">
                                        <tr class="odd gradeX" style="color:red">
                                    </c:if>
                                    <c:if test="${configRestartRecord.status != null && configRestartRecord.status == 4}">
                                        <tr class="odd gradeX" style="color:dodgerblue">
                                    </c:if>
                                    <c:if test="${configRestartRecord.status != null && (configRestartRecord.status == 1 || configRestartRecord.status == 5)}">
                                        <tr class="odd gradeX" style="color:#e5680f">
                                    </c:if>
                                    <c:if test="${configRestartRecord.status != null && configRestartRecord.status == 6}">
                                        <tr class="odd gradeX" style="color:#c3680f">
                                    </c:if>
                                    <c:if test="${configRestartRecord.status == null or (configRestartRecord.status != 3 && configRestartRecord.status != 4 && configRestartRecord.status != 1 && configRestartRecord.status != 5 && configRestartRecord.status != 6)}">
                                        <tr class="odd gradeX">
                                    </c:if>
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
                                                配置已改待重启
                                            </c:if>
                                            <c:if test="${configRestartRecord.status != null && configRestartRecord.status == 5}">
                                                配置已改重启中
                                            </c:if>
                                            <c:if test="${configRestartRecord.status != null && configRestartRecord.status == 6}">
                                                已停止
                                            </c:if>
                                        </td>
                                        <td>
                                            <c:if test="${configRestartRecord.status != null && ((configRestartRecord.status == 1 && configRestartRecord.operateType == 0) || (configRestartRecord.status == 5 && configRestartRecord.operateType == 1))}">
                                                <button class="btn btn-info" onclick="stopRestart('${configRestartRecord.appId}')">停止</button>
                                            </c:if>
                                        </td>
                                    </tr>
                                </c:forEach>
                                </tbody>
                            </table>
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
    </div>
</div>

<script type="text/javascript">
    $(function () {
        var searchDate = $('#searchDate').val();
        if (searchDate == null || searchDate == '') {
            var time = new Date();
            var day = ("0" + time.getDate()).slice(-2);
            var month = ("0" + (time.getMonth() + 1)).slice(-2);
            var today = time.getFullYear() + "-" + (month) + "-" + (day);
            $('#searchDate').val(today);
        }
        var tabVal = $('#tabId').val();
        if (tabVal == 1) {
            $('#configIndex').removeAttr("hidden");
            $('#commandIndex').attr("hidden", "hidden");
            $('#restartIndex').attr("hidden", "hidden");
        } else if (tabVal == 2) {
            $('#configIndex').attr("hidden", "hidden");
            $('#commandIndex').removeAttr("hidden");
            $('#restartIndex').attr("hidden", "hidden");
        }else if (tabVal == 3) {
            $('#configIndex').attr("hidden", "hidden");
            $('#commandIndex').attr("hidden", "hidden");
            $('#restartIndex').removeAttr("hidden");
        }

        {
            $.post('/manage/instance/getMachineList.json',
                {
                    ip: null,
                    realIp: null,
                    searchType: 1
                },
                function (data) {
                    var status = data.status;
                    if (status == 1) {
                        var ipSet = data.ipSet;
                        $('#machineIp').append("<option value=''>" + '请选择' + "</option>");
                        for (var key in ipSet) {
                            $('#machineIp').append("<option value='" + ipSet[key] + "'>" + ipSet[key] + "</option>");
                        }
                        $('#machineIp').selectpicker('refresh');
                        $('#machineIp').selectpicker('render');

                        $('.dropdown-toggle').on('click', function () {
                            $('.dropdown-toggle').dropdown();
                        });
                    }
                }
            );

            $.post('/manage/instance/getMachineList.json',
                {
                    ip: null,
                    realIp: null,
                    searchType: 2
                },
                function (data) {
                    var status = data.status;
                    if (status == 1) {
                        var ipSet = data.ipSet;
                        $('#podIp').append("<option value=''>" + '请选择' + "</option>");
                        for (var key in ipSet) {
                            $('#podIp').append("<option value='" + ipSet[key] + "'>" + ipSet[key] + "</option>");
                        }
                        $('#podIp').selectpicker('refresh');
                        $('#podIp').selectpicker('render');

                        $('.dropdown-toggle').on('click', function () {
                            $('.dropdown-toggle').dropdown();
                        });
                    }
                }
            );
        }

    });

    //验证是数字
    function testisNum(id) {
        var value = document.getElementById(id).value;
        if (value != "" && isNaN(value)) {
            alert("请输入数字类型!");
            document.getElementById(id).value = "";
            document.getElementById(id).focus();
        }
    }

    function sendExpAppsStatDataEmail() {
        var searchDate = document.getElementById("searchDate").value;
        $.get('/manage/app/tool/sendExpAppsStatDataEmail', {searchDate: searchDate});
        alert("异常应用日报已发送，请查收")
    }

    //配置检测
    function configCheck(){
        var versionId = document.getElementById("versionId");
        var configName = document.getElementById("configName");
        if (configName.value == ""){
            alert("请填写配置名");
            configName.focus();
            return false;
        }
        var expectValue = document.getElementById("expectValue");
        var compareType = document.getElementById("compareType");
        document.getElementById("moduleBtn").disabled = "true";
        alert("即将执行检测，请稍等。")
        $.post(
            '/manage/instance/configCheck.json',
            {
                versionId: versionId.value,
                configName: configName.value,
                compareType: compareType.value,
                expectValue: expectValue.value
            },
            function (data) {
                document.getElementById("moduleBtn").disabled = false;
                var status = data.status;
                if (status == 1) {
                    alert("已执行，即将刷新页面展示结果");
                    window.location.reload();
                } else {
                    alert(data.message);
                    window.location.reload();
                }
            }
        );
    }

    //命令检测
    function commandCheck(){
        var machineIp = document.getElementById("machineIp");
        var podIp = document.getElementById("podIp");
        var command = document.getElementById("command");
        document.getElementById("commandCheckBtn").disabled = "true";
        if (confirm("确认执行命令检测？")) {
            alert("即将执行，请稍等。");
            $.post(
                '/manage/instance/commandCheck.json',
                {
                    machineIps: machineIp.value,
                    podIp: podIp.value,
                    command: command.value
                },
                function (data) {
                    document.getElementById("commandCheckBtn").disabled = false;
                    var status = data.status;
                    if (status == 1) {
                        alert("已执行，即将刷新页面展示结果");
                        window.location.reload();
                    } else {
                        alert(data.message);
                        window.location.reload();
                    }
                }
            );
        }
    }

    /**
     * 停止滚动重启
     * @param appId
     */
    function stopRestart(appId) {
        if (confirm("确认停止重启任务吗？")) {
            $.get(
                '/manage/app/restart/stopRestart?appId=' + appId,
                function (data) {
                    var status = data.status;
                    if (status == 200) {
                        alert(data.data);
                    } else {
                        alert(data.error);
                    }
                }
            );
        }
    }

</script>