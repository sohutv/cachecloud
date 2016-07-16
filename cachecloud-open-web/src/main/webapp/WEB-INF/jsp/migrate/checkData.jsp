<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp"%>
<!DOCTYPE html>
<html lang="en">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<title>采样校验源数据和目标数据</title>
</head>
	<body STYLE="BACKGROUND-COLOR:#000;color:#FFF">
		<c:forEach items="${checkDataResultList}" var="line">
			<font color="white">${line}</font><br/>
		</c:forEach>
	</body>
</html>
