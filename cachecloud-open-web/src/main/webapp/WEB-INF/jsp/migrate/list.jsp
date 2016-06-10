<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp"%>

<!DOCTYPE html>
<html lang="en">
<head>
    <title>迁移数据记录列表</title>
    <jsp:include page="/WEB-INF/include/head.jsp"/>
    <script type="text/javascript">
    	function stopMigrate(id) {
    		if(window.confirm("确认要停掉id="+id+"的迁移任务吗?")) {
    			$.get(
 	       			'/data/migrate/stop.json',
 	       			{
 	       				id: id,
 	       			},
 	       	        function(data){
 	       				var status = data.status;
 	       				alert(data.message);
	    				location.href = "/data/migrate/list";
 	       	        }
 	       	     );
    		}
    	}
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
	                <h4>
	                	迁移数据记录列表
	                	<a target="_blank" href="/data/migrate/init" class="btn btn-info btn-success" role="button">添加新的迁移</a>
	                </h4>
	            </div>
			</div>
		</div>
		<div class="row">
			<br/>
			<div class="col-md-12">
	            <table class="table table-bordered table-striped table-hover" style="margin-top: 0px">
	                <thead>
		                <tr>
		                    <th>id</th>
		                    <th>迁移工具</th>
		                    <th>源数据</th>
		                    <th>目标数据</th>
		                    <th>操作人</th>
		                    <th>开始时间</th>
		                    <th>结束时间</th>
		                    <th>状态</th>
		                    <th>查看</th>
		                    <th>操作</th>
		                    <th>校验数据</th>
		                </tr>
	                </thead>
	                <tbody>
	                	<c:forEach items="${appDataMigrateStatusList}" var="appDataMigrateStatus">
	                		 <tr>
			                    <td>${appDataMigrateStatus.id}</td>
			                    <td>${appDataMigrateStatus.migrateMachineIp}:${appDataMigrateStatus.migrateMachinePort}</td>
			                    <td>
			                    	<c:choose>
			                    		<c:when test="${appDataMigrateStatus.sourceAppId <= 0}">
			                    			非cachecloud
			                    		</c:when>
			                    		<c:otherwise>
			                    			cachecloud:<a target="_blank" href="/admin/app/index.do?appId=${appDataMigrateStatus.sourceAppId}">${appDataMigrateStatus.sourceAppId}</a>
			                    		</c:otherwise>
			                    	</c:choose>
			                    	<br/>
			                    	${appDataMigrateStatus.sourceServers}<br/>
			                    	${appDataMigrateStatus.sourceMigrateTypeDesc}
			                    </td>
			                    <td>
			                    	<c:choose>
			                    		<c:when test="${appDataMigrateStatus.targetAppId <= 0}">
			                    			非cachecloud
			                    		</c:when>
			                    		<c:otherwise>
			                    			cachecloud:<a target="_blank" href="/admin/app/index.do?appId=${appDataMigrateStatus.targetAppId}">${appDataMigrateStatus.targetAppId}</a>
			                    		</c:otherwise>
			                    	</c:choose>
			                    	<br/>
			                    	${appDataMigrateStatus.targetServers}<br/>
			                    	${appDataMigrateStatus.targetMigrateTypeDesc}
			                    </td>
			                    <td>${appDataMigrateStatus.userId}</td>
   			                    <td>${appDataMigrateStatus.startTimeFormat}</td>
   			                    <td>${appDataMigrateStatus.endTimeFormat}</td>
   			                    <td>${appDataMigrateStatus.statusDesc}</td>
   			                    <td>
   			                        <a target="_blank" href="/data/migrate/log?id=${appDataMigrateStatus.id}">日志|</a>
   			                        <a target="_blank" href="/data/migrate/config?id=${appDataMigrateStatus.id}">配置|</a>
   			                        <c:choose>
   			                        	<c:when test="${appDataMigrateStatus.status == 1}">
   			                        		进度
   			                        	</c:when>
   			                        	<c:otherwise>
   			                       			<a target="_blank" href="/data/migrate/process?id=${appDataMigrateStatus.id}">进度</a>
   			                        	</c:otherwise>
   			                        </c:choose>
   			                    </td>
   			                    <td>
	                                <button <c:if test='${appDataMigrateStatus.status == 1}'>disabled="disabled"</c:if> onclick="stopMigrate(${appDataMigrateStatus.id})" type="button" class="btn btn-info">停止</button>               
   			                    </td>
   			                    <td>
   			                    	<c:choose>
   			                        	<c:when test="${appDataMigrateStatus.status == 1}">
	                                		<button disabled="disabled" type="button" class="btn btn-info">采样校验</button>               
   			                        	</c:when>
   			                        	<c:otherwise>
   			                    			<a target="_blank" href="/data/migrate/checkData?id=${appDataMigrateStatus.id}" class="btn btn-info" role="button">采样校验</a>
   			                        	</c:otherwise>
   			                        </c:choose>
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

