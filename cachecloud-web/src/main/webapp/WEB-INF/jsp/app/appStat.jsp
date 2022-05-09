<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp" %>

<script type="text/javascript">
    var startDate = '${startDate}';
    var endDate = '${endDate}';
    var yesterDate = '${yesterDay}';
    var betweenOneDay = '${betweenOneDay}';
    var masterNum = '${appDetail.masterNum}';
    var appId = '${appId}';
    var chartType = 'line';
    var chartParams = "&startDate=" + startDate + "&endDate=" + endDate;
    var chartParamsCompare = "&startDate=" + yesterDate + "&endDate=" + startDate;
    var betweenParams = "&startDate=" + yesterDate + "&endDate=" + endDate;
    var appTotalMem = '${appDetail.mem}';
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

</script>

<div class="container">
    <br/>
    <div class="row">
        <div style="float:right">
            <form method="post" action="/admin/app/index" id="ec" name="ec">
                <label style="font-weight:bold;text-align:left;">
                    日期:&nbsp;&nbsp;
                </label>
                <input type="text" size="21" name="startDate" id="startDate" value="${startDate}"
                       onFocus="WdatePicker({startDate:'%y-%M-01',dateFmt:'yyyy-MM-dd',alwaysUseStartDate:true})"/>

                <input type="hidden" size="20" name="endDate" id="endDate" value="${endDate}"
                       onFocus="WdatePicker({startDate:'%y-%M-01',dateFmt:'yyyy-MM-dd',alwaysUseStartDate:true})"/>
                <input type="hidden" name="appId" value="${appDetail.appDesc.appId}">
                <label>&nbsp;<input type="submit" class="btn-4" value="查询"/></label>
            </form>
        </div>
    </div>
    <div class="row">
        <div class="col-md-6">
            <div class="page-header">
                <h4>
                    全局信息&nbsp;&nbsp;&nbsp;
                    <a target="_blank" href="/admin/app/appScale?appId=${appId}" class="btn btn-info" role="button">申请扩容</a>
                    <a target="_blank" href="/admin/app/appConfig?appId=${appId}" class="btn btn-info" role="button">申请修改配置</a>
                    <a target="_blank" href="/client/show/index?appId=${appId}" class="btn btn-info"
                       role="button">客户端统计</a>
                    <c:choose>
                        <c:when test="${not empty appDetail.appDesc.pkey}">
                            <button type="button" class="btn btn-info" data-target="#appCodeChangeModal"
                                    data-toggle="modal" href="#">查看应用密码
                            </button>
                        </c:when>
                    </c:choose>
                </h4>
            </div>
            <table class="table table-striped table-hover">
                <tbody>
                <tr>
                    <td>内存使用率</td>
                    <td>
                        <div class="progress margin-custom-bottom0">
                            <c:choose>
                            <c:when test="${appDetail.memUsePercent >= 80.00}">
                            <div class="progress-bar progress-bar-danger"
                                 role="progressbar" aria-valuenow="${appDetail.memUsePercent}" aria-valuemax="100"
                                 aria-valuemin="0" style="width: ${appDetail.memUsePercent}%">
                                </c:when>
                                <c:otherwise>
                                <div class="progress-bar progress-bar-success"
                                     role="progressbar" aria-valuenow="${appDetail.memUsePercent}" aria-valuemax="100"
                                     aria-valuemin="0"
                                     style="width: ${appDetail.memUsePercent}%">                                            </c:otherwise>
                                    </c:choose>
                                    <label style="color: #000000">
                                        <fmt:formatNumber
                                                value="${appDetail.mem  * appDetail.memUsePercent / 100 / 1024}"
                                                pattern="0.00"/>G&nbsp;&nbsp;Used/<fmt:formatNumber
                                            value="${appDetail.mem / 1024 * 1.0}" pattern="0.00"/>G&nbsp;&nbsp;Total
                                    </label>
                                </div>
                            </div>
                    </td>
                    <td>当前连接数</td>
                    <td>${appDetail.conn}</td>
                </tr>
                <tr>
                    <td>应用版本</td>
                    <td>${appDetail.appDesc.versionName}</td>
                    <td>应用类型</td>
                    <td>
                        <c:choose>
                            <c:when test="${appDetail.appDesc.type == 2}">redis-cluster</c:when>
                            <c:when test="${appDetail.appDesc.type == 5}">redis-sentinel</c:when>
                            <c:when test="${appDetail.appDesc.type == 6}">redis-standalone</c:when>
                        </c:choose>
                    </td>
                </tr>
                <tr>
                    <td>应用主节点数</td>
                    <td>${appDetail.masterNum}</td>
                    <td>应用从节点数</td>
                    <td>${appDetail.slaveNum}</td>
                </tr>
                <tr>
                    <td>应用命中率</td>
                    <td>${appDetail.hitPercent}%</td>
                    <td>当前对象数</td>
                    <td><fmt:formatNumber value="${appDetail.currentObjNum}" pattern="#,#00"/></td>
                </tr>
                <tr>
                    <td>应用当前状态</td>
                    <td>${appDetail.appDesc.statusDesc}</td>
                    <td>应用分布机器数量</td>
                    <td>${appDetail.machineNum}</td>
                </tr>

                </tbody>
            </table>

            <div class="page-header">
                <h4>各命令峰值信息</h4>
            </div>
            <table class="table table-striped table-hover">
                <tbody>
                <tr>
                    <td>命令</td>
                    <td>峰值QPM</td>
                    <td>峰值产生时间</td>
                </tr>
                <c:forEach items="${top5ClimaxList}" var="command">
                    <tr>
                        <td>${command.commandName}</td>
                        <td><fmt:formatNumber value="${command.commandCount}" pattern="#,#00"/></td>
                        <td><fmt:formatDate value="${command.createTime}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>

        </div>
        <div class="col-md-6">
            <div class="page-header">
                <h4>命令统计</h4>
            </div>
            <div id="containerTop5"
                 style="min-width: 310px; height: 400px; margin: 0 auto"></div>
        </div>
    </div>
    <script type="text/javascript">
        $(document).ready(
            function () {
                var title = "<b>命令分布</b>";
                var chartType = "pie";
                var options = {
                    chart: {
                        renderTo: 'containerTop5',
                        animation: Highcharts.svg,
                        backgroundColor: '#E6F1F5',
                        plotBackgroundColor: '#FFFFFF',
                        type: chartType,
                        marginRight: 10
                    },
                    title: {
                        useHTML: true,
                        text: title
                    },
                    xAxis: {
                        type: 'category'
                    },
                    yAxis: {
                        title: {
                            text: ''
                        },
                        plotLines: [{
                            value: 0,
                            width: 1,
                            color: '#808080'
                        }]
                    },
                    plotOptions: {
                        line: {
                            dataLabels: {
                                enabled: true
                            }
                        },
                        series: {
                            cursor: 'pointer'
                        }
                    },
                    tooltip: {
                        formatter: function () {
                            return '<b>' + this.point.name + '</b><br/>'
                        }
                    },
                    legend: {
                        enabled: true
                    },
                    credits: {
                        enabled: false
                    },
                    exporting: {
                        enabled: true
                    },
                    series: []
                };
                var pieUrl = "/admin/app/getTop5Commands?appId=" + appId + chartParams;
                $.ajax({
                    type: "get",
                    url: pieUrl,
                    async: true,
                    success: function (data) {
                        var dataArr = eval("(" + data + ")");
                        var length = dataArr.length;
                        var legendName = "命令分布统计";
                        var arr = [];

                        for (var i = 0; i < length; i++) {
                            var data = dataArr[i];
                            var pointName = data.commandName + ":" + data.y;
                            var point = {
                                name: pointName,
                                y: data.y
                            };
                            arr.push(point);
                        }
                        var series = {
                            name: legendName,
                            data: arr
                        };
                        options.series.push(series);
                        new Highcharts.Chart(options);
                    }
                });
            });
    </script>

    <!-- 命令相关 -->
    <script type="text/javascript">
        //查询一天出每分钟数据
        if (betweenOneDay == 1) {
            $(document).ready(
                function () {
                    var options = getOption("containerCommands", "<b>全命令统计</b>", "次数");
                    var commandsUrl = "/admin/app/getMutiDatesCommandStats.json?appId=" + appId + betweenParams;
                    $.ajax({
                        type: "get",
                        url: commandsUrl,
                        async: true,
                        success: function (data) {
                            var dates = new Array();
                            dates.push(startDate);
                            dates.push(yesterDate);
                            pushOptionSeries(options, data, dates, "命令趋势图");
                            new Highcharts.Chart(options);
                        }
                    });

                });
        } else {
            $(document).ready(
                function () {
                    var options = getOption("containerCommands", "<b>全命令统计</b>", "次数");
                    var commandsUrl = "/admin/app/getCommandStats?appId=" + appId + chartParams;
                    $.ajax({
                        type: "get",
                        url: commandsUrl,
                        async: true,
                        success: function (data) {
                            var nameLegend = "命令趋势图";
                            var finalPoints = getSeriesPoints(data, nameLegend);
                            options.series.push(finalPoints);
                            new Highcharts.Chart(options);
                        }
                    });
                });
        }
    </script>
    <div id="containerCommands"
         style="min-width: 310px; height: 350px; margin: 0 auto"></div>

    <!-- 命中相关 -->
    <script type="text/javascript">
        //查询一天出每分钟数据
        if (betweenOneDay == 1) {
            $(document).ready(
                function () {
                    var options = getOption("containerHits", "<b>命中统计</b>", "次数");
                    var commandsUrl = "/admin/app/getMutiDatesAppStats.json?appId=" + appId + "&statName=hits" + betweenParams;
                    $.ajax({
                        type: "get",
                        url: commandsUrl,
                        async: true,
                        success: function (data) {
                            var dates = new Array();
                            dates.push(startDate);
                            dates.push(yesterDate);
                            pushOptionSeries(options, data, dates, "命中趋势图");
                            new Highcharts.Chart(options);
                        }
                    });
                });
        } else {
            $(document).ready(
                function () {
                    var options = getOption("containerHits", "<b>命中统计</b>", "次数");
                    var commandsUrl = "/admin/app/getAppStats.json?appId=" + appId + "&statName=hits" + chartParams + "&timeDimensionalityIndex=1";
                    $.ajax({
                        type: "get",
                        url: commandsUrl,
                        async: true,
                        success: function (data) {
                            var nameLegend = "命中趋势图";
                            var finalPoints = getSeriesPoints(data, nameLegend);
                            options.series.push(finalPoints);
                            new Highcharts.Chart(options);
                        }
                    });
                });
        }
    </script>
    <div id="containerHits"
         style="min-width: 310px; height: 350px; margin: 0 auto"></div>

    <!-- 网络流量 -->
    <script type="text/javascript">
        var allInstanceNetStatUrl = "/admin/app/appInstanceNetStat?appId=" + appId + chartParams;
        $(document).ready(
            function () {
                var options = getOption("containerNet", "网络流量<a href='" + allInstanceNetStatUrl + "' target='_blank'>(查看实例流量)</a>", "");
                //网络流量
                var netUrl = "/admin/app/getMutiStatAppStats.json?appId=" + appId + "&statName=netInput,netOutput" + chartParams;
                $.ajax({
                    type: "get",
                    url: netUrl,
                    async: true,
                    success: function (data) {

                        var dataObject = eval("(" + data.data + ")");
                        var inputDataArr = dataObject["netInput"];

                        //1.input
                        var inputPoints = getNetPoints(inputDataArr, "net_input");
                        //确认单位
                        options.yAxis.title.text = inputPoints.unitTxt;
                        var unit = inputPoints.unit;

                        options.series.push(inputPoints);

                        //2.output
                        var outputDataArr = dataObject["netOutput"];
                        var outputPoints = getNetPoints(outputDataArr, "net_output", unit);
                        options.series.push(outputPoints);

                        new Highcharts.Chart(options);
                    }
                });
            });
    </script>
    <div id="containerNet" style="min-width: 310px; height: 350px; margin: 0 auto"></div>


    <!-- CPU消耗流量 -->
    <script type="text/javascript">
        var containerCpuUsedUrl = "/admin/app/appInstanceCpuStat?appId=" + appId + chartParams;
        $(document).ready(
            function () {
                var options = getOption("containerCpuUsed", "CPU消耗<a href='" + containerCpuUsedUrl + "' target='_blank'>(查看实例CPU消耗)</a>", "");
                //网络流量
                var cpuUsedUrl = "/admin/app/getMutiStatAppStats.json?appId=" + appId + "&statName=cpuSys,cpuUser,cpuSysChildren,cpuUserChildren" + chartParams;
                $.ajax({
                    type: "get",
                    url: cpuUsedUrl,
                    async: true,
                    success: function (data) {
                        var dataObject = eval("(" + data.data + ")");

                        //1.cpuSys
                        var cpuSysDataArr = dataObject["cpuSys"];
                        var cpuSysPoints = getCpuPoints(cpuSysDataArr, "cpu_sys", 1);
                        options.series.push(cpuSysPoints);
                        console.log("cpuSysDataArr=" + cpuSysDataArr[0])
                        //确认单位
                        var unit = cpuSysPoints.unit;
                        options.yAxis.title.text = cpuSysPoints.unitTxt;

                        //2.cpuUser
                        var cpuUserDataArr = dataObject["cpuUser"];
                        var cpuUserPoints = getCpuPoints(cpuUserDataArr, "cpu_user", 1);
                        options.series.push(cpuUserPoints);

                        //3.cpuSysChildren
                        var cpuSysChildrenDataArr = dataObject["cpuSysChildren"];
                        var cpuSysChildrenPoints = getCpuPoints(cpuSysChildrenDataArr, "cpu_sys_children", 1);
                        options.series.push(cpuSysChildrenPoints);

                        //4.cpuUserChildren
                        var cpuUserChildrenDataArr = dataObject["cpuUserChildren"];
                        var cpuUserChildrenPoints = getCpuPoints(cpuUserChildrenDataArr, "cpu_user_children", 1);
                        options.series.push(cpuUserChildrenPoints);

                        // //5.cou base
                        // var cpuBasePoints = getBaseCpuPoints(cpuSysDataArr, "cpu_warning", 1);
                        // options.series.push(cpuBasePoints);

                        new Highcharts.Chart(options);
                    }
                });
            });
    </script>
    <div id="containerCpuUsed" style="min-width: 310px; height: 350px; margin: 0 auto"></div>

    <!-- 内存变化相关 -->
    <script type="text/javascript">


        /*if(betweenOneDay == 1){*/
        var containerMemFragRatioUrl = "/admin/app/appInstanceMemFragRatioStat?appId=" + appId + chartParams;
        $(document).ready(
            function () {
                var options = getOption("containerMemory", "<b>内存使用量<a href='" + containerMemFragRatioUrl + "' target='_blank'>(查看实例内存碎片率)</a></b>", "M");
                var commandsUrl = "/admin/app/getMutiStatAppStats.json?appId=" + appId + "&statName=usedMemory,usedMemoryRss" + chartParams;
                $.ajax({
                    type: "get",
                    url: commandsUrl,
                    async: true,
                    success: function (data) {
                        var dataObject = eval("(" + data.data + ")");
                        var usedMemoryDataArr = dataObject["usedMemory"];

                        //1.usedMemory
                        var usedMemoryPoints = getMemoryPoints(usedMemoryDataArr, "used_memory", 1);
                        //确认单位
                        options.yAxis.title.text = usedMemoryPoints.unitTxt;
                        var unit = usedMemoryPoints.unit;

                        options.series.push(usedMemoryPoints);

                        //2.usedMemoryRss
                        var usedMemoryRssDataArr = dataObject["usedMemoryRss"];
                        var usedMemoryRssPoints = getMemoryPoints(usedMemoryRssDataArr, "used_memory_rss", 1);
                         //确认单位
                        options.yAxis.title.text = usedMemoryRssPoints.unitTxt;
                        var unit = usedMemoryRssPoints.unit;
                        options.series.push(usedMemoryRssPoints);

                        //3. 应用总内存
                        var maxMemoryPoints = getMemoryPoints(usedMemoryRssDataArr, "应用总内存", 1, parseInt(appTotalMem));
                        options.series.push(maxMemoryPoints);

                        new Highcharts.Chart(options);

                    }
                });
            });
    </script>
    <div id="containerMemory"
         style="min-width: 310px; height: 350px; margin: 0 auto"></div>

    <!-- 碎片率变化相关
    <script type="text/javascript">
       //查询一天出每分钟数据
       var containerMemFragRatioUrl = "/admin/app/appInstanceMemFragRatioStat?appId=" + appId + chartParams;
               if (betweenOneDay == 1) {
                   $(document).ready(
                       function () {
                           var options = getOption("containerMemFragRatio", "<b>内存碎片率统计<a href='" + containerMemFragRatioUrl + "' target='_blank'>(查看实例内存碎片率)</a></b>", "");
                           var commandsUrl = "/admin/app/getMutiDatesAppStats.json?appId=" + appId + "&statName=memFragRatio" + betweenParams;
                           $.ajax({
                               type: "get",
                               url: commandsUrl,
                               async: true,
                               success: function (data) {
                                   var dates = new Array();
                                   dates.push(startDate);
                                   dates.push(yesterDate);
                                   pushMemFragRatioOptionSeries(options, data, dates, "内存碎片率趋势图", "");
                                   new Highcharts.Chart(options);
                               }
                           });
                       });
               } else {
                   $(document).ready(
                       function () {
                           var options = getOption("containerMemFragRatio", "<b>内存碎片率统计<a href='" + containerMemFragRatioUrl + "' target='_blank'>(查看实例内存碎片率)</a></b>", "");
                           var commandsUrl = "/admin/app/getAppStats.json?appId=" + appId + "&statName=memFragRatio" + chartParams + "&timeDimensionalityIndex=1";
                           $.ajax({
                               type: "get",
                               url: commandsUrl,
                               async: true,
                               success: function (data) {
                                   var nameLegend = "内存碎片率趋势图";
                                   var finalPoints = getMemFragRatioSeriesPoints(data, nameLegend);
                                   options.series.push(finalPoints);
                                   new Highcharts.Chart(options);
                               }
                           });
                       });
               }
    </script>
    <div id="containerMemFragRatio"
         style="min-width: 310px; height: 350px; margin: 0 auto"></div>
    -->

    <!-- 客户端连接数相关 -->
    <script type="text/javascript">
        //查询一天出每分钟数据
        $(document).ready(
            function () {
                var options = getOption("containerClients", "<b>客户端连接统计</b>", "个");
                var commandsUrl = "/admin/app/getMutiDatesAppStats.json?appId=" + appId + "&statName=connectedClient" + betweenParams;
                $.ajax({
                    type: "get",
                    url: commandsUrl,
                    async: true,
                    success: function (data) {
                        var dates = new Array();
                        dates.push(startDate);
                        dates.push(yesterDate);
                        pushOptionSeries(options, data, dates, "客户端连接趋势图", "个");
                        new Highcharts.Chart(options);
                    }
                });
            });
    </script>
    <div id="containerClients"
         style="min-width: 310px; height: 350px; margin: 0 auto"></div>


    <!-- bsize相关 -->
    <script type="text/javascript">
        //查询一天出每分钟数据
        $(document).ready(
            function () {
                var options = getOption("containerDbsize", "<b>键个数统计</b>", "个");
                var commandsUrl = "/admin/app/getMutiDatesAppStats.json?appId=" + appId + "&statName=objectSize" + betweenParams;
                $.ajax({
                    type: "get",
                    url: commandsUrl,
                    async: true,
                    success: function (data) {
                        var dates = new Array();
                        dates.push(startDate);
                        dates.push(yesterDate);
                        pushOptionSeries(options, data, dates, "键个数趋势图", "个");
                        new Highcharts.Chart(options);
                    }
                });
            });
    </script>
    <div id="containerDbsize"
         style="min-width: 310px; height: 350px; margin: 0 auto"></div>


    <!-- 过期/淘汰键变化相关 -->
    <script type="text/javascript">


        /*if(betweenOneDay == 1){*/
        var containerExpiredKeysUrl = "/admin/app/appInstanceExpiredEvictedKeysStat?appId=" + appId + chartParams;
        $(document).ready(
            function () {
                var options = getOption("containerExpiredEvictedKeys", "<b>过期/淘汰键统计<a href='" + containerExpiredKeysUrl + "' target='_blank'>(查看实例过期/淘汰键统计)</a></b>", "个");
                var commandsUrl = "/admin/app/getMutiStatAppStats.json?appId=" + appId + "&statName=expiredKeys,evictedKeys" + chartParams;
                $.ajax({
                    type: "get",
                    url: commandsUrl,
                    async: true,
                    success: function (data) {
                        console.log(data);
                        var dataObject = eval("(" + data.data + ")");
                        var expiredKeysDataArr = dataObject["expiredKeys"];
                        //1.expiredKeys
                        var expiredKeysPoints = getKeyPoints(expiredKeysDataArr, "expired_keys", 0);
                        //确认单位
                        console.log(expiredKeysPoints);

                        options.yAxis.title.text = expiredKeysPoints.unitTxt;
                        var unit = expiredKeysPoints.unit;
                        console.log(expiredKeysPoints);

                        options.series.push(expiredKeysPoints);

                        //2.evictedKeys
                        var evictedKeysDataArr = dataObject["evictedKeys"];
                        var evictedKeysPoints = getKeyPoints(evictedKeysDataArr, "evicted_keys", 0);
                        //确认单位
                        options.yAxis.title.text = evictedKeysPoints.unitTxt;
                        var unit = evictedKeysPoints.unit;
                        options.series.push(evictedKeysPoints);

                        new Highcharts.Chart(options);

                    }
                });
            });
    </script>
    <div id="containerExpiredEvictedKeys"
         style="min-width: 310px; height: 350px; margin: 0 auto"></div>

    <br/>
    <br/>
    <br/>
</div>

<!-- 扩容申请 -->
<div id="appScaleApplyModal" class="modal fade" tabindex="-1" data-width="400">
    <div class="modal-dialog">
        <div class="modal-content">

            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true"></button>
                <h4 class="modal-title">申请扩容</h4>
            </div>

            <form class="form-horizontal form-bordered form-row-stripped">
                <div class="modal-body">
                    <div class="row">
                        <!-- 控件开始 -->
                        <div class="col-md-12">
                            <!-- form-body开始 -->
                            <div class="form-body">

                                <div class="form-group">
                                    <label class="control-label col-md-3">申请容量:</label>
                                    <div class="col-md-7">
                                        <input type="text" name="applyMemSize" id="applyMemSize" placeholder="申请扩容容量"
                                               class="form-control"/>
                                        <span class="help-block">例如填写: 512M,1G,2G..20G</span>
                                    </div>
                                </div>

                                <div class="form-group">
                                    <label class="control-label col-md-3">申请原因:</label>
                                    <div class="col-md-7">
                                        <textarea rows="5" name="appScaleReason" id="appScaleReason"
                                                  placeholder="申请扩容原因" class="form-control"></textarea>
                                    </div>
                                </div>
                            </div>
                            <!-- form-body 结束 -->
                        </div>
                        <div id="appScaleApplyInfo"></div>
                        <!-- 控件结束 -->
                    </div>
                </div>

                <div class="modal-footer">
                    <button type="button" data-dismiss="modal" class="btn btn-info">Close</button>
                    <button type="button" class="btn btn-success" onclick="appScaleApply('${appDetail.appDesc.appId}')">
                        Ok
                    </button>
                </div>

            </form>
        </div>
    </div>
</div>

<div id="appConfigChangeModal" class="modal fade" tabindex="-1" data-width="400">
    <div class="modal-dialog">
        <div class="modal-content">

            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true"></button>
                <h4 class="modal-title">应用配置修改</h4>
            </div>

            <form class="form-horizontal form-bordered form-row-stripped">
                <div class="modal-body">
                    <div class="row">
                        <!-- 控件开始 -->
                        <div class="col-md-12">
                            <!-- form-body开始 -->
                            <div class="form-body">

                                <div class="form-group">
                                    <label class="control-label col-md-3">配置项:</label>
                                    <div class="col-md-8">
                                        <input type="text" name="appConfigKey" id="appConfigKey"
                                               placeholder="例如:maxclients" class="form-control"/>
                                    </div>
                                </div>

                                <div class="form-group">
                                    <label class="control-label col-md-3">配置值:</label>
                                    <div class="col-md-8">
                                        <input type="text" name="appConfigValue" id="appConfigValue"
                                               placeholder="例如:15000" class="form-control">
                                    </div>
                                </div>

                                <div class="form-group">
                                    <label class="control-label col-md-3">修改原因:</label>
                                    <div class="col-md-8">
                                        <textarea name="appConfigReason" id="appConfigReason"
                                                  placeholder="例如：修改原因:1.需要更多的连接数。" class="form-control"></textarea>
                                        <%--<input type="text" name="appConfigReason" id="appConfigReason" placeholder="例如：修改原因:1.需要更多的连接数。" class="form-control">--%>
                                    </div>
                                </div>

                            </div>
                            <!-- form-body 结束 -->
                        </div>
                        <div id="appConfigChangeInfo"></div>
                        <!-- 控件结束 -->
                    </div>
                </div>

                <div class="modal-footer">
                    <button type="button" data-dismiss="modal" class="btn">Close</button>
                    <button type="button" id="appConfigChangeBtn" class="btn red"
                            onclick="appConfigChange('${appId}','${instanceId}')">Ok
                    </button>
                </div>

            </form>
        </div>
    </div>
</div>

<div id="appCodeChangeModal" class="modal fade" tabindex="-1" data-width="400">
    <div class="modal-dialog">
        <div class="modal-content">

            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true"></button>
                <h4 class="modal-title">应用密码</h4>
            </div>

            <form class="form-horizontal form-bordered form-row-stripped">
                <div class="modal-body">
                    <div class="row">
                        <!-- 控件开始 -->
                        <div class="col-md-12">
                            <!-- form-body开始 -->
                            <div class="form-body">

                                <div class="form-group">
                                    <label class="control-label col-md-3">当前应用密码:</label>
                                    <div class="col-md-8">
                                        <input type="text" name="appCodeKey" id="appCodeKey" value="${md5password}"
                                               class="form-control"/>
                                    </div>
                                </div>
                            </div>
                            <!-- form-body 结束 -->
                        </div>
                    </div>
                </div>

                <div class="modal-footer">
                    <button type="button" data-dismiss="modal" class="btn btn-default">Close</button>
                </div>

            </form>
        </div>
    </div>
</div>


