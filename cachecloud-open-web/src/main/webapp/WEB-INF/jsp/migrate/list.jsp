<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp"%>

<!DOCTYPE html>
<html lang="en">
<head>
    <title>迁移数据记录列表</title>
    <jsp:include page="/WEB-INF/include/head.jsp"/>
    <script type="text/javascript">
    
    </script>
    
</head>
<body role="document">
	<div class="container">
	    <jsp:include page="/WEB-INF/include/headMenu.jsp"/>
	    <div id="systemAlert">
	    </div>
	    
	    <div class="row">
	        <div class="col-md-12">
	            <div class="page-header">
	                <h4>迁移数据记录列表</h4>
	            </div>
			</div>
		</div>
		<div class="row">
			<br/>
			<div class="col-md-12">
	            <table class="table table-striped table-hover" style="margin-top: 0px">
	                <thead>
		                <tr>
		                    <td>id</td>
		                    <td>迁移工具</td>
		                    <td>源实例列表</td>
		                    <td>目标实例列表</td>
		                    <td>源appId</td>
		                    <td>目标appId</td>
		                    <td>操作人id</td>
		                    <td>状态</td>
		                    <td>开始时间</td>
		                    <td>结束时间</td>
		                    <td>查看</td>
		                    <td>操作</td>
		                </tr>
	                </thead>
	                <tbody>
	                	<c:forEach items="${appDataMigrateStatusList}" var="appDataMigrateStatus">
	                		 <tr>
			                    <td>${appDataMigrateStatus.id}</td>
			                    <td>${appDataMigrateStatus.migrateMachineIp}:${appDataMigrateStatus.migrateMachinePort}</td>
			                    <td>${appDataMigrateStatus.sourceServers}<br/>${appDataMigrateStatus.sourceMigrateTypeDesc}</td>
			                    <td>${appDataMigrateStatus.targetServers}<br/>${appDataMigrateStatus.targetMigrateTypeDesc}</td>
			                    <td>
			                    	<c:choose>
			                    		<c:when test="${appDataMigrateStatus.sourceAppId <= 0}">
			                    			非cachecloud
			                    		</c:when>
			                    		<c:otherwise>
			                    			${appDataMigrateStatus.sourceAppId}
			                    		</c:otherwise>
			                    	</c:choose>
			                    </td>
			                    <td>
			                    	<c:choose>
			                    		<c:when test="${appDataMigrateStatus.targetAppId <= 0}">
			                    			非cachecloud
			                    		</c:when>
			                    		<c:otherwise>
			                    			${appDataMigrateStatus.targetAppId}
			                    		</c:otherwise>
			                    	</c:choose>
			                    </td>
			                    <td>${appDataMigrateStatus.userId}</td>
			                    <td>${appDataMigrateStatus.statusDesc}</td>
   			                    <td>${appDataMigrateStatus.startTimeFormat}</td>
   			                    <td>${appDataMigrateStatus.endTimeFormat}</td>
   			                    <td>
   			                        <a target="_blank" href="/data/migrate/log?id=${appDataMigrateStatus.id}">日志|</a>
   			                        <a target="_blank" href="/data/migrate/config?id=${appDataMigrateStatus.id}">配置|</a>
   			                        <a target="_blank" href="">进度</a>
   			                    </td>
   			                    <td>
	                                <button type="button" class="btn btn-small btn-primary">停止</button>               
   			                    </td>
			                </tr>
	                	</c:forEach>
	                </tbody>
	            </table>
            </div>
        </div>
	    
	    
	</div>
	<br/><br/><br/><br/><br/><br/><br/>
	<jsp:include page="/WEB-INF/include/foot.jsp"/>
</body>
</html>

