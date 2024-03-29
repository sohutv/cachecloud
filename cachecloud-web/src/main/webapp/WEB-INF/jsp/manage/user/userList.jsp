<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp"%>
<%@ page import="com.sohu.cache.utils.EnvCustomUtil"%>
<script src="/resources/manage/plugins/jquery.md5.js" type="text/javascript"></script>
<div class="page-container">
	<div class="page-content">
		<div class="row">
			<div class="col-md-12">
				<h3 class="page-title">
					用户管理
				</h3>
			</div>
		</div>
		<div class="row">
			<div class="col-md-12">
				<div class="portlet-body">
					<div class="table-toolbar">
						<div class="btn-group">
							<button id="sample_editable_1_new" class="btn green" data-target="#addUserModal" data-toggle="modal">
							添加新用户 <i class="fa fa-plus"></i>
							</button>
						</div>
						<div class="btn-group" style="float:right">
							<form action="/manage/user/list" method="post" class="form-horizontal form-bordered form-row-stripped">
								<label class="control-label">
									用户名:
								</label>
								&nbsp;<input type="text" name="searchChName" id="searchChName" value="${searchChName}" placeholder="中文名"/>
								&nbsp;<button type="submit" class="btn blue btn-sm">查询</button>
							</form>
						</div>
					</div>
					<table class="table table-striped table-bordered table-hover" id="tableDataList">
						<thead>
							<tr>
								<th>id</th>
								<th>域账户</th>
								<th>中文名</th>
								<th>邮箱</th>
								<th>手机</th>
								<th>微信</th>
								<c:if test="<%=EnvCustomUtil.openswitch%>">
									<th>所在公司</th>
									<th>使用目的</th>
								</c:if>
								<th>是否报警</th>
								<th>类型</th>
								<th>注册时间</th>
								<th>操作</th>
							</tr>
						</thead>
						<tbody>
							<c:forEach items="${users}" var="user">
								<tr class="odd gradeX">
									<td>${user.id}</td>
									<td>${user.name}</td>
									<td>${user.chName}</td>
									<td>${user.email}</td>
									<td>${user.mobile}</td>
									<td>${user.weChat}</td>
									<c:if test="<%=EnvCustomUtil.openswitch%>">
										<td>${user.company}</td>
										<td>${user.purpose}</td>
									</c:if>
									<td>
										<c:if test="${user.isAlert==0}">否</c:if>
										<c:if test="${user.isAlert==1}">是</c:if>
									</td>
									<td>
										<c:choose>
											<c:when test="${user.type == 0 }">管理员</c:when>
											<c:when test="${user.type == 2 }">普通用户</c:when>
										</c:choose>
									</td>
									<td>
										<fmt:formatDate pattern="yyyy-MM-dd HH:mm:ss" value="${user.registerTime}"/>
									</td>
									<td>
									<a href="javascript:void(0);" data-target="#addUserModal${user.id}" data-toggle="modal">[修改]</a>
									<c:if test="<%=EnvCustomUtil.pwdswitch%>">
										<a href="javascript:void(0);" data-target="#updateUserPwdModal${user.id}" data-toggle="modal">[修改密码]</a>
									</c:if>
									<a onclick="if(window.confirm('确认要删除该用户吗?!')){return true;}else{return false;}" href="/manage/user/delete?userId=${user.id}">[删除]</a>
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


<c:forEach items="${users}" var="user">
	<%@include file="addUser.jsp" %>
	<%@include file="updateUserPwd.jsp" %>
</c:forEach>
