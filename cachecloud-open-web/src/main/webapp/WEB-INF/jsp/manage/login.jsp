<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
	<title>CacheCloud系统</title>
	<link rel="stylesheet" type="text/css" href="/resources/css/login.css">
	<script type="text/javascript" src="/resources/bootstrap/jquery/jquery-1.11.0.min.js"></script>
	<script type="text/javascript">
		function loginIn() {
			var userName = document.getElementById("userName");
			var password = document.getElementById("password");
			var redirectUrl = document.getElementById("redirectUrl").value;
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
					isAdmin: false
				},
	            function(data){
					var success = data.success;
					var admin = data.admin;
	                if(success==1){
	                	if (redirectUrl != "") {
		                	window.location = redirectUrl;
	                	} else {
	                		if(admin == 1){
			                	window.location = "/manage/total/list.do";
		                	}else{
			                	window.location = "/admin/app/list.do";
		                	}
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
	<body>
		<div class="img"><img src="/resources/img/bg.png"></div>
		<div class="container">
		    <div class="logo">
		        <img class="logo-pic" src="/resources/img/logo.png">
		    </div>
		    <div class="info">
		        <form method="post" name="login" autocomplete="off">
		            <div class="input">
		                <span class="info-user">用户名</span><input type="text" id="userName" name="userName" autocomplete="off">
		            </div>
		
		            <div class="input">
		                <span class="info-user">密码</span><input type="password" id="password" name="password" autocomplete="off">
		            </div>
		            
		            <input type="hidden" value="${redirectUrl}" id="redirectUrl" name="redirectUrl">
		            
		            <div class="input login">
		                <input type="button" value="登 录" onclick="loginIn()">
		            </div>
		
		        </form>
		        <a class="register" href="/user/register">新用户注册</a>
		    </div>
		</div>
		<script src="/resources/img/pv.gif"></script>
	</body>
</html>
