<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp" %>

<div class="page-container">
    <div class="page-content">
        <div class="table-toolbar">

            <div class="btn-group" style="float:right">
                <form class="form-inline" role="form" method="post" action="/manage/app/stat/list" id="search_form">
                    <input name="tabId" id="tabId" value="${tabId}" type="hidden"/>
                    <div class="form-group">
                        <label for="searchDate">&nbsp;查询日期&nbsp;&nbsp;</label>
                        <input type="date" size="15" name="searchDate" id="searchDate" value="${searchDate}"/>
                    </div>
                    <button type="submit" class="btn-4 btn-info ">查询</button>
                    <button class="btn-4 btn-info" onclick="sendExpAppsStatDataEmail()">发送邮件</button>
                </form>
            </div>
        </div>

        <div class="row">
            <div class="col-md-12">
                <div class="portlet box light-grey" id="clientIndex">
                    <div class="portlet-title">
                        <div class="caption">
                            <i class="fa fa-globe"></i>应用列表指标上报情况统计
                        </div>
                        <div class="tools">
                            <a href="javascript:;" class="collapse"></a>
                        </div>
                    </div>
                    <table class="table table-striped table-bordered table-hover" id="tableDataList">
                        <thead>
                        <tr>
                            <th>appId</th>
                            <th>应用名称</th>
                            <th>是否测试</th>
                            <th>延迟事件</th>
                            <th>慢查询</th>
                            <th>连接异常</th>
                            <th>avg连接异常耗时(ms)</th>
                            <th>命令超时</th>
                            <th>avg命令超时耗时(ms)</th>
                            <th>命令调用</th>
                            <th>avg命令耗时(ms)</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach items="${list}" var="machine">
                            <tr class="odd gradeX">
                                <td>
                                    <c:set var="app_id" value="${machine.appId}"></c:set>
                                    <a target="_blank"
                                       href="/manage/app/index?appId=${machine.appId}">${machine.appId}</a>
                                </td>
                                <td>
                                    <a target="_blank"
                                       href="/admin/app/index?appId=${machine.appId}">${machine.name}</a>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${machine.isTest == 1}">测试</c:when>
                                        <c:otherwise>正式</c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <a target="_blank" href="/admin/app/index?searchDate=${startDate}&appId=${machine.appId}&tabTag=app_latency">
                                        <c:set var="latency_count"
                                               value="${appClientGatherStatMap[app_id]['latency_count']}"></c:set>
                                        <c:if test="${latency_count==null}"><c:set var="latency_count"
                                                                                   value="0"></c:set></c:if>
                                            ${latency_count}
                                    </a>
                                </td>
                                <td>
                                    <a target="_blank" href="/admin/app/index?searchDate=${startDate}&appId=${machine.appId}&tabTag=app_latency">
                                        <c:set var="slow_log_count"
                                               value="${appClientGatherStatMap[app_id]['slow_log_count']}"></c:set>
                                        <c:if test="${slow_log_count==null}"><c:set var="slow_log_count"
                                                                                    value="0"></c:set></c:if>
                                            ${slow_log_count}
                                    </a>
                                </td>
                                <td>
                                    <a target="_blank"
                                       href="/client/show/index?searchDate=${startDate}&appId=${machine.appId}&tabTag=app_client_exception_statistics">
                                        <c:set var="conn_exp_count"
                                               value="${appClientGatherStatMap[app_id]['conn_exp_count']}"></c:set>
                                        <c:if test="${conn_exp_count==null}"><c:set var="conn_exp_count"
                                                                                    value="0"></c:set></c:if>
                                            ${conn_exp_count}
                                    </a>
                                </td>
                                <td>
                                    <a target="_blank"
                                       href="/client/show/index?searchDate=${startDate}&appId=${machine.appId}&tabTag=app_client_exception_statistics">
                                        <c:set var="avg_conn_exp_cost"
                                               value="${appClientGatherStatMap[app_id]['avg_conn_exp_cost']}"></c:set>
                                        <c:if test="${avg_conn_exp_cost==null}"><c:set var="avg_conn_exp_cost"
                                                                                       value="0"></c:set></c:if>
                                            ${avg_conn_exp_cost}
                                    </a>
                                </td>
                                <td>
                                    <a target="_blank"
                                       href="/client/show/index?searchDate=${startDate}&appId=${machine.appId}&tabTag=app_client_exception_statistics">
                                        <c:set var="cmd_exp_count"
                                               value="${appClientGatherStatMap[app_id]['cmd_exp_count']}"></c:set>
                                        <c:if test="${cmd_exp_count==null}"><c:set var="cmd_exp_count"
                                                                                   value="0"></c:set></c:if>
                                            ${cmd_exp_count}
                                    </a>
                                </td>
                                <td>
                                    <a target="_blank"
                                       href="/client/show/index?searchDate=${startDate}&appId=${machine.appId}&tabTag=app_client_exception_statistics">
                                        <c:set var="avg_cmd_exp_cost"
                                               value="${appClientGatherStatMap[app_id]['avg_cmd_exp_cost']}"></c:set>
                                        <c:if test="${avg_cmd_exp_cost==null}"><c:set var="avg_cmd_exp_cost"
                                                                                      value="0"></c:set></c:if>
                                            ${avg_cmd_exp_cost}
                                    </a>
                                </td>
                                <td>
                                    <a target="_blank"
                                       href="/client/show/index?searchDate${startDate}&appId=${machine.appId}&tabTag=app_client_command_statistics">
                                        <c:set var="cmd_count"
                                               value="${appClientGatherStatMap[app_id]['cmd_count']}"></c:set>
                                        <c:if test="${cmd_count==null}"><c:set var="cmd_count" value="0"></c:set></c:if>
                                            ${cmd_count}
                                    </a>
                                </td>
                                <td>
                                    <a target="_blank"
                                       href="/client/show/index?searchDate${startDate}&appId=${machine.appId}&tabTag=app_client_command_statistics">
                                        <c:set var="avg_cmd_cost"
                                               value="${appClientGatherStatMap[app_id]['avg_cmd_cost']}"></c:set>
                                        <c:if test="${avg_cmd_cost==null}"><c:set var="avg_cmd_cost" value="0"></c:set></c:if>
                                            ${avg_cmd_cost}
                                    </a>
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


<script type="text/javascript">
    $(function () {
        var searchDate = $('#searchDate').val();
        if (searchDate == null || searchDate == '') {
            var time = new Date();
            var day = ("0" + time.getDate()).slice(-2);
            var month = ("0" + (time.getMonth() + 1)).slice(-2);
            var today = time.getFullYear() + "-" + (month) + "-" + (day);
            $('#searchDate').val(today);
        }
    })

    //验证是数字
    function testisNum(id) {
        var value = document.getElementById(id).value;
        if (value != "" && isNaN(value)) {
            alert("请输入数字类型!");
            document.getElementById(id).value = "";
            document.getElementById(id).focus();
        }
    }

    function sendExpAppsStatDataEmail() {
        var searchDate = document.getElementById("searchDate").value;
        $.get('/manage/app/tool/sendExpAppsStatDataEmail.json', {searchDate: searchDate});
        alert("异常应用日报已发送，请查收")
    }
</script>