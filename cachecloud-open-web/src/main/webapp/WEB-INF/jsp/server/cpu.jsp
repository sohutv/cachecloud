<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp"%>
<c:if test="${empty cpu}">
	no data
</c:if>
<c:forEach items="${cpu}" var="item" varStatus="stat">
	<div class="col-md-6">
		<div id="container${item.name}"
			style="min-width: 310px; height: 400px; margin: 0 auto"></div>
	</div>
</c:forEach>
	
<script type="text/javascript">
	$(document).ready(function() {
		initSubCpuChart();
	});
	
	function initSubCpuChart(){
		<c:forEach items="${cpu}" var="item" varStatus="stat">
			var options = getOptions("${item.name}", "container${item.name}");
			options.subtitle.text = "max user:${item.maxUser}% sys:${item.maxSys}% wa:${item.maxWa}% avg user:${item.avgUser}% sys:${item.avgSys}% wa:${item.avgWa}%";
			options.series.push(${item.userSeries.toJson()});
			options.series.push(${item.sysSeries.toJson()});
			options.series.push(${item.waSeries.toJson()});
		    new Highcharts.Chart(options); 
		</c:forEach>
	}
</script>