<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp"%>


<script type="text/javascript">

function calDateWidgetDifMs(start, end) {
	var startTime = start.replace(/-/g, "/"); 
	var endTime = end.replace(/-/g,"/");
	var startDate = new Date(Date.parse(startTime)).getTime();
	var endDate = new Date(Date.parse(endTime)).getTime();
	//毫秒差
	return (endDate - startDate);
}

function search() {
    var slowLogStartDate = document.getElementById("slowLogStartDate").value;
    var slowLogEndDate = document.getElementById("slowLogEndDate").value;
	var difTime = calDateWidgetDifMs(slowLogStartDate, slowLogEndDate);
	if (difTime > 86400000 * 30) {
		alert("日期跨度最大为一个月，请重新选择!");
	} else {
		document.getElementById("appSlowLogForm").submit();
	}
}
</script>
<div class="container">
	<br/>
    <form method="get" action="/admin/app/index.do" id="appSlowLogForm">
		<div class="row">
			<div style="float:right">
					<label style="font-weight:bold;text-align:left;">
					 	&nbsp;开始日期:&nbsp;&nbsp;
					</label>
					<input type="text" size="21" name="slowLogStartDate" id="slowLogStartDate" value="${slowLogStartDate}" onFocus="WdatePicker({startDate:'%y-%M-01',dateFmt:'yyyy-MM-dd',alwaysUseStartDate:true})"/>
					
					<label style="font-weight:bold;text-align:left;">
					 	结束日期:
					</label>
					<input type="text" size="20" name="slowLogEndDate" id="slowLogEndDate" value="${slowLogEndDate}" onFocus="WdatePicker({startDate:'%y-%M-01',dateFmt:'yyyy-MM-dd',alwaysUseStartDate:true})"/>
					
					<input type="hidden" name="appId" value="${appDesc.appId}">
					<input type="hidden" name="tabTag" value="app_slow_log">
					<label>&nbsp;<input type="button" class="btn-4" value="查询" onclick="search()"/></label>
			</div>
		</div>
	</form>
	
	<div class="row">
        <div class="col-md-12">
			<div class="page-header">
                <h4>
                    <label class="label label-success">一共${fn:length(appInstanceSlowLogList)}次慢查询</label>
                </h4>
            </div>

            <table class="table table-striped table-hover">
                <thead>
                <tr>
                    <td>序号</td>
                    <td>实例信息</td>
                    <td>个数</td>
                </tr>
                </thead>
                <tbody>
	                <c:forEach items="${appInstanceSlowLogCountMap}" var="item" varStatus="stats">
	                    <tr>
	                        <td>${stats.index + 1}</td>
	                        <td>
	                            <a href="#${item.key}">${item.key}</a>
	                        </td>
	                        <td>${item.value}</td>
	                    </tr>
	                </c:forEach>
                </tbody>
            </table>
        </div>
    </div>
    
    <c:forEach items="${instaceSlowLogMap}" var="item" varStatus="stats">
	    <div style="margin-top: 20px">
	    	<div class="page-header" id="${item.key}">
                <h4>${item.key}</h4>
            </div>
	        <table class="table table-bordered table-striped table-hover">
	            <thead>
		            <tr>
		                <td>实例</td>
		                <td>ip</td>
		                <td>port</td>
		                <td>慢查询id</td>
		                <td>耗时(单位:微秒)</td>
		                <td>命令</td>
		                <td>发生时间</td>
		            </tr>
	            </thead>
	            <tbody>
		            <c:forEach var="slowLog" items="${item.value}" varStatus="status">
		                <tr>
		                  <td>
		                  	<a href="/admin/instance/index.do?instanceId=${slowLog.instanceId}" target="_blank">${slowLog.instanceId}</a>
		                  </td>
		                  <td>${slowLog.ip}</td>
		                  <td>${slowLog.port}</td>
		                  <td>${slowLog.slowLogId}</td>
		                  <td><fmt:formatNumber value="${slowLog.costTime}" pattern="#,#00"/></td>
		                  <td>${slowLog.command}</td>
		                  <td>${slowLog.executeTime}</td>
		                </tr>
		            </c:forEach>
	            </tbody>
	        </table>
	    </div>	
    </c:forEach>
    
</div>
