<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp" %>

<script src="/resources/manage/plugins/jquery-1.10.2.min.js"></script>
<script type="text/javascript" src="/resources/select/bootstrap-select.js"></script>
<link rel="stylesheet" type="text/css" href="/resources/select/bootstrap-select.css">
<!-- 3.0 -->
<link href="/resources/manage/plugins/bootstrap/css/bootstrap.min.css" rel="stylesheet">
<script src="/resources/manage/plugins/bootstrap/js/bootstrap.min.js"></script>
<!-- 提示工具-->
<link href="/resources/css/common.css" rel="stylesheet" type="text/css"/>
<link href="/resources/toastr/toastr.min.css" rel="stylesheet" type="text/css">
<script type="text/javascript" src="/resources/toastr/toastr.min.js"></script>

<script type="text/javascript">
    $(window).on('load', function () {
        $('.selectpicker').selectpicker({
            'selectedText': 'cat'
        });
    });
</script>


<div class="page-container">
    <div class="page-content">
        <div class="modal-dialog" style="width:1000px;">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true"></button>
                    <h4 class="modal-title">
                        应用导入流程
                        <small><label id="modal-title" style="color: #00BE67"></label></small>
                    </h4>
                </div>

                <form class="form-horizontal form-bordered form-row-stripped">
                    <div class="modal-body">
                        <div class="row bs-wizard" style="border-bottom:0;">
                            <div id="appInfo" class="col-xs-2 col-md-offset-1 bs-wizard-step">
                                <div class="text-center bs-wizard-stepnum">1.确认导入配置</div>
                                <div class="progress">
                                    <div class="progress-bar"></div>
                                </div>
                                <a href="#" class="bs-wizard-dot"></a>
                                <div class="bs-wizard-info text-center">导入前准备</div>
                            </div>

                            <div id="createVersion" class="col-xs-2 bs-wizard-step disabled">
                                <div class="text-center bs-wizard-stepnum">2.创建Redis版本</div>
                                <div class="progress">
                                    <div class="progress-bar"></div>
                                </div>
                                <a href="#" class="bs-wizard-dot"></a>
                                <div class="bs-wizard-info text-center">为应用创建版本</div>
                            </div>

                            <div id="build" class="col-xs-2 bs-wizard-step disabled">
                                <div class="text-center bs-wizard-stepnum">3.新建应用</div>
                                <div class="progress">
                                    <div class="progress-bar"></div>
                                </div>
                                <a href="#" class="bs-wizard-dot"></a>
                                <div class="bs-wizard-info text-center">部署应用</div>
                            </div>

                            <div id="appMigrate" class="col-xs-2 bs-wizard-step disabled">
                                <div class="text-center bs-wizard-stepnum">4.数据迁移</div>
                                <div class="progress">
                                    <div class="progress-bar"></div>
                                </div>
                                <a href="#" class="bs-wizard-dot"></a>
                                <div class="bs-wizard-info text-center">新老实例redis数据迁移</div>
                            </div>

                            <div id="importDone" class="col-xs-2 bs-wizard-step disabled">
                                <div class="text-center bs-wizard-stepnum">5.应用导入完成</div>
                                <div class="progress">
                                    <div class="progress-bar"></div>
                                </div>
                                <a href="#" class="bs-wizard-dot"></a>
                                <div class="bs-wizard-info text-center">导入完成</div>
                            </div>
                        </div>

                        <br/>
                        <div class="row hidden" id="pre">
                            <form class="form-horizontal form-bordered form-row-stripped">
                                <div class="form-body">
                                    <div class="form-group">
                                        <div class="page-header row col-md-offset-1">
                                            <h4>源：Redis信息</h4>
                                        </div>
                                        <div class="form-group">
                                            <label class="control-label col-md-2">
                                                存储类型:
                                            </label>
                                            <div class="col-md-4">
                                                <select id="sourceType" name="sourceType" class="form-control select2_category" disabled>
                                                    <option value="5" <c:if test="${appImport.sourceType==5}">selected="selected"</c:if>>
                                                        Redis-standalone
                                                    </option>
                                                    <option value="6" <c:if test="${appImport.sourceType==6}">selected="selected"</c:if>>
                                                        Redis-sentinel
                                                    </option>
                                                    <option value="7" <c:if test="${appImport.sourceType==7}">selected="selected"</c:if> >
                                                        Redis-cluster
                                                    </option>
                                                </select>
                                            </div>
                                        </div>
                                        <div class="form-group" id="instanceSourceDiv">
                                            <label class="control-label col-md-2">源实例信息: </label>
                                            <div class="col-md-8">
                                            <textarea id="instanceSourceInfo" type="text" rows="10" class="form-control"
                                                      readonly>${appImport.instanceInfo}</textarea>
                                            </div>
                                            <label id="instanceSourceLog" class="control-label"></label>
                                        </div>

                                        <div class="form-group">
                                            <label class="control-label col-md-2"> 密码: </label>
                                            <div class="col-md-4">
                                                <label id="password" name="password" class="form-control"
                                                       readonly>${appImport.redisPassword}</label>
                                            </div>
                                        </div>

                                        <div class="page-header row col-md-offset-1">
                                            <h4>目标：应用信息</h4>
                                        </div>
                                        <label class="control-label col-md-2"> 应用名称: </label>
                                        <div class="col-md-3">
                                            <label id="appName" name="appName" class="form-control"
                                                   readonly>${appDesc.name}</label>
                                        </div>
                                        <label class="control-label col-md-2"> Redis类型: </label>
                                        <div class="col-md-3">
                                            <label id="type" name="type" class="form-control" readonly>
                                                <c:if test="${appDesc.type==2}">Cluster</c:if>
                                                <c:if test="${appDesc.type==5}">Sentinel</c:if>
                                                <c:if test="${appDesc.type==6}">Standalone</c:if>
                                            </label>
                                        </div>
                                    </div>

                                    <div class="form-group">
                                        <label class="control-label col-md-2"> 应用描述: </label>
                                        <div class="col-md-3">
                                            <label id="appIntro" name="appIntro" class="form-control"
                                                   readonly>${appDesc.intro}</label>
                                        </div>
                                        <label class="control-label col-md-2"> Redis版本: </label>
                                        <div class="col-md-3">
                                            <label id="version" name="type" class="form-control"
                                                   readonly>${appImport.redisVersionName}</label>
                                        </div>
                                    </div>

                                    <div class="form-group">
                                        <label class="control-label col-md-2"> 应用总内存: </label>
                                        <div class="col-md-3">
                                            <label id="appMem" name="appMem" class="form-control"
                                                   readonly>${appImport.memSize} G</label>
                                        </div>

                                        <label class="control-label col-md-2"> 是否测试: </label>
                                        <div class="col-md-3">
                                            <label id="isTest" name="isTest" class="form-control" readonly>
                                                <c:if test="${appDesc.isTest==0}">正式</c:if>
                                                <c:if test="${appDesc.isTest==1}">测试</c:if>
                                            </label>
                                        </div>
                                    </div>


                                </div>
                            </form>
                        </div>

                        <div class="row hidden" id="redisVersion" style="text-align: center">
                            <form class="form-horizontal form-bordered form-row-stripped">
                                <form class="form-horizontal form-bordered form-row-stripped">
                                    <div class="form-group">
                                        <label> redis版本状态: </label>
                                        <c:if test="${hasRedisVersion==0}">
                                            <label> ${appImport.redisVersionName} 不存在
                                                <a target="_blank"
                                                   href="/manage/app/resource/index?tab=redis">【创建版本】</a>
                                            </label>
                                        </c:if>
                                    </div>
                                </form>
                            </form>
                        </div>

                        <div class="row display" id="appBuild" style="text-align: center">
                            <form class="form-horizontal form-bordered form-row-stripped">
                                <div class="form-group">
                                    <label class="control-label col-md-2 col-md-offset-3"> 应用创建状态: </label>
                                    <c:if test="${appImport.status==21}">
                                        <label class="control-label col-md-7" style="text-align: left">
                                            请部署应用：${appImport.appId}
                                            <a target="_blank"
                                               href="/manage/app/initAppDeploy?appId=${appImport.appId}&importId=${appImport.id}">【部署应用】</a>
                                        </label>
                                    </c:if>
                                    <c:if test="${appImport.status==22}">
                                        <label class="control-label col-md-7"
                                               style="text-align: left"> ${appImport.appId} 应用部署中，请稍等...
                                            <a target="_blank"
                                               href="/manage/task/flow?taskId=${appImport.appBuildTaskId}">【查看部署任务】</a>
                                        </label>
                                    </c:if>
                                    <c:if test="${appImport.status==23}">
                                        <label class="control-label col-md-7"
                                               style="text-align: left;color: red"> ${appImport.appId}应用部署异常，请
                                            <a target="_blank"
                                               href="/manage/task/flow?taskId=${appImport.appBuildTaskId}">【修复】</a>
                                            或
                                            <a target="_blank"
                                               href="/manage/app/initAppDeploy?appId=${appImport.appId}&importId=${appImport.id}"
                                               onclick="return preRebuildApp(${appImport.id},${appImport.appId})">【重新部署】</a>
                                        </label>
                                    </c:if>
                                </div>
                            </form>
                        </div>

                        <div class="row hidden" id="migrate" style="text-align: center">
                            <form class="form-horizontal form-bordered form-row-stripped">
                                <div class="form-group">
                                    <label class="control-label col-md-2 col-md-offset-3"> 数据迁移状态: </label>
                                    <c:if test="${appImport.status==30}">
                                        <label class="control-label col-md-7" style="text-align: left"> 暂无数据迁移任务，请
                                            <a target="_blank"
                                               href="/data/migrate/init?importId=${appImport.id}">【进行数据迁移】</a>
                                        </label>
                                    </c:if>
                                    <c:if test="${appImport.status==32}">
                                        <label class="control-label col-md-7" style="text-align: left"> 数据迁移中，请稍等...
                                            <a target="_blank"
                                               href="/data/migrate/index?migrateId=${appImport.migrateId}&status=-2">【查看迁移任务】</a>
                                        </label>
                                    </c:if>
                                    <c:if test="${appImport.status==33}">
                                        <label class="control-label col-md-7" style="text-align: left;color: red">
                                            数据迁移异常，请
                                            <a target="_blank"
                                               href="/data/migrate/index?migrateId=${appImport.migrateId}&status=-2">【修复】</a>
                                            或
                                            <a target="_blank"
                                               href="/data/migrate/init?importId=${appImport.id}"
                                               onclick="if(window.confirm('确认 应用${appImport.appId} 数据已经清空 或 允许重写?')){return true;}else{return false;}">
                                                【重新迁移】
                                            </a>
                                        </label>
                                    </c:if>

                                </div>
                            </form>
                        </div>

                        <div class="row hidden" id="done" style="text-align: center">
                            <form class="form-horizontal form-bordered form-row-stripped">
                                <div class="form-group">
                                    <label style="color: #00BE67; font: bold;">
                                        恭喜您，应用导入成功！
                                        <a target="_blank" href="/admin/app/index?appId=${appImport.appId}">【查看应用】</a>
                                    </label>
                                </div>
                            </form>
                        </div>

                    </div>

                    <div class="modal-footer">
                        <button id="import" type="button" class="btn btn-primary"
                                onclick="importApp(${appImport.id},11,0,0)">
                            开始导入
                        </button>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>
<input type="hidden" id="status" name="status" value="${appImport.status}">


<script>
    var navs = ["appInfo", "createVersion", "build", "appMigrate", "importDone"];
    var divs = ["pre", "redisVersion", "appBuild", "migrate", "done"];
    $(function () {
        var status = document.getElementById("status").value;
        console.log("status: " + status);
        if (status == 0) {
            $("#import").html("开始导入");
            active("appInfo");
            show("pre");
        } else if (status >= 10 && status < 20) {
            hidden("import");
            complete("appInfo");
            active("createVersion");
            show("redisVersion");
        } else if (status >= 20 && status < 30) {
            hidden("import");
            complete("appInfo");
            complete("createVersion");
            active("build");
            show("appBuild");
        } else if (status >= 30 && status < 40) {
            hidden("import");
            complete("appInfo");
            complete("createVersion");
            complete("build");
            active("appMigrate");
            show("migrate");
        } else if (status == 3) {
            $("#import").html("导入完成");
            display("import");
            disable("import");
            complete("appInfo");
            complete("createVersion");
            complete("build");
            complete("appMigrate");
            active("importDone");
            show("done");
        }
    });

    function importApp(importId, status, appBuildTaskId, migrateId) {
        $.get(
            '/import/app/goOn.json',
            {
                importId: importId,
                status: status,
                appBuildTaskId: appBuildTaskId,
                migrateId: migrateId
            },
            function (data) {
                var success = data.success;
                if (success == 1) {
                    console.log("success: " + success);
                    window.location.reload();
                }
            }
        );
    }

    function updateMigrate(importId) {
        var migrateId = document.getElementById("migrateId");
        if (migrateId.value == '') {
            migrateId.focus();
            return false;
        } else if (migrateId.value > 0) {
            importApp(importId, 0, 0, migrateId.value);
        }
    }

    function updateAppBuild(importId) {
        var appBuildTaskId = document.getElementById("appBuildTaskId");
        if (appBuildTaskId.value == '') {
            appBuildTaskId.focus();
            return false;
        } else if (appBuildTaskId.value > 0) {
            importApp(importId, 0, appBuildTaskId.value, 0);
        }
    }

    function preRebuildApp(importId, appId) {
        $.get(
            '/import/app/preRebuildApp.json',
            {
                importId: importId,
                appId: appId
            },
            function (data) {
                var success = data.success;
                if (success == 1) {
                    console.log("preRebuildApp success");
                    return true;
                } else {
                    alert("应用部署回退异常！");
                    return false;
                }
            }
        );
    }

    function show(id) {
        display(id)
        for (var i = 0; i < divs.length; i++) {
            if (divs[i] != id) {
                hidden(divs[i]);
            }
        }
    }

    function warn(id) {
        $("#" + id).addClass("warn");
    }

    function disable(id) {
        $("#" + id).removeClass("active").addClass("disabled");
    }

    function active(id) {
        $("#" + id).removeClass("disabled").removeClass("warn").addClass("active");
    }

    function complete(id) {
        $("#" + id).removeClass("disabled").removeClass("active").removeClass("warn").addClass("complete");
    }

    function hidden(id) {
        $("#" + id).removeClass("display").addClass("hidden");
    }

    function display(id) {
        $("#" + id).removeClass("hidden").addClass("display");
    }
</script>