<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <title>模块配置信息</title>
    <jsp:include page="/WEB-INF/include/head.jsp"/>
</head>
<body role="document">
<div class="container">
    <jsp:include page="/WEB-INF/include/headMenu.jsp"/>
    <div class="row">
        <div class="page-header">
            <h4>Redisearch模块配置信息</h4>
        </div>
        <div style="margin-top: 20px">
            <table class="table table-bordered table-striped table-hover">
                <thead>
                <tr>
                    <td>配置项</td>
                    <td>配置值</td>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="configMap" items="${configList}" varStatus="status">
                    <c:forEach var="config" items="${configMap}" varStatus="status">
                        <tr>
                            <td>${config.key}</td>
                            <td>${config.value}</td>
                        </tr>
                    </c:forEach>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </div>
    <jsp:include page="/WEB-INF/include/foot.jsp"/>
</div>
</body>
</html>