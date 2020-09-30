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
	width: 165px;
	height: 500px;
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
	<li id="quick_index"><a href="${request.contextPath}/wiki/quickstart/index">快速接入</a></li>
	<li id="intro_index"><a href="${request.contextPath}/wiki/intro/index">系统介绍</a></li>
	<ul class="nav sub-menu">
		<li id="intro_redisVersion"><a href="${request.contextPath}/wiki/intro/redisVersion">Redis版本说明</a></li>
		<li id="intro_releaseNote"><a href="${request.contextPath}/wiki/intro/releaseNote">CC客户端版本说明</a></li>
	</ul>
	<li><a href="${request.contextPath}/wiki/access/index">系统接入</a></li>
	<ul class="nav sub-menu">
		<li id="access_init"><a href="${request.contextPath}/wiki/access/init">系统初始化</a></li>
		<li id="access_config"><a href="${request.contextPath}/wiki/access/config">系统配置说明</a></li>
	<li id="access_resource"><a href="${request.contextPath}/wiki/access/resource">系统资源管理</a></li>
	<li id="access_client"><a href="${request.contextPath}/wiki/access/client">客户端接入文档</a></li>
		<%--<li id="access_docker"><a href="${request.contextPath}/wiki/access/docker">docker部署文档</a></li>--%>
	</ul>
	<li><a href="${request.contextPath}/wiki/function/index">系统功能</a></li>
	<ul class="nav sub-menu">
		<li id="function_statistics"><a href="${request.contextPath}/wiki/function/statistics">全局统计</a></li>
		<li id="function_server-statistic"><a href="${request.contextPath}/wiki/function/server-statistic">运维端指标</a></li>
		<li id="function_client-statistic"><a href="${request.contextPath}/wiki/function/client-statistic">客户端指标</a></li>
		<li id="function_operations"><a href="${request.contextPath}/wiki/function/operations">运维端功能</a></li>
		<li id="function_client"><a href="${request.contextPath}/wiki/function/client">客户端功能</a></li>
		<li id="function_job"><a href="${request.contextPath}/wiki/function/job">工单流程</a></li>
	</ul>
	<li><a href="${request.contextPath}/wiki/architecture/index">系统架构</a></li>
	<ul class="nav sub-menu">
		<li id="architecture_server"><a href="${request.contextPath}/wiki/architecture/service">服务架构</a></li>
		<li id="architecture_logic"><a href="${request.contextPath}/wiki/architecture/tech">技术架构</a></li>
	</ul>
	<li><a href="${request.contextPath}/wiki/operate/index">运维手册</a></li>
	<ul class="nav sub-menu">
		<li id="operate_baseConcept"><a href="${request.contextPath}/wiki/operate/baseConcept">基础概念</a></li>
		<li id="operate_baseOperate"><a href="${request.contextPath}/wiki/operate/baseOperate">基础运维</a></li>
		<li id="operate_appDeploy"><a href="${request.contextPath}/wiki/operate/appDeploy">应用部署</a></li>
		<li id="operate_migrateTool"><a href="${request.contextPath}/wiki/operate/migrateTool">迁移工具</a></li>
		<li id="operate_appMigrate"><a href="${request.contextPath}/wiki/operate/appMigrate">应用迁移</a></li>
		<li id="operate_appAlert"><a href="${request.contextPath}/wiki/operate/appAlert">Redis报警指标</a></li>
		<li id="operate_ssh"><a href="${request.contextPath}/wiki/operate/ssh">Redis机器授权</a></li>
		<li id="operate_baseOptimize"><a href="${request.contextPath}/wiki/operate/baseOptimize">系统运维优化</a></li>
		<li id="access_resource"><a href="${request.contextPath}/wiki/access/resource">系统资源管理</a></li>
	</ul>
	<li><a href="${request.contextPath}/wiki/troubleshooting/index">常见问题</a></li>
	<ul class="nav sub-menu">
		<li id="troubleshooting_exception"><a href="${request.contextPath}/wiki/troubleshooting/exception">常见Jedis异常类</a></li>
		<li id="troubleshooting_jedispoolconfig"><a href="${request.contextPath}/wiki/troubleshooting/jedispoolconfig">JedisPool优化</a></li>
		<li id="troubleshooting_bigkey"><a href="${request.contextPath}/wiki/troubleshooting/bigkey">bigkey的寻找和优化</a></li>
		<li id="troubleshooting_hotkey"><a href="${request.contextPath}/wiki/troubleshooting/hotkey">hotkey的寻找和优化</a></li>
		<li id="troubleshooting_memory"><a href="${request.contextPath}/wiki/troubleshooting/memory">Redis内存优化</a></li>
		<li id="troubleshooting_liunx"><a href="${request.contextPath}/wiki/troubleshooting/liunx">Liunx系统优化</a></li>
		<li id="troubleshooting_activefrag"><a href="${request.contextPath}/wiki/troubleshooting/activefrag">Activefrag引起redis周期性延迟</a></li>
	</ul>
</ul>
<script type="text/javascript">
	$(function(){
		var id = window.location.pathname.substring(6).replace(/\//g,"_");
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