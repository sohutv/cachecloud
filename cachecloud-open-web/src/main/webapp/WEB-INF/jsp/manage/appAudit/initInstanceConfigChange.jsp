<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<title>CacheCloud管理后台</title>
	<meta http-equiv="X-UA-Compatible" content="IE=edge">
	
	<%@include file="/WEB-INF/jsp/manage/include/cache_cloud_main_css.jsp" %>
		
</head>

<body class="page-header-fixed">
	<%@include file="/WEB-INF/jsp/manage/include/head.jsp" %>

	<%@include file="/WEB-INF/jsp/manage/include/left.jsp" %>
	
	<%@include file="instanceConfigChangeDetail.jsp" %>
	
	<%@include file="/WEB-INF/jsp/manage/include/foot.jsp" %>

	<%@include file="/WEB-INF/jsp/manage/include/cache_cloud_main_js.jsp" %>
	
</body>
</html>