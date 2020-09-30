<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp" %>
<script type="text/javascript" src="/resources/bootstrap/jquery/jquery-1.11.0.js"></script>
<script type="text/javascript" src="/resources/select/bootstrap-select.js"></script>
<link rel="stylesheet" type="text/css" href="/resources/select/bootstrap-select.css"/>
<script type="text/javascript" src="/resources/js/selectpicker.js?<%=System.currentTimeMillis()%>"></script>
<script type="text/javascript">
    function changeAppIdSelect(appId, instance_select) {
        console.log('instance_select:' + instance_select);
        console.log(appId);

        document.getElementById(instance_select).options.length = 0;
        document.getElementById('appConfigKey').options.length = 0;
        $('#appConfigKey').selectpicker('refresh');

        $.post('/manage/app/tool/diagnostic/appInstances',
            {
                appId: appId,
            },
            function (data) {
                var status = data.status;
                if (status == 1) {
                    var appInstanceList = data.appInstanceList;
                    $('#' + instance_select).append("<option style='display: none'></option>");
                    for (var i = 0; i < appInstanceList.length; i++) {
                        var val = appInstanceList[i].id;
                        var term = appInstanceList[i].hostPort + '（角色：' + appInstanceList[i].roleDesc + '）';
                        $('#' + instance_select).append("<option value='" + val + "'>" + term + "</option>");
                    }
                    $('#' + instance_select).append("<option value=''>" + "所有实例" + "</option>");
                    $('#' + instance_select).selectpicker('refresh');
                    $('#' + instance_select).selectpicker('render');
                } else {
                    console.log('data.status:' + status);
                }
            }
        );
    }
    function changeInstanceSelect(instanceId, appConfigKey) {
        console.log(instanceId);

        var appId = $('#appConfig_appId').selectpicker('val');
        if (appId == null || appId == '') {
            alert("请选择应用");
            $('#appConfig_appId').focus();
            return false;
        }

        document.getElementById(appConfigKey).options.length = 0;

        $.post('/admin/app/redisConfig',
            {
                appId: appId,
                instanceId:  instanceId
            },
            function (data) {
                var status = data.status;
                if (status == 1) {
                    var redisConfigMap = data.redisConfigMap;
                    for(var key in redisConfigMap){
                        var item= '配置项：'+key+"  配置值："+redisConfigMap[key];
                        $('#' + appConfigKey).append("<option value='" + key + "'>" + item + "</option>");
                    }
                    $('#' + appConfigKey).selectpicker('refresh');
                    $('#' + appConfigKey).selectpicker('render');
                } else {
                    console.log('data.status:' + status);
                }
            }
        );
    }
    function appConfigChange() {

        var appId = $('#appConfig_appId').selectpicker('val');
        if (appId == null || appId == '') {
            alert("请选择应用");
            $('#appConfig_appId').focus();
            return false;
        }
        var instanceId = $('#instance-select').selectpicker('val');
        console.log("instanceId:" + instanceId);
        if(instanceId == null){
            alert("请选择实例");
            $('#instance-select').focus();
            return false;
        }

        var appConfigKey = document.getElementById("appConfigKey");
        if (appConfigKey.value == "") {
            alert("配置项不能为空");
            appConfigKey.focus();
            return false;
        }

        var appConfigValue = document.getElementById("appConfigValue");
        if (appConfigValue.value == "") {
            alert("配置值不能为空");
            appConfigValue.focus();
            return false;
        }

        var appConfigReason = document.getElementById("appConfigReason");
        if (appConfigReason.value == "") {
            alert("配置原因不能为空");
            appConfigReason.focus();
            return false;
        }

        if(!confirm("确认提交应用配置修改？")){
            return ;
        }

        var appConfigChangeBtn = document.getElementById("appConfigChangeBtn");
        appConfigChangeBtn.disabled = true;

        var url;
        var data;
        if (instanceId == null || instanceId == '') {
            url = '/admin/app/changeAppConfig';
            data = {
                appId: appId,
                instanceId: instanceId,
                appConfigKey: appConfigKey.value,
                appConfigValue: appConfigValue.value,
                appConfigReason: appConfigReason.value
            };
        } else {
            url = '/admin/app/changeInstanceConfig';
            data = {
                appId: appId,
                instanceId: instanceId,
                instanceConfigKey: appConfigKey.value,
                instanceConfigValue: appConfigValue.value,
                instanceConfigReason: appConfigReason.value
            };
        }

        $.post(
            url,
            data,
            function (data) {
                if (data == 1) {
                    $("#appConfigChangeInfo").html("<div class='alert alert-error' ><button class='close' data-dismiss='alert'>×</button><font color='green'><strong>Success!</strong>配置修改申请提交成功，即将跳转工单列表！</font></div>");
                    setTimeout("location.href = '/admin/app/jobs'", 1000);
                } else {
                    appConfigChangeBtn.disabled = false;
                    $("#appConfigChangeInfo").html("<div class='alert alert-error' ><button class='close' data-dismiss='alert'>×</button><font color='red'><strong>Error!</strong>更新失败！<font></div>");
                }
            }
        );
    }
</script>

<div class="col-md-9">
    <div class="row">
        <div class="col-md-12">
            <h3 class="page-header">
                修改应用配置
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
                        <select id="appConfig_appId" name="appId" class="selectpicker show-tick form-control"
                                data-live-search="true" title="选择应用" data-width="30%"
                                onchange="changeAppIdSelect(this.value,'instance-select')">
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
                    <label class="control-label col-md-2 col-md-offset-1">实例<font color='red'>(*)</font></label>
                    <div class="col-md-5">
                        <select id="instance-select" name="nodes"
                                class="selectpicker show-tick form-control"
                                data-live-search="true" title="请选择实例 " data-width="30%" data-size="8"
                                onchange="changeInstanceSelect(this.value,'appConfigKey')">
                            <option style='display: none'></option>
                            <c:if test="${appInstanceList!=null}">
                                <c:forEach items="${appInstanceList}" var="instanceInfo">
                                    <option value="${instanceInfo.id}" <c:if test="${instanceId == instanceInfo.id}">selected</c:if>>
                                            ${instanceInfo.hostPort}（角色：${instanceInfo.roleDesc})
                                    </option>
                                </c:forEach>
                            </c:if>
                            <option value="">所有实例</option>
                        </select>
                    </div>
                </div>

                <div class="form-group row">
                    <label class="control-label col-md-2 col-md-offset-1">配置项<font color='red'>(*)</font></label>
                    <div class="col-md-5">
                        <select id="appConfigKey" name="appConfigKey"
                                class="selectpicker show-tick form-control"
                                data-live-search="true" title="选择配置项" data-width="30%" data-size="8">
                            <c:forEach items="${redisConfigMap}" var="entry">
                                <option value="${appDesc.appId}">
                                配置项：${entry.key} 配置值：+${entry.value};
                                </option>
                            </c:forEach>
                        </select>
                    </div>
                </div>

                <div class="form-group row">
                    <label class="control-label col-md-2 col-md-offset-1">新配置值<font color='red'>(*)</font></label>
                    <div class="col-md-5">
                        <input type="text" name="appConfigValue" id="appConfigValue"
                               placeholder="填写新的配置值" class="form-control">
                    </div>
                </div>

                <div class="form-group row">
                    <label class="control-label col-md-2 col-md-offset-1">修改原因<font color='red'>(*)</font></label>
                    <div class="col-md-5">
                        <textarea name="appConfigReason" id="appConfigReason" placeholder="如：1.需要更多的连接数。"
                                  class="form-control"></textarea>
                    </div>
                </div>

                <div class="form-group row">
                    <div class="col-md-offset-3 col-md-2">
                        <button type="button" id="appConfigChangeBtn" class="btn btn-danger" onclick="appConfigChange()">
                            <i class="fa fa-check"></i>
                            提交申请
                        </button>
                    </div>
                </div>

                <div class="form-group row">
                    <div id="appConfigInfo" class="col-md-offset-3 col-md-9"></div>
                </div>
            </div>
            <!-- form-body 结束 -->
        </div>
        <div id="appConfigChangeInfo"></div>
        <!-- 控件结束 -->
    </div>
</div>
