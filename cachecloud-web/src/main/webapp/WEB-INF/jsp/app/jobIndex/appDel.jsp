<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp" %>
<script type="text/javascript" src="/resources/bootstrap/jquery/jquery-1.11.0.js"></script>
<script type="text/javascript" src="/resources/select/bootstrap-select.js"></script>
<link rel="stylesheet" type="text/css" href="/resources/select/bootstrap-select.css"/>
<script type="text/javascript" src="/resources/js/selectpicker.js?<%=System.currentTimeMillis()%>"></script>
<script type="text/javascript" src="/resources/js/getInstancesByAppId.js"></script>
<script type="text/javascript">
    $(function () {
        $("input[name='cleanType']").change(function () {
            if ($(this).val() == "0") {
                $("#instance-div").hide();
                $("#pattern-div").hide();
            } else {
                $("#instance-div").show();
                $("#pattern-div").show();
            }
        });
    });

    function appDataCleanApply() {
        var appId = $('#appDel_appId').selectpicker('val');
        if (appId == null || appId == '') {
            alert("请选择应用");
            $('#appDel_appId').focus();
            return false;
        }
        var cleanType = $('input[name="cleanType"]:checked').val();

        var nodeInfos = $('#instance-select').selectpicker('val');
        var pattern = document.getElementById("delPattern");
        if (cleanType==1){
            if (pattern.value == "") {
                alert("删除数据需要填写键匹配的格式!");
                pattern.focus();
                return false;
            }
        }
        var reason = document.getElementById("appDelReason");
        if (reason.value == "") {
            alert("请填写数据清理原因!");
            reason.focus();
            return false;
        }

        if (!confirm("确认提交数据清理申请？")) {
            return;
        }

        var btn = document.getElementById("appDelBtn");
        btn.disabled = true;

        var cleanTypeDesc = cleanType == 0 ? "清理全库" : "删除数据";
        $.post(
            '/admin/app/job/submit',
            {
                jobType: 7,
                appId: appId,
                nodeInfos: nodeInfos == null ? "" : nodeInfos.toString(),
                param: '数据清理类型: ' + cleanTypeDesc + '，key格式: ' + pattern.value,
                reason: reason.value
            },
            function (data) {
                if (data == 1) {
                    $("#appDelApplyInfo").html("<div class='alert alert-error' ><button class='close' data-dismiss='alert'>×</button><font color='green'><strong>Success!</strong>应用诊断申请提交成功，即将跳转工单列表！</font></div>");
                    setTimeout("location.href = '/admin/app/jobs'", 1000);
                } else {
                    btn.disabled = false;
                    $("#appDelApplyInfo").html("<div class='alert alert-error' ><button class='close' data-dismiss='alert'>×</button><font color='red'><strong>Error!</strong>更新失败！<font></div>");
                }
            }
        );
    }
</script>

<div class="col-md-9">
    <div class="row">
        <div class="col-md-12">
            <h3 class="page-header">
                数据清理
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
                        <select id="appDel_appId" name="appId" class="selectpicker show-tick form-control"
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
                    <label class="control-label col-md-2 col-md-offset-1">数据清理类型<font color='red'>(*)</font></label>
                    <div class="col-md-5">
                        <label class="radio-inline">
                            <input type="radio" name="cleanType" value="0" checked> 清理全库
                        </label>
                        <label class="radio-inline">
                            <input type="radio" name="cleanType" value="1"> 删除数据
                        </label>
                    </div>
                </div>

                <div class="form-group row" id="instance-div" style="display:none;">
                    <label class="control-label col-md-2 col-md-offset-1">实例</label>
                    <div class="col-md-5">
                        <select id="instance-select" name="nodes"
                                class="selectpicker show-tick form-control" multiple
                                data-live-search="true" title="选择实例 (默认全部)" data-width="30%" data-size="8">
                        </select>
                    </div>
                </div>

                <div class="form-group row" id="pattern-div" style="display:none;">
                    <label class="control-label col-md-2 col-md-offset-1">key格式<font color='red'>(*)</font></label>
                    <div class="col-md-5">
                        <textarea rows="5" class="form-control" name="delPattern" id="delPattern" placeholder="键匹配格式，*表示通配符，多个用逗号分隔，如：
abc*,
*abc,
ab*c"></textarea>
                        <span class="help-block" style="color: green">
                            如，清理以abc开头的键：abc*,
                            清理包含abc的键：*abc*,
                            清理以abc结尾的键：*abc
                        </span>
                    </div>
                </div>

                <div class="form-group row">
                    <label class="control-label col-md-2 col-md-offset-1">申请原因<font color='red'>(*)</font></label>
                    <div class="col-md-5">
                        <textarea rows="3" name="appDelReason" id="appDelReason" placeholder="申请数据清理原因"
                                  class="form-control"></textarea>
                    </div>
                </div>

                <div class="form-group row">
                    <div class="col-md-offset-3 col-md-2">
                        <button id="appDelBtn" class="btn btn-danger" onclick="appDataCleanApply()">
                            <i class="fa fa-check"></i>
                            提交申请
                        </button>
                    </div>
                </div>

                <div class="form-group row">
                    <div id="appDelApplyInfo" class="col-md-offset-3 col-md-9"></div>
                </div>
            </div>
            <!-- form-body 结束 -->
        </div>
        <!-- 控件结束 -->
    </div>
</div>