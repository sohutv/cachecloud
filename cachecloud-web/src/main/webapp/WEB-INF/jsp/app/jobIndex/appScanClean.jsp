<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp" %>
<script type="text/javascript" src="/resources/bootstrap/jquery/jquery-1.11.0.js"></script>
<script type="text/javascript" src="/resources/select/bootstrap-select.js"></script>
<link rel="stylesheet" type="text/css" href="/resources/select/bootstrap-select.css"/>
<script type="text/javascript" src="/resources/js/selectpicker.js?<%=System.currentTimeMillis()%>"></script>
<script type="text/javascript" src="/resources/js/getInstancesByAppId.js"></script>
<script type="text/javascript">
    function appDataScanCleanApply() {
        var appId = $('#appDel_appId').selectpicker('val');
        if (appId == null || appId == '') {
            alert("请选择应用");
            $('#appDel_appId').focus();
            return false;
        }
        var isClean = $('input[name="isClean"]:checked').val();

        var nodeInfos = $('#instance-select').selectpicker('val');
        var condition = document.getElementById("condition");
        if (condition.value == "") {
            alert("需要填写分析清理条件!");
            condition.focus();
            return false;
        }
        var reason = document.getElementById("appDelReason");
        if (reason.value == "") {
            alert("请填写分析清理原因!");
            reason.focus();
            return false;
        }

        if (!confirm("确认提交数据分析清理申请？")) {
            return;
        }

        var btn = document.getElementById("appDelBtn");
        btn.disabled = true;

        var cleanTypeDesc = isClean == 0 ? "分析数据" : (isClean == 1 ? "删除数据" : "重置ttl");
        $.post(
            '/admin/app/job/submit',
            {
                jobType: 13,
                appId: appId,
                nodeInfos: nodeInfos == null ? "" : nodeInfos.toString(),
                param: '分析清理类型: ' + cleanTypeDesc + '，条件: ' + condition.value,
                reason: reason.value
            },
            function (data) {
                if (data == 1) {
                    $("#appDelApplyInfo").html("<div class='alert alert-error' ><button class='close' data-dismiss='alert'>×</button><font color='green'><strong>Success!</strong>数据分析清理申请提交成功，即将跳转工单列表！</font></div>");
                    setTimeout("location.href = '/admin/app/jobs'", 1000);
                } else {
                    btn.disabled = false;
                    $("#appDelApplyInfo").html("<div class='alert alert-error' ><button class='close' data-dismiss='alert'>×</button><font color='red'><strong>Error!</strong>数据分析清理申请失败！<font></div>");
                }
            }
        );
    }
</script>

<div class="col-md-9">
    <div class="row">
        <div class="col-md-12">
            <h3 class="page-header">
                数据分析清理
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
                    <label class="control-label col-md-2 col-md-offset-1">分析清理类型<font color='red'>(*)</font></label>
                    <div class="col-md-5">
                        <div class="row">
                            <label class="radio-inline">
                                <input type="radio" name="isClean" value="0" checked> 分析
                            </label>
                            <label class="radio-inline">
                                <input type="radio" name="isClean" value="1"> 清理
                            </label>
                            <label class="radio-inline">
                                <input type="radio" name="isClean" value="2"> 重置ttl
                            </label>
                        </div>
                        <div class="row">
                            <span class="help-block" style="color: red">
                                如生产集群申请该操作，请发送邮件，给相关同事和领导
                            </span>
                        </div>
                    </div>
                </div>

                <div class="form-group row" id="instance-div">
                    <label class="control-label col-md-2 col-md-offset-1">实例</label>
                    <div class="col-md-5">
                        <select id="instance-select" name="nodes"
                                class="selectpicker show-tick form-control" multiple
                                data-live-search="true" title="选择实例 (默认全部)" data-width="30%" data-size="8">
                        </select>
                    </div>
                </div>

                <div class="form-group row" id="pattern-div">
                    <label class="control-label col-md-2 col-md-offset-1">分析清理条件<font color='red'>(*)</font></label>
                    <div class="col-md-5">
                        <textarea rows="8" class="form-control" name="condition" id="condition" placeholder="键匹配，对key进行过滤。
（必填）匹配模式：abc_*
（可选）如需筛选key中某部分大于或小于指定值，在匹配串中添加 @Less{指定值}Less@ ，如为大于，将Less替换为More
（可选）重置ttl时间大于（1000）小于（3000）
（可选）筛选ttl剩余时间小于（3600）/大于（7000）"></textarea>
                        <span class="help-block" style="color: green">
                            如，abc开头、包含、结尾的键：abc*、*abc*、*abc,<br>
                            如，abc_{数字开头}的键：abc_[0-9]*<br>
                            如，abc_{数字开头和结尾}的键：abc_[0-9]*[0-9]<br>
                            如，abc_{数字}大于2000的键：abc_@More{2000}More@<br>
                            如，重置键ttl时间：重置ttl时间大于（1000）小于（3000）<br>
                            如，根据ttl剩余时间过滤键，ttl小于（3600）/大于（7000）。
                        </span>
                    </div>
                </div>

                <div class="form-group row">
                    <label class="control-label col-md-2 col-md-offset-1">申请原因<font color='red'>(*)</font></label>
                    <div class="col-md-5">
                        <textarea rows="3" name="appDelReason" id="appDelReason" placeholder="申请数据分析清理原因"
                                  class="form-control"></textarea>
                    </div>
                </div>

                <div class="form-group row">
                    <div class="col-md-offset-3 col-md-2">
                        <button id="appDelBtn" class="btn btn-danger" onclick="appDataScanCleanApply()">
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