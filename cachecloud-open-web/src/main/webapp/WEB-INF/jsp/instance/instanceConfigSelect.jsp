<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<div class="container">
    <br/>
    <div class="row">
        <div class="col-md-12">
            <div class="page-header">
                <h4>实例当前配置信息</h4>
            </div>
            <table class="table table-striped table-hover">
                <thead>
                <tr>
                    <td>配置项</td>
                    <td>配置值</td>
                </tr>
                </thead>
                <tbody>
                <c:forEach items="${redisConfigList}" var="redisConfig" varStatus="status">
                    <tr>
                        <td>${redisConfig.key}</td>
                        <td>${redisConfig.value}</td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </div>
</div>

