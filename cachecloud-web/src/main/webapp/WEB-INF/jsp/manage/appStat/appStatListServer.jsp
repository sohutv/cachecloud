<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp" %>

<div class="page-container">
    <div class="page-content">
        <div class="table-toolbar">
            <div class="btn-group">
                <ul class="nav nav-tabs" id="app_tabs">
                    <li <c:if test="${tabId == 1}">class="active"</c:if>><a
                            href="/manage/app/stat/list/server?tabId=1">内存指标</a></li>
                    <li <c:if test="${tabId == 2}">class="active"</c:if>><a
                            href="/manage/app/stat/list/server?tabId=2">碎片率指标</a></li>
                    <li <c:if test="${tabId == 3}">class="active"</c:if>><a
                            href="/manage/app/stat/list/server?tabId=3">应用拓扑诊断</a></li>
                </ul>
            </div>

            <div class="btn-group" style="float:right">
                <form class="form-inline" role="form" method="post" action="/manage/app/stat/list/server"
                      id="search_form">
                    <input name="tabId" id="tabId" value="${tabId}" type="hidden"/>
                    <div class="form-group">
                        <label for="searchDate">&nbsp;查询日期&nbsp;&nbsp;</label>
                        <input type="date" size="15" name="searchDate" id="searchDate" value="${searchDate}"/>
                    </div>
                    <button type="submit" class="btn-4 btn-info ">查询</button>
                    <button class="btn-4 btn-info" onclick="sendExpAppsStatDataEmail()">发送日报</button>
                </form>
            </div>
        </div>

        <div class="row">
            <div class="col-md-12">
                <div class="portlet box light-grey" id="memIndex">
                    <div class="portlet-title">
                        <div class="caption">
                            <i class="fa fa-globe"></i>内存使用情况统计
                        </div>
                        <div class="tools">
                            <a href="javascript:;" class="collapse"></a>
                        </div>
                    </div>
                    <table class="table table-striped table-bordered table-hover" name="tableDataList">
                        <thead>
                        <tr>
                            <th>appId</th>
                            <th>应用名称</th>
                            <th>是否测试</th>
                            <th>应用类型</th>
                            <th>分片数*分片内存G</th>
                            <th>内存使用</th>
                            <th>rss内存使用</th>
                            <th>内存使用率%</th>
                            <th>内存报警阀值%</th>
                            <th>客户端连接数</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach items="${appDescList}" var="appDesc">
                            <c:set var="app_id" value="${appDesc.appId}"></c:set>
                            <c:set var="appDetail" value="${appDetailVOMap[app_id]}"></c:set>
                            <tr class="odd gradeX">
                                <td>
                                    <a target="_blank"
                                       href="/manage/app/index?appId=${appDesc.appId}">${appDesc.appId}</a>
                                </td>
                                <td>
                                    <a target="_blank"
                                       href="/admin/app/index?appId=${appDesc.appId}">${appDesc.name}</a>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${appDesc.isTest == 1}">测试</c:when>
                                        <c:otherwise>正式</c:otherwise>
                                    </c:choose>
                                </td>
                                <td>${appDesc.typeDesc}</td>
                                <td>
                                    <c:set var="master_num" value="${appDetailVOMap[app_id].masterNum}"></c:set>
                                    <c:set var="slave_num" value="${appDetailVOMap[app_id].slaveNum}"></c:set>
                                    <c:if test="${master_num==null}"><c:set var="master_num" value="0"></c:set></c:if>
                                    <fmt:formatNumber var="insance_mem"
                                                      value="${appDetailVOMap[app_id].mem/appDetailVOMap[app_id].masterNum/1024}"
                                                      pattern="##.##" minFractionDigits="2"></fmt:formatNumber>
                                        ${master_num}&nbsp;*&nbsp;${insance_mem}G
                                </td>

                                <td>
                                    <fmt:formatNumber var="usedMem"
                                                      value="${((appClientGatherStatMap[app_id]['used_memory'])/1024/1024/1024)}"
                                                      pattern="0.00"/>
                                    <fmt:formatNumber var="mem" value="${appDetail.mem/1024}" pattern="0.00"/>
                                    <fmt:formatNumber var="fmtMemoryUsageRatio" value="${usedMem/mem*100.0}"
                                                      pattern="0.00"/>
                                    <div class="progress margin-custom-bottom0">
                                        <c:choose>
                                            <c:when test="${fmtMemoryUsageRatio >= 80.00}"><c:set
                                                    var="memUsedProgressBarStatus"
                                                    value="progress-bar-danger"/></c:when>
                                            <c:when test="${fmtMemoryUsageRatio >= 60.00}"><c:set
                                                    var="memUsedProgressBarStatus"
                                                    value="progress-bar-warning"/></c:when>
                                            <c:otherwise><c:set var="memUsedProgressBarStatus"
                                                                value="progress-bar-success"/></c:otherwise>
                                        </c:choose>
                                        <div class="progress-bar ${memUsedProgressBarStatus}"
                                             role="progressbar" aria-valuenow="${fmtMemoryUsageRatio}"
                                             aria-valuemax="100"
                                             aria-valuemin="0" style="width: ${fmtMemoryUsageRatio}%">
                                            <label style="color: #000000">
                                                    ${usedMem}G&nbsp;&nbsp;Used/
                                                    ${mem}G&nbsp;&nbsp;Total
                                            </label>
                                        </div>
                                    </div>
                                </td>

                                <td>
                                    <fmt:formatNumber var="usedMemRss"
                                                      value="${((appClientGatherStatMap[app_id]['used_memory_rss'])/1024/1024/1024)}"
                                                      pattern="0.00"/>
                                    <fmt:formatNumber var="mem" value="${appDetail.mem/1024}" pattern="0.00"/>
                                    <fmt:formatNumber var="fmtMemoryUsageRatio" value="${usedMemRss/mem*100.0}"
                                                      pattern="0.00"/>
                                    <div class="progress margin-custom-bottom0">
                                        <c:choose>
                                            <c:when test="${fmtMemoryUsageRatio >= 80.00}"><c:set
                                                    var="memUsedProgressBarStatus"
                                                    value="progress-bar-danger"/></c:when>
                                            <c:when test="${fmtMemoryUsageRatio >= 60.00}"><c:set
                                                    var="memUsedProgressBarStatus"
                                                    value="progress-bar-warning"/></c:when>
                                            <c:otherwise><c:set var="memUsedProgressBarStatus"
                                                                value="progress-bar-success"/></c:otherwise>
                                        </c:choose>
                                        <div class="progress-bar ${memUsedProgressBarStatus}"
                                             role="progressbar" aria-valuenow="${fmtMemoryUsageRatio}"
                                             aria-valuemax="100"
                                             aria-valuemin="0" style="width: ${fmtMemoryUsageRatio}%">
                                            <label style="color: #000000">
                                                    ${usedMemRss}G&nbsp;&nbsp;Used/
                                                    ${mem}G&nbsp;&nbsp;Total
                                            </label>
                                        </div>
                                    </div>
                                </td>

                                <td>
                                    <c:set var="mem_used_ratio"
                                           value="${appDetailVOMap[app_id].memUsePercent}"></c:set>
                                    <c:if test="${mem_used_ratio==null}"><c:set var="mem_used_ratio" value="0"></c:set></c:if>
                                        ${mem_used_ratio}
                                </td>

                                <td>
                                    <c:set var="memUseThreshold"
                                           value="${appDetailVOMap[app_id].appDesc.memAlertValue}"></c:set>
                                        ${memUseThreshold}
                                </td>
                                <td>
                                    <c:set var="conn"
                                           value="${appClientGatherStatMap[app_id]['connected_clients']}"></c:set>
                                    <c:if test="${conn==null}"><c:set var="conn" value="0"></c:set></c:if>
                                        ${conn}
                                    <button class="btn btn-warning btn-sm" style="float: right;">
                                        <a href="/admin/app/index?appId=${app_id}&tabTag=app_clientList"
                                           target="_blank"><font style="color: white">连接信息</font></a>
                                    </button>
                                </td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>
                <div class="portlet box light-grey" id="fragRatioIndex">
                    <div class="portlet-title">
                        <div class="caption">
                            <i class="fa fa-globe"></i>碎片率指标情况统计
                        </div>
                        <div class="tools">
                            <a href="javascript:;" class="collapse"></a>
                        </div>
                    </div>
                    <table class="table table-striped table-bordered table-hover" name="tableDataList">
                        <thead>
                        <tr>
                            <th>appId</th>
                            <th>应用名称</th>
                            <th>是否测试</th>
                            <th>redis版本</th>
                            <th>key数量</th>
                            <th>内存使用</th>
                            <th>rss内存使用</th>
                            <th>平均碎片率(%)</th>
                            <th>max cpuSys(s)</th>
                            <th>max cpuUser(s)</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach items="${appDescList}" var="appDesc">
                            <c:set var="app_id" value="${appDesc.appId}"></c:set>
                            <c:set var="appDetail" value="${appDetailVOMap[app_id]}"></c:set>
                            <tr class="odd gradeX">
                                <td>
                                    <a target="_blank"
                                       href="/manage/app/index?appId=${app_id}">${app_id}</a>
                                </td>
                                <td>
                                    <a target="_blank"
                                       href="/admin/app/index?appId=${app_id}">${appDesc.name}</a>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${appDesc.isTest == 1}">测试</c:when>
                                        <c:otherwise>正式</c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                        ${appDesc.versionName}
                                </td>
                                <td>
                                    <c:set var="object_size"
                                           value="${appClientGatherStatMap[app_id]['object_size']}"></c:set>
                                    <c:if test="${object_size==null}"><c:set var="object_size" value="0"></c:set></c:if>
                                        ${object_size}
                                </td>
                                <td>
                                    <fmt:formatNumber var="used_memory"
                                                      value="${((appClientGatherStatMap[app_id]['used_memory'])/1024/1024)}"
                                                      pattern="0.00"/>
                                        ${used_memory}
                                </td>
                                <td>
                                    <fmt:formatNumber var="used_memory_rss"
                                                      value="${((appClientGatherStatMap[app_id]['used_memory_rss'])/1024/1024)}"
                                                      pattern="0.00"/>
                                        ${used_memory_rss}
                                </td>
                                <td>
                                    <c:set var="avg_mem_frag_ratio"
                                           value="${appClientGatherStatMap[app_id]['avg_mem_frag_ratio']}"></c:set>
                                    <c:if test="${avg_mem_frag_ratio==null}"><c:set var="avg_mem_frag_ratio"
                                                                                    value="0"></c:set></c:if>
                                        ${avg_mem_frag_ratio}
                                </td>
                                <td>
                                    <c:set var="max_cpu_sys"
                                           value="${appClientGatherStatMap[app_id]['max_cpu_sys']}"></c:set>
                                    <c:if test="${max_cpu_sys==null}"><c:set var="max_cpu_sys" value="0"></c:set></c:if>
                                        ${max_cpu_sys}
                                </td>
                                <td>
                                    <c:set var="max_cpu_user"
                                           value="${appClientGatherStatMap[app_id]['max_cpu_user']}"></c:set>
                                    <c:if test="${max_cpu_user==null}"><c:set var="max_cpu_user"
                                                                              value="0"></c:set></c:if>
                                        ${max_cpu_user}
                                </td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>
                <div class="portlet box light-grey" id="appTopologyIndex">
                    <div class="portlet-title">
                        <div class="caption">
                            <i class="fa fa-globe"></i>应用拓扑诊断
                        </div>
                        <div class="tools">
                            <a href="javascript:;" class="collapse"></a>
                        </div>
                    </div>
                    <table class="table table-striped table-bordered table-hover" name="tableDataList">
                        <thead>
                        <tr>
                            <th>appId</th>
                            <th>应用名称</th>
                            <th>是否测试</th>
                            <th>类型</th>
                            <th>redis版本</th>
                            <th>节点分布</th>
                            <th>拓扑诊断
                                <button class="btn-4 btn-success" onclick="topologyUpdate()">诊断更新</button>
                            </th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach items="${appDescList}" var="appDesc">
                            <c:set var="app_id" value="${appDesc.appId}"></c:set>
                            <c:set var="appDetail" value="${appDetailVOMap[app_id]}"></c:set>
                            <tr class="odd gradeX">
                                <td>
                                    <a target="_blank"
                                       href="/manage/app/index?appId=${app_id}">${app_id}</a>
                                </td>
                                <td>
                                    <a target="_blank"
                                       href="/admin/app/index?appId=${app_id}">${appDesc.name}</a>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${appDesc.isTest == 1}">测试</c:when>
                                        <c:otherwise>正式</c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                        ${appDesc.typeDesc}
                                </td>
                                <td>
                                        ${appDesc.versionName}
                                </td>
                                <td>
                                    <c:set var="master_num" value="${appDetailVOMap[app_id].masterNum}"></c:set>
                                    <c:set var="slave_num" value="${appDetailVOMap[app_id].slaveNum}"></c:set>
                                    <c:if test="${master_num==null}"><c:set var="master_num" value="0"></c:set></c:if>
                                    <c:if test="${slave_num==null}"><c:set var="slave_num" value="0"></c:set></c:if>
                                    主从节点:
                                    <c:choose>
                                        <c:when test="${master_num == slave_num}">${master_num},${slave_num}</c:when>
                                        <c:otherwise><span
                                                style="color:red">${master_num},${slave_num}</span></c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${appClientGatherStatMap[app_id]['topology_exam_result']== 0}">
                                            <span style="color:green">正常</span>
                                        </c:when>
                                        <c:when test="${appClientGatherStatMap[app_id]['topology_exam_result']== 1}">
                                            <span style="color:red">异常</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span style="color:red">未检测</span>
                                        </c:otherwise>
                                    </c:choose>
                                    [<a target="_blank" href="/manage/app/index.do?appId=${app_id}&tabTag=app_ops_tool">查看诊断</a>]
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
        var tabVal = $('#tabId').val();
        if (tabVal == 1) {
            $('#memIndex').removeAttr("hidden");
            $('#fragRatioIndex').attr("hidden", "hidden");
            $('#appTopologyIndex').attr("hidden", "hidden");
        } else if (tabVal == 2) {
            $('#memIndex').attr("hidden", "hidden");
            $('#fragRatioIndex').removeAttr("hidden");
            $('#appTopologyIndex').attr("hidden", "hidden");
        } else if (tabVal == 3) {
            $('#memIndex').attr("hidden", "hidden");
            $('#fragRatioIndex').attr("hidden", "hidden");
            $('#appTopologyIndex').removeAttr("hidden");
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

    function topologyUpdate() {
        if(confirm("确认提交拓扑检查更新？确认后请等待，更新完毕页面会自动刷新")){
            $.get('/manage/app/stat/topologyUpdate.json', function (result) {
                if (result.status == 1) {
                    location.href = "/manage/app/stat/list/server?tabId=3";
                }else {
                    alert("拓扑检测更新失败！")
                }
            });
        }

    }
</script>