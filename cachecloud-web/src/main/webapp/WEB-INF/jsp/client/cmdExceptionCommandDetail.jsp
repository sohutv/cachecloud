<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <title>客户端超时命令详情</title>
    <jsp:include page="/WEB-INF/include/head.jsp"/>
</head>
<body role="document">
<div class="container">
    <jsp:include page="/WEB-INF/include/headMenu.jsp"/>
    <div class="row page-header">
        <h4>&nbsp;&nbsp;应用：<label class="label label-success">${appId}</label>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
            查询时间：<label class="label label-success">${searchTime}</label>
        </h4>
    </div>

    <div class="row">
        <div class="col-md-12">
            <table class="table table-striped table-hover">
                <thead>
                <tr>
                    <td>序号</td>
                    <td>实例信息</td>
                    <td>次数</td>
                    <td>平均耗时(单位:毫秒)</td>
                </tr>
                </thead>
                <tbody>
                <c:forEach items="${sumCmdExpStatMap}" var="item" varStatus="stats">
                    <tr>
                        <td>${stats.index + 1}</td>
                        <td><a href="#${item.key}">${item.key}</a></td>
                        <td>${item.value['count']}</td>
                        <td>${item.value['cost']}</td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </div>

    <c:forEach items="${latencyCommandDetailMap}" var="item" varStatus="stats">
        <div style="margin-top: 20px">
            <div class="page-header" id="${item.key}">
                <h4>${item.key}</h4>
            </div>
            <table class="table table-bordered table-striped table-hover">
                <thead>
                <tr>
                    <td>序号</td>
                    <td>执行时间</td>
                    <td>命令明文</td>
                    <td>参数(长参数裁剪)</td>
                    <td>参数字节数</td>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="latencyCommandDetail" items="${item.value}" varStatus="status">
                    <tr>
                        <td>${status.index + 1}</td>
                        <td>${latencyCommandDetail.format_invoke_time}</td>
                        <td>${latencyCommandDetail.command}</td>
                        <td>${latencyCommandDetail.args}</td>
                        <td>${latencyCommandDetail.size}</td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </c:forEach>

</div>
<jsp:include page="/WEB-INF/include/foot.jsp"/>
<script type="text/javascript" src="/resources/js/docs.min.js"></script>
</body>
</html>