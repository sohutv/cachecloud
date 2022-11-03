<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <title>模块信息</title>
    <jsp:include page="/WEB-INF/include/head.jsp"/>
</head>
<body role="document">
<div class="container">
    <jsp:include page="/WEB-INF/include/headMenu.jsp"/>

    <div class="row">
        <div class="page-header">
            <h4>Redisearch模块信息</h4>
        </div>
        <div style="margin-top: 20px">
            <table class="table table-bordered table-striped table-hover">
                <thead>
                <tr>
                    <td>索引名称</td>
                    <td>索引info信息</td>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="index" items="${indexList}" varStatus="status">
                    <tr>
                        <td>${index}</td>
                        <td>
                            <a href="/admin/app/module/redisearch/info?appId=${appId}&indexName=${index}#redisearchInfoId">INFO查询</a>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>


        <div class="page-content" style="margin-top: 40px">
            <h4>索引—— ${indexName} ——INFO信息</h4>
        </div>
        <div style="margin-top: 20px">
            <table id="redisearchInfoId" class="table table-bordered table-striped table-hover">
                <thead>
                <tr>
                    <td>名称</td>
                    <td>信息</td>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="info" items="${infoMap}" varStatus="status">
                    <tr>
                        <td>${info.key}</td>
                        <c:choose>
                            <c:when test="${info.value != null && fn:containsIgnoreCase(info.value, '[') && fn:containsIgnoreCase(info.value, ']')}">
                                <td>
                                    <c:forEach var="subInfo" items="${info.value}" varStatus="status">
                                        ${subInfo}
                                        <br>
                                    </c:forEach>
                                </td>
                            </c:when>
                            <c:otherwise>
                                <td>${info.value}</td>
                            </c:otherwise>
                        </c:choose>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>

    </div>
    <jsp:include page="/WEB-INF/include/foot.jsp"/>
</div>
</body>
</html>