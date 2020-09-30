<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <title>应用延迟详情</title>
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
                    <td>延迟个数</td>
                </tr>
                </thead>
                <tbody>
                <c:forEach items="${sumInstanceLatencyStatMap}" var="item" varStatus="stats">
                    <tr>
                        <td>${stats.index + 1}</td>
                        <td><a href="#${item.key}">${item.key}</a></td>
                        <td>${item.value}</td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </div>

    <c:forEach items="${latencyInfoDetailMap}" var="item" varStatus="stats">
        <div style="margin-top: 20px">
            <div class="page-header" id="${item.key}">
                <c:set var="instanceId" value="${(item.value)[0].instance_id}"></c:set>
                <h5>
                    <li><a href="/admin/instance/index?instanceId=${instanceId}"
                       target="_blank">${item.key}</a>
                    </li>
                </h5>
            </div>
            <table class="table table-bordered table-striped table-hover">
                <thead>
                <tr>
                    <td>序号</td>
                    <td>事件名称</td>
                    <td>延迟时间点</td>
                    <td>延迟耗时(单位：毫秒)</td>
                    <td>关联慢查询</td>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="latencyInfoDetail" items="${item.value}" varStatus="status">
                    <tr>
                        <td>${status.index + 1}</td>
                        <td>${latencyInfoDetail.event}</td>
                        <td>${latencyInfoDetail.execute_date}</td>
                        <td>${latencyInfoDetail.execution_cost}</td>
                        <td>
                            <button type="button" class="btn btn-success" data-target="#slowlog-modal"
                                    data-toggle="modal"
                                    data-instanceid="${latencyInfoDetail.instance_id}"
                                    data-executedate="${latencyInfoDetail.execute_date}">
                                查看关联慢查询
                            </button>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </c:forEach>

</div>


<div id="slowlog-modal" class="modal fade" tabindex="-1">
    <div class="modal-dialog" style="width: max-content">
        <div class="modal-content">

            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true"></button>
                <h4 class="modal-title">关联慢查询</h4>
            </div>

            <form class="form-horizontal form-bordered form-row-stripped">
                <div class="modal-body">
                    <div class="row">
                        <!-- 控件开始 -->
                        <div class="col-md-12">
                            <table class="table table-bordered table-striped table-hover">
                                <thead>
                                <tr>
                                    <td>实例id</td>
                                    <td>redis实例</td>
                                    <td>慢查询id</td>
                                    <td>耗时(单位:微秒)</td>
                                    <td>命令</td>
                                    <td>发生时间</td>
                                </tr>
                                </thead>
                                <tbody id="slowlogTable"></tbody>
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

<jsp:include page="/WEB-INF/include/foot.jsp"/>
<script type="text/javascript" src="/resources/js/docs.min.js">
</script>
</body>
</html>

<script type="text/javascript">
    $('#slowlog-modal').on('shown.bs.modal', function (e) {
        var instanceId = $(e.relatedTarget).data('instanceid');
        var executeDate = $(e.relatedTarget).data('executedate');

        console.log(instanceId + ' ' + executeDate);
        $('#slowlogTable').html('');
        $.get(
            '/admin/app/latencyRelatedSlowLog.json',
            {
                instanceId: instanceId,
                executeDate: executeDate
            },
            function (data) {
                var instanceSlowLogList = data.instanceSlowLogList;

                instanceSlowLogList.forEach(function (slowlog, index) {
                    var instanceId = slowlog.instanceId;
                    var instance = slowlog.ip + ":" + slowlog.port;
                    var slowLogId = slowlog.slowLogId;
                    var command = slowlog.command;
                    var costTime = slowlog.costTime;
                    var executeTime = slowlog.executeTime;

                    let date = new Date(executeTime)
                    var formatExecuteTime = dateFormat("YYYY-mm-dd HH:MM:SS", date)

                    $('#slowlogTable').append(
                        '<tr>' +
                        '<td>' + instanceId + '</td>' +
                        '<td>' + instance + '</td>' +
                        '<td>' + slowLogId + '</td>' +
                        '<td>' + costTime + '</td>' +
                        '<td>' + command + '</td>' +
                        '<td>' + formatExecuteTime + '</td>' +
                        '</tr>'
                    );
                })
            }
        );

    });


    function dateFormat(fmt, date) {
        let ret;
        const opt = {
            "Y+": date.getFullYear().toString(),        // 年
            "m+": (date.getMonth() + 1).toString(),     // 月
            "d+": date.getDate().toString(),            // 日
            "H+": date.getHours().toString(),           // 时
            "M+": date.getMinutes().toString(),         // 分
            "S+": date.getSeconds().toString()          // 秒
        };
        for (let k in opt) {
            ret = new RegExp("(" + k + ")").exec(fmt);
            if (ret) {
                fmt = fmt.replace(ret[1], (ret[1].length == 1) ? (opt[k]) : (opt[k].padStart(ret[1].length, "0")))
            }
            ;
        }
        ;
        return fmt;
    }

</script>