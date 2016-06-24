<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp"%>
<!DOCTYPE html>
<html lang="en">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<title>配置预览</title>
</head>
	<body STYLE="BACKGROUND-COLOR:#000;color:#FFF">
		<c:choose>
			<c:when test="${type == 2}">
				Redis Cluster配置，所用参数port=${port}
			</c:when>
			<c:when test="${type == 5}">
				Redis Sentinel配置，所用参数masterName=${masterName},host:port=${host}:${port}, sentinelPort=${sentinelPort}
			</c:when>
			<c:when test="${type == 6}">
				Redis普通节点配置，所用参数port=${port},maxmemory=${maxMemory}
			</c:when>
		</c:choose>
		
		<br/><br/>配置模板预览:<br/>
	
		<c:forEach items="${configList}" var="line">
			<font color="white">${line}</font><br/>
		</c:forEach>
	</body>
</html>
