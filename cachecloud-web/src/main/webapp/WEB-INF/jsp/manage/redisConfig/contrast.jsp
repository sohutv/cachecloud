<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp"%>
<!DOCTYPE html>
<html lang="en">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<title>配置预览</title>
</head>
	<body STYLE="BACKGROUND-COLOR:#000;color:#FFF">

		==================================<font color="green">1.${currentVersion.name}&${upgradeVersion.name} 配置差异项:</font>======================================
		<br/>
		版本<a style="color:green" href="/manage/redisConfig/init?versionid=${upgradeVersion.id}" targer="_self">${upgradeVersion.name}</a> 差异项数:(<font style="color:red">${upgradeConfigMap.size()}</font>)<br/>
		<c:forEach items="${upgradeConfigMap}" var="upgradeConfig">
			<font color="red">${upgradeConfig.key}:${upgradeConfig.value}</font><br/>
		</c:forEach>
		<br/>
		版本<a style="color:green" href="/manage/redisConfig/init?versionid=${currentVersion.id}" targer="_self">${currentVersion.name}</a> 差异项数:(<font style="color:red">${currentConfigMap.size()}</font>)<br/>
		<c:forEach items="${currentConfigMap}" var="currentConfig">
			<font style="color:red">${currentConfig.key}:${currentConfig.value}</font><br/>
		</c:forEach>
		<br/>
		==================================<font color="green">2.${currentVersion.name}&${upgradeVersion.name} 配置相同项:</font>===================================================
		<br/>
		配置项数:(<font color="green">${sameConfigMap.size()}</font>)<br/>
		<c:forEach items="${sameConfigMap}" var="sameConfig">
			<font color="white">${sameConfig.key}:${sameConfig.value}</font><br/>
		</c:forEach>
	</body>
</html>
