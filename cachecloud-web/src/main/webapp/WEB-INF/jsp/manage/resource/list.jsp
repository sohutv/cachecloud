<%@ taglib prefix="html" uri="http://struts.apache.org/tags-html" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<script type="text/javascript" src="/resources/bootstrap/jquery/jquery-1.11.0.js"></script>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>CacheCloud管理后台</title>
    <meta http-equiv="X-UA-Compatible" content="IE=edge">

    <%@include file="/WEB-INF/jsp/manage/include/cache_cloud_main_css.jsp" %>

    <link href="/resources/css/mem-cloud.css" rel="stylesheet"/>


    <!-- add -->
    <link href="/resources/css/common.css" rel="stylesheet" type="text/css"/>
    <script type="text/javascript" src="/resources/highchart3/js/highcharts.js"></script>
    <script type="text/javascript" src="/resources/highchart3/js/modules/exporting.js"></script>
    <script type="text/javascript" src="/resources/js/myhighchart.js?<%=System.currentTimeMillis()%>"></script>
</head>

<body class="page-header-fixed">
<%@include file="/WEB-INF/jsp/manage/include/head.jsp" %>

<%@include file="/WEB-INF/jsp/manage/include/left.jsp" %>

<%@include file="index.jsp" %>

<%@include file="/WEB-INF/jsp/manage/include/foot.jsp" %>

<%@include file="/WEB-INF/jsp/manage/include/cache_cloud_paginator_js.jsp" %>

</body>
</html>

