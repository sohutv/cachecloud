<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp"%>
<div class="container">
    <div class="row">
        <div class="col-md-12">
            <div class="page-header">
                <h4>应用详情-
                ${appDesc.name}
                (${appDesc.typeDesc})
                </h4>
            </div>
            <table class="table table-striped table-hover table-bordered">
                <tbody>
	                <tr>
	                    <td>应用id</td>
	                    <td>${appDesc.appId}</td>
	                    <td>应用名称</td>
	                    <td>${appDesc.name}</td>
	                </tr>
	                <tr>
	                    <td>应用申请人</td>
	                    <td>${appDesc.officer}</td>
	                    <td>应用类型</td>
	                    <td>
	                    	<c:choose>
	        		            <c:when test="${appDesc.type == 2}">redis-cluster</c:when>
			        		    <c:when test="${appDesc.type == 5}">redis-sentinel</c:when>
			        		    <c:when test="${appDesc.type == 6}">redis-standalone</c:when>
	                    	</c:choose>
	                    </td>
	                </tr>
	                <tr>
	                    <td>负责人</td>
	                    <td>${appDesc.officer}</td>
	                    <td>详情</td>
	                    <td>${appDesc.intro}</td>
	                </tr>
	                <tr>
	                    <td>创建时间</td>
	                    <td><fmt:formatDate pattern="yyyy-MM-dd HH:mm:ss" value="${appDesc.createTime}"/></td>
	                    <td>审批通过时间</td>
	                    <td><fmt:formatDate pattern="yyyy-MM-dd HH:mm:ss" value="${appDesc.passedTime}"/></td>
	                </tr>
	                <tr>
	                    <td>测试</td>
	                    <td>
	                    	<c:choose>
	                    		<c:when test="${appDesc.isTest == 0}">否</c:when>
	                    		<c:when test="${appDesc.isTest == 1}">是</c:when>
	                    	</c:choose>
	                    </td>
	                    <td>后端是否有数据源</td>
	                    <td>
	                    	<c:choose>
	                    		<c:when test="${appDesc.hasBackStore == 0}">有</c:when>
	                    		<c:when test="${appDesc.hasBackStore == 1}">无</c:when>
	                    	</c:choose>
	                    </td>
	                </tr>
	                <tr>
	                    <td>是否需要持久化</td>
	                    <td>
	                    	<c:choose>
	                    		<c:when test="${appDesc.needPersistence == 0}">不需要</c:when>
	                    		<c:when test="${appDesc.needPersistence == 1}">需要</c:when>
	                    	</c:choose>
	                    </td>
	                    <td>预计对象数</td>
	                    <td>${appDesc.forecastObjNum}</td>
	                </tr>
	                <tr>
	                	<td>预计QPS</td>
	                	<td>${appDesc.forecaseQps}</td>
	                	<td>是否需要热备</td>
	                	<td>
	                		<c:choose>
	                    		<c:when test="${appDesc.needHotBackUp == 0}">不需要</c:when>
	                    		<c:when test="${appDesc.needHotBackUp == 1}">需要</c:when>
	                    	</c:choose>
	                	</td>
	                </tr>
	                <tr>
	                	<td>内存报警阀值</td>
	                	<td>${appDesc.memAlertValue}%</td>
	                </tr>
                </tbody>
            </table>
        </div>
    </div>
    
    <div class="row">
   	 	<div class="col-md-12 page-header">
            <h4>申请记录</h4>
        </div>
   		<div class="col-md-12">
   		  	<table class="table table-striped table-hover table-bordered">
	   			<thead>
					<tr>
						<th>审批id</th>
						<th>申请人</th>
						<th>申请类型</th>
						<th>申请详情</th>
						<th>申请时间</th>
						<th>审批时间</th>
						<th>审批人</th>
						<th>审批结果</th>
						<th>审批意见</th>
					</tr>
				</thead>
				<tbody>
					<c:forEach items="${appAuditList}" var="appAudit">
						<tr>
							<td>${appAudit.id}</td>
							<td>${appAudit.userName}</td>
							<td>
								<c:choose>
									<c:when test="${appAudit.type == 0}">申请应用</c:when>
									<c:when test="${appAudit.type == 1}">应用扩容</c:when>
									<c:when test="${appAudit.type == 2}">修改配置</c:when>
								</c:choose>
							</td>
							<td>${appAudit.info}</td>
							<td><fmt:formatDate pattern="yyyy-MM-dd HH:mm:ss" value="${appAudit.createTime}"/></td>
							<td><fmt:formatDate pattern="yyyy-MM-dd HH:mm:ss" value="${appAudit.modifyTime}"/></td>
							<td>${appAudit.appAuditLog.appUser.name}</td>
							<td>
								<c:choose>
									<c:when test="${appAudit.status == 1}">审批通过</c:when>
									<c:when test="${appAudit.status == -1}">驳回</c:when>
								</c:choose>
							</td>
							<td>${appAudit.refuseReason}</td>
						</tr>
					</c:forEach>
				</tbody>
			</table>
   		</div>
    </div>

    <br/><br/><br/>
	
</div>





