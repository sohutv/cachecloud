<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp"%>
<script type="text/javascript" src="/resources/bootstrap/jquery/jquery-1.11.0.js"></script>
<script type="text/javascript" src="/resources/select/bootstrap-select.js"></script>
<link rel="stylesheet" type="text/css" href="/resources/select/bootstrap-select.css"/>
<script type="text/javascript" src="/resources/js/selectpicker.js?<%=System.currentTimeMillis()%>"></script>
<script type="text/javascript">
    function appScaleApply(){
        var appId = $('#appScale_appId').selectpicker('val');
        if (appId == null || appId == '') {
            alert("请选择应用");
            $('#appScale_appId').focus();
            return false;
        }

        var applyMemSize = document.getElementById("applyMemSize");
        if(applyMemSize.value == ""){
            alert("请填写要扩容的容量!");
            applyMemSize.focus();
            return false;
        }

        var appScaleReason = document.getElementById("appScaleReason");
        if(appScaleReason.value == ""){
            alert("请填写申请扩容的原因!");
            appScaleReason.focus();
            return false;
        }
        var appScaleApplyBtn = document.getElementById("appScaleApplyBtn");
        appScaleApplyBtn.disabled = true;

        $.post(
            '/admin/app/scale',
            {
                appId: appId,
                applyMemSize: applyMemSize.value,
                appScaleReason: appScaleReason.value
            },
            function(data){
                if (data == 1) {
                    $("#appScaleApplyInfo").html("<div class='alert alert-error' ><button class='close' data-dismiss='alert'>×</button><font color='green'><strong>Success!</strong>容量变更申请提交成功，即将跳转工单列表！</font></div>");
                    setTimeout("location.href = '/admin/app/jobs'", 1000);
                } else {
                    btn.disabled = false;
                    $("#appScaleApplyInfo").html("<div class='alert alert-error' ><button class='close' data-dismiss='alert'>×</button><font color='red'><strong>Error!</strong>更新失败！<font></div>");
                }
            }
        );
    }
</script>

<div class="col-md-9">
    <div class="row">
        <div class="col-md-12">
            <h3 class="page-header">
                扩容/缩容
            </h3>
        </div>
    </div>

    <div class="row">
        <!-- 控件开始 -->
        <div class="col-md-12">
            <!-- form-body开始 -->
            <div class="form-body">

                <div class="form-group row">
                    <label class="control-label col-md-2 col-md-offset-1">应用&<font color='red'>(*)</font></label>
                    <div class="col-md-5">
                        <select id="appScale_appId" name="appId" class="selectpicker show-tick form-control"
                                data-live-search="true" title="选择应用" data-width="30%">
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
                <div class="form-group row">
                    <label class="control-label col-md-2 col-md-offset-1">申请容量<font color='red'>(*)</font></label>
                    <div class="col-md-5">
                        <input type="text" name="applyMemSize" id="applyMemSize" value="2G" class="form-control"/>
                        <span class="help-block">如: 512M,1G,2G..20G</span>
                    </div>
                </div>
                <div class="form-group row">
                    <label class="control-label col-md-2 col-md-offset-1">申请原因<font color='red'>(*)</font></label>
                    <div class="col-md-5">
                        <textarea rows="5" name="appScaleReason" id="appScaleReason" placeholder="申请扩容原因" class="form-control"></textarea>
                    </div>
                </div>

                <div class="form-group row">
                    <div class="col-md-offset-3 col-md-2">
                        <button id="appScaleApplyBtn" class="btn btn-info" onclick="appScaleApply()">
                            <i class="fa fa-check"></i>
                            提交申请
                        </button>
                    </div>
                </div>

                <div class="form-group row">
                    <div id="appScaleApplyInfo" class="col-md-offset-3 col-md-9"></div>
                </div>

            </div>
            <!-- form-body 结束 -->
        </div>

        <!-- 控件结束 -->
    </div>
</div>