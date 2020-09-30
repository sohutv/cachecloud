<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<div class="header navbar navbar-inverse navbar-fixed-top">
	<div class="header-inner">
		<a class="navbar-brand" href="/manage/total/list">
			<img src="/resources/manage/img/logo_new.png" alt="logo" class="img-responsive" />
		</a>
		
		<a href="javascript:;" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-collapse">
			<img src="/resources/manage/img/menu-toggler.png" alt="" />
		</a>
		
		<ul class="nav navbar-nav pull-right">
			<li class="dropdown user">
				<a href="#" class="dropdown-toggle" data-toggle="dropdown" data-hover="dropdown" data-close-others="true">
					<span class="glyphicon glyphicon-cog"></span>
				<span class="username">${userInfo.chName}</span>
				<i class="fa fa-angle-down"></i>
				</a>
				<ul class="dropdown-menu">
					<li><a target="_blank" href="/admin/app/list"><span class="glyphicon glyphicon-home"></span> 应用前台</a></li>
					<li><a href="javascript;" data-target="#addUserModal${userInfo.id}" data-toggle="modal"><span class="glyphicon glyphicon-pencil"></span> 修改资料</a></li>
					<li><a href="/manage/logout"><span class="glyphicon glyphicon-log-out"></span> 注销</a></li>
				</ul>
			</li>
		</ul>
	</div>
</div>
<div class="clearfix"></div>

<c:set var="user" value="${userInfo}"/>
<%@include file="../user/addUser.jsp" %>
