<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<div class="container">
<br/>
<div class="row">
    <div class="col-md-12">
        <div class="page-header">
            <h4>当前客户端连接信息</h4>
        </div>
        <table class="table table-striped table-hover">
            <thead>
            <tr>
                <td>连接信息</td>
            </tr>
            </thead>
            <tbody>
            <c:forEach items="${clientList}" var="clientInfo" varStatus="status">
                <tr>
                    <td>${clientInfo}</td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </div>
</div>
</div>
