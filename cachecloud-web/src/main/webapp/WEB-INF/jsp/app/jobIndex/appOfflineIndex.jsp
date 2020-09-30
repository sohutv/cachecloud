<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp" %>
<script type="text/javascript" src="/resources/bootstrap/jquery/jquery-1.11.0.js"></script>
<html>
<head>
    <title>CacheCloud客户端</title>
    <jsp:include page="/WEB-INF/include/head.jsp"/>
</head>
<body role="document">
<div class="container">
    <jsp:include page="/WEB-INF/include/headMenu.jsp"/>
    <div class="container">
        <jsp:include page="/WEB-INF/jsp/app/jobIndex/nav.jsp"/>
        <jsp:include page="/WEB-INF/jsp/app/jobIndex/appOffline.jsp"/>
    </div>
    <jsp:include page="/WEB-INF/include/foot.jsp"/>
</div>
</body>
</html>



