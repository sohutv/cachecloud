<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp"%>
<script type="text/javascript" src="/resources/bootstrap/jquery/jquery-1.11.0.js"></script>
<script type="text/javascript" src="/resources/select/bootstrap-select.js"></script>
<link rel="stylesheet" type="text/css" href="/resources/select/bootstrap-select.css"/>
<script type="text/javascript" src="/resources/js/selectpicker.js?<%=System.currentTimeMillis()%>"></script>
<script type="text/javascript" src="/resources/js/getInstancesByAppId.js"></script>
<script type="text/javascript">

    function appKeyAnalysis(){
        var appId = $('#keyAnalysis_appId').selectpicker('val');
        if (appId == null || appId == '') {
            alert("请选择应用");
            $('#keyAnalysis_appId').focus();
            return false;
        }

        var reason = document.getElementById("appAnalysisReason");
        if(reason.value == ""){
            alert("请填写键值分析原因!");
            reason.focus();
            return false;
        }
        var nodeInfos = $('#instance-select').selectpicker('val');

        if(!confirm("确认提交键值分析申请？")){
            return ;
        }

        var btn = document.getElementById("appKeyAnalysisBtn");
        btn.disabled = true;

        $.post(
            '/admin/app/keyAnalysis',
            {
                appId: appId,
                nodeInfo: nodeInfos == null ? "" : nodeInfos.toString(),
                appAnalysisReason: reason.value
            },
            function(data){
                if (data == 1) {
                    $("#appKeyAnalysisInfo").html("<div class='alert alert-error' ><button class='close' data-dismiss='alert'>×</button><font color='green'><strong>Success!</strong>键值分析申请提交成功，即将跳转工单列表！</font></div>");
                    setTimeout("location.href = '/admin/app/jobs'", 1000);
                } else {
                    btn.disabled = false;
                    $("#appKeyAnalysisInfo").html("<div class='alert alert-error' ><button class='close' data-dismiss='alert'>×</button><font color='red'><strong>Error!</strong>更新失败！<font></div>");
                }
            }
        );
    }
</script>

<div class="col-md-9">
    <div class="row">
        <div class="col-md-12">
            <h3 class="page-header">
                集群键值分析
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
                        <select id="keyAnalysis_appId" name="appId" class="selectpicker show-tick form-control"
                                data-live-search="true" title="选择应用" data-width="30%"
                                onchange="changeAppIdSelect(this.value,'instance-select')">
                            <option value="">选择应用</option>
                            <c:forEach items="${appDescMap}" var="appDescEntry">
                                <c:set value="${appDescEntry.value}" var="appDesc"></c:set>
                                <c:if test="${appDesc.status==2}">
                                <option value="${appDesc.appId}" title="${appDesc.appId} ${appDesc.name}" <c:if test="${appId == appDesc.appId}">selected</c:if>>
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
                                data-live-search="true" title="选择实例 (默认所有主节点)" data-width="30%" data-size="8">
                            <c:if test="${appInstanceList!=null}">
                                <c:forEach items="${appInstanceList}" var="instanceInfo">
                                    <option value="${instanceInfo.hostPort}" <c:if test="${instanceId == instanceInfo.id}">selected</c:if>>
                                            ${instanceInfo.hostPort}（角色：${instanceInfo.roleDesc})
                                    </option>
                                </c:forEach>
                            </c:if>
                        </select>
                    </div>
                </div>
                <div class="form-group row">
                    <label class="control-label col-md-2 col-md-offset-1">申请原因<font color='red'>(*)</font></label>
                    <div class="col-md-5">
                        <textarea rows="5"  name="appAnalysisReason" id="appAnalysisReason" placeholder="申请原因" class="form-control"></textarea>
                    </div>
                </div>

                <div class="form-group row">
                    <div class="col-md-offset-3 col-md-2">
                        <button id="appKeyAnalysisBtn" class="btn btn-info" onclick="appKeyAnalysis()">
                            <i class="fa fa-check"></i>
                            提交申请
                        </button>
                    </div>
                </div>


                <div class="form-group row">
                    <div id="appKeyAnalysisInfo" class="col-md-offset-3 col-md-9"></div>
                </div>
            </div>

            <!-- form-body 结束 -->
        </div>
        <!-- 控件结束 -->
    </div>
</div>