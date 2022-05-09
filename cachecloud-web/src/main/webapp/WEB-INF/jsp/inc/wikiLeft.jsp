<%@ page contentType="text/html;charset=UTF-8" language="java" %>
	<style type="text/css">
.nav-sidebar .sub-menu {
	list-style-type: none;
	padding: 0;
}

.nav-sidebar .sub-menu a {
	padding-top: 5px;
	padding-bottom: 5px;
	padding-left: 30px;
	font-size: 12px;
	font-weight: 400;
}

.nav>li>span {
	position: relative;
	display: block;
	padding: 10px 15px;
}

.leftBox {
	padding-top: 10px;
	position: fixed;
	width: 170px;
	height: 600px;
	overflow-y: scroll;
	border: 1px solid #eaecef;
	word-break:break-all;
　　word-wrap:break-word;
}

.leftBox a {
	color: #333;
}

.leftBox li.active a {
	color: #35b558 !important;
}

.right-side-bar {
	width: 100px;
	position: fixed;
	bottom: 32px;
	right: 0px;
	padding-left:3px;
	border: 1px solid #eaecef;
	background-color: #fff;
}
.right-side-bar ul {
	list-style-type: none;
	padding: 0;
	font-size:10px;
}

.right-side-bar ul li{
	padding-top: 4px;
}

.right-side-bar ul li a{
	color: #333;
	cursor:pointer;
}
.right-side-bar li a.active {
	color: #35b558 !important;
}
</style>

<ul id="leftBox" class="nav nav-sidebar leftBox">
	<li id="quickstart_index"><a href="${request.contextPath}/wiki/quickstart/index"><i class="glyphicon glyphicon-play-circle"></i> 快速接入</a></li>

	<li id="intro_index"><a href="${request.contextPath}/wiki/intro/index"><i class="glyphicon glyphicon-tag"></i> 系统介绍</a></li>
	<ul class="nav sub-menu">
		<li id="intro_redisVersion"><a href="${request.contextPath}/wiki/intro/redisVersion">Redis版本说明</a></li>
		<li id="intro_releaseNote"><a href="${request.contextPath}/wiki/intro/releaseNote">CC客户端版本说明</a></li>
	</ul>

	<li id="architecture_index"><a href="${request.contextPath}/wiki/architecture/index"><i class="glyphicon glyphicon-road"></i> 系统架构</a></li>
	<ul class="nav sub-menu">
		<li id="architecture_service"><a href="${request.contextPath}/wiki/architecture/service">服务架构</a></li>
		<li id="architecture_tech"><a href="${request.contextPath}/wiki/architecture/tech">技术架构</a></li>
	</ul>


	<li id="access_index"><a href="${request.contextPath}/wiki/access/index"><i class="glyphicon glyphicon-import"></i> 系统接入</a></li>
	<ul class="nav sub-menu">
		<li id="access_init"><a href="${request.contextPath}/wiki/access/init">系统初始化</a></li>
		<li id="access_config"><a href="${request.contextPath}/wiki/access/config">系统配置说明</a></li>
		<li id="access_resource"><a href="${request.contextPath}/wiki/access/resource">系统资源管理</a></li>
		<li id="access_client"><a href="${request.contextPath}/wiki/access/client">客户端接入</a></li>
		<%--<li id="access_docker"><a href="${request.contextPath}/wiki/access/docker">docker部署文档</a></li>--%>
	</ul>

	<li id="function_index"><a href="${request.contextPath}/wiki/function/index"><i class="glyphicon glyphicon-list"></i> 系统功能</a></li>
	<ul class="nav sub-menu">
		<li id="function_client"><a href="${request.contextPath}/wiki/function/client">客户端功能</a></li>
		<li id="function_client-register"><a href="${request.contextPath}/wiki/function/client-register">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;账户申请</a></li>
		<li class="dropdown"><a class="dropdown-toggle" data-toggle="dropdown">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;应用管理<b class="caret"></b></a>
			<ul class="dropdown-menu">
				<li id="function_client-appStats"><a href="${request.contextPath}/wiki/function/client-appStats">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;统计信息</a></li>
				<li id="function_client-desc"><a href="${request.contextPath}/wiki/function/client-desc">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;应用详情</a></li>
				<li id="function_client-instances"><a href="${request.contextPath}/wiki/function/client-instances">实例列表&应用拓扑</a></li>
				<li id="function_client-conn"><a href="${request.contextPath}/wiki/function/client-conn">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;连接信息</a></li>
				<li id="function_client-cmd"><a href="${request.contextPath}/wiki/function/client-cmd">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;命令曲线</a></li>
				<li id="function_client-latency"><a href="${request.contextPath}/wiki/function/client-latency">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;延迟监控</a></li>
				<li id="function_client-daily"><a href="${request.contextPath}/wiki/function/client-daily">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;日报统计</a></li>
				<li id="function_client-cmdexe"><a href="${request.contextPath}/wiki/function/client-cmdexe">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;命令执行</a></li>
				<li id="function_client-analysis"><a href="${request.contextPath}/wiki/function/client-analysis">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;键值分析</a></li>
			</ul>
		</li>
		<li id="function_job"><a href="${request.contextPath}/wiki/function/job">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;我的申请</a></li>


		<li id="function_operations"><a href="${request.contextPath}/wiki/function/operations">运维端功能</a></li>
		<li class="dropdown"><a class="dropdown-toggle" data-toggle="dropdown">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;数据统计<b class="caret"></b></a>
			<ul class="dropdown-menu">
				<li id="function_statistics"><a href="${request.contextPath}/wiki/function/statistics">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;全局统计</a></li>
				<li id="function_client-statistic"><a href="${request.contextPath}/wiki/function/client-statistic">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;client统计</a></li>
				<li id="function_server-statistic"><a href="${request.contextPath}/wiki/function/server-statistic">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;server统计</a></li>
			</ul>
		</li>
		<li class="dropdown"><a class="dropdown-toggle" data-toggle="dropdown">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;运维功能<b class="caret"></b></a>
			<ul class="dropdown-menu">
				<li id="function_operation-job"><a href="${request.contextPath}/wiki/function/operation-job">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;工单审批</a></li>
				<li id="function_operation-app"><a href="${request.contextPath}/wiki/function/operation-app">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;应用运维</a></li>
				<li id="function_operation-instance"><a href="${request.contextPath}/wiki/function/operation-instance">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;实例运维</a></li>
				<li id="function_operation-import"><a href="${request.contextPath}/wiki/function/operation-import">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;应用导入</a></li>
				<li id="function_operation-migrate"><a href="${request.contextPath}/wiki/function/operation-migrate">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;数据迁移</a></li>
				<li id="function_operation-diagnostic"><a href="${request.contextPath}/wiki/function/operation-diagnostic">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;诊断工具</a></li>
				<li id="function_operation-machine"><a href="${request.contextPath}/wiki/function/operation-machine">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;机器管理</a></li>
			</ul>
		</li>
		<li class="dropdown"><a class="dropdown-toggle" data-toggle="dropdown">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;配置管理<b class="caret"></b></a>
			<ul class="dropdown-menu">
				<li id="function_operation-alert"><a href="${request.contextPath}/wiki/function/operation-alert">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;报警配置</a></li>
				<li id="function_operation-systemalert"><a href="${request.contextPath}/wiki/function/operation-systemalert">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;系统配置</a></li>
				<li id="function_operation-resource"><a href="${request.contextPath}/wiki/function/operation-resource">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;资源管理</a></li>
				<li id="function_operation-module"><a href="${request.contextPath}/wiki/function/operation-module">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;模块管理</a></li>
				<li id="function_operation-template"><a href="${request.contextPath}/wiki/function/operation-template">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;模板配置</a></li>
			</ul>
		</li>
		<li class="dropdown"><a class="dropdown-toggle" data-toggle="dropdown">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;任务管理<b class="caret"></b></a>
			<ul class="dropdown-menu">
				<li id="function_operation-task"><a href="${request.contextPath}/wiki/function/operation-task">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;任务流</a></li>
				<li id="function_operation-schedule"><a href="${request.contextPath}/wiki/function/operation-schedule">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;调度任务</a></li>
			</ul>
		</li>
		<li id="function_operation-user"><a href="${request.contextPath}/wiki/function/operation-user">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;用户管理</a></li>
		<li id="function_system-alert"><a href="${request.contextPath}/wiki/function/system-alert">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;系统通知</a></li>
	</ul>

	<li id="operate_index"><a href="${request.contextPath}/wiki/operate/index"><i class="glyphicon glyphicon-book"></i> 运维手册</a></li>
	<ul class="nav sub-menu">
		<li id="operate_baseConcept"><a href="${request.contextPath}/wiki/operate/baseConcept">基础概念</a></li>
		<li id="operate_baseOperate"><a href="${request.contextPath}/wiki/operate/baseOperate">基础运维</a></li>
		<li id="operate_appDeploy"><a href="${request.contextPath}/wiki/operate/appDeploy">应用部署</a></li>
		<li id="operate_appUpgrade"><a href="${request.contextPath}/wiki/operate/appUpgrade">应用升级</a></li>
		<li id="operate_appAlert"><a href="${request.contextPath}/wiki/operate/appAlert">应用报警</a></li>
		<li id="operate_ssh"><a href="${request.contextPath}/wiki/operate/ssh">Redis机器授权</a></li>
		<li id="operate_baseOptimize"><a href="${request.contextPath}/wiki/operate/baseOptimize">系统运维优化</a></li>
	</ul>

	<li id="troubleshooting_index"><a href="${request.contextPath}/wiki/troubleshooting/index"><i class="glyphicon glyphicon-question-sign"></i>FAQ常见问题</a></li>
	<ul class="nav sub-menu">
		<li id="troubleshooting_cachecloud"><a href="${request.contextPath}/wiki/troubleshooting/cachecloud">关于Cachecloud平台</a></li>

		<li class="dropup"><a class="dropdown-toggle" data-toggle="dropdown">关于Redis使用<b class="caret"></b></a>
			<ul class="dropdown-menu">
				<li id="troubleshooting_exception"><a href="${request.contextPath}/wiki/troubleshooting/exception">常见Jedis异常类</a></li>
				<li id="troubleshooting_jedispoolconfig"><a href="${request.contextPath}/wiki/troubleshooting/jedispoolconfig">JedisPool优化</a></li>
				<li id="troubleshooting_bigkey"><a href="${request.contextPath}/wiki/troubleshooting/bigkey">bigkey的寻找和优化</a></li>
				<li id="troubleshooting_hotkey"><a href="${request.contextPath}/wiki/troubleshooting/hotkey">hotkey的寻找和优化</a></li>
				<li id="troubleshooting_memory"><a href="${request.contextPath}/wiki/troubleshooting/memory">Redis内存优化</a></li>
				<li id="troubleshooting_liunx"><a href="${request.contextPath}/wiki/troubleshooting/liunx">Liunx系统优化</a></li>
				<li id="troubleshooting_activefrag"><a href="${request.contextPath}/wiki/troubleshooting/activefrag">Activefrag引起redis周期性延迟</a></li>
			</ul>
		</li>

	</ul>
</ul>
<script type="text/javascript">
	$(function(){
		var id = window.location.pathname.substring(6).replace(/\//g,"_");
		console.log("id:"+id);
		$("#"+id).addClass("active");
		$("#leftBox").niceScroll({
			cursorcolor : "#ddd",
			horizrailenabled : false
		});
		$(".right-side-bar a").each(function(){
			$(this).click(function(){
				$("html,body").animate({scrollTop: $($(this).attr('href')).offset().top - 52}, 500);
				return false;
			});
		});
		$(".right-side-bar a:first").addClass("active");
		//浏览器滚动条滚动触发的事件
	    $(window).scroll(function() {
	        //获取当前滚动条的高度
	        var scrollTop = $(window).scrollTop();
	        if(scrollTop <= 0) {
	        	$(".right-side-bar a").removeClass("active");
	        	$(".right-side-bar a:first").addClass("active");
	        	return;
	        }
	        var top = scrollTop + $(window).height() - 100;
	        //遍历所有的div
	        $(".markdown-body h2 > span").each(function(index) {
	            var thisTop = $(this).offset().top;
	            if (top >= thisTop) {
	                $(".right-side-bar a").removeClass("active");
	                $(".right-side-bar a[href='#"+$(this).attr("id")+"']").addClass("active");
	            }
	        });
	    });
		
	    var page = window.location.pathname.substr(6);
	    page = page.replace(/\//g, "_");
	    if(page && $("#"+page).offset()){
	    	if($("#"+page).offset().top > 510){
				$("#leftBox").animate({scrollTop: $("#"+page).offset().top - 81}, 1000);
	    	}
		}
	});
</script>