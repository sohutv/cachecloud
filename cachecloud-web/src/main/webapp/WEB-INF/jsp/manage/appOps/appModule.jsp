<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp" %>

<script>

    function loadModule(appId) {
        $.post(
            '/manage/app/loadModule',
            {
                appId: appId,
                moduleName: $('#module option:selected').attr("key")
            },
            function (data) {
                if (data.status == 1) {
                    $("#instanceAddModuleInfo").html("<div class='alert alert-error' ><button class='close' data-dismiss='alert'>×</button><label style=\"color:green\">装载完成!</label></div>");
                    var targetId = "#loadModuleModal";
                    setTimeout("$('" + targetId + "').modal('hide');window.location.reload();", 2000);
                } else if(data.status == -1){
                    var message = data.message;
                    $("#instanceAddModuleInfo").html("<div class='alert alert-error' ><button class='close' data-dismiss='alert'>×</button><label style=\"color:sandybrown\">部分装载失败! "+message+" </label></div>");
                    var targetId = "#loadModuleModal";
                    setTimeout("$('" + targetId + "').modal('hide');window.location.reload();", 5000);
                }else {
                    $("#instanceAddModuleInfo").html("<div class='alert alert-error' ><button class='close' data-dismiss='alert'>×</button><label style=\"color:red\">装载失败，请查找原因！</label></div>");
                }
            }
        );
    }

    function unloadModule(appId) {
        $.post(
            '/manage/app/unloadModule',
            {
                appId: appId,
                moduleName: $('#module2 option:selected').attr("value")
            },
            function (data) {
                if (data.status == 1) {
                    $("#instanceUnModuleInfo").html("<div class='alert alert-error' ><button class='close' data-dismiss='alert'>×</button><label style=\"color:green\">卸载完成!</label></div>");
                    var targetId = "#unloadModuleModal";
                    setTimeout("$('" + targetId + "').modal('hide');window.location.reload();", 2000);
                } else {
                    $("#instanceUnModuleInfo").html("<div class='alert alert-error' ><button class='close' data-dismiss='alert'>×</button><label style=\"color:red\">卸载失败，请查找原因！</label></div>");
                }
            }
        );
    }



</script>


<div class="container">
    <div class="row">
        <div class="col-md-12 page-header">
            <h4>应用模块管理-${appDesc.name}(${appDesc.typeDesc})</h4>
        </div>
        <div class="col-md-12">
            <table class="table table-striped table-bordered table-hover">
                <thead>
                <tr>
                <th>ip</th>
                <th>操作系统</th>
                <th>redis模块安装(${basePath})</th>
                </tr>
                </thead>
                    <h4 style="color:lightslategray">1.机器集成模块情况:
                        <%--<button type="button" class="btn btn-small btn-info" data-target="#loadModuleModal"--%>
                        <%--data-toggle="modal">添加模块</button>--%>
                    </h4>
                    <label class="label label-success">     </label>：已安装模块
                    <label class="label label-danger">      </label>：未安装模块
                    <tbody>
                    <c:forEach items="${appMachineList}" var="machine">
                        <tr class="odd gradeX">
                        <td>
                        <a target="_blank" href="/manage/machine/machineInstances?ip=${machine.ip}">${machine.ip}</a>
                        </td>
                        <td>${machine.versionInfo}</td>
                        <td>
                        <c:forEach items="${machine.moduleInfo}" var="module">
                            <c:if test="${module.value==true}">
                                <label class="label label-success">${module.key}</label>
                            </c:if>
                            <c:if test="${module.value==false}">
                                <label class="label label-danger">${module.key}</label>
                            </c:if>
                        </c:forEach>
                        </td>
                        </tr>
                    </c:forEach>
                    </tbody>
            </table>
        </div>

        <div class="col-md-12">
            <table class="table table-striped table-bordered table-hover">
                <thead>
                <tr>
                    <th>ID</th>
                    <th>实例信息</th>
                    <th>角色</th>
                    <th>已装载模块</th>
                    <th>日志</th>
                </tr>
                </thead>
                <h4 style="color:lightslategray">2.实例装载模块情况:
                    <button type="button" class="btn btn-small btn-info" data-target="#loadModuleModal"
                    data-toggle="modal">安装模块</button>
                    <button type="button" class="btn btn-small btn-warning" data-target="#unloadModuleModal"
                    data-toggle="modal">卸载模块</button>
                </h4>
                <label class="label label-success">bf</label>：布隆过滤器模块
                <label class="label label-success">search</label>：redis-search模块
                <tbody>
                    <c:forEach items="${instanceList}" var="instance">
                        <c:if test="${instance.status!=2}">
                            <tr class="odd gradeX">
                                <td>
                                    <a href="/admin/instance/index?instanceId=${instance.id}" target="_blank">${instance.id}</a>
                                </td>
                                <td>
                                    <a target="_blank" href="/manage/machine/machineInstances?ip=${instance.ip}">${instance.ip}:${instance.port}</a>
                                </td>
                                <td>${instance.roleDesc}</td>
                                <td>
                                    <c:forEach items="${instance.modules}" var="module">
                                        <label class="label label-success">${module.name}</label>
                                    </c:forEach>
                                </td>
                                 <td>
                                    <a target="_blank" href="/manage/instance/log?instanceId=${instance.id}">查看</a>
                                </td>
                            </tr>
                        </c:if>
                    </c:forEach>
                    </tbody>
            </table>
        </div>
    </div>
</div>

<div id="loadModuleModal" class="modal fade" tabindex="-1" data-width="400">
    <div class="modal-dialog">
        <div class="modal-content">

            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true"></button>
                <h4 class="modal-title">装载redis模块</h4>
            </div>

            <form class="form-horizontal form-bordered form-row-stripped">
                <div class="modal-body">
                    <div class="row">
                        <!-- 控件开始 -->
                        <div class="col-md-12">
                            <!-- form-body开始 -->
                            <div class="form-body">
                                <div class="form-group">
                                <label class="control-label col-md-3">选择安装模块:</label>
                                <div class="col-md-6">
                                    <select id="module" name="type" class="form-control select2_category">
                                        <c:forEach items="${moduleMap}" var="module">
                                            <c:if test="${module.key.contains(\".so\")}">
                                                <option value="${module.value}" key="${module.key}">${module.key}</option>
                                            </c:if>
                                        </c:forEach>
                                    </select>
                                </div>
                            </div>
                            <!-- form-body 结束 -->
                            <div id="instanceAddModuleInfo"></div>
                        </div>
                    </div>
                </div>
                </div>

                <div class="modal-footer">
                    <button type="button" data-dismiss="modal" class="btn">Close</button>
                    <button type="button" id="loadModuleBtn" class="btn red"
                            onclick="loadModule('${appDesc.appId}')">Ok
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>

    <div id="unloadModuleModal" class="modal fade" tabindex="-1" data-width="400">
    <div class="modal-dialog">
        <div class="modal-content">

            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true"></button>
                <h4 class="modal-title">卸载redis模块</h4>
            </div>

            <form class="form-horizontal form-bordered form-row-stripped">
                <div class="modal-body">
                    <div class="row">
                        <!-- 控件开始 -->
                        <div class="col-md-12">
                            <!-- form-body开始 -->
                            <div class="form-body">
                                <div class="form-group">
                                <label class="control-label col-md-3">选择卸载模块:</label>
                                <div class="col-md-6">
                                    <select id="module2" name="type" class="form-control select2_category">
                                        <c:forEach items="${moduleMap}" var="module">
                                            <option value="${module.value}" key="${module.key}">${module.key}</option>
                                        </c:forEach>
                                    </select>
                                </div>
                            </div>
                            <!-- form-body 结束 -->
                            <div id="instanceUnModuleInfo"></div>
                        </div>
                    </div>
                </div>
                </div>

                <div class="modal-footer">
                    <button type="button" data-dismiss="modal" class="btn">Close</button>
                    <button type="button" id="unloadModuleBtn" class="btn red"
                            onclick="loadModule('${appDesc.appId}')">Ok
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>






