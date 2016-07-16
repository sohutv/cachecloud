<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp"%>

<style>
#clientInstanceDetailModal .modal-dialog
{
    width: 1000px; /* your width */
}
</style>

<script type="text/javascript">

function search() {
    var costDistriStartDate = document.getElementById("costDistriStartDate").value;
    var costDistriEndDate = document.getElementById("costDistriEndDate").value;
	var difTime = calDateWidgetDifMs(costDistriStartDate, costDistriEndDate);
	var oneDayTime = 86400000;
	if (difTime > oneDayTime) {
		alert("由于数据量较大,耗时查询暂不支持跨天查询!");
	} else {
		document.getElementById("clientCostForm").submit();
	}
}
</script>

<div class="container" id="mainClientCostContainer">
	<br/>
	<form method="get" action="/client/show/index.do" id="clientCostForm">
		<div class="row">
			<div style="float:right">
					<label style="font-weight:bold;text-align:left;">
					 	&nbsp;开始日期:&nbsp;&nbsp;
					</label>
					<input type="text" size="21" name="costDistriStartDate" id="costDistriStartDate" value="${costDistriStartDate}" onFocus="WdatePicker({startDate:'%y-%M-01',dateFmt:'yyyy-MM-dd',alwaysUseStartDate:true})"/>
					
					<label style="font-weight:bold;text-align:left;">
					 	结束日期:
					</label>
					<input type="text" size="20" name="costDistriEndDate" id="costDistriEndDate" value="${costDistriEndDate}" onFocus="WdatePicker({startDate:'%y-%M-01',dateFmt:'yyyy-MM-dd',alwaysUseStartDate:true})"/>
					
					<input type="hidden" name="appId" value="${appDesc.appId}">
					<input type="hidden" name="tabTag" value="app_client_cost_distribute">
					<input type="hidden" id="firstCommand" name="firstCommand" value="${firstCommand}">
					<label>&nbsp;<input type="button" class="btn-4" value="查询" onclick="search()"/></label>
			</div>
		</div>
		<div class="row">
				<c:set var="needSelect" value="0"></c:set>
				&nbsp;&nbsp;Top5命令:
				<c:forEach items="${allCommands}" var="item" varStatus="stat">
					<c:choose>
						<c:when test="${stat.index < 5}">
								<input type="radio" name="optionsRadios" value="${item}" 
								<c:if test="${firstCommand == item}">checked="checked"</c:if>
								 onchange="changeCommandChart(this.value)" />
								${item}
						</c:when>
						<c:otherwise>
							<c:set var="needSelect" value="1"></c:set>
						</c:otherwise>
					</c:choose>
				</c:forEach>
				<c:if test="${needSelect == 1}">
					&nbsp;&nbsp;&nbsp;其余命令:
					<select name="optionsRadios" onchange="changeCommandChart(this.value)">
						<option>请选择</option>
						<c:forEach items="${allCommands}" var="item" varStatus="stat">
							<c:choose>
								<c:when test="${stat.index >= 5}">
									<label>
										<option value="${item}" <c:if test="${firstCommand == item}">selected</c:if>>
							         		${item}
							        	</option>
									</label>							
								</c:when>
							</c:choose>
						</c:forEach>
					</select>
				</c:if>
		</div>
	</form>
	<script type="text/javascript">
		var firstCommand = '${firstCommand}';
		var costDistriStartDate = '${costDistriStartDate}';
		var costDistriEndDate = '${costDistriEndDate}';
		var appId = '${appId}'; 
		var chartParams = "&costDistriStartDate="+costDistriStartDate+"&costDistriEndDate="+costDistriEndDate+"&firstCommand="+firstCommand;
		//应用下客户端和实例的全局耗时
		var appChartStatListJson = '${appChartStatListJson}';
		
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
		function changeCommandChart(value){
			document.getElementById("firstCommand").value = value;
			document.getElementById("clientCostForm").submit();
		}
		$(document).ready(
			function() {
				var unit = "毫秒";
				var dataArr = eval("("+appChartStatListJson+")");
				var appTotalOptions = getOption("clientCostContainer", "<b>"+firstCommand+"命令-客户端耗时统计</b>", unit);
				var appTotalData = dataArr.app;
				var appTotalTags = new Array("mean", "平均值", "median", "中位值","max90","90%最大值","max99", "99%最大值", "max100", "最大值");
				//appTotalOptions.plotOptions.series.animation = false;
				appTotalOptions.series = getClientCostSeriesPoints(1, appTotalData, appTotalTags, unit);
				var appTotalchart = new Highcharts.Chart(appTotalOptions);
				if (appTotalchart.series.length >= 5) {
					appTotalchart.series[1].hide();
					appTotalchart.series[2].hide();
					appTotalchart.series[3].hide();
				}
		 });
		
		function showDetailChart(instanceHost, instancePort, instanceId, clientIp){
			var unit = "毫秒";
			var url = "/client/show/getAppClientInstanceCommandCost?appId=" + appId + "&instanceId=" + instanceId + "&clientIp=" + clientIp + chartParams;
			$.ajax({
				type : "get",
				url : url,
				async : false,
				success : function(data) {
					var title = "(" + clientIp + "--" + instanceHost + ":" + instancePort + ")";
					var dataArr = eval("("+data+")");
					var clientInstanceOptions = getOption("detailContainer", "<b>"+firstCommand+"命令-客户端实例耗时统计"+title+"</b>", unit);
					var clientInstanceData = dataArr.clientInstanceStat;
					var clientInstanceTags = new Array("mean", "平均值", "median", "中位值","max90","90%最大值","max99", "99%最大值", "max100", "最大值");
					//clientInstanceOptions.plotOptions.series.animation = false;
					clientInstanceOptions.series = getClientCostSeriesPoints(0, clientInstanceData, clientInstanceTags, unit);
					var clientInstancechart = new Highcharts.Chart(clientInstanceOptions);
					if (clientInstancechart.series.length >= 5) {
						clientInstancechart.series[1].hide();
						clientInstancechart.series[2].hide();
						clientInstancechart.series[4].hide();
					}
					//标题
					document.getElementById("detailInstanceAndClient").innerHTML = title;
				}
			});
		}
		
		
	</script>
	<div class="page-header">
		<h4>
			应用耗时&nbsp;&nbsp;&nbsp;
		</h4>
	</div>
	<div id="clientCostContainer"
		style="min-width: 310px; height: 350px; margin: 0 auto"></div>
	<br/>
	<div class="page-header">
		<h4>
			(客户端--redis实例)关系表
		</h4>
	</div>
	<table id="clientInstanceDetailTable" class="table table-striped table-hover table-bordered" style="margin-top: 0px">
 		<thead>
			<tr>
				<td>序号</td>
				<td>客户端</td>
				<td>redis实例</td>
				<td>图表</td>
			</tr>
		</thead>
		<tbody>
			<c:forEach items="${appInstanceClientRelationList}" var="item" varStatus="stat">
				<tr>
					<td>${stat.index}</td>
					<td>${item.clientIp}</td>
					<td>${item.instanceHost}:${item.instancePort}</td>
					<td>
						<button type='button' class='btn btn-small btn-success' onclick="showDetailChart('${item.instanceHost}', '${item.instancePort}', '${item.instanceId}', '${item.clientIp}')" data-target='#clientInstanceDetailModal' data-toggle='modal'>chart</button>
					</td>
				</tr>
			</c:forEach>
		</tbody>
	</table>
	
	<br/><br/><br/><br/><br/><br/><br/><br/>
	
	
</div>

<div id="clientInstanceDetailModal" class="modal fade" tabindex="-1" data-width="1200">
	<div class="modal-dialog">
		<div class="modal-content">
				<div class="modal-header">
					<button type="button" class="close" data-dismiss="modal" aria-hidden="true"></button>
					<h4 class="modal-title">
					客户端-redis实例耗时
						<span id="detailInstanceAndClient"></span>
					</h4>
				</div>
				<div class="modal-body">
					<div class="row">
						<div class="col-md-12">
							<div id="detailContainer" style="width: 700px; height: 350px; margin: 0 auto"></div>
						</div>
					</div>
				</div>
		</div>
	</div>
</div>


