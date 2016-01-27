<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp"%>
<jsp:include page="/WEB-INF/include/head.jsp"/>
<script type="text/javascript" src="/resources/js/jquery-console.js"></script>
<script type="text/javascript" src="/resources/js/chart.js"></script>
<script type="text/javascript" src="/resources/js/appClient.js"></script>

<script type="text/javascript">
var startDate = '${startDate}';
var endDate = '${endDate}';
var appId = '${appId}'; 
var chartParams = "&startDate="+startDate+"&endDate="+endDate;
Highcharts.setOptions({
	global : {
		useUTC : false
	}
});
Highcharts.setOptions({
	colors : ['#2f7ed8', '#E3170D', '#0d233a', '#8bbc21', '#1aadce',
			'#492970', '#804000', '#f28f43', '#77a1e5',
			'#c42525', '#a6c96a']
});

function search() {
    var startDate = document.getElementById("startDate").value;
    var endDate = document.getElementById("endDate").value;
	var difTime = calDateWidgetDifMs(startDate, endDate);
	var oneDayTime = 86400000;
	if (difTime > oneDayTime) {
		alert("由于数据量较大,耗时查询暂不支持跨天查询!");
	} else {
		document.getElementById("appInstanceNetStatForm").submit();
	}
}

</script>

<div class="container">
	<jsp:include page="/WEB-INF/include/headMenu.jsp"/>
	<div class="page-header">
		<h4>
			应用(<a target="_blank" href="/admin/app/index.do?appId=${appDesc.appId}">${appDesc.name}</a>)实例流量统计
		</h4>
	</div>
	<br/>
	<form method="get" action="/admin/app/appInstanceNetStat.do" id="appInstanceNetStatForm">
		<div class="row">
			<div style="float:right">
					<label style="font-weight:bold;text-align:left;">
					 	开始日期:&nbsp;&nbsp;
					</label>
					<input type="text" size="21" name="startDate" id="startDate" value="${startDate}" onFocus="WdatePicker({startDate:'%y-%M-01',dateFmt:'yyyy-MM-dd',alwaysUseStartDate:true})"/>
					<label style="font-weight:bold;text-align:left;">
					 	结束日期:
					</label>
					<input type="text" size="20" name="endDate" id="endDate" value="${endDate}" onFocus="WdatePicker({startDate:'%y-%M-01',dateFmt:'yyyy-MM-dd',alwaysUseStartDate:true})"/>
					<input type="hidden" name="appId" value="${appId}">
					<label>&nbsp;<input type="button" class="btn-4" value="查询" onclick="search()"/></label>
			</div>
		</div>
	</form>
	<script type="text/javascript">
		
		function genDetailContainer(i) {
			var mainContainer = document.getElementById("allInstanceContainers");
			var divNode = document.createElement("div");
			divNode.setAttribute('id', "appInstanceNetContainer" + i);
			divNode.setAttribute("style","min-width: 550px; height: 350px; margin: 0 auto;");
			mainContainer.appendChild(divNode);
		}
		
		function fillDetailTable(instanceInfo, index) {
		    var tb =document.getElementById('instanceNetDetailTable');
		    var newTr = tb.insertRow(-1);
		    newTr.align='center';
		    var indexTd = newTr.insertCell();
		    var instanceTd = newTr.insertCell();
		    indexTd.innerHTML = (index+1);
		    instanceTd.innerHTML = instanceInfo;
		 }

		$(document).ready(
				function() {
					var url = "/admin/app/getAppInstancesNetStat.json?appId=" + appId + chartParams;
					$.ajax({
						type : "get",
						url : url,
						async : false,
						success : function(data) {
							var dataArr = eval("(" + data + ")");
							var length = dataArr.length;
							for (var i = 0; i < length; i++) {
								var instance = dataArr[i];
								var instanceNetStatMapList = instance.instanceNetStatMapList;
								if(instanceNetStatMapList.length == 0) {
									continue;
								}
								genDetailContainer(i);
								var title = "<b>实例("+instance.instanceInfo+")网络流量</b>";
								var options = getOption("appInstanceNetContainer" + i, title,"次数");
								var inPoints = getInstanceNetPoints(instance, "net_input", "i");
								
								//统一流量单位
								options.yAxis.title.text = inPoints.unitTxt;
								var unit = inPoints.unit;
								
								var outPoints = getInstanceNetPoints(instance, "net_output", "o", unit);
								options.series.push(inPoints);
								options.series.push(outPoints);
								new Highcharts.Chart(options);
							}
						}
					});
				});
	</script>
	
	
	
	<div id="allInstanceContainers">
		
	</div>
	
</div>
