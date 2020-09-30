<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <title>迁移数据记录列表</title>
    <jsp:include page="/WEB-INF/include/head.jsp"/>
    <script type="text/javascript">
        function stopMigrate(id) {
            if (window.confirm("确认要停掉id=" + id + "的迁移任务吗?")) {
                $.get(
                    '/data/migrate/stop.json',
                    {
                        id: id,
                    },
                    function (data) {
                        var status = data.status;
                        alert(data.message);
                        location.href = "/data/migrate/list";
                    }
                );
            }
        }
    </script>
</head>
<body role="document">
<div class="container">
    <jsp:include page="/WEB-INF/include/headMenu.jsp"/>
    <div id="systemAlert">
    </div>

    <div class="row">
        <div class="col-md-12">
            <div class="page-header">
                <h3>
                    迁移数据记录列表
                </h3>
                <h4>
                    <a target="_blank" href="/data/migrate/init" class="btn btn-info btn-success"
                       role="button">+ 添加新迁移</a>
                </h4>
            </div>

            <div style="float:left">
                <form class="form-inline" role="form" method="post" action="/data/migrate/list" id="appList" name="ec">
                    <div class="form-group">
                        <input type="text" class="form-control col-md-1" id="sourceAppId" name="sourceAppId"
                               value="${appDataMigrateSearch.sourceAppId}" placeholder="源appId">
                    </div>
                    <div class="form-group">
                        <input type="text" class="form-control" id="targetAppId" name="targetAppId"
                               value="${appDataMigrateSearch.targetAppId}" placeholder="目标appId">
                    </div>
                    <div class="form-group">
                        <input type="text" class="form-control" id="migrateMachine" name="migrateMachine"
                               value="${appDataMigrateSearch.migrateMachine}" placeholder="迁移机器">
                    </div>
                    <div class="form-group">
                        <select name="userId" class="form-control">
                            <option value="" <c:if test="${appDataMigrateSearch.userId == ''}">selected</c:if>>
                                操作人
                            </option>
                            <c:forEach items="${adminList}" var="admin">
                                <option value="${admin.id}" <c:if test="${appDataMigrateSearch.userId == admin.id}">selected</c:if>>
                                【${admin.id}】${admin.name}&nbsp;${admin.chName}
                                </option>
                            </c:forEach>
                        </select>
                    </div>

                    <div class="form-group">
                        <select name="status" class="form-control">
                            <option value="3" <c:if test="${appDataMigrateSearch.status == 3}">selected</c:if>>
                                增量同步
                            </option>
                            <option value="-2" <c:if test="${appDataMigrateSearch.status == -2}">selected</c:if>>
                                全部状态
                            </option>
                            <option value="0" <c:if test="${appDataMigrateSearch.status == 0}">selected</c:if>>
                                全量同步
                            </option>
                            <option value="1" <c:if test="${appDataMigrateSearch.status == 1}">selected</c:if>>
                                同步结束
                            </option>
                            <option value="2" <c:if test="${appDataMigrateSearch.status == 2}">selected</c:if>>
                                同步异常
                            </option>
                        </select>
                    </div>

                    <input type="hidden" name="pageNo" id="pageNo">
                    <button type="submit" class="btn btn-default">查询</button>
                </form>
            </div>

        </div>
    </div>
    <div class="row">
        <br/>
        <div class="col-md-12">
            <table class="table table-striped table-hover" style="margin-top: 0px">
                <thead>
                <tr align="center">
                    <td>序号</td>
                    <td>迁移ID</td>
                    <td>迁移工具</td>
                    <td>迁移机器</td>
                    <td>操作人</td>
                    <td>源数据</td>
                    <td>目标数据</td>
                    <td>开始时间</td>
                    <td>结束时间</td>
                    <td>状态</td>
                    <td>查看</td>
                    <td>操作</td>
                    <td>校验数据</td>
                </tr>
                </thead>
                <tbody>
                <c:forEach items="${appDataMigrateStatusList}" var="appDataMigrateStatus" varStatus="stat">
                    <tr>
                        <td>${stat.index + 1}
                            <input type="hidden" id="id" value="${appDataMigrateStatus.id}"/>
                        </td>
                        <td>${appDataMigrateStatus.migrateId}</td>
                        <td>
                            <c:choose>
                                <c:when test="${appDataMigrateStatus.migrateTool == 0}">
                                    redis-shake
                                </c:when>
                                <c:otherwise>
                                    redis-migrate-tool
                                </c:otherwise>
                            </c:choose>
                            <input type="hidden" id="migrateTool" value="${appDataMigrateStatus.migrateTool}"/>
                        </td>
                        <td>
                            <c:choose>
                                <c:when test="${appDataMigrateStatus.migrateTool == 1}">
                                    ${appDataMigrateStatus.migrateMachineIp}:${appDataMigrateStatus.migrateMachinePort}
                                </c:when>
                                <c:otherwise>
                                    ${appDataMigrateStatus.migrateMachineIp}
                                </c:otherwise>
                            </c:choose>
                        </td>
                        <td>${appDataMigrateStatus.userName}</td>
                        <td>
                            数据源：
                            <c:choose>
                                <c:when test="${appDataMigrateStatus.sourceAppId <= 0}">
                                非cachecloud
                                <br/>
                                ${appDataMigrateStatus.sourceServers}
                                </c:when>
                                <c:otherwise>
                                    cachecloud:<a target="_blank" href="/admin/app/index?appId=${appDataMigrateStatus.sourceAppId}">${appDataMigrateStatus.sourceAppId}</a>
                                </c:otherwise>
                            </c:choose>
                            <br/>
                            源类型：redis-${appDataMigrateStatus.sourceMigrateTypeDesc}
                            <br/>
                            redis版本：${appDataMigrateStatus.redisSourceVersion}
                        </td>
                        <td>
                            数据源：
                            <c:choose>
                                <c:when test="${appDataMigrateStatus.targetAppId <= 0}">
                                    非cachecloud
                                    <br/>
                                    ${appDataMigrateStatus.targetServers}
                                </c:when>
                                <c:otherwise>
                                    cachecloud:<a target="_blank" href="/admin/app/index?appId=${appDataMigrateStatus.targetAppId}">${appDataMigrateStatus.targetAppId}</a>
                                </c:otherwise>
                            </c:choose>
                            <br/>
                            目标类型：redis-${appDataMigrateStatus.targetMigrateTypeDesc}
                            <br/>
                            redis版本：${appDataMigrateStatus.redisTargetVersion}
                        </td>
                        <td>${appDataMigrateStatus.startTimeFormat}</td>
                        <td>${appDataMigrateStatus.endTimeFormat}</td>
                        <td>${appDataMigrateStatus.statusDesc}</td>
                        <td>
                            <a target="_blank" href="/data/migrate/log?id=${appDataMigrateStatus.id}">日志</a><br/><br/>
                            <a target="_blank"
                               href="/data/migrate/config?id=${appDataMigrateStatus.id}">配置</a><br/><br/>
                            <c:choose>
                                <c:when test="${appDataMigrateStatus.status == 0}">
                                    <c:if test="${appDataMigrateStatus.migrateTool == 1}">
                                        <a target="_blank"
                                           href="/data/migrate/process?id=${appDataMigrateStatus.id}&migrateTool=${appDataMigrateStatus.migrateTool}">进度</a>
                                    </c:if>
                                    <c:if test="${appDataMigrateStatus.migrateTool == 0}">
                                        <a data-toggle="modal" data-target="#processModal">进度</a>
                                    </c:if>
                                </c:when>
                                <c:otherwise></c:otherwise>
                            </c:choose>
                            <div class="modal fade" id="processModal" tabindex="-1" role="dialog"
                                 aria-labelledby="myModalLabel" aria-hidden="true">
                                <div class="modal-dialog">
                                    <div class="modal-content">
                                        <div class="modal-header">
                                            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">
                                                &times;
                                            </button>
                                            <h4 class="modal-title" id="myModalLabel">
                                                迁移进度
                                            </h4>
                                        </div>
                                        <div class="modal-body" id="processModal_body">

                                        </div>
                                        <div class="modal-footer">
                                            <button type="button" class="btn btn-default" data-dismiss="modal">关闭
                                            </button>
                                        </div>
                                    </div><!-- /.modal-content -->
                                </div><!-- /.modal -->
                            </div>
                        </td>
                        <td>
                            <button
                                    <c:if test='${appDataMigrateStatus.status == 1}'>disabled="disabled"</c:if>
                                    <c:if test='${appDataMigrateStatus.status == 2}'>disabled="disabled"</c:if>
                                    onclick="stopMigrate(${appDataMigrateStatus.id})" type="button"
                                    class="btn btn-info">停止
                            </button>
                        </td>
                        <td>
                            <c:choose>
                                <c:when test="${appDataMigrateStatus.migrateTool == 1}">
                                    <a target="_blank" href="/data/migrate/checkData?id=${appDataMigrateStatus.id}"
                                       class="btn btn-info" role="button">采样校验</a>
                                </c:when>
                                <c:otherwise>
                                    <a target="_blank" href="/data/migrate/checkData?id=${appDataMigrateStatus.id}"
                                       class="btn btn-info" role="button">数据校验</a>
                                    <br/><br/>
                                    <a target="_blank" href="/data/migrate/checkData/log?id=${appDataMigrateStatus.id}">&nbsp;校验日志</a>
                                </c:otherwise>
                            </c:choose>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
        <div style="margin-bottom: 10px;float: right;margin-right: 15px">
				<span>
					<ul id='ccPagenitor' style="margin-bottom: 0px;margin-top: 0px"></ul>
					<div id="pageDetail"
                         style="float:right;padding-top:7px;padding-left:8px;color:#4A64A4;display: none">共${page.totalPages}页,${page.totalCount}条</div>
				</span>
        </div>
    </div>
</div>
<br/><br/><br/><br/><br/><br/><br/>
<jsp:include page="/WEB-INF/include/foot.jsp"/>

<script type="text/javascript" src="/resources/js/mem-cloud.js"></script>
<script src="/resources/bootstrap/paginator/bootstrap-paginator.js"></script>
<script src="/resources/bootstrap/paginator/custom-pagenitor.js"></script>
<script type="text/javascript">
    $(function () {
        //分页点击函数
        var pageClickedFunc = function (e, originalEvent, type, page) {
            //form传参用pageSize
            document.getElementById("pageNo").value = page;
            document.getElementById("appList").submit();
        };
        //分页组件
        var element = $('#ccPagenitor');
        //当前page号码
        var pageNo = '${page.pageNo}';
        //总页数
        var totalPages = '${page.totalPages}';
        //显示总页数
        var numberOfPages = '${page.numberOfPages}';
        var options = generatePagenitorOption(pageNo, numberOfPages, totalPages, pageClickedFunc);
        if (totalPages > 0) {
            element.bootstrapPaginator(options);
            document.getElementById("pageDetail").style.display = "";
        } else {
            element.html("未查询到相关记录！");
        }
    });

    $(function () {
        $('#processModal').on('shown.bs.modal',
            function () {
                $.get(
                    '/data/migrate/process.json',
                    {
                        id: document.getElementById("id").value,
                        migrateTool: document.getElementById("migrateTool").value
                    },
                    function (data) {
                        var result = data.process.replace(/\n/, '<br>');
                        $("#processModal_body").html(result);
                    }
                );
            })
    });
</script>
</body>
</html>

