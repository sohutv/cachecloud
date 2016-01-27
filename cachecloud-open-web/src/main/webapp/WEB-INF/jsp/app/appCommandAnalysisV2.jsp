<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<script type="text/javascript">
    var firstCommand = '${firstCommand}';
    var startDate = '${startDate}';
    var endDate = '${endDate}';
    var yesterDate = '${yesterDay}';
    var betweenOneDay = '${betweenOneDay}';
    var appId = '${appId}';
    var chartParams = "&startDate="+startDate+"&endDate="+endDate;
    var chartParamsCompare = "&startDate="+yesterDate+"&endDate="+startDate;
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

    function changeCommandChart(value){
        document.getElementById("firstCommand").value = value;
        document.getElementById("formSingCommand").submit();
    }

    var app_command_analysis_c_cu={
        ${firstCommand}:"/admin/app/getCommandStatsV2.do?appId=" + appId+ "&commandName=" + firstCommand + chartParams
    }

    var app_command_analysis_c_o={
        ${firstCommand}:{
        }
    }




</script>

<div class="container">
	<br/>
	<form method="get" action="/admin/app/index.do" id="formSingCommand">
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
					<input type="hidden" name="appId" value="${appDetail.appDesc.appId}">
					<input type="hidden" name="tabTag" value="app_command_analysis">
					<input type="hidden" id="firstCommand" name="firstCommand" value="${firstCommand}">
					<label>&nbsp;<input type="submit" class="btn-4" value="查询"/></label>
			</div>
		</div>
		<div class="row">
				<c:set var="needSelect" value="0"></c:set>
				&nbsp;&nbsp;Top5命令:
				<c:forEach items="${allCommands}" var="item" varStatus="stat">
					<c:choose>
						<c:when test="${stat.index < 5}">
								<input type="radio" name="optionsRadios" value="${item.commandName}" 
								<c:if test="${firstCommand == item.commandName}">checked="checked"</c:if>
								 onchange="changeCommandChart(this.value)" />
								${item.commandName}
						</c:when>
						<c:otherwise>
							<c:set var="needSelect" value="1"></c:set>
						</c:otherwise>
					</c:choose>
				</c:forEach>
				<c:if test="${needSelect == 1}">
					&nbsp;&nbsp;&nbsp;其余命令:
					<select name="optionsRadios" onchange="changeCommandChart(this.value)">
						<c:forEach items="${allCommands}" var="item" varStatus="stat">
							<c:choose>
								<c:when test="${stat.index >= 5}">
									<label>  
										<option value="${item.commandName}" <c:if test="${firstCommand == item.commandName}">selected</c:if>>
							         		${item.commandName}
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
		var title = "<b>"+firstCommand+"命令</b>";
		if(betweenOneDay == 1){
			$(document).ready(
				function() {
					var options = getOption("containerSingleCommand", title, "次数");
					var commandsUrl = "/admin/app/getCommandStats.do?appId=" + appId+ "&commandName=" + firstCommand + chartParams;
					$.ajax({
						type : "get",
						url : commandsUrl,
						async : false,
						success : function(data) {
							var nameLegend = firstCommand + "命令趋势图(" + startDate + ")";
							var finalPoints = getSeriesPoints(data, nameLegend);
							options.series.push(finalPoints);
						}
					});
					
					//比较
					var commandsUrlCompare = "/admin/app/getCommandStats.do?appId=" + appId + "&commandName=" + firstCommand + chartParamsCompare + "&addDay=1";
					$.ajax({
						type : "get",
						url : commandsUrlCompare,
						async : false,
						success : function(data) {
							var nameLegend = firstCommand + "命令趋势图(" + yesterDate + ")";
							var finalPoints = getSeriesPoints(data, nameLegend);
							options.series.push(finalPoints);
						}
					});
					new Highcharts.Chart(options);
			 });
		}else{
			$(document).ready(
				function() {
					var options = getOption("containerSingleCommand", title, "次数");
					var commandsUrl = "/admin/app/getCommandStats.do?appId=" + appId + "&commandName=" + firstCommand + chartParams;
					$.ajax({
						type : "get",
						url : commandsUrl,
						async : false,
						success : function(data) {
							var nameLegend = firstCommand + "命令趋势图";
							var finalPoints = getSeriesPoints(data, nameLegend);
							options.series.push(finalPoints);
						}
					});
					new Highcharts.Chart(options);
			 });
		}
	</script>
	
	<div id="containerSingleCommand"
		style="min-width: 310px; height: 350px; margin: 0 auto"></div>
    <div id="${firstCommand}" class="page-body">
    </div>

</div>
