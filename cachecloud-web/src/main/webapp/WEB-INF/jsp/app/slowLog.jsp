<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp" %>

<div class="container">
    <br/>
    <form method="get" action="/admin/app/index">
        <div class="row">
            <div style="float:right">
                <label style="font-weight:bold;text-align:left;">
                    &nbsp;查询日期:&nbsp;&nbsp;
                </label>
                <input type="text" size="20" name="searchDate" id="searchDate" value="${searchDate}"
                       onFocus="WdatePicker({startDate:'%y-%M-01',dateFmt:'yyyy-MM-dd',alwaysUseStartDate:true})"/>
                <input type="hidden" name="appId" value="${appDesc.appId}">
                <input type="hidden" name="tabTag" value="app_latency">
                <label type="submit">&nbsp;<input type="submit" class="btn-4" value="查询"/></label>
            </div>
        </div>
    </form>

    <script type="text/javascript">
        var appId = '${appDesc.appId}';
        var searchDate = '${searchDate}';
        var appLatencyStatsMap = '${appLatencyStatsJson}';
        var appLatencyStatsJson = eval("(" + appLatencyStatsMap + ")");

        Highcharts.setOptions({
            global: {
                useUTC: false
            }
        });
        Highcharts.setOptions({
            colors: ['#2f7ed8', '#E3170D', '#0d233a', '#8bbc21', '#1aadce',
                '#492970', '#804000', '#f28f43', '#77a1e5',
                '#c42525', '#a6c96a']
        });

        $(document).ready(
            function () {
                var data = appLatencyStatsJson;
                var unit = "个数";
                var appTotalOptions = getOption("LatencyContainer", "<b>" + "延迟事件统计</b>", unit);
                appTotalOptions.series = getAppLatencyInfo(data, 'count', unit, searchDate);
                var appTotalchart = new Highcharts.Chart(appTotalOptions);
            });
    </script>


    <div class="alert alert-warning alert-dismissable">
        <button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button>
        提示：点击图中点查看延迟事件详情！
    </div>
    <div id="LatencyContainer" style="min-width: 310px; height: 350px; margin: 0 auto"></div>
    <br/>


    <div class="row">
        <div class="col-md-12">
            <div class="page-header">
                <h4>redis实例延迟&慢查询统计</h4>
            </div>

            <table class="table table-striped table-hover">
                <thead>
                <tr>
                    <td>序号</td>
                    <td>实例信息</td>
                    <td>延迟事件个数</td>
                    <td>慢查询个数</td>
                </tr>
                </thead>
                <tbody>
                <c:forEach items="${instanceSet}" var="key" varStatus="stats">
                    <tr>
                        <td>${stats.index + 1}</td>
                        <td><a href="#${key}">${key}</a></td>
                        <td>
                            <c:set var="latency_count" value="${appLatencyStatsGroupByInstance[key]}"></c:set>
                            <c:if test="${latency_count==null}"><c:set var="latency_count" value="0"></c:set></c:if>
                            ${latency_count}
                        </td>
                        <td>
                            <c:set var="slowlog_count" value="${appInstanceSlowLogCountMap[key]}"></c:set>
                            <c:if test="${slowlog_count==null}"><c:set var="slowlog_count" value="0"></c:set></c:if>
                            ${slowlog_count}
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </div>


    <div class="page-header">
        <h4>
            各实例慢查询情况&nbsp&nbsp<label class="label label-info">共${fn:length(appInstanceSlowLogList)}次</label>
        </h4>
    </div>
    <c:forEach items="${instaceSlowLogMap}" var="item" varStatus="stats">
        <div style="margin-top: 0px">
            <div class="page-header" id="${item.key}">
                <h5>
                    <c:set var="instanceId" value="${(item.value)[0].instanceId}"></c:set>
                    <li>
                        <a href="/admin/instance/index?instanceId=${instanceId}"
                           target="_blank">${item.key}</a>
                    </li>
                </h5>
            </div>
            <table class="table table-bordered table-striped table-hover">
                <thead>
                <tr>
                    <td>序号</td>
                    <td>慢查询id</td>
                    <td>耗时(单位:微秒)</td>
                    <td>命令</td>
                    <td>发生时间</td>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="slowLog" items="${item.value}" varStatus="status">
                    <tr>
                        <td>${status.index + 1}</td>
                        <td>${slowLog.slowLogId}</td>
                        <td><fmt:formatNumber value="${slowLog.costTime}" pattern="#,#00"/></td>
                        <td>${slowLog.command}</td>
                        <td>${slowLog.executeTime}</td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </c:forEach>

</div>
