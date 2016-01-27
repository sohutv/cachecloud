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
            <li class="active"><a href="#app_ops_instance" data-url="/manage/app/instance.do?appId=${appId}" data-toggle="tab">应用实例</a></li>
            <li><a href="#app_ops_machine" data-url="/manage/app/machine.do?appId=${appId}" data-toggle="tab">应用机器列表</a></li>
            <li><a href="#app_ops_detail" data-url="/manage/app/detail.do?appId=${appId}" data-toggle="tab">应用详情和审批列表</a></li>
        </ul>
        <div class="tab-content">
            <div class="tab-pane active" id="app_ops_instance">
            </div>
            <div class="tab-pane" id="app_ops_machine">
            </div>
            <div class="tab-pane" id="app_ops_detail">
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
        });
    });

    var tabTag = "${tabTag}";
    if (tabTag.length > 0 && $('#' + tabTag).length > 0) {
        var tabId = '#' + tabTag;
        $("a[href=" + tabId + "]").click();
    } else {
        $("a[href=#app_ops_instance]").click();
    }

</script>
<script type="text/javascript" src="/resources/js/docs.min.js"></script>
</body>
</html>
