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
    document.getElementById("appInstanceCpuStatForm").submit();
}

</script>

<div class="container">
	<jsp:include page="/WEB-INF/include/headMenu.jsp"/>
	<div class="page-header">
		<h4>
			应用(<a target="_blank" href="/admin/app/index?appId=${appDesc.appId}">${appDesc.name}</a>)实例CPU消耗统计
		</h4>
	</div>
	<br/>
	<form method="get" action="/admin/app/appInstanceCpuStat" id="appInstanceCpuStatForm">
		<div class="row">
			<div style="float:right">
					<label style="font-weight:bold;text-align:left;">
					 	日期:&nbsp;&nbsp;
					</label>
					<input type="text" size="21" name="startDate" id="startDate" value="${startDate}" onFocus="WdatePicker({startDate:'%y-%M-01',dateFmt:'yyyy-MM-dd',alwaysUseStartDate:true})"/>
					<input type="hidden" name="appId" value="${appId}">
					<label>&nbsp;<input type="button" class="btn-4" value="查询" onclick="search()"/></label>
			</div>
		</div>
	</form>
	<script type="text/javascript">
		
		function genDetailContainer(i) {
			var mainContainer = document.getElementById("allInstanceContainers");
			var divNode = document.createElement("div");
			divNode.setAttribute('id', "appInstanceCpuContainer" + i);
			divNode.setAttribute("style","min-width: 550px; height: 350px; margin: 0 auto;");
			mainContainer.appendChild(divNode);
		}
		
		function fillDetailTable(instanceInfo, index) {
		    var tb =document.getElementById('instanceCpuDetailTable');
		    var newTr = tb.insertRow(-1);
		    newTr.align='center';
		    var indexTd = newTr.insertCell();
		    var instanceTd = newTr.insertCell();
		    indexTd.innerHTML = (index+1);
		    instanceTd.innerHTML = instanceInfo;
		 }

		$(document).ready(
				function() {
					var url = "/admin/app/getAppInstancesCpuStat.json?appId=" + appId + chartParams;
					$.ajax({
						type : "get",
						url : url,
						async : true,
						success : function(data) {
							var dataArr = eval("(" + data + ")");
							var length = dataArr.length;
							for (var i = 0; i < length; i++) {
								var instance = dataArr[i];
								var instanceCpuStatMapList = instance.instanceCpuStatMapList;
								if(instanceCpuStatMapList.length == 0) {
									continue;
								}
								genDetailContainer(i);
								var title = "<b>实例("+instance.instanceInfo+")CPU消耗流量</b>";
								var options = getOption("appInstanceCpuContainer" + i, title,"次数");

								var csPoints = getInstanceCpuPoints(instance, "cpu_sys", "cs");
								options.yAxis.title.text = csPoints.unitTxt;
								var cuPoints = getInstanceCpuPoints(instance, "cpu_user", "cu");
								var csChildPoints = getInstanceCpuPoints(instance, "cpu_sys_children", "cs_child");
								var cuChildPoints = getInstanceCpuPoints(instance, "cpu_user_children", "cu_child");
								var cpuBasePoints = getInstanceBaseCpuPoints(instance, "cpu_warning", 0.3);

								options.series.push(csPoints);
								options.series.push(cuPoints);
								options.series.push(csChildPoints);
								options.series.push(cuChildPoints);
								options.series.push(cpuBasePoints);

								new Highcharts.Chart(options);
							}
						}
					});
				});
	</script>
	
	
	
	<div id="allInstanceContainers">
		
	</div>
	
</div>
