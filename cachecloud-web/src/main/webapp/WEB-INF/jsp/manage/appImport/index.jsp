<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp" %>
<% request.setAttribute("vEnter", "\n");%>

<div class="page-container">
    <div class="page-content">
        <div class="row">
            <div class="col-md-12">
                <h2 class="page-header">
                    应用导入列表
                </h2>
            </div>
        </div>

        <br/>
        <div class="row">
            <div class="col-md-12">
                <div class="portlet box light-grey" id="clientIndex">

                    <table class="table table-striped table-bordered table-hover" id="tableDataList">
                        <thead>
                        <tr>
                            <td>序号</td>
                            <th>源：实例信息</th>
                            <th>目标：应用id</th>
                            <th>应用部署任务id</th>
                            <th>迁移任务id</th>
                            <th>创建时间</th>
                            <th>更新时间</th>
                            <th>导入状态</th>
                            <th>查看流程</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach items="${appImportList}" var="record" varStatus="status">
                            <c:set var="app_id" value="${record.appId}"></c:set>
                            <tr>
                                <td>${status.index + 1}</td>
                                <td>
                                    <c:set var="instanceInfo"
                                           value="${fn:replace(record.instanceInfo, vEnter, '<br/>')}"/>
                                        ${instanceInfo}
                                </td>
                                <td>
                                    <a target="_blank" href="/manage/app/index?appId=${app_id}">${app_id}</a>
                                </td>
                                <td>
                                    <c:if test="${record.appBuildTaskId>0}">
                                    <a target="_blank" href="/manage/task/flow?taskId=${record.appBuildTaskId}">${record.appBuildTaskId}</a>
                                    </c:if>
                                </td>
                                <td>
                                    <c:if test="${record.migrateId>0}">${record.migrateId}</c:if>
                                </td>
                                <td>
                                    <fmt:formatDate value="${record.createTime}" pattern="yyyy-MM-dd HH:mm:ss"/>
                                </td>
                                <td>
                                    <fmt:formatDate value="${record.updateTime}" pattern="yyyy-MM-dd HH:mm:ss"/>
                                </td>
                                <td>${appImportStatusMap[record.status].info}</td>
                                <td>
                                    <c:if test="${record.status==0}">
                                        <a target="_blank" type="button" class="btn btn-small btn-success"
                                           href="/import/app/init?importId=${record.id}">导入</a>
                                    </c:if>
                                    <c:if test="${record.status!=0}">
                                        <a target="_blank" type="button" class="btn btn-small btn-info"
                                           href="/import/app/init?importId=${record.id}">查看</a>
                                    </c:if>
                                </td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</div>

