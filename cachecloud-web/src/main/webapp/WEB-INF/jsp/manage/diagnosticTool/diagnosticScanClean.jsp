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

                $('#tableDataList').dataTable({
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

                jQuery('#tableDataList_wrapper>div:first-child').css("display", "none");
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
        // $('.dropdown-toggle').on('click', function () {
        //     $('.dropdown-toggle').dropdown();
        // });
        App.init(); // initlayout and core plugins
        TableManaged.init();
    });

    $('#modal-diagnosticResult').on('shown.bs.modal', function (e) {
        $('#modal-title').html('');
        $('#diagnosticResultCount').html('');
        $('#diagnosticResultTable').html('');

        var redisKey = $(e.relatedTarget).data('rediskey');
        var title = $(e.relatedTarget).data('title');
        $('#modal-title').html(title);
        $.get(
            '/manage/app/tool/diagnostic/data.json',
            {
                redisKey: redisKey,
                type: 6
            },
            function (data) {
                /*$('#diagnosticResultCount').append(
                    '<tr>' +
                    '<td>key (共计' + data.count + '个）</td>' +
                    '</tr>'
                );*/
                var diagnosticResultList = data.result;
                diagnosticResultList.forEach(function (diagnosticResult, index) {
                    $('#diagnosticResultTable').append(
                        '<tr>' +
                        '<td>' + diagnosticResult + '</td>' +
                        '</tr>'
                    );
                })
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
                    $('#' + instance_select).append("<option value='allMaster'>所有主节点</option>");
                    $('#' + instance_select).append("<option value='allSlave'>所有从节点</option>");
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

    function changeTtlResetShow() {
        var operateType = $('#scan_clean').selectpicker('val');
        if(operateType == 0 || operateType == 1){
            $('#resetTtl').css("display", "none");
        }else{
            $('#resetTtl').css("display", "block");
        }
    }

    function submitDiagnostic() {
        var appId;
        var nodes;
        var paramMap = new Map();
        appId = $('#scan-select').selectpicker('val');
        if (appId == null || appId == '') {
            alert("请选择应用");
            return;
        }
        nodes = $('#scan_instance-select').selectpicker('val');
        if (nodes == null || nodes == ''
            || nodes.toString() == null || nodes.toString() ==  '') {
            alert("请选择实例");
            return;
        }

        var pattern = $('#scan-pattern').val();
        if (pattern == null || pattern == '') {
            alert("请填写键匹配字符串");
            return;
        }

        var ttlResetLess = null;
        var ttlResetMore = null;
        var operateType = $('#scan_clean').selectpicker('val');
        if (operateType == 2) {
            ttlResetLess = $('#ttl-reset-less').val();
            ttlResetMore = $('#ttl-reset-more').val();
            if (ttlResetLess == null || ttlResetLess == ''
                || ttlResetMore == null || ttlResetMore == ''
                || ttlResetLess <= ttlResetMore) {
                alert("请填写ttl重置时间范围，且不能为同一时间，尽量分开，避免集中过期");
                return;
            }
        }
        paramMap.set("nodes", nodes.toString());
        paramMap.set("operateType", operateType);
        paramMap.set("pattern", pattern);
        paramMap.set("ttlLess", $('#ttl-value-less').val());
        paramMap.set("ttlMore", $('#ttl-value-more').val());
        paramMap.set("ttlResetLess", ttlResetLess);
        paramMap.set("ttlResetMore", ttlResetMore);
        paramMap.set("perCount", $('#per_count').val());

        $.post(
            '/manage/app/tool/diagnostic/submit.json',
            {
                type: 6,
                appId: appId,
                params: mapToJson(paramMap)
            },
            function (data) {
                var status = data.status;
                if (status == 'success') {
                    alert("检测任务提交成功，任务id：" + data.taskId);
                    location.href = "/manage/app/tool/index?tabTag=scanClean";
                } else {
                    toastr.error("检测任务提交失败,请查看系统日志确认相关原因!");
                }
            }
        );
    }

    function mapToJson(map) {
        var str = '{';
        var i = 1;
        for (var [key, value] of map.entries()) {
            if (i == map.size) {
                str += '"' + key + '":"'+ value + '"';
            }else{
                str += '"' + key + '":"'+ value + '",';
            }
            i++;
        }
        str += '}';
        return str;
    }

</script>

<div class="row">
    <div class="col-md-12">
        <h4 class="page-header">
            数据分析清理任务
        </h4>
    </div>
</div>
<div class="row">
    <div id="scan-div" class="col-md-12">
        <form class="form-inline" role="form" name="ec">
            <div class="row">
                <div class="form-group col-md-3">
                    <label for="scan-select">应用&nbsp;&nbsp;</label>
                    <select id="scan-select" name="appId" class="selectpicker show-tick form-control"
                            data-live-search="true" title="选择应用" data-width="35%"
                            onchange="changeAppIdSelect(this.value,'scan_instance-select')">
                        <option value="">选择应用</option>
                        <c:forEach items="${appDescMap}" var="appDescEntry">
                            <c:set value="${appDescEntry.value}" var="appDesc"></c:set>
                            <option value="${appDesc.appId}" title="${appDesc.appId} ${appDesc.name}">
                                【${appDesc.appId}】&nbsp;名称：${appDesc.name}&nbsp;类型：${appDesc.typeDesc}&nbsp;版本：${appDesc.versionName}
                            </option>
                        </c:forEach>
                    </select>
                </div>
                <div class="form-group col-md-4 col-md-offset-1">
                    <label for="scan_instance-select">实例&nbsp;&nbsp;</label>
                    <select id="scan_instance-select" name="nodes"
                            class="selectpicker show-tick form-control" multiple
                            data-live-search="true" title="选择实例" data-width="35%">
                    </select>
                </div>
            </div>

            <div id="patternDiv" class="margin-top-10">
                <div class="row margin-top-12">
                    <div class="form-group col-md-4">
                        <label for="scan-pattern">键匹配&nbsp;&nbsp;</label>
                        <input id="scan-pattern" style="width: 80%" type="text" class="form-control" name="scan-pattern"
                               placeholder="支持通配符*，数字[0-9]匹配">
                    </div>
                    <div class="col-md-8">
                        <label><font style="font-weight: lighter; color: yellowgreen">如需筛选key中某部分大于或小于指定值，在匹配串中添加<font style="font-weight: bold">@Less{指定值}Less@</font>，如为大于，将Less替换为More</font></label>
                    </div>
                </div>

                <div class="row margin-top-10">
                    <div class="form-group col-md-4">
                        <label for="per_count">扫描数量&nbsp;&nbsp;</label>
                        <input id="per_count" name="per_count" style="width: 80%" type="text" class="form-control" placeholder="每次获取数量">
                    </div>

                    <div class="form-group col-md-3">
                        <label for="scan_clean">操作类型&nbsp;&nbsp;</label>
                        <select id="scan_clean" name="scan_clean" class="selectpicker show-tick form-control" title="操作类型"
                                data-width="15%" onchange="changeTtlResetShow()">
                            <option value="0">
                                仅扫描分析
                            </option>
                            <option value="1">
                                分析清理
                            </option>
                            <option value="2">
                                分析重置ttl
                            </option>
                        </select>
                    </div>
                </div>
            </div>

            <div id = "resetTtl" class="row" style="display: none">
                <div class="form-group col-md-8">
                    <label for="ttl-reset-more">重置ttl时间 ttl > &nbsp;&nbsp;</label>
                    <input id="ttl-reset-more" style="width: 25%" type="text" class="form-control" name="ttl-reset-more"
                           placeholder="最小过期时间">
                    <label for="ttl-reset-less"> and ttl <&nbsp;</label>
                    <input id="ttl-reset-less" style="width: 25%" type="text" class="form-control" name="ttl-reset-less"
                           placeholder="最大过期时间">
                </div>
            </div>

            <div id="filterDiv" class="margin-top-10">
                <label><font color="orange">过滤匹配：提供键包含、键关键字段值、ttl过滤</font></label>
                <div class="row">
                    <div class="form-group col-md-8">
                        <label for="ttl-value-more"> 剩余时间 ttl > &nbsp;&nbsp;</label>
                        <input id="ttl-value-more" style="width: 25%" type="text" class="form-control" name="ttl-value-more"
                               placeholder="最小剩余时间">
                        <label for="ttl-value-less"> and ttl < &nbsp;&nbsp;</label>
                        <input id="ttl-value-less" style="width: 25%" type="text" class="form-control" name="ttl-value-less"
                               placeholder="最大剩余时间">
                    </div>
                </div>

            </div>

            <div class="row">
                <div class="form-group col-md-1" style="float:right; width: max-content">
                    <button type="button" class="form-control btn green" onclick="submitDiagnostic()">执行任务</button>
                </div>

            </div>

        </form>
    </div>
</div>


<div class="row">
    <div class="col-md-12">
        <h4 class="page-header">
            分析清理任务列表
        </h4>
    </div>
</div>
<div class="row">
    <div class="col-md-12">
        <div style="float:left">
            <form class="form-inline" role="form" method="post" action="/manage/app/tool/index?tabTag=scanClean"
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
                <button type="submit" class="btn btn-success">查询</button>
            </form>
        </div>
    </div>
</div>
<br/>
<div class="row">
    <div class="col-md-12">
        <div class="portlet box light-grey" id="clientIndex">

            <table class="table table-striped table-bordered table-hover" id="tableDataList">
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
                    <th width="20%" style="word-break: break-word">诊断条件</th>
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
                            分析清理
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
                        <td width="20%" style="word-wrap:break-word;word-break:break-all;">
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
                                        data-target="#modal-diagnosticResult" data-toggle="modal"
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
<div id="modal-diagnosticResult" class="modal fade" tabindex="-1">
    <div class="modal-dialog" style="width: max-content">
        <div class="modal-content">

            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true"></button>
                <h4 class="modal-title">
                    诊断结果
                    <small><label id="modal-title" style="color: #00BE67"></label></small>
                </h4>
            </div>

            <form class="form-horizontal form-bordered form-row-stripped">
                <div class="modal-body" style="height:500px; overflow:scroll;">
                    <div class="row">
                        <!-- 控件开始 -->
                        <div class="col-md-12">
                            <table class="table table-bordered table-striped table-hover">
                                <thead id="diagnosticResultCount"></thead>
                                <tbody id="diagnosticResultTable"></tbody>
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



