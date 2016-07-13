<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp"%>
<div class="col-md-6">
	<div class="page-header">
		<h4>
		服务器信息
		</h4>
	</div>
	<div class="tabbable-custom">
		<table class="table table-striped table-hover">
			<tr>
			    <td>ip&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>
			    <td>${info.ip}</td>
			</tr>
			<tr>
			    <td>host</td>
			    <td>${info.host}</td>
			</tr>
			<tr>
			    <td>cpu核数</td>
			    <td>${info.cpus}</td>
			</tr>
			<tr>
			    <td>nmon版本</td>
			    <td>${info.nmon}</td>
			</tr>
			<tr>
			    <td>max file</td>
			    <td>${file}</td>
			</tr>
			<tr>
			    <td>max procs</td>
			    <td>${process}</td>
			</tr>
			<tr>
				<td>cpu型号</td>
			    <td>${info.cpuModel}</td>
			</tr>
			<tr>
			    <td>发行版本</td>
			    <td>${info.dist}</td>
			</tr>
			<tr>
			   <td>内核版本</td>
			   <td>${info.kernel}</td>
			</tr>
		</table>
	</div>
</div>
<div class="col-md-6">
	<div class="page-header">
		<div style="float:right">
			<form method="post" action="/server/index.do" id="ec" name="ec">
				<label style="font-weight:bold;text-align:left;">
				 	日期:&nbsp;&nbsp;
				</label>
				<input type="text" size="21" name="date" id="startDate" readonly="readonly" value="${date}" onFocus="WdatePicker({startDate:'%y-%M-01',dateFmt:'yyyy-MM-dd',alwaysUseStartDate:true})"/>
				<input type="hidden" name="ip" value="${info.ip}">
				<label>&nbsp;<input type="submit" class="btn-4" value="查询"/></label>
			</form>
		</div>
		<h4>load</h4>
	</div>
	<div id="containerLoad"
		style="min-width: 310px; height: 400px; margin: 0 auto"></div>
</div>
<div class="col-md-6">
	<div class="page-header">
		<h4>cpu</h4>
	</div>
	<div id="containerCpu"
		style="min-width: 310px; height: 400px; margin: 0 auto"></div>
</div>
<div class="col-md-6">
	<div class="page-header">
		<h4>memory</h4>
	</div>
	<div id="containerMemory"
		style="min-width: 310px; height: 400px; margin: 0 auto"></div>
</div>
<div class="col-md-6">
	<div class="page-header">
		<h4>swap</h4>
	</div>
	<div id="containerSwap"
		style="min-width: 310px; height: 400px; margin: 0 auto"></div>
</div>
<div class="col-md-6">
	<div class="page-header">
		<h4>net</h4>
	</div>
	<div id="containerNet"
		style="min-width: 310px; height: 400px; margin: 0 auto"></div>
</div>
<div class="col-md-6">
	<div class="page-header">
		<h4>tcp connection</h4>
	</div>
	<div id="containerTcp"
		style="min-width: 310px; height: 400px; margin: 0 auto"></div>
</div>
<div class="col-md-6">
	<div class="page-header">
		<h4>disk</h4>
	</div>
	<div id="containerDisk"
		style="min-width: 310px; height: 400px; margin: 0 auto"></div>
</div>
<script type="text/javascript">
	$(document).ready(
		function() {
			initLoadChart();
			initCpuChart();
			initMemoryChart();
			initSwapChart();
			initNetChart();
			initTcpChart();
			initDiskChart();
	});
	
	function initLoadChart(){
		var title = "1-min-max:${maxLoad1} 1-min-avg:${avgLoad1}";
		var options = getOptions(title, "containerLoad");
		push(options.series, ${load1});
		push(options.series, ${load5});
		push(options.series, ${load15});
	    new Highcharts.Chart(options); 
	}
	
	function initCpuChart(){
		var title = "max user:${maxUser}% sys:${maxSys}% wa:${maxWa}%";
		var options = getOptions(title, "containerCpu");
		push(options.series, ${user});
		push(options.series, ${sys});
		push(options.series, ${wa});
	    new Highcharts.Chart(options); 
	}
	
	function initMemoryChart(){
		var title = "now free:${curFree}G max use:${maxUse}G cache:${maxCache}G buffer:${maxBuffer}G";
		var options = getOptions(title, "containerMemory", "area");
		push(options.series, ${mtotal});
		push(options.series, ${muse});
		push(options.series, ${mcache});
		push(options.series, ${mbuffer});
	    new Highcharts.Chart(options); 
	}
	
	function initSwapChart(){
		var title = "max use:${maxSwap}M";
		var options = getOptions(title, "containerSwap");
		push(options.series, ${mswap});
		push(options.series, ${mswapUse});
	    new Highcharts.Chart(options); 
	}
	
	function initNetChart(){
		var title = "max in:${maxNetIn}M/s out:${maxNetOut}M/s";
		var options = getOptions(title, "containerNet");
		push(options.series, ${nin});
		push(options.series, ${nout});
	    new Highcharts.Chart(options); 
	}
	
	function initTcpChart(){
		var title = "max estab:${maxConn} tw:${maxWait} orphan:${maxOrphan}";
		var options = getOptions(title, "containerTcp");
		push(options.series, ${testab});
		push(options.series, ${twait});
		push(options.series, ${torph});
	    new Highcharts.Chart(options); 
	}
	
	function initDiskChart(){
		var title = "max read:${maxRead}M/s write:${maxWrite}M/s busy:${maxBusy}% iops:${maxIops}次/s";
		var options = getOptions(title, "containerDisk");
		options.yAxis = [{title:{text:""}},{opposite: true},{opposite: true}];
		var dread = eval(${dread});
		dread.tooltip = {
            valueSuffix: 'k/s'
        };
		push(options.series, dread);
		var dwrite = eval(${dwrite});
		dwrite.tooltip = {
            valueSuffix: 'k/s'
        };
		push(options.series, dwrite);
		var dbusy = eval(${dbusy});
		dbusy.tooltip = {
            valueSuffix: '%'
        };
		push(options.series, dbusy);
		var diops = eval(${diops});
		diops.tooltip = {
            valueSuffix: '次/s'
        };
		push(options.series, diops);
	    new Highcharts.Chart(options); 
	}
	
	function push(series, value){
		if(value){
			series.push(value);
		}
	}
	
	function getOptions(title, renderTo, chartType){
		var showTitle = title;
	    if(!chartType){
	    	chartType = "";
	    }
	    var marginRight = 10;
	    if(renderTo.indexOf("Disk") != -1){
	    	marginRight = 30;
	    }
	   	var options = {
			chart: {
				renderTo: renderTo,
				animation: Highcharts.svg,
				backgroundColor: '#E6F1F5',
				plotBackgroundColor:'#FFFFFF',
				zoomType: "x",
				type: chartType,
				marginRight: marginRight
			},
            title: {
            	useHTML:true,
                text: showTitle
            },
            subtitle: {
                text: ''
            },
            xAxis: {
            	categories: ${xAxis},
            	tickInterval: 24
            },
            yAxis: {
            	title: {
               		text: ''
           		},
                plotLines: [{
                    value: 0,
                    width: 1,
                    color: '#808080'
                }],
                min: 0
            },
            plotOptions: {
                line: {
                    dataLabels: {
                        enabled: true
                    }
                },
                series: {
	                cursor: 'pointer',
	                marker: {
                		enabled: false
                	}
	            }
            },
            tooltip: {
                shared:true
            },
            legend: {
                enabled: true
            },
            credits:{
            	enabled: false
            },
            exporting: {
                enabled: true
            },
            series: []
        };
	   	if(renderTo.indexOf("Cpu") != -1 || renderTo.indexOf("CPU") != -1 || renderTo.indexOf("Busy") != -1 || renderTo.indexOf("Space") != -1){
	   		options.tooltip.pointFormat = "<span>{series.name}</span>:{point.y:,.f}%<br/>";
	    }
	   	if(renderTo.indexOf("Memory") != -1 || renderTo.indexOf("Swap") != -1){
	   		options.tooltip.pointFormat = "<span>{series.name}</span>:{point.y:,.f}M<br/>";
	    }
	   	if(renderTo.indexOf("Net") != -1 || renderTo.indexOf("eth") != -1 || renderTo.indexOf("Read") != -1 || renderTo.indexOf("Write") != -1){
	   		options.tooltip.pointFormat = "<span>{series.name}</span>:{point.y:,.f}k/s<br/>";
	    }
	   	if(renderTo.indexOf("Iops") != -1){
	   		options.tooltip.pointFormat = "<span>{series.name}</span>:{point.y:,.f}次/s<br/>";
	    }
	   	return options;
	}
</script>