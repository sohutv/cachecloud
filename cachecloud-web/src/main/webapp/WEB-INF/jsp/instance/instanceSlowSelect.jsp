<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<div class="container">
    <br/>

    <div class="row">
        <div class="col-md-12">
            <div class="page-header">
                <h4>慢查询列表</h4>
            </div>
            <table class="table table-striped table-hover">
                <thead>
                <tr>
                    <td>id</td>
                    <td>时间</td>
                    <td>执行耗时(微秒)</td>
                    <td>命令</td>
                </tr>
                </thead>
                <tbody>
                <c:forEach items="${redisSlowLogs}" varStatus="status" var="redisSlowLog">
                    <tr>
                        <td>${redisSlowLog.id}</td>
                        <td>${redisSlowLog.timeStamp}</td>
                        <td><fmt:formatNumber value="${redisSlowLog.executionTime}" pattern="#,#00"/></td>
                        <td>${redisSlowLog.command}</td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </div>
</div>
