<%@ page contentType="text/html;charset=UTF-8" language="java"  %>
<%@ include file="/WEB-INF/jsp/manage/commons/appConstants.jsp"%>

<!DOCTYPE html>
<html lang="en">
<head>
    <title>键值分析</title>
    <jsp:include page="/WEB-INF/include/head.jsp"/>
</head>
<body role="document">
<div class="container">
    <jsp:include page="/WEB-INF/include/headMenu.jsp"/>
    
	<div class="row">
    		<div class="page-header">
				<h4>集群键值分析</h4>
			</div>
    		<div class="col-md-6">
				<div id="idleKeyDistriContainer"
					style="min-width: 310px; height: 400px; margin: 0 auto"></div>
			</div>
			<div class="col-md-6">
				<div id="keyTtlDistriContainer"
					style="min-width: 310px; height: 400px; margin: 0 auto"></div>
			</div>
			<div class="col-md-6">
				<div id="keyTypeDistriContainer"
					style="min-width: 310px; height: 400px; margin: 0 auto"></div>
			</div>
			<div class="col-md-6">
				<div id="keyValueSizeDistriContainer"
					style="min-width: 310px; height: 400px; margin: 0 auto"></div>
			</div>
    </div>
	
    
    
    
    <div class="row">
        <div class="col-md-12">
            <div class="page-header">
                <h4>键值类型分布</h4>
            </div>
		</div>
	</div>
	<div class="row">
		<div class="col-md-12">
            <table class="table table-striped table-hover" style="margin-top: 0px">
                <thead>
	                <tr>
	                    <td>类型</td>
	                    <td>个数</td>
	                </tr>
                </thead>
                <tbody>
                	<c:forEach items="${keyTypeParamCountList}" var="keyTypeParamCount">
                		 <tr>
		                    <td>${keyTypeParamCount.param}</td>
                            <td>${keyTypeParamCount.count}</td>
		                </tr>
                	</c:forEach>
                </tbody>
            </table>
        </div>
    </div>
    
      <div class="row">
        <div class="col-md-12">
            <div class="page-header">
                <h4>键值过期分布</h4>
            </div>
		</div>
	</div>
	<div class="row">
		<div class="col-md-12">
            <table class="table table-striped table-hover" style="margin-top: 0px">
                <thead>
	                <tr>
	                    <td>分布</td>
	                    <td>个数</td>
	                </tr>
                </thead>
                <tbody>
                	<c:forEach items="${keyTtlParamCountList}" var="ttlKeyParamCount">
                		 <tr>
		                    <td>${ttlKeyParamCount.param}</td>
                            <td>${ttlKeyParamCount.count}</td>
		                </tr>
                	</c:forEach>
                </tbody>
            </table>
        </div>
    </div>
    
    <div class="row">
        <div class="col-md-12">
            <div class="page-header">
                <h4>键值空闲分布</h4>
            </div>
		</div>
	</div>
	<div class="row">
		<div class="col-md-12">
            <table class="table table-striped table-hover" style="margin-top: 0px">
                <thead>
	                <tr>
	                    <td>分布</td>
	                    <td>个数</td>
	                </tr>
                </thead>
                <tbody>
                	<c:forEach items="${idleKeyParamCountList}" var="idleKeyParamCount">
                		 <tr>
		                    <td>${idleKeyParamCount.param}</td>
                            <td>${idleKeyParamCount.count}</td>
		                </tr>
                	</c:forEach>
                </tbody>
            </table>
        </div>
    </div>
    
    <div class="row">
        <div class="col-md-12">
            <div class="page-header">
                <h4>键值值分布</h4>
            </div>
		</div>
	</div>
	<div class="row">
		<div class="col-md-12">
            <table class="table table-striped table-hover" style="margin-top: 0px">
                <thead>
	                <tr>
	                    <td>分布</td>
	                    <td>个数</td>
	                </tr>
                </thead>
                <tbody>
                	<c:forEach items="${keyValueSizeParamCountList}" var="keyValueSizeParamCount">
                		 <tr>
		                    <td>${keyValueSizeParamCount.param}</td>
                            <td>${keyValueSizeParamCount.count}</td>
		                </tr>
                	</c:forEach>
                </tbody>
            </table>
        </div>
    </div>
    
    <div class="row">
        <div class="col-md-12">
            <div class="page-header">
                <h4>BigKey列表(共${instanceBigKeyCount}个)</h4>
            </div>
		</div>
	</div>
	<div class="row">
		<div class="col-md-12">
            <table class="table table-striped table-hover" style="margin-top: 0px">
                <thead>
	                <tr>
	                    <td>实例信息</td>
                    	<td>键名称</td>
	                    <td>类型</td>
	                    <td>长度</td>
	                    <td>创建时间</td>
	                </tr>
                </thead>
                <tbody>
                	<c:forEach items="${instanceBigKeyList}" var="instanceBigKey">
                		 <tr>
		                    <td>${instanceBigKey.ip}:${instanceBigKey.port}</td>
                            <td>${instanceBigKey.bigKey}</td>
                            <td>${instanceBigKey.type}</td>
                            <td>${instanceBigKey.length}</td>
                            <td>
                            	<fmt:formatDate pattern="yyyy-MM-dd HH:mm:ss" value="${instanceBigKey.createTime}"/>
                            </td>
		                </tr>
                	</c:forEach>
                </tbody>
            </table>
        </div>
    </div>
    
</div>

<jsp:include page="/WEB-INF/include/foot.jsp"/>

<script type="text/javascript">
	generateDataPieChart('idleKeyDistriContainer', 'key闲置分布', ${idleKeyDistri});
	generateDataPieChart('keyTtlDistriContainer', 'key过期分布', ${keyTtlDistri});
	generateDataPieChart('keyTypeDistriContainer', 'key类型分布', ${keyTypeDistri});
	generateDataPieChart('keyValueSizeDistriContainer', 'key值分布', ${keyValueSizeDistri});
	
</script>

<script type="text/javascript" src="/resources/js/mem-cloud.js"></script>

</body>
</html>
