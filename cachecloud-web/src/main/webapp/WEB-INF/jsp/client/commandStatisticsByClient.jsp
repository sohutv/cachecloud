<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <title>客户端命令调用详情</title>
    <jsp:include page="/WEB-INF/include/head.jsp"/>
</head>
<body role="document">
<div class="container">
    <jsp:include page="/WEB-INF/include/headMenu.jsp"/>
    <form method="get" action="/client/show/commandStatistics/client" id="clientCommandStatisticsForm">
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
            <c:forEach items="${clientList}" var="item" varStatus="stat">
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
                    <c:forEach items="${clientList}" var="item" varStatus="stat">
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
            <input type="radio" name="client_optionsRadios" value="all"
                   <c:if test="${firstClient == 'all'}">checked="checked"</c:if>
                   onchange="changeClientChart(this.value)"/>all
        </div>
        <div class="row page-header">
            <c:set var="command_needSelect" value="0"></c:set>
            &nbsp;&nbsp;命令: &nbsp;&nbsp;
            <c:forEach items="${commandList}" var="item" varStatus="stat">
                <c:choose>
                    <c:when test="${stat.index < 5}">
                        <input type="radio" name="optionsRadios" value="${item}"
                               <c:if test="${firstCommand == item}">checked="checked"</c:if>
                               onchange="changeCommandChart(this.value)"/>
                        ${item}
                    </c:when>
                    <c:otherwise>
                        <c:set var="command_needSelect" value="1"></c:set>
                    </c:otherwise>
                </c:choose>
            </c:forEach>
            <c:if test="${command_needSelect == 1}">
                &nbsp;&nbsp;&nbsp;其他:
                <select name="command_optionsRadios" onchange="changeCommandChart(this.value)">
                    <option>请选择</option>
                    <c:forEach items="${commandList}" var="item" varStatus="stat">
                        <c:choose>
                            <c:when test="${stat.index >= 5}">
                                <label>
                                    <option value="${item}" <c:if test="${firstCommand == item}">selected</c:if>>
                                            ${item}
                                    </option>
                                </label>
                            </c:when>
                        </c:choose>
                    </c:forEach>
                </select>
            </c:if>
            <input type="radio" name="optionsRadios" value="all"
                   <c:if test="${firstCommand == 'all'}">checked="checked"</c:if>
                   onchange="changeCommandChart(this.value)"/>all
        </div>
        <input type="hidden" name="appId" value="${appId}">
        <input type="hidden" id="firstCommand" name="firstCommand" value="${firstCommand}">
        <input type="hidden" id="firstClient" name="firstClient" value="${firstClient}">
    </form>

    <script type="text/javascript">
        var searchDate = '${searchDate}';
        var firstCommand = '${firstCommand}';
        var firstClient = '${firstClient}';

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

        function changeCommandChart(value) {
            document.getElementById("firstCommand").value = value;
            document.getElementById("clientCommandStatisticsForm").submit();
        }

        function changeClientChart(value) {
            document.getElementById("firstClient").value = value;
            document.getElementById("clientCommandStatisticsForm").submit();
        }


        $(document).ready(
            function () {
                if (firstClient == 'all') {
                    var sumCommandStatJson = '${sumCommandStatJson}';
                    var sumCommandStatList = eval("(" + sumCommandStatJson + ")");
                    var tbody = "";
                    $.each(sumCommandStatList, function (n, value) {
                        console.log("value:" + value);
                        var trs = "";
                        trs += "<tr>"
                            + "<td>" + (n + 1) + " </td>"
                            + "<td>" + value.client_ip + " </td>"
                            + "<td>" + value.count + "</td >"
                            + "<td>" + value.cost + "</td >"
                            + " </tr>";
                        tbody += trs;
                    });
                    $('#sumTable-tbody').append(tbody);
                    $("#countContainer").attr("hidden", "hidden");
                    $("#byteContainer").attr("hidden", "hidden");
                    $("#byteOutContainer").attr("hidden", "hidden");
                    $("#costContainer").attr("hidden", "hidden");
                    $("#sumTable").removeAttr("hidden");
                } else if (firstCommand == 'all') {
                    var sumClientStatJson = '${sumClientStatJson}';
                    var sumClientStatList = eval("(" + sumClientStatJson + ")");
                    var tbody = "";
                    $.each(sumClientStatList, function (n, value) {
                        console.log("value:" + value);
                        var trs = "";
                        trs += "<tr>"
                            + "<td>" + n + " </td>"
                            + "<td>" + value.command + " </td>"
                            + "<td>" + value.count + "</td >"
                            + "<td>" + value.cost + "</td >"
                            + " </tr>";
                        tbody += trs;
                    });
                    $('#sumTable-tbody').append(tbody);
                    $("#countContainer").attr("hidden", "hidden");
                    $("#byteContainer").attr("hidden", "hidden");
                    $("#byteOutContainer").attr("hidden", "hidden");
                    $("#costContainer").attr("hidden", "hidden");
                    $("#sumTable").removeAttr("hidden");
                } else {
                    $("#sumTable").attr("hidden", "hidden");
                    var appClientCommandStatisticsMap = '${appClientCommandStatisticsJson}';
                    var appClientCommandStatisticsJson = eval("(" + appClientCommandStatisticsMap + ")");

                    var data = appClientCommandStatisticsJson;

                    var count_unit = "次数";
                    var count_appTotalOptions = getOption("countContainer", "<b>" + firstCommand + "命令调用次数</b>", count_unit);
                    count_appTotalOptions.series = getClientStatisticsByType(data, 'count', count_unit, searchDate);
                    var count_appTotalchart = new Highcharts.Chart(count_appTotalOptions);

                    //cost
                    var cost_unit = "毫秒";
                    var cost_appTotalOptions = getOption("costContainer", "<b>" + firstCommand + "命令平均耗时</b>", cost_unit);
                    cost_appTotalOptions.series = getClientStatisticsByType(data, 'cost', cost_unit, searchDate);
                    var cost_appTotalchart = new Highcharts.Chart(cost_appTotalOptions);

                    //bytesIn
                    var byte_unit = "MB";
                    var byte_appTotalOptions = getOption("byteContainer", "<b>" + firstCommand + "命令输入流量</b>", byte_unit);
                    byte_appTotalOptions.series = getClientStatisticsByType(data, 'bytesIn', byte_unit, searchDate);
                    var byte_appTotalchart = new Highcharts.Chart(byte_appTotalOptions);

                    //bytesOut
                    var byteOut_appTotalOptions = getOption("byteOutContainer", "<b>" + firstCommand + "命令输出流量</b>", byte_unit);
                    byteOut_appTotalOptions.series = getClientStatisticsByType(data, 'bytesOut', byte_unit, searchDate);
                    var byteOut_appTotalOptions = new Highcharts.Chart(byteOut_appTotalOptions);

                }
            });
    </script>

    <div id="sumTable" style="min-width: 310px; height: 350px; margin: 0 auto">
        <table class="table table-striped table-hover table-bordered">
            <caption>
                <h5>
                    <c:if test="${firstClient=='all'}">命令&nbsp;${firstCommand}</c:if>
                    <c:if test="${firstCommand=='all'}">客户端&nbsp;${firstClient}</c:if>
                    &nbsp;--&nbsp;分布统计表
                </h5>
            </caption>
            <thead>
            <tr>
                <td>序号</td>
                <td>
                    <c:if test="${firstClient=='all'}">客户端</c:if>
                    <c:if test="${firstClient!='all'&& firstCommand=='all'}">命令</c:if>
                </td>
                <td>调用总次数</td>
                <td>平均耗时(单位:毫秒)</td>
            </tr>
            </thead>
            <tbody id="sumTable-tbody"></tbody>
        </table>
    </div>
    <div id="countContainer" style="min-width: 310px; height: 350px; margin: 0 auto"></div>
    <br/>
    <div id="costContainer" style="min-width: 310px; height: 350px; margin: 0 auto"></div>
    <br/>
    <div id="byteContainer" style="min-width: 310px; height: 350px; margin: 0 auto"></div>
    <br/>
    <div id="byteOutContainer" style="min-width: 310px; height: 350px; margin: 0 auto"></div>
    <br/>


    <br/><br/><br/><br/>
</div>
<jsp:include page="/WEB-INF/include/foot.jsp"/>
<script type="text/javascript" src="/resources/js/docs.min.js"></script>
</body>
</html>