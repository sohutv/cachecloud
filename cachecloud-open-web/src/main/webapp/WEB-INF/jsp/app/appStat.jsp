<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp"%>

<script type="text/javascript">
var startDate = '${startDate}';
var endDate = '${endDate}';
var yesterDate = '${yesterDay}';
var betweenOneDay = '${betweenOneDay}';
var appId = '${appId}';
var chartType = 'line';
var chartParams = "&startDate="+startDate+"&endDate="+endDate;
var chartParamsCompare = "&startDate="+yesterDate+"&endDate="+startDate;
var betweenParams = "&startDate="+yesterDate+"&endDate="+endDate;
var appTotalMem = '${appDetail.mem}';
Highcharts.setOptions({
	global : {
		useUTC : false
	}
});
Highcharts.setOptions({
	colors : [ '#2f7ed8', '#E3170D', '#0d233a', '#8bbc21', '#1aadce',
			'#492970', '#804000', '#f28f43', '#77a1e5',
			'#c42525', '#a6c96a' ]
});

</script>

<div class="container">
	<br/>
	<div class="row">
		<div style="float:right">
			<form method="post" action="/admin/app/index.do" id="ec" name="ec">
				<label style="font-weight:bold;text-align:left;">
				 	开始日期:&nbsp;&nbsp;
				</label>
				<input type="text" size="21" name="startDate" id="startDate" value="${startDate}" onFocus="WdatePicker({startDate:'%y-%M-01',dateFmt:'yyyy-MM-dd',alwaysUseStartDate:true})"/>
				<label style="font-weight:bold;text-align:left;">
				 	结束日期:
				</label>
				<input type="text" size="20" name="endDate" id="endDate" value="${endDate}" onFocus="WdatePicker({startDate:'%y-%M-01',dateFmt:'yyyy-MM-dd',alwaysUseStartDate:true})"/>
				<input type="hidden" name="appId" value="${appDetail.appDesc.appId}">
				<label>&nbsp;<input type="submit" class="btn-4" value="查询"/></label>
			</form>
		</div>
	</div>
	<div class="row">
		<div class="col-md-6">
			<div class="page-header">
				<h4>
				全局信息&nbsp;&nbsp;&nbsp;
				<button type="button" id="appScaleApplyBtn" class="btn btn-info" data-target="#appScaleApplyModal" data-toggle="modal">申请扩容</button>
				<button type="button" class="btn btn-info" data-target="#appConfigChangeModal" data-toggle="modal" href="#">申请修改配置</button>
				<a target="_blank" href="/client/show/index.do?appId=${appId}" class="btn btn-info" role="button">客户端统计</a>
				</h4>
			</div>
			<table class="table table-striped table-hover">
				<tbody>
					<tr>
						<td>内存使用率</td>
						<td>
							<div class="progress margin-custom-bottom0">
								<c:choose>
		                    		<c:when test="${appDetail.memUsePercent >= 80.00}">
										<div class="progress-bar progress-bar-danger"
											role="progressbar" aria-valuenow="${appDetail.memUsePercent}" aria-valuemax="100"
											aria-valuemin="0" style="width: ${appDetail.memUsePercent}%">
			                    	</c:when>
		                    		<c:otherwise>
										<div class="progress-bar progress-bar-success"
											role="progressbar" aria-valuenow="${appDetail.memUsePercent}" aria-valuemax="100"
											aria-valuemin="0" style="width: ${appDetail.memUsePercent}%">				                    		</c:otherwise>
		                    	</c:choose>
									<label style="color: #000000">
										<fmt:formatNumber value="${appDetail.mem  * appDetail.memUsePercent / 100 / 1024}" pattern="0.00"/>G&nbsp;&nbsp;Used/<fmt:formatNumber value="${appDetail.mem / 1024 * 1.0}" pattern="0.00"/>G&nbsp;&nbsp;Total
									</label>
								</div>
							</div>
						</td>
						<td>当前连接数</td>
						<td>${appDetail.conn}</td>
					</tr>
					<tr>
						<td>应用主节点数</td>
						<td>${appDetail.masterNum}</td>
						<td>应用从节点数</td>
						<td>${appDetail.slaveNum}</td>
					</tr>
					<tr>
						<td>应用命中率</td>
						<td>${appDetail.hitPercent}%</td>
						<td>当前对象数</td>
						<td><fmt:formatNumber value="${appDetail.currentObjNum}" pattern="#,#00"/></td>
					</tr>
                    <tr>
                        <td>应用当前状态</td>
                        <td>${appDetail.appDesc.statusDesc}</td>
                        <td>应用分布机器节点数</td>
                        <td>${appDetail.machineNum}</td>
                    </tr>
				</tbody>
			</table>

			<div class="page-header">
				<h4>各命令峰值信息</h4>
			</div>
			<table class="table table-striped table-hover">
				<tbody>
					<tr>
						<td>命令</td>
						<td>峰值QPM</td>
						<td>峰值产生时间</td>
					</tr>
					<c:forEach items="${top5ClimaxList}" var="command">
						<tr>
							<td>${command.commandName}</td>
							<td><fmt:formatNumber value="${command.commandCount}" pattern="#,#00"/></td>
							<td><fmt:formatDate value="${command.createTime}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
						</tr>
					</c:forEach>
				</tbody>
			</table>

		</div>
		<div class="col-md-6">
			<div class="page-header">
				<h4>命令统计</h4>
			</div>
			<div id="containerTop5"
				style="min-width: 310px; height: 400px; margin: 0 auto"></div>
		</div>
	</div>
	<script type="text/javascript">
		$(document).ready(
			function() {
			    var title =  "<b>命令分布</b>";
			    var chartType = "pie";
			   	var options = {
					chart: {
						renderTo:'containerTop5',
						animation: Highcharts.svg,
						backgroundColor: '#E6F1F5',
						plotBackgroundColor:'#FFFFFF',
						type: chartType,
						marginRight: 10
					},
		            title: {
		            	useHTML:true,
		                text: title
		            },
		            xAxis: {
		                type: 'category'
		            },
		            yAxis: {
		            	title: {
                    		text: ''
                		},
		                plotLines: [{
		                    value: 0,
		                    width: 1,
		                    color: '#808080'
		                }]
		            },
		            plotOptions: {
		                line: {
		                    dataLabels: {
		                        enabled: true
		                    }
		                },
		                series: {
			                cursor: 'pointer'
			            }
		            },
		            tooltip: {
		                formatter: function() {
		                        return '<b>'+ this.point.name +'</b><br/>'
		                }
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
	            var pieUrl = "/admin/app/getTop5Commands.do?appId="+appId+chartParams;
		    	$.ajax({  
		          type : "get",  
		          url : pieUrl,  
		          async : true,  
		          success : function(data){
		          	var dataArr = eval("("+data+")");
	    			var length = dataArr.length;
	    			var legendName = "命令分布统计";
	    			var arr = [];
	                
	                for (var i = 0; i <length; i++) {
	                    var data = dataArr[i];
                  		var pointName = data.commandName + ":" + data.y;
	                    var point = {
	                    	name:pointName,
	                        y: data.y
	                    };
	                    arr.push(point);
	                }
             	    var series={
	    				name:legendName,
	    				data:arr
	    			};
	    			options.series.push(series);
     		        new Highcharts.Chart(options); 
		          }
		    	});
		    });
	</script>
	
	<!-- 命令相关 -->
	<script type="text/javascript">
		//查询一天出每分钟数据
		if(betweenOneDay == 1){
			$(document).ready(
				function() {
					var options = getOption("containerCommands", "<b>全命令统计</b>", "次数");
					var commandsUrl = "/admin/app/getMutiDatesCommandStats.json?appId=" + appId + betweenParams;
					$.ajax({
						type : "get",
						url : commandsUrl,
						async : true,
						success : function(data) {
							var dates = new Array();
							dates.push(startDate); 
							dates.push(yesterDate);
							pushOptionSeries(options, data, dates, "命令趋势图");
							new Highcharts.Chart(options);
						}
					});
					
			 });
		}else{
			$(document).ready(
				function() {
					var options = getOption("containerCommands", "<b>全命令统计</b>", "次数");
					var commandsUrl = "/admin/app/getCommandStats.do?appId=" + appId + chartParams;
					$.ajax({
						type : "get",
						url : commandsUrl,
						async : true,
						success : function(data) {
							var nameLegend = "命令趋势图";
							var finalPoints = getSeriesPoints(data, nameLegend);
							options.series.push(finalPoints);
							new Highcharts.Chart(options);
						}
					});
			 });
		}
	</script>
	<div id="containerCommands"
		style="min-width: 310px; height: 350px; margin: 0 auto"></div>

	
	
	<!-- 命中相关 -->
	<script type="text/javascript">
		//查询一天出每分钟数据
		if(betweenOneDay == 1){
			$(document).ready(
				function() {
					var options = getOption("containerHits", "<b>命中统计</b>", "次数");
					var commandsUrl = "/admin/app/getMutiDatesAppStats.json?appId=" + appId + "&statName=hits"+betweenParams;
					$.ajax({
						type : "get",
						url : commandsUrl,
						async : true,
						success : function(data) {
							var dates = new Array();
							dates.push(startDate); 
							dates.push(yesterDate);
							pushOptionSeries(options, data, dates, "命中趋势图");
							new Highcharts.Chart(options);
						}
					});
			 });
		}else{
			$(document).ready(
				function() {
					var options = getOption("containerHits", "<b>命中统计</b>", "次数");
					var commandsUrl = "/admin/app/getAppStats.json?appId=" + appId + "&statName=hits"+chartParams + "&timeDimensionalityIndex=1";
					$.ajax({
						type : "get",
						url : commandsUrl,
						async : true,
						success : function(data) {
							var nameLegend = "命中趋势图";
							var finalPoints = getSeriesPoints(data, nameLegend);
							options.series.push(finalPoints);
							new Highcharts.Chart(options);
						}
					});
			 });
		}
	</script>
	<div id="containerHits"
		style="min-width: 310px; height: 350px; margin: 0 auto"></div>
	
	<!-- 网络流量 -->
	<script type="text/javascript">
		var allInstanceNetStatUrl = "/admin/app/appInstanceNetStat?appId=" + appId +chartParams; 
			$(document).ready(
				function() {
					var options = getOption("containerNet", "网络流量<a href='"+allInstanceNetStatUrl+"' target='_blank'>(查看实例流量)</a>", "");
					//网络流量
					var netUrl = "/admin/app/getMutiStatAppStats.json?appId=" + appId + "&statName=netInput,netOutput" + chartParams;
					$.ajax({
						type : "get",
						url : netUrl,
						async : true,
						success : function(data) {

							var dataObject = eval("(" + data.data + ")");
							var inputDataArr = dataObject["netInput"];

							//1.input
							var inputPoints = getNetPoints(inputDataArr, "net_input");
							//确认单位
							options.yAxis.title.text = inputPoints.unitTxt;
							var unit = inputPoints.unit;
							options.series.push(inputPoints);
							
							//2.output
							var outputDataArr = dataObject["netOutput"];
							var outputPoints = getNetPoints(outputDataArr, "net_output", unit);
							options.series.push(outputPoints);

							new Highcharts.Chart(options);
						}
					});
			 });
	</script>
	<div id="containerNet"
		style="min-width: 310px; height: 350px; margin: 0 auto"></div>
		
	<!-- 内存变化相关 -->
	<script type="text/javascript">
		//查询一天出每分钟数据
		
		/*if(betweenOneDay == 1){*/
			
			$(document).ready(
				function() {
					var options = getOption("containerMemory", "<b>内存使用量</b>", "M");
					var commandsUrl = "/admin/app/getAppStats.do?appId=" + appId + "&statName=usedMemory"+chartParams;
					$.ajax({
						type : "get",
						url : commandsUrl,
						async : true,
						success : function(data) {
							var nameLegend = "内存使用量(" + startDate + ")";
							var finalPoints = getSeriesPoints(data, nameLegend, "M");
							options.series.push(finalPoints);
							var maxMemoryPoints = getSeriesPoints(data, "应用总内存", "M", parseInt(appTotalMem));
							options.series.push(maxMemoryPoints);
							new Highcharts.Chart(options);
						}
					});
			 });
		/*}*/
		/*
		else{
			$(document).ready(
				function() {
					var options = getOption("containerMemory", "<b>内存使用量</b>", "字节");
					var commandsUrl = "/admin/app/getAppStats.do?appId=" + appId + "&statName=usedMemory"+chartParams;
					$.ajax({
						type : "get",
						url : commandsUrl,
						async : false,
						success : function(data) {
							var nameLegend = "内存使用量";
							var finalPoints = getSeriesPoints(data, nameLegend, "M");
							options.series.push(finalPoints);
						}
					});
					new Highcharts.Chart(options);
			 });
		}
		*/
		
	</script>
	<div id="containerMemory"
		style="min-width: 310px; height: 350px; margin: 0 auto"></div>
	
	
	<!-- 客户端连接数相关 -->
	<script type="text/javascript">
		//查询一天出每分钟数据
			$(document).ready(
				function() {
					var options = getOption("containerClients", "<b>客户端连接统计</b>", "个");
					var commandsUrl = "/admin/app/getMutiDatesAppStats.json?appId=" + appId + "&statName=connectedClient"+betweenParams;
					$.ajax({
						type : "get",
						url : commandsUrl,
						async : true,
						success : function(data) {
							var dates = new Array();
							dates.push(startDate); 
							dates.push(yesterDate);
							pushOptionSeries(options, data, dates, "客户端连接趋势图", "个");
							new Highcharts.Chart(options);
						}
					});
			 });
	</script>
	<div id="containerClients"
		style="min-width: 310px; height: 350px; margin: 0 auto"></div>
	
	
	<!-- bsize相关 -->
	<script type="text/javascript">
		//查询一天出每分钟数据
			$(document).ready(
				function() {
					var options = getOption("containerDbsize", "<b>键个数统计</b>", "个");
					var commandsUrl = "/admin/app/getMutiDatesAppStats.json?appId=" + appId + "&statName=objectSize"+betweenParams;
					$.ajax({
						type : "get",
						url : commandsUrl,
						async : true,
						success : function(data) {
							var dates = new Array();
							dates.push(startDate); 
							dates.push(yesterDate);
							pushOptionSeries(options, data, dates, "键个数趋势图", "个");
							new Highcharts.Chart(options);
						}
					});
			 });
	</script>
	<div id="containerDbsize"
		style="min-width: 310px; height: 350px; margin: 0 auto"></div>
	
	<br/>
	<br/>
	<br/>
</div>

<!-- 扩容申请 -->
<div id="appScaleApplyModal" class="modal fade" tabindex="-1" data-width="400">
	<div class="modal-dialog">
		<div class="modal-content">
		
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal" aria-hidden="true"></button>
				<h4 class="modal-title">申请扩容</h4>
			</div>
			
			<form class="form-horizontal form-bordered form-row-stripped">
				<div class="modal-body">
					<div class="row">
						<!-- 控件开始 -->
						<div class="col-md-12">
							<!-- form-body开始 -->
							<div class="form-body">
								
								<div class="form-group">
									<label class="control-label col-md-3">申请容量:</label>
									<div class="col-md-7">
										<input type="text" name="applyMemSize" id="applyMemSize" placeholder="申请扩容容量" class="form-control"/>
										<span class="help-block">例如填写: 512M,1G,2G..20G</span>
									</div>
								</div>
								
								<div class="form-group">
									<label class="control-label col-md-3">申请原因:</label>
									<div class="col-md-7">
										<textarea rows="5"  name="appScaleReason" id="appScaleReason" placeholder="申请扩容原因" class="form-control"></textarea>
									</div>
								</div>
							</div>
							<!-- form-body 结束 -->
						</div>
						<div id="appScaleApplyInfo"></div>
						<!-- 控件结束 -->
					</div>
				</div>
				
				<div class="modal-footer">
					<button type="button" data-dismiss="modal" class="btn" >Close</button>
					<button type="button" class="btn red" onclick="appScaleApply('${appDetail.appDesc.appId}')">Ok</button>
				</div>
			
			</form>
		</div>
	</div>
</div>

<div id="appConfigChangeModal" class="modal fade" tabindex="-1" data-width="400">
	<div class="modal-dialog">
		<div class="modal-content">
		
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal" aria-hidden="true"></button>
				<h4 class="modal-title">应用配置修改</h4>
			</div>
			
			<form class="form-horizontal form-bordered form-row-stripped">
				<div class="modal-body">
					<div class="row">
						<!-- 控件开始 -->
						<div class="col-md-12">
							<!-- form-body开始 -->
							<div class="form-body">
								
								<div class="form-group">
									<label class="control-label col-md-3">配置项:</label>
									<div class="col-md-8">
										<input type="text" name="appConfigKey" id="appConfigKey" placeholder="例如:maxclients" class="form-control" />
									</div>
								</div>
								
								<div class="form-group">
									<label class="control-label col-md-3">配置值:</label>
									<div class="col-md-8">
										<input type="text" name="appConfigValue" id="appConfigValue" placeholder="例如:15000" class="form-control">
									</div>
								</div>

                                <div class="form-group">
                                    <label class="control-label col-md-3">修改原因:</label>
                                    <div class="col-md-8">
                                        <textarea name="appConfigReason" id="appConfigReason" placeholder="例如：修改原因:1.需要更多的连接数。" class="form-control"></textarea>
                                        <%--<input type="text" name="appConfigReason" id="appConfigReason" placeholder="例如：修改原因:1.需要更多的连接数。" class="form-control">--%>
                                    </div>
                                </div>
								
							</div>
							<!-- form-body 结束 -->
						</div>
						<div id="appConfigChangeInfo"></div>
						<!-- 控件结束 -->
					</div>
				</div>
				
				<div class="modal-footer">
					<button type="button" data-dismiss="modal" class="btn" >Close</button>
					<button type="button" id="appConfigChangeBtn" class="btn red" onclick="appConfigChange('${appId}','${instanceId}')">Ok</button>
				</div>
			
			</form>
		</div>
	</div>
</div>


