<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp"%>
<script type="text/javascript">
	function startInstance(appId, instanceId){
		if(confirm("确认要开启"+instanceId+"实例吗?")){
			$.ajax({
                type: "get",
                url: "/manage/instance/startInstance.json",
                data: 
                {
                	appId: appId,
                	instanceId: instanceId
                },
                success: function (result) {
                	if(result.success == 1){
                		alert("开启成功!");
                	}else{
                		alert("开启失败, msg: " + result.message)
                	}
                    window.location.reload();
                }
            });
        }
	}
	
	function shutdownInstance(appId, instanceId){
		if(confirm("确认要下线"+instanceId+"实例吗?")){
			$.ajax({
                type: "get",
                url: "/manage/instance/shutdownInstance.json",
                data: 
                {
                	appId: appId,
                	instanceId: instanceId
                },
                success: function (result) {
                	if(result.success == 1){
                		alert("关闭成功!");
                	}else{
                		alert("关闭失败, msg: " + result.message)
                	}
                    window.location.reload();
                }
            });
        }
	}	
	
</script>
<div class="page-container">
    <div class="page-content">
        <div class="row">
		    <div class="col-md-12">
		        <h3 class="page-title">
		           	 机器(ip=${machineInfo.ip})实例列表
		        </h3>
		    </div>
		</div>
		<div class="row">
		    <div class="col-md-12">
		        <div class="portlet box light-grey">
		            <div class="portlet-title">
		                <div class="caption"><i class="fa fa-globe"></i>实例列表</div>
		                <div class="tools">
		                    <a href="javascript:;" class="collapse"></a>
		                </div>
		            </div>
		            <div class="portlet-body">
		                <table class="table table-striped table-bordered table-hover" id="tableDataList">
		                    <thead>
		                    <tr>
		                        <td>ID</td>
		                        <th>应用ID</th>
		                        <th>应用名</th>
		                        <th>负责人</th>
		                        <td>服务器ip:port</td>
		                        <td>实例空间使用情况</td>
		                        <td>连接数</td>
		                        <td>角色</td>
		                        <td>实例所在机器信息可用内存(G)</td>
		                        <td>实例操作</td>
		                    </tr>
		                    </thead>
		                    <tbody>
		                    <c:forEach var="instance" items="${instanceList}" varStatus="status">
		                        <tr>
		                            <td><a href="/admin/instance/index.do?instanceId=${instance.id}"
		                                   target="_blank">${instance.id}</a></td>
		                            <c:set var="instanceStatsMapKey" value="${instance.ip}:${instance.port}"></c:set>
		                            <c:set var="curAppId" value="${(instanceStatsMap[instanceStatsMapKey]).appId}"></c:set>
		                            <td>
		                            	<c:if test="${curAppId > 0}">
			                            	<a target="_blank" href="/admin/app/index.do?appId=${curAppId}">
			                            		${curAppId}
			                            	</a>
		                            	</c:if>
		                            </td>
		                            <td>${(appInfoMap[curAppId]).name}</td>
		                            <td>${(appInfoMap[curAppId]).officer}</td>
		                            <td>${instance.ip}:${instance.port}</td>
		                            <td>
		                                <div class="progress margin-custom-bottom0">
		                                	<c:choose>
				                        		<c:when test="${(instanceStatsMap[instanceStatsMapKey]).memUsePercent >= 80}">
													<c:set var="progressBarStatus" value="progress-bar-danger"/>
				                        		</c:when>
				                        		<c:otherwise>
													<c:set var="progressBarStatus" value="progress-bar-success"/>
				                        		</c:otherwise>
				                        	</c:choose>
		                                    <div class="progress-bar ${progressBarStatus}"
		                                         role="progressbar"
		                                         aria-valuenow="${(instanceStatsMap[instanceStatsMapKey]).memUsePercent }"
		                                         aria-valuemax="100"
		                                         aria-valuemin="0"
		                                         style="width: ${(instanceStatsMap[instanceStatsMapKey]).memUsePercent }%">
		                                            <label style="color: #000000">
		                                                <fmt:formatNumber
		                                                        value="${(instanceStatsMap[instanceStatsMapKey]).usedMemory / 1024 / 1024 / 1024}"
		                                                        pattern="0.00"/>G&nbsp;&nbsp;Used/<fmt:formatNumber value="${(instanceStatsMap[instanceStatsMapKey]).maxMemory / 1024 / 1024 / 1024}" pattern="0.00"/>G&nbsp;&nbsp;Total
		                                            </label>
		                                     </div>
		                                </div>
		                            </td>
                            		   <td>${(instanceStatsMap[instanceStatsMapKey]).currConnections}</td>
                            		   <td>${instance.roleDesc}</td>
		                            <td><fmt:formatNumber
		                                    value="${(machineCanUseMem[instance.ip])/1024/1024/1024}"
		                                    pattern="0.00"/>
		                            </td>
		                            <td>
                                        <c:choose>
                                            <c:when test="${instance.status == 2}">
                                                <a target="_blank" onclick="startInstance('${curAppId}', '${instance.id}')" class="btn btn-success">
                                                 	启动实例
                                                </a>
                                            </c:when>
                                            <c:otherwise>
                                                <a target="_blank" onclick="shutdownInstance('${curAppId}', '${instance.id}')" class="btn btn-danger">
                                                    下线实例
                                                </a>
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
		</div>
    </div>
</div>