<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp"%>

<div class="container">
	<br/>
	<div class="row">
		<div class="page-header">
	        <h4>
	        集群键值分析
				<a target="_blank" href="/admin/app/appKeyAnalysis?appId=${appDesc.appId}" class="btn btn-info" role="button">键值分析</a>
	        </h4>

	    </div>
        <div class="col-md-12">
        	<table class="table table-bordered table-striped table-hover">
	            <thead>
		            <tr>
		                <td>审批ID</td>
		                <td>申请时间</td>
		                <td>申请人</td>
		                <td>审批状态</td>
		                <td>申请描述</td>
		                <td>详情</td>
		            </tr>
	            </thead>
	            <tbody>
		            <c:forEach var="appAudit" items="${appAuditList}" varStatus="status">
		                <tr>
		                  <td>${appAudit.id}</td>
		                  <td>
		                  	  <fmt:formatDate pattern="yyyy-MM-dd HH:mm:ss" value="${appAudit.createTime}"/>
		                  </td>
		                  <td>${appAudit.userName}</td>
		                  <td>
		                 	 <c:choose>
								<c:when test="${appAudit.status == 0}">待审</c:when>
								<c:when test="${appAudit.status == 1}">通过</c:when>
								<c:when test="${appAudit.status == 2}">审核已处理</c:when>
								<c:when test="${appAudit.status == -1}">驳回</c:when>
							</c:choose>
		                  </td>
		                  <td>${appAudit.info}</td>
		                  <td>
		                  	<c:choose>
		                  		<c:when test="${appAudit.status == 1}">
		                  			<a href="/admin/app/keyAnalysisResult?appId=${appDesc.appId}&auditId=${appAudit.id}">[查看结果]</a>
		                  		</c:when>
		                  		<c:otherwise>
		                  			[分析中]
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
