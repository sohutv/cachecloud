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

                $('#delkey_tableDataList').dataTable({
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

                jQuery('#delkey_tableDataList_wrapper>div:first-child').css("display","none");
            }
        };
    }();

    $(function() {
        $('.selectpicker').selectpicker({
            'selectedText': 'cat',
            'size': 8,
            'dropupAuto':false
        });
        $('.selectpicker').selectpicker('refresh');

        App.init(); // initlayout and core plugins
        TableManaged.init();
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
        var nodes;
        var params = [];
        if (type == 4) {
            appId = $('#delkey-select').selectpicker('val');
            if (appId == null || appId == '') {
                alert("请选择应用");
                return;
            }
            nodes = $('#delkey_instance-select').selectpicker('val');

            params.push($('#delkey-pattern').val());
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
                    location.href = "/manage/app/tool/index?tabTag=deleteKey";
                } else {
                    toastr.error("检测任务提交失败,请查看系统日志确认相关原因!");
                }
            }
        );
    }

    function submitSampleScan() {
        document.getElementById('checkSampleScan').style.display = 'none';
        document.getElementById('submitDiagnosticBtn').disabled = true;

        $('#sampleResultCount').html('');
        $('#sampleResultTable').html('');
        var appId = $('#delkey-select').selectpicker('val');
        if (appId == null || appId == '') {
            alert("请选择应用");
            return;
        }

        var nodes = $('#delkey_instance-select').selectpicker('val');

        var pattern = $('#delkey-pattern').val();


        document.getElementById('checkSampleScan').style.display = 'inline';
        $('#checkSampleScan').html('采样进行中，请等待...');

        $.post(
            '/manage/app/tool/diagnostic/sampleScan.json',
            {
                appId: appId,
                nodes: nodes == null ? "" : nodes.toString(),
                pattern: pattern
            },
            function (data) {
                var status = data.status;
                if (status == 'success') {
                    alert("采样校验完成，请确认校验结果后提交删除任务！");
                    $('#checkSampleScan').html('查看采样结果');
                    document.getElementById('submitDiagnosticBtn').disabled = false;

                    $('#sampleResultCount').append(
                        '<tr>' +
                        '<td>key (共计' + data.count + '个）</td>' +
                        '</tr>'
                    );
                    var diagnosticResultList = data.result;
                    diagnosticResultList.forEach(function (diagnosticResult, index) {
                        $('#sampleResultTable').append(
                            '<tr>' +
                            '<td>' + diagnosticResult + '</td>' +
                            '</tr>'
                        );
                    })
                } else {
                    toastr.error("采样校验失败,请查看系统日志确认相关原因!");
                }
            }
        );
    }

</script>

<div class="row">
    <div class="col-md-12">
        <h4 class="page-header">
            提交删除任务
        </h4>
    </div>
</div>
<div class="row">
    <div id="delkey-div" class="col-md-12">
        <form class="form-inline" role="form" method="post" action="" id="delkey-form" name="ec">
            <div class="form-group col-md-2">
                <label for="delkey-select">应用&nbsp;&nbsp;</label>
                <select id="delkey-select" name="appId" class="selectpicker show-tick form-control"
                        data-live-search="true" title="选择应用" data-width="35%"
                        onchange="changeAppIdSelect(this.value,'delkey_instance-select')">
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
                <label for="delkey_instance-select">实例&nbsp;&nbsp;</label>
                <select id="delkey_instance-select" name="nodes"
                        class="selectpicker show-tick form-control" multiple
                        data-live-search="true" title="选择实例" data-width="35%">
                </select>
            </div>

            <div class="form-group col-md-2 col-md-offset-1" style="width: max-content">
                <input id="delkey-pattern" type="text" class="form-control" name="pattern" placeholder="匹配模式 pattern">
            </div>


            <div class="form-group col-md-1" style="float:right; width: max-content;">
                <button id="submitDiagnosticBtn" type="button" class="form-control btn red" disabled="disabled" onclick="submitDiagnostic(4)">提交删除</button>
            </div>
            <div class="form-group col-md-1" style="float:right; width: max-content;">
                <button type="button" class="form-control btn yellow" onclick="submitSampleScan()">采样校验</button>
                <a id="checkSampleScan" style="display: none" data-target="#modal-sampleResult" data-toggle="modal" data-title="应用${app_id}">采样进行中，请等待...</a>
                <div id="modal-sampleResult" class="modal fade" tabindex="-1">
                    <div class="modal-dialog" style="width: max-content">
                        <div class="modal-content">
                            <div class="modal-header">
                                <button type="button" class="close" data-dismiss="modal" aria-hidden="true"></button>
                                <h4 class="modal-title">
                                    采样诊断结果
                                    <small><label id="modal-title" style="color: #00BE67"></label></small>
                                </h4>
                            </div>

                            <form class="form-horizontal form-bordered form-row-stripped">
                                <div class="modal-body" style="height:500px; overflow:scroll;">
                                    <div class="row">
                                        <!-- 控件开始 -->
                                        <div class="col-md-12">
                                            <table class="table table-bordered table-striped table-hover">
                                                <thead id="sampleResultCount"></thead>
                                                <tbody id="sampleResultTable"></tbody>
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
            </div>

        </form>
    </div>
</div>


<div class="row">
    <div class="col-md-12">
        <h4 class="page-header">
            删除任务列表
        </h4>
    </div>
</div>
<div class="row">
    <div class="col-md-12">
        <div>
            <form class="form-inline" role="form" method="post" action="/manage/app/tool/index?tabTag=deleteKey"
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

            <table class="table table-striped table-bordered table-hover" id="delkey_tableDataList">
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
                    <th>删除键数量</th>
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
                             删除任务
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
                                ${record.redisKey}
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </div>
</div>

