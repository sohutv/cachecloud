<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp"%>
<div class="row">
    <div class="col-md-12 page-header">
    	实例故障情况
    </div>
    <div style="margin-top: 20px">
        <table class="table table-bordered table-striped table-hover">
            <thead>
            <tr>
                <td>应用ID</td>
                <td>实例ip:port</td>
                <td>实例状态</td>
                <td>类型</td>
                <td>触发时间</td>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="instance" items="${list}" varStatus="status">
                <tr>
                    <td>
                        <a href="/admin/app/index.do?appId=${instance.appId}" target="_blank">${instance.appId}</a>
                    </td>
                    <td>
                        <a href="/admin/instance/index.do?instanceId=${instance.instId}" target="_blank">${instance.ip}:${instance.port}</a>
                    </td>
                    <td>${instance.statusDesc}</td>
                    <td>${instance.typeDesc}</td>
                    <td><fmt:formatDate value="${instance.createTime}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </div>
</div>
