<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp" %>
<script type="text/javascript" src="/resources/bootstrap/jquery/jquery-1.11.0.js"></script>
<script type="text/javascript" src="/resources/select/bootstrap-select.js"></script>
<link rel="stylesheet" type="text/css" href="/resources/select/bootstrap-select.css"/>
<script type="text/javascript" src="/resources/js/selectpicker.js?<%=System.currentTimeMillis()%>"></script>
<script type="text/javascript" src="/resources/js/getInstancesByAppId.js"></script>
<script type="text/javascript">
    function appDiagnosticApply() {
        var appId = $('#appDiagnostic_appId').selectpicker('val');
        if (appId == null || appId == '') {
            alert("请选择应用");
            $('#appDiagnostic_appId').focus();
            return false;
        }

        var type = document.getElementById("diagnostic_type");
        if (type.value == "") {
            alert("请选择诊断类型!");
            type.focus();
            return false;
        }

        var nodeInfos = $('#instance-select').selectpicker('val');

        var reason = document.getElementById("appDiagnosticReason");
        if (reason.value == "") {
            alert("请填写原因!");
            reason.focus();
            return false;
        }
        if(!confirm("确认提交应用诊断申请？")){
            return ;
        }

        var btn = document.getElementById("appDiagnosticBtn");
        btn.disabled = true;

        $.post(
            '/admin/app/job/submit',
            {
                jobType: 8,
                appId: appId,
                param: type.value,
                nodeInfos: nodeInfos == null ? "" : nodeInfos.toString(),
                reason: reason.value
            },
            function (data) {
                if (data == 1) {
                    $("#appDiagnosticInfo").html("<div class='alert alert-error' ><button class='close' data-dismiss='alert'>×</button><font color='green'><strong>Success!</strong>应用诊断申请提交成功，即将跳转工单列表！</font></div>");
                    setTimeout("location.href = '/admin/app/jobs'", 1000);
                } else {
                    btn.disabled = false;
                    $("#appDiagnosticInfo").html("<div class='alert alert-error' ><button class='close' data-dismiss='alert'>×</button><font color='red'><strong>Error!</strong>更新失败！<font></div>");
                }
            }
        );
    }

</script>

<div class="col-md-9">
    <div class="row">
        <div class="col-md-12">
            <h3 class="page-header">
                诊断应用
            </h3>
        </div>
    </div>

    <div class="row">
        <!-- 控件开始 -->
        <div class="col-md-12">
            <!-- form-body开始 -->
            <div class="form-body">

                <div class="form-group row">
                    <label class="control-label col-md-2 col-md-offset-1">应用<font color='red'>(*)</font></label>
                    <div class="col-md-5">
                        <select id="appDiagnostic_appId" name="appId" class="selectpicker show-tick form-control"
                                data-live-search="true" title="选择应用" data-width="30%"
                                onchange="changeAppIdSelect(this.value,'instance-select')">
                            <option value="">选择应用</option>
                            <c:forEach items="${appDescMap}" var="appDescEntry">
                                <c:set value="${appDescEntry.value}" var="appDesc"></c:set>
                                <c:if test="${appDesc.status==2}">
                                <option value="${appDesc.appId}" title="${appDesc.appId} ${appDesc.name}">
                                    【${appDesc.appId}】&nbsp;名称：${appDesc.name}&nbsp;类型：${appDesc.typeDesc}&nbsp;版本：${appDesc.versionName}
                                </option>
                                </c:if>
                            </c:forEach>
                        </select>
                    </div>
                </div>
                <div class="form-group row">
                    <label class="control-label col-md-2 col-md-offset-1">实例</label>
                    <div class="col-md-5">
                        <select id="instance-select" name="nodes"
                                class="selectpicker show-tick form-control" multiple
                                data-live-search="true" title="选择实例 (默认全部)" data-width="30%" data-size="8">
                        </select>
                    </div>
                </div>
                <div class="form-group row">
                    <label class="control-label col-md-2 col-md-offset-1">诊断类型&nbsp;&nbsp;</label>
                    <div class="col-md-5">
                        <select id="diagnostic_type" name="type" class="form-control">
                            <c:forEach items="${diagnosticTypeMap}" var="entry">
                                <option value="${entry.value}">
                                        ${entry.value}
                                </option>
                            </c:forEach>
                        </select>
                    </div>
                </div>



                <div class="form-group row">
                    <label class="control-label col-md-2 col-md-offset-1">申请原因/描述<font color='red'>(*)</font></label>
                    <div class="col-md-5">
                        <textarea rows="6" name="appDiagnosticReason" id="appDiagnosticReason"
                                  placeholder="scan：扫描匹配的键，如abc*&#13;&#10;memoryUsed：扫描大内存键，如内存大于1MB&#13;&#10;idlekey：扫描空闲时间长的键，如7天&#13;&#10;hotkey：扫描热点键&#13;&#10;deletekey：删除匹配的键，如abc*&#13;&#10;slotAnalysis：分析集群槽的键分布"
                                  class="form-control"></textarea>
                    </div>
                </div>

                <div class="form-group row">
                    <div class="col-md-offset-3 col-md-2">
                        <button id="appDiagnosticBtn" class="btn btn-info" onclick="appDiagnosticApply()">
                            <i class="fa fa-check"></i>
                            提交申请
                        </button>
                    </div>
                </div>

                <div class="form-group row">
                    <div id="appDiagnosticInfo" class="col-md-offset-3 col-md-9"></div>
                </div>
            </div>
            <!-- form-body 结束 -->
        </div>

        <!-- 控件结束 -->
    </div>
</div>
