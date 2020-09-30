<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp" %>
<script src="/resources/manage/plugins/data-tables/jquery.dataTables.js" type="text/javascript"></script>
<script src="/resources/manage/plugins/data-tables/DT_bootstrap.js" type="text/javascript"></script>
<link href="/resources/manage/plugins/font-awesome/css/font-awesome.min.css" rel="stylesheet" type="text/css"/>
<style>

</style>
<script type="text/javascript">
    var TableManaged = function () {
        return {
            //main function to initiate the module
            init: function () {
                if (!jQuery().dataTable) {
                    return;
                }
                $('#job_tableDataList').dataTable({
                    "searching": true,
                    "scrollX": true,
                    "autoWidth": true,
                    "bSort": true,
                    "bLengthChange": false,
                    "iDisplayLength": 15,
                    "sPaginationType": "bootstrap",
                    "aaSorting": [],
                    "oLanguage": {
                        "oPaginate": {
                            "sFirst": "首页",
                            "sPrevious": "前一页",
                            "sNext": "后一页",
                            "sLast": "尾页"
                        },
                        "sLengthMenu": "每页显示 _MENU_条",
                        "sZeroRecords": "没有找到符合条件的数据",
                        "sInfo": "当前第 _START_ - _END_ 条　共计 _TOTAL_ 条",
                        "sSearch": "搜索：",
                    }
                });
            }
        };
    }();

    $(function () {
        TableManaged.init();
    });
</script>
<div class="col-md-9">
    <div class="row">
    <div class="col-md-12">
        <h3 class="page-header">
            我的工单 &nbsp;&nbsp;
        </h3>
    </div>
    </div>

    <div class="row">
    <div class="col-md-12">
        <table class="table table-striped table-bordered table-hover">
            <tr>
                <td><span style="font-weight:bold">工单总数</span></td>
                <td>${fn:length(jobList)}</td>
                <td><span style="font-weight:bold">完成工单数</span></td>
                <td>${statusStatisMap['1']==null?0:statusStatisMap['1']}</td>
                <td style="color:orange;"><span style="font-weight:bold">待处理工单数</span></td>
                <td style="color:orange;">${(statusStatisMap['0']+statusStatisMap['2'])==null?0:(statusStatisMap['0']+statusStatisMap['2'])}</td>
                <td><span style="font-weight:bold">被驳回工单数</span></td>
                <td>${statusStatisMap['-1']==null?0:statusStatisMap['-1']}</td>
            </tr>
            <tr>
                <td><span style="font-weight:bold">申请应用</span></td>
                <td>${typeStatisMap['0']==null?0:typeStatisMap['0']}</td>
                <td><span style="font-weight:bold">下线应用</span></td>
                <td>${typeStatisMap['10']==null?0:typeStatisMap['10']}</td>
                <td><span style="font-weight:bold">键值分析</span></td>
                <td>${typeStatisMap['6']==null?0:typeStatisMap['6']}</td>
                <td><span style="font-weight:bold">诊断应用</span></td>
                <td>${typeStatisMap['8']==null?0:typeStatisMap['8']}</td>
            </tr>
            <tr>
                <td><span style="font-weight:bold">容量变更</span></td>
                <td>${typeStatisMap['1']==null?0:typeStatisMap['1']}</td>
                <td><span style="font-weight:bold">配置修改</span></td>
                <td>${(typeStatisMap['4']+typeStatisMap['2'])==null?0:(typeStatisMap['4']+typeStatisMap['2'])}</td>
                <td><span style="font-weight:bold">用户注册</span></td>
                <td>${typeStatisMap['3']==null?0:typeStatisMap['3']}</td>
                <td><span style="font-weight:bold">应用数据迁移</span></td>
                <td>${typeStatisMap['11']==null?0:typeStatisMap['11']}</td>

            </tr>

        </table>
        <br/>
    </div>
    </div>
    <div calss="divider"></div>
    <div class="row">
    <div class="col-md-12">
        <table class="table table-bordered table-hover table-responsive table-condensed" id="job_tableDataList">
            <thead>
            <tr>
                <th>序号</th>
                <th>应用ID</th>
                <th>应用名</th>
                <th>申请人</th>
                <th>申请类型</th>
                <th>申请描述</th>
                <th>申请时间</th>
                <th>审批状态</th>
                <th>处理人</th>
                <th>完成时间</th>
                <th>结果</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach items="${jobList}" var="item" varStatus="stat">
                <tr class="odd gradeX">
                    <td>${stat.index + 1}</td>
                    <td>
                        <c:choose>
                            <c:when test="${item.type == 3}">
                                无
                            </c:when>
                            <c:otherwise>
                                <a target="_blank" href="/admin/app/index?appId=${item.appId}">${item.appId}</a>
                            </c:otherwise>
                        </c:choose>
                    </td>
                    <td>
                        <c:choose>
                            <c:when test="${item.type == 3}">
                                无
                            </c:when>
                            <c:otherwise>
                                ${item.appDesc.name}
                            </c:otherwise>
                        </c:choose>
                    </td>
                    <td>${item.userName}</td>
                    <td>${appAuditTypeMap[item.type].info}</td>
                    <td>${item.info}</td>
                    <td><fmt:formatDate pattern="yyyy-MM-dd HH:mm:ss" value="${item.createTime}"/></td>
                    <td>
                        <c:choose>
                        <c:when test="${item.status == 0}"><font style="color: orange;">待审</font></c:when>
                        <c:when test="${item.status == 1}"><font style="color: green;">通过</font></c:when>
                        <c:when test="${item.status == 2}"><font style="color: cornflowerblue;">已受理</font></c:when>
                        <c:when test="${item.status == -1}"><font style="color: darkred;">驳回</c:when>
                        </c:choose>
                    </td>
                    <td>${adminMap[item.operateId].chName}</td>
                    <td>
                        <c:if test="${item.status == 1 || item.status == -1}"><fmt:formatDate pattern="yyyy-MM-dd HH:mm:ss" value="${item.modifyTime}"/></c:if>
                    </td>
                    <td>
                        <c:choose>
                            <c:when test="${item.status == 1 && item.type == 6}"><a target="_blank" href="/admin/app/keyAnalysisResult?appId=${item.appId}&auditId=${item.id}">[查看结果]</a></c:when>
                            <c:when test="${item.status == -1}">${item.refuseReason}</c:when>
                        </c:choose>
                    </td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </div>
    </div>
</div>
