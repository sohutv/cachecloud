<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp" %>
<link rel="stylesheet" type="text/css" href="/resources/select/bootstrap-select.css"/>
<script src="/resources/manage/plugins/jquery-1.10.2.min.js"></script>
<script type="text/javascript">var jQuery_1_10_2 = $;</script>
<script src="/resources/manage/plugins/bootstrap/js/bootstrap.min.js" type="text/javascript"></script>
<script type="text/javascript" src="/resources/select/bootstrap-select.js"></script>

<!DOCTYPE html>
<html lang="en">
<head>
    <title>CacheCloud实例运维</title>
    <jsp:include page="/WEB-INF/include/head.jsp"/>
    <script type="text/javascript" src="/resources/js/jquery-console.js"></script>

</head>
<body role="document">
<div class="container">
    <jsp:include page="/WEB-INF/include/headAdmin.jsp"/>
    <div class="tabbable-custom">
        <div class="page-container">
            <div class="page-content">
                <div class="row">
                    <div class="col-md-12">
                        <div class="portlet box light-grey" id="configIndex">
                            <div class="portlet-title">
                                <div class="caption">
                                    <i class="fa fa-globe"></i>实例redis配置检测结果
                                </div>
                                <div class="tools">
                                    <a href="javascript:;" class="collapse"></a>
                                </div>
                            </div>

                            <table class="table table-striped table-bordered table-hover" name="checkResultList">
                                <thead>
                                <tr>
                                    <th>实例id</th>
                                    <th>实例</th>
                                    <th>版本</th>
                                    <th>所属应用id</th>
                                    <th>所属应用名称</th>
                                    <th>检测时间</th>
                                    <th>检测条件</th>
                                    <th>异常信息</th>
                                    <th>操作</th>
                                </tr>
                                </thead>
                                <tbody>
                                <c:forEach items="${checkResultList}" var="checkResult">
                                    <c:forEach items="${checkResult.instanceCheckList}" var="instanceCheck">
                                        <c:if test="${instanceCheck.success==false}">
                                            <tr class="odd gradeX">
                                                <td>
                                                    <a href="/admin/instance/index?instanceId=${instanceCheck.instanceInfo.id}"
                                                       target="_blank">${instanceCheck.instanceInfo.id}</a>
                                                </td>
                                                <td>
                                                    ${instanceCheck.instanceInfo.ip}:${instanceCheck.instanceInfo.port}
                                                </td>
                                                <td>
                                                    <c:forEach items="${redisVersionList}" var="redisVersion">
                                                        <c:if test="${redisVersion.id==checkResult.appDesc.versionId}">
                                                            ${redisVersion.name}
                                                        </c:if>
                                                    </c:forEach>
                                                </td>
                                                <td>
                                                    <a href="/manage/app/index?appId=${checkResult.appDesc.appId}"
                                                       target="_blank">${checkResult.appDesc.appId}</a>
                                                </td>
                                                <td>
                                                    <a href="/manage/app/index?appId=${checkResult.appDesc.appId}"
                                                       target="_blank">${checkResult.appDesc.name}</a>
                                                </td>
                                                <td>
                                                    ${checkResult.createTimeStr}
                                                </td>
                                                <td>
                                                    ${checkResult.configName}
                                                </td>
                                                <td>
                                                    <c:if test="${instanceCheck.success==true}">否</c:if>
                                                    <c:if test="${instanceCheck.success==false}">

                                                        期望值：
                                                        <c:forEach items="${compareTypeList}" var="compareTypeEnum">
                                                            <c:if test="${checkResult.compareType == compareTypeEnum.type}">
                                                                ${compareTypeEnum.info}
                                                            </c:if>
                                                        </c:forEach>
                                                        ${checkResult.expectValue}
                                                        <br>
                                                        实际值：
                                                        ${instanceCheck.realValue}
                                                    </c:if>
                                                </td>
                                                <td>
                                                    <c:if test="${instanceCheck.success==true}"></c:if>
                                                    <c:if test="${instanceCheck.success==false}">
                                                        <button class="btn btn-warning btn-sm" style="float: right;">
                                                            <a href="/manage/app/index?appId=${checkResult.appDesc.appId}&configName=${checkResult.configName}&&expectValue=${checkResult.expectValue}&instanceIds=${instanceCheck.instanceInfo.id}"
                                                               target="_blank"><font style="color: white">查看修复</font></a>
                                                        </button>
                                                    </c:if>
                                                </td>
                                            </tr>
                                        </c:if>
                                    </c:forEach>
                                </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
<jsp:include page="/WEB-INF/include/foot.jsp"/>