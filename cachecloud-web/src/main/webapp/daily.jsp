<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp"%>

<!DOCTYPE html>
<html lang="en">
<head>
    <title>CacheCloud值班电话</title>
    <jsp:include page="/WEB-INF/include/head.jsp"/>
</head>
<body>
	<div class="container">
		<jsp:include page="/WEB-INF/include/headMenu.jsp"/>
		<div class="row">
			<div class="page-header">
                <h4>
                	值班联系人: 
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
