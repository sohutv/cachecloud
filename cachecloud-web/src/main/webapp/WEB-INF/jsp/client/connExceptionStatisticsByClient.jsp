<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <title>客户端连接异常统计</title>
    <jsp:include page="/WEB-INF/include/head.jsp"/>

</head>
<body role="document">
<div class="container">
    <jsp:include page="/WEB-INF/include/headMenu.jsp"/>
    <form method="get" action="/client/show/exceptionStatistics/client" id="clientExceptionStatisticsForm">
        <div class="row">
            <div style="float:right">
                <label style="font-weight:bold;text-align:left;">
                    &nbsp;查询日期:&nbsp;&nbsp;
                </label>
                <input type="text" size="20" name="searchDate" id="searchDate" value="${searchDate}"
                       onFocus="WdatePicker({startDate:'%y-%M-01',dateFmt:'yyyy-MM-dd',alwaysUseStartDate:true})"/>
                <label>&nbsp;<input type="submit" class="btn-4" value="查询"/></label>
            </div>
        </div>
        <div class="row page-header">
            <h4>&nbsp;&nbsp;应用：<label class="label label-success">${appId}</label>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                查询时间：<label class="label label-success">${searchDate}</label>
            </h4>
        </div>
        <div class="row page-header">
            <c:set var="client_needSelect" value="0"></c:set>
            &nbsp;&nbsp;客户端: &nbsp;&nbsp;
            <c:forEach items="${clientConfigMap}" var="clientConfig" varStatus="stat">
                <c:set var="item" value='${clientConfig.key}'/>
                <c:choose>
                    <c:when test="${stat.index < 5}">
                        <input type="radio" name="client_optionsRadios" value="${item}"
                               <c:if test="${firstClient == item}">checked="checked"</c:if>
                               onchange="changeClientChart(this.value)"/>
                        ${item}
                    </c:when>
                    <c:otherwise>
                        <c:set var="client_needSelect" value="1"></c:set>
                    </c:otherwise>
                </c:choose>
            </c:forEach>
            <c:if test="${client_needSelect == 1}">
                &nbsp;&nbsp;&nbsp;其他:
                <select name="optionsRadios" onchange="changeClientChart(this.value)">
                    <option>请选择</option>
                    <c:forEach items="${clientConfigMap}" var="clientConfig" varStatus="stat">
                        <c:set var="item" value='${clientConfig.key}'/>
                        <c:choose>
                            <c:when test="${stat.index >= 5}">
                                <label>
                                    <option value="${item}" <c:if test="${firstClient == item}">selected</c:if>>
                                            ${item}
                                    </option>
                                </label>
                            </c:when>
                        </c:choose>
                    </c:forEach>
                </select>
            </c:if>
        </div>
        <input type="hidden" name="appId" value="${appId}">
        <input type="hidden" id="firstClient" name="firstClient" value="${firstClient}">
        <input type="hidden" id="exceptionType" name="exceptionType" value=0>
    </form>

    <script type="text/javascript">
        var searchDate = '${searchDate}';
        //应用下某客户端某命令的统计情况
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

        function changeClientChart(value) {
            document.getElementById("firstClient").value = value;
            document.getElementById("clientExceptionStatisticsForm").submit();
        }

        $(document).ready(
            function () {
                var data = appClientExceptionStatisticsJson;

                var count_unit = "次数";
                var count_appTotalOptions = getOption("countContainer", "<b>" + "连接异常次数</b>", count_unit);
                count_appTotalOptions.series = getClientStatisticsByType(data, 'count', count_unit, searchDate);
                var count_appTotalchart = new Highcharts.Chart(count_appTotalOptions);

                //cost
                var cost_unit = "毫秒";
                var cost_appTotalOptions = getOption("costContainer", "<b>" + "连接异常平均耗时</b>", cost_unit);
                cost_appTotalOptions.series = getClientStatisticsByType(data, 'cost', cost_unit, searchDate);
                var cost_appTotalchart = new Highcharts.Chart(cost_appTotalOptions);
            });
    </script>

    <div class="row page-header">
        &nbsp;&nbsp;Jedis配置：
        <c:set var="configs" value="${clientConfigMap[firstClient]}"></c:set>
        <select class="label label-info" style="border: 0;">
            <c:forEach items="${configs}" var="item" varStatus="stat">
                <option>${item}</option>
            </c:forEach>
        </select>
    </div>
    <div id="countContainer" style="min-width: 310px; height: 350px; margin: 0 auto"></div>
    <br/>
    <div id="costContainer" style="min-width: 310px; height: 350px; margin: 0 auto"></div>
    <br/>
    <div class="page-header">
        <h4>
            redis节点连接异常统计表
        </h4>
    </div>
    <table id="instanceDetailTable" class="table table-striped table-hover table-bordered" style="margin-top: 0px">
        <thead>
        <tr>
            <td>序号</td>
            <td>redis实例</td>
            <td>异常总次数</td>
            <td>平均耗时(单位:毫秒)</td>
        </tr>
        </thead>
        <tbody>
        <c:forEach items="${appNodeExceptionStatisticsList}" var="item" varStatus="stat">
            <tr>
                <td>${stat.index}</td>
                <td>${item.node}</td>
                <td>${item.count}</td>
                <td>${item.cost}</td>
            </tr>
        </c:forEach>
        </tbody>
    </table>

    <br/><br/><br/><br/>
</div>
<jsp:include page="/WEB-INF/include/foot.jsp"/>
<script type="text/javascript">

</script>
<script type="text/javascript" src="/resources/js/docs.min.js"></script>
</body>
</html>