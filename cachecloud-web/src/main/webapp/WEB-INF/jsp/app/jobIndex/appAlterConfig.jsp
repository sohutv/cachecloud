<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp"%>
<script type="text/javascript" src="/resources/bootstrap/jquery/jquery-1.11.0.js"></script>
<script type="text/javascript" src="/resources/select/bootstrap-select.js"></script>
<link rel="stylesheet" type="text/css" href="/resources/select/bootstrap-select.css"/>
<script type="text/javascript" src="/resources/js/selectpicker.js?<%=System.currentTimeMillis()%>"></script>
<script type="text/javascript">
    function appAlertConfigChange() {
        var appId = $('#appAlterConfig_appId').selectpicker('val');
        if (appId == null || appId == '') {
            alert("请选择应用");
            $('#appAlterConfig_appId').focus();
            return false;
        }

        var memAlertValue = document.getElementById("memAlertValue");
        if (memAlertValue.value == "") {
            alert("内存报警阀值不能为空");
            memAlertValue.focus();
            return false;
        }
        var clientConnAlertValue = document.getElementById("clientConnAlertValue");
        if (clientConnAlertValue.value == "") {
            alert("客户端连接数报警阀值不能为空");
            clientConnAlertValue.focus();
            return false;
        }
        var hitPrecentAlertValue = document.getElementById("hitPrecentAlertValue");
        if (hitPrecentAlertValue.value == "") {
            alert("应用平均命中率报警阀值不能为空");
            hitPrecentAlertValue.focus();
            return false;
        }
        var isAccessMonitor = jQuery("#isAccessMonitor option:selected");
        if (isAccessMonitor.attr("value") == "") {
            alert("应用全局报警不能为空");
            isAccessMonitor.focus();
            return false;
        }
        if(!confirm("确认提交报警修改申请？")){
            return ;
        }
        var btn = document.getElementById("appAlterConfigChangeBtn");
        btn.disabled = true;
        $.post(
            '/admin/app/changeAppAlertConfig',
            {
                appId: appId,
                memAlertValue: memAlertValue.value,
                clientConnAlertValue: clientConnAlertValue.value,
                hitPrecentAlertValue: hitPrecentAlertValue.value,
                isAccessMonitor: isAccessMonitor.attr("value")
            },
            function (data) {
                if (data == 1) {
                    $("#appAlterConfigChangeInfo").html("<div class='alert alert-error' ><button class='close' data-dismiss='alert'>×</button><font color='green'><strong>Success!</strong>配置修改成功，已生效！</font></div>");
                    setTimeout("location.href = '/admin/app/jobs'", 1500);
                } else {
                    btn.disabled = false;
                    $("#appAlterConfigChangeInfo").html("<div class='alert alert-error' ><button class='close' data-dismiss='alert'>×</button><font color='red'><strong>Error!</strong>更新失败！<font></div>");
                }
            }
        );
    }
    function changeAppId(appId) {
        $('#memAlertValue').val('');
        $('#clientConnAlertValue').val('');
        $('#hitPrecentAlertValue').val('');
        $.post('/admin/app/appDesc',
            {
                appId: appId,
            },
            function (data) {
                var status = data.status;
                if (status == 1) {
                    var appDesc = data.appDesc;
                    $('#memAlertValue').val(appDesc.memAlertValue);
                    $('#clientConnAlertValue').val(appDesc.clientConnAlertValue);
                    $('#hitPrecentAlertValue').val(appDesc.hitPrecentAlertValue);
                } else {
                    console.log('data.status:' + status);
                }
            }
        );
    }
</script>

<div class="col-md-9">
    <div class="row">
        <div class="col-md-12">
            <h3 class="page-header">
                修改报警
            </h3>
        </div>
    </div>

    <div class="row">
        <!-- 控件开始 -->
        <div class="col-md-12">
            <!-- form-body开始 -->
            <div class="form-body">

                <div class="form-group">
                    <label class="control-label col-md-3">应用<font color='red'>(*)</font></label>
                    <div class="col-md-7">
                        <select id="appAlterConfig_appId" name="appId" class="selectpicker show-tick form-control"
                                data-live-search="true" title="选择应用" data-width="30%"
                                onchange="changeAppId(this.value)">
                            <option value="">选择应用</option>
                            <c:forEach items="${appDescMap}" var="appDescEntry">
                                <c:set value="${appDescEntry.value}" var="appDesc"></c:set>
                                <c:if test="${appDesc.status==2}">
                                    <option value="${appDesc.appId}" title="${appDesc.appId} ${appDesc.name}" <c:if test="${appId == appDesc.appId}">selected="selected"</c:if>>
                                        【${appDesc.appId}】&nbsp;名称：${appDesc.name}&nbsp;类型：${appDesc.typeDesc}&nbsp;版本：${appDesc.versionName}
                                    </option>
                                </c:if>
                            </c:forEach>
                        </select>
                    </div>
                </div>

                <br><br><br>
                <div class="form-group">
                    <label class="control-label col-md-3">内存报警阀值<font color='red'>(*)</font></label>
                    <div class="col-md-7">
                        <input type="text" name="memAlertValue"
                               value="${appDescMap[appId].memAlertValue}" id="memAlertValue" placeholder="内存报警阀值"
                               class="form-control" onchange="testisNum(this.id)">
                        <span class="help-block">如果内存使用率超过90%报警，请填90<font color="red">（不需要报警请填100以上)</font></span>
                    </div>
                </div>

                <div class="form-group">
                    <label class="control-label col-md-3">客户端连接数报警阀值<font color='red'>(*)</font></label>
                    <div class="col-md-7">
                        <input type="text" name="clientConnAlertValue"
                               value="${appDescMap[appId].clientConnAlertValue}"
                               id="clientConnAlertValue" placeholder="客户端连接数报警阀值" class="form-control"
                               onchange="testisNum(this.id)">
                        <span class="help-block">如果客户端连接数超过2000报警，请填2000</span>
                    </div>
                </div>

                <div class="form-group">
                    <label class="control-label col-md-3">应用平均命中率报警阀值<font color='red'>(*)</font></label>
                    <div class="col-md-7">
                        <input type="text" name="hitPrecentAlertValue"
                               value="${appDescMap[appId].hitPrecentAlertValue}"
                               id="hitPrecentAlertValue" placeholder="平均命中率报警阀值" class="form-control"
                               onchange="testisNum(this.id)">
                        <span class="help-block">如果应用平均命中率低于80%报警，请填80</span>
                    </div>
                </div>

                <div class="form-group">
                    <label class="control-label col-md-3">开启应用全局报警<font color='red'>(*)</font></label>
                    <div class="col-md-7">
                        <select id="isAccessMonitor" name="isAccessMonitor" class="form-control">
                            <option value="0"
                                    <c:if test="${appDetail.appDesc.isAccessMonitor == null || appDetail.appDesc.isAccessMonitor == 0}">selected</c:if>>
                                否
                            </option>
                            <option value="1"
                                    <c:if test="${appDetail.appDesc.isAccessMonitor == 1}">selected</c:if>>
                                是
                            </option>
                        </select>
                        <span class="help-block">是否接收全局报警邮件</span>
                    </div>
                </div>

                <div class="form-group">
                    <div class="col-md-offset-3 col-md-3">
                        </button>
                        <button type="button" id="appAlterConfigChangeBtn" class="btn btn-danger"
                                onclick="appAlertConfigChange()">
                            <i class="fa fa-check"></i>
                            提交申请
                        </button>
                    </div>
                </div>

                <div class="form-group">
                    <div id="appAlterConfigChangeInfo" class="col-md-offset-3 col-md-9"></div>
                </div>
            </div>
            <!-- form-body 结束 -->
        </div>

        <!-- 控件结束 -->
    </div>
</div>