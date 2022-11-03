<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp"%>

<div class="row">
    <div class="page-header">
        <h4>应用模块扩展</h4>
    </div>
    <div style="margin-top: 20px">
        <table class="table table-bordered table-striped table-hover">
            <thead>
            <tr>
                <td>模块名称</td>
                <td>模块版本</td>
                <td>模块信息</td>
                <td>模块配置</td>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="module" items="${moduleList}" varStatus="status">
                <tr>
                    <td>${module.name}</td>
                    <td>${module.tag}</td>
                    <c:choose>
                        <c:when test="${fn:containsIgnoreCase(module.name, 'RediSearch')}">
                            <td>
                                <c:choose>
                                    <c:when test="${indexList != null && indexList.size() > 0}">
                                        <c:forEach var="indexName" items="${indexList}" varStatus="status">
                                            <a href="/admin/app/module/redisearch/info?appId=${appId}&indexName=${indexName}#redisearchInfoId" target="_blank">${indexName}</a>
                                            <br>
                                        </c:forEach>
                                    </c:when>
                                    <c:otherwise>
                                        -
                                    </c:otherwise>
                                </c:choose>
                            </td>
                        </c:when>
                        <c:otherwise>
                            <td>-</td>
                        </c:otherwise>
                    </c:choose>
                    <c:choose>
                        <c:when test="${fn:containsIgnoreCase(module.name, 'RediSearch')}">
                            <td>
                                <a href="/admin/app/module/redisearch/config?appId=${appId}" target="_blank">配置查询</a>
                            </td>
                        </c:when>
                        <c:otherwise>
                            <td>-</td>
                        </c:otherwise>
                    </c:choose>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </div>
</div>

<div class="row">
    <div id="console" class="console"></div>
    <script type="text/javascript">
        $(document).ready(function () {
            var console = $('#console');
            var controller = console.console({
                promptLabel: 'appId:${appId}> ',
                commandValidate: function (line) {
                    if (line == "") return false;
                    else return true;
                },
                commandHandle: function (line,report) {
                    $.ajax({
                        url: "/admin/app/commandExecute.json",
                        data: {appId: $('#appId').val(), command: line},
                        dataType: "json",
                        success: function (result) {
                            report([
                                {msg: result.result,
                                    className: "jquery-console-message-value"}
                            ]);
                        }
                    });
                },
                autofocus: true,
                animateScroll: true,
                promptHistory: true
            });
        });
    </script>
    <center><h4><font color='red'>注意：非测试应用只可以执行只读命令，如有需要清理数据请联系管理员！</font></h4></center>
    <input type="hidden" id="appId" value="${appId}">
</div>

<div class="row">
    <div class="page-header">
        <iframe src="https://redis.io/commands/?group=search" frameborder="0" scrolling="0" width="100%" height="800" title="GitHub"></iframe>
    </div>
</div>

<script type="text/javascript">
    function add() {
        var console = $('#console');
        console.console("FT._LIST");
    }
</script>
