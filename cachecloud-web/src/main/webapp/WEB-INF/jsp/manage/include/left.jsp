<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp"%>
<script src="/resources/manage/plugins/jquery-1.10.2.min.js" type="text/javascript"></script>
<!-- 提示工具 -->
<link href="/resources/toastr/toastr.min.css" rel="stylesheet" type="text/css">
<script type="text/javascript" src="/resources/toastr/toastr.min.js"></script>


<div id="leftMenu" class="page-sidebar navbar-collapse collapse">
	<ul class="page-sidebar-menu">
		<li>
			<div class="sidebar-toggler hidden-phone"></div>
		</li>
		
		<li <c:if test="${totalActive == 1}">class="active"</c:if>>
			<a href="/manage/total/statlist">
				<i class="glyphicon glyphicon-align-left"></i>
				<span class="title"> 全局统计</span>
				<c:if test="${totalActive == 1}">
					<span class="selected"></span>
					<span class="arrow"></span>
				</c:if>
			</a>
		</li>

		<li <c:if test="${appStatActive == 1}">class="active"</c:if>>
			<a href="/manage/app/stat/list">
				<i class="glyphicon glyphicon-stats"></i>
				<span class="title">client统计</span>
				<c:if test="${appStatActive == 1}">
					<span class="selected"></span>
					<span class="arrow"></span>
				</c:if>
			</a>
		</li>
		<li <c:if test="${appStatServerActive == 1}">class="active"</c:if>>
			<a href="/manage/app/stat/list/server">
				<i class="glyphicon glyphicon-stats"></i>
				<span class="title">server统计</span>
				<c:if test="${appStatServerActive == 1}">
					<span class="selected"></span>
					<span class="arrow"></span>
				</c:if>
			</a>
		</li>

		<li <c:if test="${checkActive == 1}">class="active"</c:if>>
			<a href="/manage/app/auditList">
				<i class="glyphicon glyphicon-forward"></i>
				<span class="title">工单审批</span>
				<c:if test="${checkActive == 1}">
					<span class="selected"></span>
					<span class="arrow"></span>
				</c:if>
			</a>
		</li>

		<li <c:if test="${appOperateActive == 1}">class="active"</c:if>>
			<a href="/manage/total/list">
				<i class="glyphicon glyphicon-list"></i>
				<span class="title">应用运维</span>
				<c:if test="${appOperateActive == 1}">
					<span class="selected"></span>
					<span class="arrow"></span>
				</c:if>
			</a>
		</li>

		<li <c:if test="${diagnosticActive == 1}">class="active"</c:if>>
			<a href="/manage/app/tool/index">
				<i class="glyphicon glyphicon-magnet"></i>
				<span class="title">诊断工具</span>
				<c:if test="${diagnosticActive == 1}">
					<span class="selected"></span>
					<span class="arrow"></span>
				</c:if>
			</a>
		</li>



		<li <c:if test="${machineActive == 1}">class="active"</c:if>>
			<a href="/manage/machine/index">
				<i class="glyphicon glyphicon-wrench"></i>
				<span class="title">机器管理</span>
				<c:if test="${machineActive == 1}">
					<span class="selected"></span>
					<span class="arrow"></span>
				</c:if>
			</a>
		</li>


		<li <c:if test="${instanceAlertValueActive == 1}">class="active"</c:if>>
			<a href="/manage/instanceAlert/init">
				<i class="glyphicon glyphicon-warning-sign"></i>
				<span class="title">报警配置</span>
				<c:if test="${instanceAlertValueActive == 1}">
					<span class="selected"></span>
					<span class="arrow"></span>
				</c:if>
			</a>
		</li>
		
		<li <c:if test="${configActive == 1}">class="active"</c:if>>
			<a href="/manage/config/init">
				<i class="glyphicon glyphicon-cog"></i>
				<span class="title">系统配置</span>
				<c:if test="${configActive == 1}">
					<span class="selected"></span>
					<span class="arrow"></span>
				</c:if>
			</a>
		</li>

		<li <c:if test="${reourcesActive == 1}">class="active"</c:if>>
			<a href="/manage/app/resource/index">
				<i class="fa fa-map-marker"></i>
				<span class="title">资源管理</span>
				<c:if test="${reourcesActive == 1}">
					<span class="selected"></span>
					<span class="arrow"></span>
				</c:if>
			</a>
		</li>

		<li <c:if test="${redisConfigActive == 1}">class="active"</c:if>>
			<a href="/manage/redisConfig/init?resourceId=29">
				<i class="glyphicon glyphicon-file"></i>
				<span class="title">配置模版</span>
				<c:if test="${redisConfigActive == 1}">
					<span class="selected"></span>
					<span class="arrow"></span>
				</c:if>
			</a>
		</li>

		<li <c:if test="${taskActive == 1}">class="active"</c:if>>
			<a href="/manage/task/list?status=1">
				<i class="fa fa fa-tasks"></i>
				<span class="title">任务管理</span>
				<c:if test="${taskActive == 1}">
					<span class="selected"></span>
					<span class="arrow"></span>
				</c:if>
			</a>
		</li>

		<li <c:if test="${quartzActive == 1}">class="active"</c:if>>
			<a href="/manage/quartz/list">
				<i class="glyphicon glyphicon-th-large"></i>
				<span class="title">调度任务</span>
				<c:if test="${quartzActive == 1}">
					<span class="selected"></span>
					<span class="arrow"></span>
				</c:if>
			</a>
		</li>

		<li <c:if test="${userActive == 1}">class="active"</c:if>>
			<a href="/manage/user/list">
				<i class="glyphicon glyphicon-user"></i>
				<span class="title">用户管理</span>
				<c:if test="${userActive == 1}">
					<span class="selected"></span>
					<span class="arrow"></span>
				</c:if>
			</a>
		</li>

		<li <c:if test="${noticeActive == 1}">class="active"</c:if>>
			<a href="/manage/notice/initNotice">
				<i class="glyphicon glyphicon-bell"></i>
				<span class="title">系统通知</span>
				<c:if test="${noticeActive == 1}">
					<span class="selected"></span>
					<span class="arrow"></span>
				</c:if>
			</a>
		</li>
		
	</ul>
</div>
