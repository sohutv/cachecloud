<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <title>CacheCloud应用统计信息</title>
    <jsp:include page="/WEB-INF/include/head.jsp"/>
    <script type="text/javascript" src="/resources/js/jquery-console.js"></script>
    <script type="text/javascript" src="/resources/js/chart.js"></script>

</head>
<body role="document">
<div class="container">
    <jsp:include page="/WEB-INF/include/headMenu.jsp"/>
    <div id="systemAlert">
    </div>
    <div class="tabbable-custom">
        <ul class="nav nav-tabs" id="app_tabs">
            <li id="app_stat" class="active" data-url="/admin/app/stat?appId=${appId}&startDate=${startDate}&endDate=${endDate}">
                <a href="?appId=${appId}&tabTag=app_stat">应用统计信息</a>
            </li>
            <li id="app_topology" data-url="/admin/app/topology?appId=${appId}">
                <a href="?appId=${appId}&tabTag=app_topology">实例列表</a>
            </li>
            <li id="app_detail" data-url="/admin/app/detail?appId=${appId}">
                <a href="?appId=${appId}&tabTag=app_detail">应用详情</a>
            </li>
            <li id="app_clientList" data-url="/admin/app/clientList?appId=${appId}&condition=${condition}">
                <a href="?appId=${appId}&tabTag=app_clientList">连接信息</a>
            </li>
            <li id="app_command_analysis" data-url="/admin/app/commandAnalysis?appId=${appId}&startDate=${startDate}&endDate=${endDate}&firstCommand=${firstCommand}">
                <a href="?appId=${appId}&tabTag=app_command_analysis">命令曲线</a>
            </li>
            <li id="app_command" data-url="/admin/app/command?appId=${appId}">
                <a href="?appId=${appId}&tabTag=app_command">命令执行</a>
            </li>

            <c:if test="${installModuleFlag != null && installModuleFlag == true}">
                <li id="app_module" data-url="/admin/app/module?appId=${appId}">
                    <a href="?appId=${appId}&tabTag=app_module">模块扩展</a>
                </li>
            </c:if>

            <li id="app_demo" data-url="/wiki/access/client?entry=client">
                <a href="?appId=${appId}&tabTag=app_demo">接入代码</a>
            </li>
            <li id="app_latency" data-url="/admin/app/latencyMonitor?appId=${appId}&searchDate=${searchDate}">
                <a href="?appId=${appId}&tabTag=app_latency">延迟监控</a>
            </li>
            <li id="app_top_pic" data-url="/admin/app/machineInstancesTopology?appId=${appId}">
                <a href="?appId=${appId}&tabTag=app_top_pic">应用拓扑</a>
            </li>
            <li id="app_daily" data-url="/admin/app/daily?appId=${appId}&dailyDate=${dailyDate}">
                <a href="?appId=${appId}&tabTag=app_daily">日报统计</a>
            </li>
            <li id="app_key_analysis" data-url="/admin/app/key?appId=${appId}">
                <a href="?appId=${appId}&tabTag=app_key_analysis">键值分析</a>
            </li>
        </ul>
        <div class="tab-content">
            <div class="tab-pane active" id="app_statTab">
            </div>
            <div class="tab-pane" id="app_topologyTab">
            </div>
            <div class="tab-pane" id="app_detailTab">
            </div>
            <div class="tab-pane" id="app_clientListTab">
            </div>
            <div class="tab-pane" id="app_command_analysisTab">
            </div>
            <div class="tab-pane" id="app_commandTab">
            </div>
            <div class="tab-pane" id="app_moduleTab">
            </div>
            <div class="tab-pane" id="app_demoTab">
            </div>
            <div class="tab-pane" id="app_latencyTab">
            </div>
            <div class="tab-pane" id="app_top_picTab">
            </div>
            <div class="tab-pane" id="app_dailyTab">
            </div>
            <div class="tab-pane" id="app_key_analysisTab">
            </div>
        </div>
    </div>
</div>
<jsp:include page="/WEB-INF/include/foot.jsp"/>
<script type="text/javascript">
    function showTab(tab) {
        $.get($("#" + tab).attr("data-url"), function (result) {
            $("#" + tab + "Tab").html(result);
        });
    }

    function refreshActiveTab() {
        var tab = getQueryString("tabTag");
        if (tab) {
            $("#" + tab).addClass("active").siblings().removeClass("active");
            $("#" + tab + "Tab").addClass("active").siblings().removeClass("active");
        } else {
            tab = "app_stat";
        }
        console.log("tab:" + tab)
        showTab(tab);
        $("#tabs li a").tooltip({placement: "bottom"});
    }

    $(function () {
        refreshActiveTab();
    });

    function getQueryString(name) {
        var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
        console.log("window.location.search: "+ window.location.search);
        var r = window.location.search.substr(1).match(reg);
        if (r != null) return unescape(r[2]);
        return null;
    }
</script>
<script type="text/javascript" src="/resources/js/mem-cloud.js"></script>
<script type="text/javascript" src="/resources/js/docs.min.js"></script>
</body>
</html>
