<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp"%>

<!DOCTYPE html>
<html lang="en">
<head>
    <title>CacheCloud异常</title>
    <jsp:include page="/WEB-INF/include/head.jsp"/>
</head>
<body>
	<div class="container">
		<div class="row">
			<div class="page-header">
                <h4>
                	<img width="70" height="60" src="/resources/img/cry.jpg">
                	出错了！请联系我们: 
                </h4>
            </div>
            <div id="contact" class="page-body">
               <div class="well">
			   	  <jsp:include page="/WEB-INF/include/contact.jsp"/>
			   </div>
            </div>
			
		</div>
	</div>
</body>
</html>
