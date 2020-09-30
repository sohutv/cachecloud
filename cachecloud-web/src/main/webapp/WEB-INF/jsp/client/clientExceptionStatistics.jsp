<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp" %>

<div class="container" id="mainClientExceptionContainer">
    <br/>
    <div class="alert alert-success alert-dismissable">
        <button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button>
        提示：请升级cachecloud-client-redis版本到2.0.1-RELEASE及以上！
    </div>
    <br/>
    <form method="get" action="/client/show/index" id="clientExceptionStatisticsForm">
        <div class="row">
            <div style="float:right">
                <label style="font-weight:bold;text-align:left;">
                    &nbsp;查询日期:&nbsp;&nbsp;
                </label>
                <input type="text" size="20" name="searchDate" id="searchDate" value="${searchDate}"
                       onFocus="WdatePicker({startDate:'%y-%M-01',dateFmt:'yyyy-MM-dd',alwaysUseStartDate:true})"/>
                <input type="hidden" name="appId" value="${appId}">
                <input type="hidden" name="tabTag" value="app_client_exception_statistics">
                <label>&nbsp;<input type="submit" class="btn-4" value="查询"/></label>
            </div>
        </div>
    </form>
    <script type="text/javascript">
        var searchDate = '${searchDate}';
        var appId = '${appId}';
        var chartParams = "&searchDate=" + searchDate;
        //应用下各客户端命令统计
        var appClientExceptionStatisticsMap = '${appClientExceptionStatisticsJson}';
        var appClientExceptionStatisticsJson = eval("(" + appClientExceptionStatisticsMap + ")");

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
                var data = appClientExceptionStatisticsJson;

                var count_unit = "次数";
                var count_appTotalOptions = getOption("countContainer", "<b>" + "异常次数</b>", count_unit);
                count_appTotalOptions.series = getClientStatisticsByType(data, 'count', count_unit, searchDate);
                var count_appTotalchart = new Highcharts.Chart(count_appTotalOptions);

                //cost
                var cost_unit = "毫秒";
                var cost_appTotalOptions = getOption("costContainer", "<b>" + "异常平均耗时</b>", cost_unit);
                cost_appTotalOptions.series = getClientStatisticsByType(data, 'cost', cost_unit, searchDate);
                var cost_appTotalchart = new Highcharts.Chart(cost_appTotalOptions);

            });
    </script>
    <div class="page-header">
        <h4>异常情况全局统计</h4>
        <ul>
            <li><a target="_blank" href="/client/show/exceptionStatistics/client?appId=${appId}&searchDate=${searchDate}&exceptionType=0">
                <c:set var="conn_exp_count" value="${appClientGatherStat['conn_exp_count']}"></c:set>
                <c:if test="${conn_exp_count==null}"><c:set var="conn_exp_count" value="0"></c:set></c:if>
                客户端连接异常详情（${conn_exp_count}次）
            </a>
            </li>
            <li><a target="_blank" href="/client/show/exceptionStatistics/client?appId=${appId}&searchDate=${searchDate}&exceptionType=1">
                <c:set var="cmd_exp_count" value="${appClientGatherStat['cmd_exp_count']}"></c:set>
                <c:if test="${cmd_exp_count==null}"><c:set var="cmd_exp_count" value="0"></c:set></c:if>
                客户端命令超时详情（${cmd_exp_count}次）
            </a>
            </li>
        </ul>

    </div>
    <div id="countContainer" style="min-width: 310px; height: 350px; margin: 0 auto"></div>
    <br/>
    <div id="costContainer" style="min-width: 310px; height: 350px; margin: 0 auto"></div>
    <br/>

    <br/><br/><br/><br/>


</div>