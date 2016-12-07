<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp" %>

<div class="row">
    <div class="col-md-12">
        <h3 class="page-title">
           	 应用(id=${appId})实例列表
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
                        <td>服务器ip:port</td>
                        <td>实例空间使用情况</td>
                        <td>角色</td>
                        <%--<td>主实例ID</td>--%>
                        <td>实例所在机器信息可用内存</td>
                        <td>对象数</td>
                        <td>slot分布</td>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="instance" items="${instanceList}" varStatus="status">
                        <tr>
                            <td><a href="/admin/instance/index.do?instanceId=${instance.id}"
                                   target="_blank">${instance.id}</a></td>
                            <td>${instance.ip}:${instance.port}</td>
                            <td>
                                <c:set var="instanceStatsMapKey" value="${instance.ip}:${instance.port}"></c:set>
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
                            <td>${instance.roleDesc}</td>
                            <td><fmt:formatNumber
                                    value="${(machineCanUseMem[instance.ip])/1024/1024/1024}"
                                    pattern="0.00"/>G
                            </td>
                            <td>
                            	${instanceStatsMap[instanceStatsMapKey].currItems}
                            </td>
                            <td>
                            	${clusterSlotsMap[instanceStatsMapKey].slotDistributeList}
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
                <br/>
                <table class="table table-striped table-bordered table-hover" id="tableDataList">
                    <tbody>
                    <tr>
                        <td>申请人：</td>
                        <td>${appAudit.userName}</td>
                        <td>appId:</td>
                        <td>${appAudit.appId}</td>
                    </tr>
                    <tr>
                        <td>申请原因：</td>
                        <td>${appAudit.info}</td>
                        <td>申请时间：</td>
                        <td><fmt:formatDate value="${appAudit.createTime}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
                    </tr>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>