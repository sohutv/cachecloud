<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp"%>
<div class="header navbar navbar-inverse navbar-fixed-top">
	<div class="header-inner">
		<div class="navbar-header">
			<a class="navbar-brand" href="/manage/total/list.do">
				<img src="/resources/manage/img/logo_new.png" alt="logo" class="img-responsive" />
			</a>
        </div>
		
		<ul class="nav navbar-nav pull-right">
			<li class="dropdown user">
				<a href="#" class="dropdown-toggle" data-toggle="dropdown" data-hover="dropdown" data-close-others="true">
				<span class="username">${userInfo.chName}</span>
				<i class="fa fa-angle-down"></i>
				</a>
				<ul class="dropdown-menu">
					<li><a target="_blank" href="/admin/app/list.do"><i class="fa fa-user"></i>应用前台页面</a></li>
					<li><a href="/manage/logout.do"><i class="fa fa-user"></i>注销</a></li>
				</ul>
			</li>
		</ul>
	</div>
</div>
<div class="clearfix"></div>
