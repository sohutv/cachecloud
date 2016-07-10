<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp"%>

<div class="page-container">
	<div class="page-content">
		<div class="row">
			<div class="col-md-12">
				<h3 class="page-title">
					应用审批列表
				</h3>
			</div>
		</div>
		<div class="row">
			<div class="col-md-12">
				<div class="portlet box light-grey">
					<div class="portlet-title">
						<div class="caption"><i class="fa fa-globe"></i>审批列表</div>
						<div class="tools">
							<a href="javascript:;" class="collapse"></a>
						</div>
					</div>
					<div class="portlet-body">
						<div class="table-toolbar">
							<div class="btn-group" style="float:right">
								<form action="/manage/app/auditList.do" method="post" class="form-horizontal form-bordered form-row-stripped">
									<label class="control-label">
										审核状态:
									</label>
									<select name="status">
										<option value="0" <c:if test="${status == 0}">selected="selected"</c:if>>
											待处理列表
										</option>
                                        <option value="2" <c:if test="${status == 2}">selected="selected"</c:if>>
                                                                                                              审核已处理列表
                                        </option>
										<option value="1" <c:if test="${status == 1}">selected="selected"</c:if>>
											通过列表
										</option>
										<option value="-1" <c:if test="${status == -1}">selected="selected"</c:if>>
											驳回列表
										</option>
									</select>
									
									&nbsp;<button type="submit" class="btn blue btn-sm">查询</button>
								</form>
							</div>
						</div>
						<table class="table table-striped table-bordered table-hover" id="tableDataList">
							<thead>
								<tr>
									<th>appID</th>
									<th>应用名</th>
									<th>申请人</th>
									<th>审核状态</th>
									<th>申请类型</th>
									<th>申请描述</th>
									<th>申请时间</th>
									<th>操作</th>
								</tr>
							</thead>
							<tbody>
								<c:forEach items="${list}" var="item">
									<tr class="odd gradeX">
										<td>
											<c:choose>
												<c:when test="${item.type == 3}">
													无
												</c:when>
												<c:otherwise>
													<a target="_blank" href="/admin/app/index.do?appId=${item.appId}">${item.appId}</a>
												</c:otherwise>
											</c:choose>	
										</td>
										<td>
											<c:choose>
												<c:when test="${item.type == 3}">
													无
												</c:when>
												<c:otherwise>
													${item.appDesc.name}												
												</c:otherwise>
											</c:choose>	
										</td>
										<td>${item.userName}</td>
										<td>
											<c:choose>
												<c:when test="${item.status == 0}">待审</c:when>
												<c:when test="${item.status == 1}">通过</c:when>
												<c:when test="${item.status == 2}">审核已处理</c:when>
												<c:when test="${item.status == -1}">驳回</c:when>
											</c:choose>
										</td>
										<td>
											<c:choose>
												<c:when test="${item.type == 0}">
													应用申请
												</c:when>
												<c:when test="${item.type == 1}">
													应用扩容
												</c:when>
												<c:when test="${item.type == 2}">
													应用配置修改
												</c:when>
												<c:when test="${item.type == 3}">
													注册用户申请
												</c:when>
												<c:when test="${item.type == 4}">
													实例配置修改
												</c:when>
											</c:choose>	
										</td>
										<td>${item.info}</td>
										<td><fmt:formatDate pattern="yyyy-MM-dd HH:mm:ss" value="${item.createTime}"/></td>
										<td>
											<c:choose>
												<c:when test="${item.type == 3}">
													<a onclick="if(window.confirm('确认要通过该申请请求吗?')){return true;}else{return false;}" href="/manage/user/addAuditStatus.do?status=1&appAuditId=${item.id}">[通过]</a>
												</c:when>
												<c:otherwise>
													<c:set var="auditUrl" value="/manage/app/addAuditStatus.do?status=1&appAuditId=${item.id}"/>
												</c:otherwise>
											</c:choose>	
											<c:choose>
												<c:when test="${item.status == 2}">
													<a onclick="if(window.confirm('确认要通过该申请请求吗?')){return true;}else{return false;}" href="${auditUrl}">[通过]</a>
												</c:when>
											</c:choose>
											<c:choose>
												<c:when test="${item.status == 0}">
													<a href="javascript:void(0);" data-target="#appRefuseModal${item.id}" data-toggle="modal">[驳回]</a>
												</c:when>
											</c:choose>
											&nbsp;
											<c:choose>
												<c:when test="${item.type == 0}">
													<c:set var="auditDealUrl" value="/manage/app/initAppDeploy.do?appAuditId=${item.id}"/>
												</c:when>
												<c:when test="${item.type == 1}">
													<c:set var="auditDealUrl" value="/manage/app/initAppScaleApply.do?appAuditId=${item.id}"/>
												</c:when>
												<c:when test="${item.type == 2}">
													<c:set var="auditDealUrl" value="/manage/app/initAppConfigChange.do?appAuditId=${item.id}"/>
												</c:when>
												<c:when test="${item.type == 4}">
													<c:set var="auditDealUrl" value="/manage/instance/initInstanceConfigChange.do?appAuditId=${item.id}"/>
												</c:when>
											</c:choose>
											<c:choose>
												<c:when test="${item.status == 0 && item.type != 3}">
													<a href="${auditDealUrl}">[审批处理]</a>
												</c:when>
											</c:choose>
										</td>
									</tr>
								</c:forEach>
							</tbody>
						</table>
					</div>
				</div>
				<!-- END EXAMPLE TABLE PORTLET-->
			</div>
		</div>
	</div>
</div>

<c:forEach items="${list}" var="item">
	<%@include file="addAudit.jsp" %>
</c:forEach>







