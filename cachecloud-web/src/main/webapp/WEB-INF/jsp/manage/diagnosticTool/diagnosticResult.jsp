<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp" %>


<div class="page-container">
    <div class="page-content">
        <div class="row">
            <div class="col-md-12">
                <h2 class="page-header">
                    应用诊断任务列表
                </h2>
            </div>
        </div>

        <div class="row">
            <div class="col-md-12">
                <div style="float:right">
                    <form class="form-inline" role="form" method="post" action="/manage/app/tool/diagnostic/result"
                          id="appList" name="ec">
                        <div class="form-group">
                            <input type="text" class="form-control" id="appId" name="appId"
                                   value="${appId}" placeholder="应用id">
                        </div>
                        <div class="form-group">
                            <input type="text" class="form-control" id="parentTaskId" name="parentTaskId"
                                   value="${parentTaskId}" placeholder="任务id">
                        </div>
                        <div class="form-group">
                            <input type="text" class="form-control" id="auditId" name="auditId"
                                   value="${auditId}" placeholder="审批id">
                        </div>
                        <div class="form-group">
                            <select name="type" class="form-control">
                                <option value="" <c:if test="${type == ''}">selected</c:if>>
                                    诊断类型
                                </option>
                                <c:forEach items="${diagnosticTypeMap}" var="entry">
                                    <option value="${entry.key}" <c:if test="${entry.key == type}">selected</c:if>>
                                            ${entry.value}
                                    </option>
                                </c:forEach>
                            </select>
                        </div>

                        <div class="form-group">
                            <select name="diagnosticStatus" class="form-control">
                                <option value="" <c:if test="${diagnosticStatus == ''}">selected</c:if>>
                                    诊断状态
                                </option>
                                <option value="0" <c:if test="${diagnosticStatus == 0}">selected</c:if>>
                                    诊断中
                                </option>
                                <option value="1" <c:if test="${diagnosticStatus == 1}">selected</c:if>>
                                    诊断完成
                                </option>
                                <option value="2" <c:if test="${diagnosticStatus == 2}">selected</c:if>>
                                    诊断异常
                                </option>
                            </select>
                        </div>
                        <button type="submit" class="btn btn-success">查询</button>
                    </form>
                </div>
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
                            <th>appId</th>
                            <th>应用名称</th>
                            <th>诊断类型</th>
                            <th>任务id</th>
                            <th>子任务id</th>
                            <th>审批id</th>
                            <th>节点</th>
                            <th>诊断条件</th>

                            <th>创建时间</th>
                            <th>诊断状态</th>
                            <th>诊断耗时</th>
                            <th>诊断结果</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach items="${diagnosticTaskRecordList}" var="record" varStatus="status">
                            <c:set var="app_id" value="${record.appId}"></c:set>
                            <tr>
                                <td>${status.index + 1}</td>
                                <td>
                                    <a target="_blank" href="/manage/app/index?appId=${app_id}">${app_id}</a>
                                </td>
                                <td>
                                    <a target="_blank"
                                       href="/admin/app/index?appId=${app_id}">${appDescMap[app_id].name}</a>
                                </td>
                                <td>
                                        ${diagnosticTypeMap[record.type]}
                                </td>
                                <td>
                                    <a target="_blank" href="/manage/task/flow?taskId=${record.parentTaskId}">
                                            ${record.parentTaskId}
                                    </a>
                                </td>
                                <td>
                                    <a target="_blank" href="/manage/task/flow?taskId=${record.taskId}">
                                            ${record.taskId}
                                    </a>
                                </td>
                                <td>
                                    <a target="_blank"
                                       href="/manage/app/auditList?auditId=${record.auditId}">
                                            ${record.auditId}
                                    </a>
                                </td>
                                <td>
                                        ${record.node}
                                </td>
                                <td>
                                        ${record.diagnosticCondition}
                                </td>
                                <td>
                                    <fmt:formatDate value="${record.createTime}" pattern="yyyy-MM-dd HH:mm:ss"/>
                                </td>
                                <td>
                                    <c:if test="${record.status==0}">诊断中</c:if>
                                    <c:if test="${record.status==1}">诊断完成</c:if>
                                    <c:if test="${record.status==2}">诊断异常</c:if>
                                </td>
                                <td>
                                    <c:if test="${record.status==1}">${record.formatCostTime}</c:if>
                                </td>
                                <td>
                                    <c:if test="${record.status==1&&record.type!=4}">
                                        <button type="button" class="btn btn-sm btn-info"
                                                data-target="#modal-diagnosticResult" data-toggle="modal"
                                                data-rediskey="${record.redisKey}"
                                                data-title="应用${app_id} 节点${record.node}">
                                            查看结果
                                        </button>
                                    </c:if>
                                </td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>


        <div id="modal-diagnosticResult" class="modal fade" tabindex="-1">
            <div class="modal-dialog" style="width: max-content">
                <div class="modal-content">

                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal" aria-hidden="true"></button>
                        <h4 class="modal-title">
                            诊断结果
                            <small><label id="modal-title" style="color: #00BE67"></label></small>
                        </h4>
                    </div>

                    <form class="form-horizontal form-bordered form-row-stripped">
                        <div class="modal-body" style="height:500px; overflow:scroll;">
                            <div class="row">
                                <!-- 控件开始 -->
                                <div class="col-md-12">
                                    <table class="table table-bordered table-striped table-hover">
                                        <thead id="diagnosticResultCount"></thead>
                                        <tbody id="diagnosticResultTable"></tbody>
                                    </table>
                                </div>
                            </div>
                        </div>

                        <div class="modal-footer">
                            <button type="button" data-dismiss="modal" class="btn">Close</button>
                        </div>
                    </form>
                </div>
            </div>
        </div>

    </div>
</div>

