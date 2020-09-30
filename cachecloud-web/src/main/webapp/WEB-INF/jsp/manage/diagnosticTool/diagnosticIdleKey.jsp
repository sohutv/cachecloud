<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<script type="text/javascript" src="/resources/bootstrap/jquery/jquery-1.11.0.js"></script>
<%@include file="/WEB-INF/jsp/manage/commons/taglibs.jsp" %>
<%@include file="/WEB-INF/jsp/manage/include/cache_cloud_main_js_include.jsp" %>
<%@include file="/WEB-INF/jsp/manage/include/cache_cloud_main_css.jsp" %>

<script type="text/javascript">
    var TableManaged = function () {
        return {
            //main function to initiate the module
            init: function () {

                if (!jQuery().dataTable) {
                    return;
                }

                $('#idlekey_tableDataList').dataTable({
                    "searching": true,
                    "bLengthChange": false,
                    "iDisplayLength": 15,
                    "sPaginationType": "bootstrap",
                    "oLanguage": {
                        "sLengthMenu": "_MENU_ records",
                        "oPaginate": {
                            "sPrevious": "Prev",
                            "sNext": "Next"
                        }
                    }
                });

                jQuery('#idlekey_tableDataList_wrapper>div:first-child').css("display", "none");
            }
        };
    }();

    $(function () {
        $('.selectpicker').selectpicker({
            'selectedText': 'cat',
            'size': 8,
            'dropupAuto': false
        });
        $('.selectpicker').selectpicker('refresh');

        App.init(); // initlayout and core plugins
        TableManaged.init();
    });

    $('#modal-idlekeyResult').on('shown.bs.modal', function (e) {
        $('#modal-idlekeytitle').html('');
        $('#idleKeyResultCount').html('');
        $('#idleKeyResultTable').html('');

        var redisKey = $(e.relatedTarget).data('rediskey');
        var title = $(e.relatedTarget).data('title');
        $('#modal-idlekeytitle').html(title);
        $.get(
            '/manage/app/tool/diagnostic/data.json',
            {
                redisKey: redisKey,
                type: 2
            },
            function (data) {
                $('#idleKeyResultCount').append(
                    '<tr>' +
                    '<td>key (共计' + data.count + '个）</td>' +
                    '<td>idleTime(单位：天)</td>' +
                    '</tr>'
                );
                var map = data.result;
                for (var key in map) {
                    $('#idleKeyResultTable').append(
                        '<tr>' +
                        '<td>' + key + '</td>' +
                        '<td>' + (map[key] / 3600 / 24).toFixed(1) + '</td>' +
                        '</tr>'
                    );
                }
            }
        );

    });

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
                    $('#' + instance_select).append("<option value=''>所有主节点</option>");
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

    function submitDiagnostic(type) {

        var appId;
        var size;
        var nodes;
        var params = [];
        if (type == 2) {
            appId = $('#idlekey-select').selectpicker('val');
            if (appId == null || appId == '') {
                alert("请选择应用");
                return;
            }
            nodes = $('#idlekey_instance-select').selectpicker('val');

            var idleDay = $('#idleDay').val();

            params.push(idleDay == '' ? 7 : idleDay);
            size = $('#idlekey_size-select').selectpicker('val');

            params.push(size);

        }

        $.post(
            '/manage/app/tool/diagnostic/submit.json',
            {
                type: type,
                appId: appId,
                nodes: nodes == null ? "" : nodes.toString(),
                params: params.toString()
            },
            function (data) {
                var status = data.status;
                if (status == 'success') {
                    alert("检测任务提交成功，任务id：" + data.taskId);
                    location.href = "/manage/app/tool/index?tabTag=idlekey";
                } else {
                    toastr.error("检测任务提交失败,请查看系统日志确认相关原因!");
                }
            }
        );
    }

    function testisNum(id) {
        var value = document.getElementById(id).value;
        if (value != "" && isNaN(value)) {
            alert("字节数请填入整数");
            document.getElementById(id).value = "";
            document.getElementById(id).focus();
            return false;
        }
        return true;
    }

</script>

<div class="row">
    <div class="col-md-12">
        <h4 class="page-header">
            提交idlekey诊断
        </h4>
    </div>
</div>
<div class="row">
    <div id="idlekey-div" class="col-md-12">
        <form class="form-inline" role="form" method="post" action="" id="idlekey-form" name="ec">
            <div class="form-group col-md-2">
                <label for="idlekey-select">应用&nbsp;&nbsp;</label>
                <select id="idlekey-select" name="appId" class="selectpicker show-tick form-control"
                        data-live-search="true" title="选择应用" data-width="35%"
                        onchange="changeAppIdSelect(this.value,'idlekey_instance-select')">
                    <option value="">选择应用</option>
                    <c:forEach items="${appDescMap}" var="appDescEntry">
                        <c:set value="${appDescEntry.value}" var="appDesc"></c:set>
                        <option value="${appDesc.appId}" title="${appDesc.appId} ${appDesc.name}">
                            【${appDesc.appId}】&nbsp;名称：${appDesc.name}&nbsp;类型：${appDesc.typeDesc}&nbsp;版本：${appDesc.versionName}
                        </option>
                    </c:forEach>
                </select>
            </div>
            <div class="form-group col-md-2 col-md-offset-1">
                <label for="idlekey_instance-select">实例&nbsp;&nbsp;</label>
                <select id="idlekey_instance-select" name="nodes"
                        class="selectpicker show-tick form-control" multiple
                        data-live-search="true" title="选择实例" data-width="35%">
                </select>
            </div>


            <div class="form-group col-md-2 col-md-offset-1">
                <label class="fa fa-question-circle" aria-hidden="true" title="空闲时间，单位天，默认7天"></label>
                <input id="idleDay" style="width: 70%" type="text" class="form-control" name="idleDay"
                       placeholder="空闲天数" onchange="testisNum(this.id)">
            </div>

            <div class="form-group col-md-2">
                <label for="idlekey_size-select">数量&nbsp;&nbsp;</label>
                <select id="idlekey_size-select" name="size" class="selectpicker show-tick form-control" title="数量"
                        data-width="15%">
                    <option value="-1">
                        全部
                    </option>
                    <option value="20">
                        top 20
                    </option>
                    <option value="50">
                        top 50
                    </option>
                    <option value="100">
                        top 100
                    </option>
                    <option value="200">
                        top 200
                    </option>
                    <option value="500">
                        top 500
                    </option>
                </select>
            </div>

            <div class="form-group col-md-1" style="float:right; width: max-content;">
                <button type="button" class="form-control btn green" onclick="submitDiagnostic(2)">提交检测</button>
            </div>
        </form>
    </div>
</div>


<div class="row">
    <div class="col-md-12">
        <h4 class="page-header">
            idlekey诊断任务列表
        </h4>
    </div>
</div>
<div class="row">
    <div class="col-md-12">
        <div style="float:left">
            <form class="form-inline" role="form" method="post" action="/manage/app/tool/index?tabTag=idlekey"
                  id="appList" name="ec">
                <div class="form-group">
                    <input type="text" class="form-control" id="appId" name="appId"
                           value="${appId}" placeholder="应用id">
                </div>
                <div class="form-group">
                    <input type="text" class="form-control" id="parentTaskId" name="parentTaskId"
                           value="${parentTaskId}" placeholder="任务id">
                </div>
                <div class="form-group">
                    <input type="text" class="form-control" id="auditId" name="auditId"
                           value="${auditId}" placeholder="审批id">
                </div>

                <div class="form-group">
                    <select name="diagnosticStatus" class="form-control">
                        <option value="" <c:if test="${diagnosticStatus == ''}">selected</c:if>>
                            诊断状态
                        </option>
                        <option value="0" <c:if test="${diagnosticStatus == 0}">selected</c:if>>
                            诊断中
                        </option>
                        <option value="1" <c:if test="${diagnosticStatus == 1}">selected</c:if>>
                            诊断完成
                        </option>
                        <option value="2" <c:if test="${diagnosticStatus == 2}">selected</c:if>>
                            诊断异常
                        </option>
                    </select>
                </div>
                <button type="submit" class="btn btn-success">查询</button>
            </form>
        </div>
    </div>
</div>
<br/>
<div class="row">
    <div class="col-md-12">
        <div class="portlet box light-grey" id="clientIndex">

            <table class="table table-striped table-bordered table-hover" id="idlekey_tableDataList">
                <thead>
                <tr>
                    <td>序号</td>
                    <th>appId</th>
                    <th>应用名称</th>
                    <th>诊断类型</th>
                    <th>任务id</th>
                    <th>子任务id</th>
                    <th>审批id</th>
                    <th>节点</th>
                    <th>诊断条件</th>

                    <th>创建时间</th>
                    <th>诊断状态</th>
                    <th>诊断耗时</th>
                    <th>诊断结果</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach items="${diagnosticTaskRecordList}" var="record" varStatus="status">
                    <c:set var="app_id" value="${record.appId}"></c:set>
                    <tr>
                        <td>${status.index + 1}</td>
                        <td>
                            <a target="_blank" href="/manage/app/index?appId=${app_id}">${app_id}</a>
                        </td>
                        <td>
                            <a target="_blank"
                               href="/admin/app/index?appId=${app_id}">${appDescMap[app_id].name}</a>
                        </td>
                        <td>
                            idlekey诊断
                        </td>
                        <td>
                            <a target="_blank" href="/manage/task/flow?taskId=${record.parentTaskId}">
                                    ${record.parentTaskId}
                            </a>
                        </td>
                        <td>
                            <a target="_blank" href="/manage/task/flow?taskId=${record.taskId}">
                                    ${record.taskId}
                            </a>
                        </td>
                        <td>
                            <a target="_blank"
                               href="/manage/app/auditList?auditId=${record.auditId}">
                                    ${record.auditId}
                            </a>
                        </td>
                        <td>
                                ${record.node}
                        </td>
                        <td>
                                ${record.diagnosticCondition}
                        </td>
                        <td>
                            <fmt:formatDate value="${record.createTime}" pattern="yyyy-MM-dd HH:mm:ss"/>
                        </td>
                        <td>
                            <c:if test="${record.status==0}">诊断中</c:if>
                            <c:if test="${record.status==1}">诊断完成</c:if>
                            <c:if test="${record.status==2}">诊断异常</c:if>
                        </td>
                        <td>
                            <c:if test="${record.status==1}">${record.formatCostTime}</c:if>
                        </td>
                        <td>
                            <c:if test="${record.status==1&&record.type!=4}">
                                <button type="button" class="btn btn-sm btn-info"
                                        data-target="#modal-idlekeyResult" data-toggle="modal"
                                        data-rediskey="${record.redisKey}"
                                        data-title="应用${app_id} 节点${record.node}">
                                    查看结果
                                </button>
                            </c:if>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </div>
</div>
<div id="modal-idlekeyResult" class="modal fade" tabindex="-1">
    <div class="modal-dialog" style="width: max-content">
        <div class="modal-content">

            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true"></button>
                <h4 class="modal-title">
                    诊断结果
                    <small><label id="modal-idlekeytitle" style="color: #00BE67"></label></small>
                </h4>
            </div>

            <form class="form-horizontal form-bordered form-row-stripped">
                <div class="modal-body" style="height:500px; overflow:scroll;">
                    <div class="row">
                        <!-- 控件开始 -->
                        <div class="col-md-12">
                            <table class="table table-bordered table-striped table-hover">
                                <thead id="idleKeyResultCount"></thead>
                                <tbody id="idleKeyResultTable"></tbody>
                            </table>
                        </div>
                    </div>
                </div>

                <div class="modal-footer">
                    <button type="button" data-dismiss="modal" class="btn">Close</button>
                </div>
            </form>
        </div>
    </div>
</div>
