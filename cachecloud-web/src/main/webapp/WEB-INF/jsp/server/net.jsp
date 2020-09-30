<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp"%>
<c:if test="${empty cpu}">
	no data
</c:if>
<c:forEach items="${net}" var="item" varStatus="stat">
	<div class="col-md-6">
		<div id="container${item.name}"
			style="min-width: 310px; height: 400px; margin: 0 auto"></div>
	</div>
</c:forEach>
	
<script type="text/javascript">
	$(document).ready(function() {
		initSubNetChart();
	});
	
	function initSubNetChart(){
		<c:forEach items="${net}" var="item" varStatus="stat">
			var options = getOptions("${item.name}", "container${item.name}");
			options.subtitle.text = "max in:${item.maxIn}k/s out:${item.maxOut}k/s avg in:${item.avgIn}k/s out:${item.avgOut}k/s";
			options.series.push(${item.inSeries.toJson()});
			options.series.push(${item.outSeries.toJson()});
		    new Highcharts.Chart(options); 
		</c:forEach>
	}
</script>