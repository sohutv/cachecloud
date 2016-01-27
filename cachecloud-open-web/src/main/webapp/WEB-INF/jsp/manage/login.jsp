<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
	<title>CacheCloud系统</title>
	
	<link rel="stylesheet" href="/resources/external/d2ea59be8e96916df3d534861fad3a96.reg.css" type="text/css" media="screen,print">
	<script src="/resources/external/40779259d6cb8446472f5b9f98386159.instant.min.js"></script>
	<link rel="stylesheet" type="text/css" href="/resources/external/play.css">
	<script type="text/javascript">
		function jumpToUserRegister(){
			window.location = "/user/register";
		}
		function login(){
			var userName = document.getElementById("userName");
			var password = document.getElementById("password");
			var isAdmin = document.getElementById("isAdmin");
			if(userName.value == ""){
	        	alert("用户名不能为空!");
	        	userName.focus();
				return false;
	        }
			if(password.value == ""){
	        	alert("密码不能为空!");
				password.focus();
				return false;
	        }
			$.post(
				'/manage/loginIn.json',
				{
					userName: userName.value,
					password: password.value,
					isAdmin: isAdmin.checked
				},
	            function(data){
					var success = data.success;
					var admin = data.admin;
	                if(success==1){
	                	if(admin == 1){
		                	window.location = "/manage/total/list.do";
	                	}else{
		                	window.location = "/admin/app/list.do";
	                	}
	                }else if(success == 0){
	                	alert("用户名或者密码错误，请重新输入!");
	                }else if(success == -1){
	                	alert("系统不存在该用户名，请确认该用户申请了cachecloud权限!");
	                }else if(success == -2){
	                	alert("您不是超级管理员!");
	                }
	            }
	         );
		}
	</script>
</head>
<body class="zhi no-auth">
	<div class="wrapper">
		<div class="top">
			<div class="video-bg">
				<div style="max-width: 1500px; margin: 0 auto; z-index: 0; text-align: center;">
					<video autoplay="autoplay" loop="loop" preload=""> 
						<source src="/resources/external/home_video_v3.1.mp4" type="video/mp4"> 
						<source src="/resources/external/home_video_v3.1.webm" type="video/webm">
						<img src="/resources/external/home-bg.png" style="margin: 0 auto; height: 360px; width: 1000;">
					 </video>
				</div>
				<div class="video-mask"></div>
			</div>

			<div class="inner-wrapper">
				<div class="form-wrapper" id="js-form-wrapper">
					<div class="videopopup" data-vid="XNjEwNTk4MjIw">
						<div class="logo"></div>
					</div>

					<div id="js-sign-flow" class="desk-front sign-flow clearfix dialog">
						<div class="view view-signin selected" id="signin" style="opacity: 1; right: 0px; position: relative;">
							<form method="post" class="zu-side-login-box" novalidate="novalidate">
								<div class="email input-wrapper">
									<input type="text" name="userName" id="userName" placeholder="域账户名">
								</div>
								
								<div class="input-wrapper">
									<input type="password" id="password" name="password" placeholder="密码">
								</div>
								
								<div class="button-wrapper command">
									<button class="sign-button" type="button" onclick="jumpToUserRegister()">&nbsp;&nbsp;注&nbsp;&nbsp;&nbsp;册&nbsp;&nbsp;</button>
									&nbsp;&nbsp;
									<button class="sign-button" type="button" onclick="login()">&nbsp;登&nbsp;&nbsp;&nbsp;录&nbsp;</button>
								</div>
								
								<div class="signin-misc-wrapper clearfix">
									<input id="isAdmin" type="checkbox" name="isAdmin" > 超级管理员
									
								</div>
							</form>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
	
	<script src="/resources/external/be1f4e09e99650d8ff09ac81943bc61f.extern_src.min.js"></script>

</body>
</html>