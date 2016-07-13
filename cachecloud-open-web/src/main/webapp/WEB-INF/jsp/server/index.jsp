<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <title>CacheCloud服务器状态</title>
    <jsp:include page="/WEB-INF/include/head.jsp"/>
    <script type="text/javascript" src="/resources/js/chart.js"></script>

</head>
<body role="document">
	<div class="container">
    	<jsp:include page="/WEB-INF/include/headMenu.jsp"/>
	    <div class="tabbable-custom">
	        <ul class="nav nav-tabs" id="app_tabs">
	            <li class="active"><a href="#overview" data-url="/server/overview.do?ip=${ip}&date=${date}" data-toggle="tab">概览</a></li>
	            <li><a href="#cpu" data-url="/server/cpu.do?ip=${ip}&date=${date}" data-toggle="tab">cpu</a></li>
	            <li><a href="#net" data-url="/server/net.do?ip=${ip}&date=${date}" data-toggle="tab">net</a></li>
	        	<li><a href="#disk" data-url="/server/disk.do?ip=${ip}&date=${date}" data-toggle="tab">disk</a></li>
	        </ul>
	        <div class="tab-content">
	            <div class="tab-pane active" id="overview">
	            </div>
	            <div class="tab-pane" id="cpu">
	            </div>
	            <div class="tab-pane" id="net">
	            </div>
	            <div class="tab-pane" id="disk">
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
	        $("a[href=#overview]").click();
	    }
	</script>
	<script type="text/javascript" src="/resources/js/docs.min.js"></script>
</body>
</html>
