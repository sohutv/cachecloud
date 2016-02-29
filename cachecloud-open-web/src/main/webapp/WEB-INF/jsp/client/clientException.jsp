<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp"%>

<script type="text/javascript">
function search() {
    var exceptionStartDate = document.getElementById("exceptionStartDate").value;
    var exceptionEndDate = document.getElementById("exceptionEndDate").value;
	var difTime = calDateWidgetDifMs(exceptionStartDate, exceptionEndDate);
	if (difTime > 86400000 * 7) {
		alert("日期跨度最大为一个星期，请重新选择!");
	} else {
		document.getElementById("searchExceptionForm").submit();
	}
}
</script>

<div class="container">
	<div class="row">
		<div class="col-md-12"></div>
	</div>
    <div class="row">
        <div class="col-md-12">
        	<form method="get" action="/client/show/index.do" id="searchExceptionForm">
				<div style="float:right">
						<label style="font-weight:bold;text-align:left;">
						 	客户端ip:
						</label>
						<input type="text" value="${clientIp}" name="clientIp" size="12">
						
						<label style="font-weight:bold;text-align:left;">
						 	&nbsp;异常类型:
						</label>
						<select name="type" style="height:26px">
							<option value="">全部</option>
							<option <c:if test="${type == 1}">selected="selected"</c:if> value="1">redis异常</option>
							<option <c:if test="${type == 2}">selected="selected"</c:if> value="2">客户端异常</option>
							<c:if test="${appDesc.type == 2}">
								<option <c:if test="${type == 3}">selected="selected"</c:if> value="3">Redis-Cluster异常</option>
							</c:if>
						</select>
				
						<label style="font-weight:bold;text-align:left;">
						 	&nbsp;开始日期:&nbsp;&nbsp;
						</label>
						<input type="text" size="21" name="exceptionStartDate" id="exceptionStartDate" value="${exceptionStartDate}" onFocus="WdatePicker({startDate:'%y-%M-01',dateFmt:'yyyy-MM-dd',alwaysUseStartDate:true})"/>
						
						<label style="font-weight:bold;text-align:left;">
						 	结束日期:
						</label>
						<input type="text" size="20" name="exceptionEndDate" id="exceptionEndDate" value="${exceptionEndDate}" onFocus="WdatePicker({startDate:'%y-%M-01',dateFmt:'yyyy-MM-dd',alwaysUseStartDate:true})"/>
						
						<input type="hidden" name="appId" value="${appDesc.appId}">
						<input type="hidden" name="tabTag" value="app_client_exception">
						<input type="hidden" name="pageNo" id="pageNo">
						<label>&nbsp;<input type="button" class="btn-4" value="查询" onclick="search()"/></label>
				</div>
			</form>
        </div>
    </div>
    
    <div class="row">
		<div style="margin-bottom: 10px;float: right;margin-right: 15px">
			<ul id='ccPagenitor' style="margin-bottom: 0px"></ul>
			<div id="pageDetail" style="float:right;padding-top:28px;padding-left:8px;color:#4A64A4;display: none">共${page.totalPages}页,${page.totalCount}条</div>		
		</div>
        <div class="col-md-12">
        	<table class="table table-striped table-hover table-bordered" style="margin-top: 0px">
	   			<thead>
					<tr>
						<td style="text-align:center">id</td>
						<td style="text-align:center">异常类型</td>
						<td style="text-align:center">收集时间</td>
						<td style="text-align:center">客户端ip</td>
						<td style="text-align:center">异常类</td>
						<td style="text-align:center">次数</td>
						<td style="text-align:center">实例地址</td>
					</tr>
				</thead>
				<tbody>
					<c:forEach items="${appClientExceptionList}" var="appClientException">
						<tr>
							<td style="text-align:center">${appClientException.id}</td>
							<td style="text-align:center">
								<c:choose>
									<c:when test="${appClientException.type == 1}">redis异常</c:when>
									<c:when test="${appClientException.type == 2}">客户端异常</c:when>
									<c:when test="${appClientException.type == 3}">redis-cluster异常</c:when>
								</c:choose>
							</td>
							<td style="text-align:center">${appClientException.collectTimeFormat}</td>
							<td style="text-align:center">${appClientException.clientIp}</td>
							<td style="text-align:center">${appClientException.exceptionClass}</td>
							<td style="text-align:center">${appClientException.exceptionCount}</td>
							<td style="text-align:center">
								<c:choose>
									<c:when test="${appClientException.type == 1}">
										${appClientException.instanceHost}:${appClientException.instancePort}
									</c:when>
									<c:when test="${appClientException.type == 2}">无</c:when>
									<c:when test="${appClientException.type == 3}">无</c:when>
								</c:choose>
							</td>
						</tr>
					</c:forEach>
				</tbody>
			</table>
        </div>
    </div>      		
</div>
<script src="/resources/bootstrap/paginator/bootstrap-paginator.js"></script>
<script src="/resources/bootstrap/paginator/custom-pagenitor.js"></script>
<script type="text/javascript">
    $(function(){
    	//分页点击函数
    	var pageClickedFunc = function (e, originalEvent, type, page){
    		//form传参用
    		document.getElementById("pageNo").value=page;
    		document.getElementById("searchExceptionForm").submit();
    	};
    	//分页组件
        var element = $('#ccPagenitor');
        //当前page号码
        var pageNo = '${page.pageNo}';
        //总页数
        var totalPages = '${page.totalPages}';
        //显示总页数
        var numberOfPages = '${page.numberOfPages}';
		var options = generatePagenitorOption(pageNo, numberOfPages, totalPages, pageClickedFunc);
		if(totalPages > 0){
			element.bootstrapPaginator(options);
			document.getElementById("pageDetail").style.display = "";
		}else{
			element.html("未查询到相关记录！");
		}
    });
</script>





