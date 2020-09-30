<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <title>实例统计信息</title>
    <jsp:include page="/WEB-INF/include/head.jsp"/>
    <script type="text/javascript" src="/resources/js/jquery-console.js"></script>
</head>
<body role="document">
<div class="container">
    <jsp:include page="/WEB-INF/include/headMenu.jsp"/>
    <div id="systemAlert">
    </div>
    <div class="tabbable-custom">
        <ul class="nav nav-tabs" id="instance_tabs">
            <li id="instance_stat" class="active" data-url="/admin/instance/stat.html?instanceId=${instanceId}&startDate=${startDate}&endDate=${endDate}"><a href="?instanceId=${instanceId}&tabTag=instance_stat">实例统计信息</a></li>
            <li id="app_topology" data-url="/admin/app/topology.html?appId=${appId}"><a href="?instanceId=${instanceId}&tabTag=app_topology">拓扑结构</a></li>
            <c:if test="${type == 2 or type == 6}">
            <li id="instance_slowSelect" data-url="/admin/instance/slowSelect.html?instanceId=${instanceId}"><a href="?instanceId=${instanceId}&tabTag=instance_slowSelect">慢查询分析</a></li>
            <li id="instance_configSelect" data-url="/admin/instance/configSelect.html?instanceId=${instanceId}&appId=${appId}"><a href="?instanceId=${instanceId}&tabTag=instance_configSelect">配置查询</a></li>
            <li id="instance_clientList" data-url="/admin/instance/clientList.html?instanceId=${instanceId}&condition=${condition}"><a href="?instanceId=${instanceId}&tabTag=instance_clientList">连接信息</a></li>
            </c:if>
            <li id="instance_fault" data-url="/admin/instance/fault.html?instanceId=${instanceId}&startDate=${startDate}&endDate=${endDate}"><a href="?instanceId=${instanceId}&tabTag=instance_fault">故障报警</a></li>
            <li id="instance_advancedAnalysis" data-url="/admin/instance/advancedAnalysis.html?instanceId=${instanceId}&startDate=${startDate}&endDate=${endDate}"><a href="?instanceId=${instanceId}&tabTag=instance_advancedAnalysis">命令曲线</a></li>
            <li id="instance_command" data-url="/admin/instance/command.html?instanceId=${instanceId}"><a href="?instanceId=${instanceId}&tabTag=instance_command">命令执行</a></li>
        </ul>
        <div class="tab-content">
            <div class="tab-pane active" id="instance_statTab"></div>
            <div class="tab-pane" id="app_topologyTab"></div>
            <div class="tab-pane" id="instance_slowSelectTab"></div>
            <div class="tab-pane" id="instance_configSelectTab"></div>
            <div class="tab-pane" id="instance_clientListTab"></div>
            <div class="tab-pane" id="instance_faultTab"></div>
            <div class="tab-pane" id="instance_advancedAnalysisTab"></div>
            <div class="tab-pane" id="instance_commandTab"></div>
        </div>
    </div>
</div>
<jsp:include page="/WEB-INF/include/foot.jsp"/>
<script src="/resources/js/chart.js"></script>
<script type="text/javascript" src="/resources/js/mem-cloud.js"></script>
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
            tab = "instance_stat";
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
</body>
</html>
