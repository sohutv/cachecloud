<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <title>CacheCloud应用运维</title>
    <jsp:include page="/WEB-INF/include/head.jsp"/>
    <script type="text/javascript" src="/resources/js/jquery-console.js"></script>

</head>
<body role="document">
<div class="container">
    <jsp:include page="/WEB-INF/include/headAdmin.jsp"/>
    <div class="tabbable-custom">
        <ul class="nav nav-tabs" id="app_tabs">
            <li id="app_ops_instance" class="active" data-url="/manage/app/instance?appId=${appId}">
                <a href="?appId=${appId}&tabTag=app_ops_instance">应用实例</a>
            </li>
            <li id="app_ops_machine" data-url="/manage/app/machine?appId=${appId}">
                <a href="?appId=${appId}&tabTag=app_ops_machine">应用机器列表</a>
            </li>
            <li id="app_ops_detail" data-url="/manage/app/detail?appId=${appId}">
                <a href="?appId=${appId}&tabTag=app_ops_detail">应用详情和审批列表</a>
            </li>
            <li id="app_ops_code" data-url="/manage/app/initAppPassword?appId=${appId}">
                <a href="?appId=${appId}&tabTag=app_ops_code">应用密码修改</a>
            </li>
            <li id="app_ops_tool" data-url="/manage/tool/topologyExam?appId=${appId}">
                <a href="?appId=${appId}&tabTag=app_ops_tool">应用拓扑诊断</a>
            </li>
        </ul>
        <div class="tab-content">
            <div class="tab-pane active" id="app_ops_instanceTab">
            </div>
            <div class="tab-pane" id="app_ops_machineTab">
            </div>
            <div class="tab-pane" id="app_ops_detailTab">
            </div>
            <div class="tab-pane" id="app_ops_codeTab">
            </div>
            <div class="tab-pane" id="app_ops_toolTab">
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
            tab = "app_ops_instance";
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
        var r = window.location.search.substr(1).match(reg);
        if (r != null) return unescape(r[2]);
        return null;
    }
</script>
<script type="text/javascript" src="/resources/js/docs.min.js"></script>
</body>
</html>
