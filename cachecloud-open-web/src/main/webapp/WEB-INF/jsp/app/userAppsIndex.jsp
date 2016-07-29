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
            <li class="active"><a href="#app_stat"
                                  data-url="/admin/app/stat.do?appId=${appId}&startDate=${startDate}&endDate=${endDate}"
                                  data-toggle="tab">应用统计信息</a></li>
            <!-- 
            <li><a href="#app_fault" data-url="/admin/app/fault.do?appId=${appId}" data-toggle="tab">故障报警</a></li>
            -->
            <li><a href="#app_topology" data-url="/admin/app/topology.do?appId=${appId}" data-toggle="tab">实例列表</a></li>
            <li><a href="#app_detail" data-url="/admin/app/detail.do?appId=${appId}" data-toggle="tab">应用详情</a></li>
            <li><a href="#app_command_analysis"
                   data-url="/admin/app/commandAnalysis.do?appId=${appId}&startDate=${startDate}&endDate=${endDate}&firstCommand=${firstCommand}"
                   data-toggle="tab">命令曲线</a></li>
            <li><a href="#app_command" data-url="/admin/app/command.html?appId=${appId}"
                   data-toggle="tab">命令执行</a>
            </li>
            <li><a href="#app_demo" data-url="/admin/app/demo.html?appId=${appId}"
                   data-toggle="tab">接入代码</a>
            </li>
            <li><a href="#app_slow_log" data-url="/admin/app/slowLog?appId=${appId}&slowLogStartDate=${slowLogStartDate}&slowLogEndDate=${slowLogEndDate}"
                   data-toggle="tab">慢查询</a>
            </li>
            <li><a href="#app_top_pic" data-url="/admin/app/machineInstancesTopology.do?appId=${appId}" data-toggle="tab">应用拓扑</a></li>
        </ul>
        <div class="tab-content">
            <div class="tab-pane active" id="app_stat">
            </div>
            <!-- 
             <div class="tab-pane" id="app_fault">
            </div>
            -->
            <div class="tab-pane" id="app_topology">
            </div>
            <div class="tab-pane" id="app_detail">
            </div>
            <div class="tab-pane" id="app_command_analysis">
            </div>
            <div class="tab-pane" id="app_command">
            </div>
            <div class="tab-pane" id="app_demo">
            </div>
            <div class="tab-pane" id="app_slow_log">
            </div>
            <div class="tab-pane" id="app_top_pic">
            </div>
        </div>
    </div>
</div>
<jsp:include page="/WEB-INF/include/foot.jsp"/>
<script type="text/javascript">
    $('#app_tabs a').click(function (e) {
        e.preventDefault();

        var url = $(this).attr("data-url");
        var href = this.hash;
        var pane = $(this);
        var id = $(href).attr("id");
        // ajax load from data-url
        $(href).load(url, function (result) {
            pane.tab('show');
            initChart(id);
        });
    });

    var tabTag = "${tabTag}";
    if (tabTag.length > 0 && $('#' + tabTag).length > 0) {
        var tabId = '#' + tabTag;
        $("a[href=" + tabId + "]").click();
    } else {
        $("a[href=#app_stat]").click();
    }

</script>
<script type="text/javascript" src="/resources/js/mem-cloud.js"></script>
<script type="text/javascript" src="/resources/js/docs.min.js"></script>
</body>
</html>
