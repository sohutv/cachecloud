<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <title>CacheCloud客户端信息</title>
    <jsp:include page="/WEB-INF/include/head.jsp"/>
    <script type="text/javascript" src="/resources/js/jquery-console.js"></script>
    <script type="text/javascript" src="/resources/js/chart.js"></script>
    <script type="text/javascript" src="/resources/js/appClient.js"></script>

</head>
<body role="document">
	<div class="container">
	    <jsp:include page="/WEB-INF/include/headMenu.jsp"/>
	    <div class="tabbable-custom">
	        <ul class="nav nav-tabs" id="app_tabs">
	            <li class="active"><a href="#app_client_cost_distribute" data-url="/client/show/costDistribute.do?appId=${appId}&costDistriStartDate=${costDistriStartDate}&costDistriEndDate=${costDistriEndDate}&firstCommand=${firstCommand}&timeDimensionality=${timeDimensionality}" data-toggle="tab">耗时统计</a></li>
	            <li><a href="#app_client_value_distribute" data-url="/client/show/valueDistribute.do?appId=${appId}&valueDistriStartDate=${valueDistriStartDate}&valueDistriEndDate=${valueDistriEndDate}" data-toggle="tab">值分布统计</a></li>
	            <li><a href="#app_client_exception" data-url="/client/show/exception.do?appId=${appId}&exceptionStartDate=${exceptionStartDate}&exceptionEndDate=${exceptionEndDate}&type=${type}&clientIp=${clientIp}&pageNo=${pageNo}" data-toggle="tab">异常统计</a></li>
	        </ul>
	        <div class="tab-content">
	            <div class="tab-pane active" id="app_client_cost_distribute">
	            </div>
	            <div class="tab-pane" id="app_client_value_distribute">
	            </div>
	            <div class="tab-pane" id="app_client_exception">
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
	        $("a[href=#app_client_cost_distribute]").click();
	    }
	</script>
	<script type="text/javascript" src="/resources/js/docs.min.js"></script>
</body>
</html>
