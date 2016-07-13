<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp"%>
<div class="col-md-6">
	<div id="containerRead"
		style="min-width: 310px; height: 400px; margin: 0 auto"></div>
</div>
<div class="col-md-6">
	<div id="containerWrite"
		style="min-width: 310px; height: 400px; margin: 0 auto"></div>
</div>
<div class="col-md-6">
	<div id="containerBusy"
		style="min-width: 310px; height: 400px; margin: 0 auto"></div>
</div>
<div class="col-md-6">
	<div id="containerIops"
		style="min-width: 310px; height: 400px; margin: 0 auto"></div>
</div>
<div class="col-md-6">
	<div id="containerSpace"
		style="min-width: 310px; height: 400px; margin: 0 auto"></div>
</div>
	
<script type="text/javascript">
	$(document).ready(function() {
		initReadChart();
		initWriteChart();
		initBusyChart();
		initIopsChart();
		initSpaceChart();
	});
	
	function initReadChart(){
		var options = getOptions("read", "containerRead");
		options.subtitle.text = "max:${read.max}k/s avg:${read.avg}k/s";
		<c:forEach items="${read.series}" var="item" varStatus="stat">
			options.series.push(${item.toJson()});
		</c:forEach>
	    new Highcharts.Chart(options); 
	}
	function initWriteChart(){
		var options = getOptions("write", "containerWrite");
		options.subtitle.text = "max:${write.max}k/s avg:${write.avg}k/s";
		<c:forEach items="${write.series}" var="item" varStatus="stat">
			options.series.push(${item.toJson()});
		</c:forEach>
	    new Highcharts.Chart(options); 
	}
	function initBusyChart(){
		var options = getOptions("busy", "containerBusy");
		options.subtitle.text = "max:${busy.max}% avg:${busy.avg}%";
		<c:forEach items="${busy.series}" var="item" varStatus="stat">
			options.series.push(${item.toJson()});
		</c:forEach>
	    new Highcharts.Chart(options); 
	}
	function initIopsChart(){
		var options = getOptions("iops", "containerIops");
		options.subtitle.text = "max:${iops.max}次/s avg:${iops.avg}次/s";
		<c:forEach items="${iops.series}" var="item" varStatus="stat">
			options.series.push(${item.toJson()});
		</c:forEach>
	    new Highcharts.Chart(options); 
	}
	function initSpaceChart(){
		var options = getOptions("space use", "containerSpace");
		options.subtitle.text = "max:${space.max}% avg:${space.avg}%";
		<c:forEach items="${space.series}" var="item" varStatus="stat">
			options.series.push(${item.toJson()});
		</c:forEach>
	    new Highcharts.Chart(options); 
	}
</script>