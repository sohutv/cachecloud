<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<script src="/resources/manage/plugins/jquery-1.10.2.min.js"></script>
<script type="text/javascript">
    var jQuery_1_10_2 = $;
</script>
<script type="text/javascript" src="/resources/select/bootstrap-select.js"></script>
<link rel="stylesheet" type="text/css" href="/resources/select/bootstrap-select.css"/>
<script type="text/javascript">
    $(window).on('load', function () {
        jQuery_1_10_2('.selectpicker').selectpicker({'selectedText': 'cat'});
        jQuery_1_10_2('.selectpicker').selectpicker('refresh');
    });
    $(function () {
        jQuery_1_10_2('.selectpicker').selectpicker({
            noneSelectedText: '选择用户',
        });
        console.log($('#officer').val());
        console.log($('#officer').val().split(','));
        $('#officer_select').val($('#officer').val().split(','));
        $('#officer_select').selectpicker('refresh');


        var td_officer = "";
        for (var i = 0; i < $("#officer_select").find("option:selected").length; i++) {
            if (i == 0) {
                td_officer = $("#officer_select").find("option:selected")[i].innerText;
            } else {
                td_officer = td_officer + ',' + $("#officer_select").find("option:selected")[i].innerText;
            }
        }
        $('#td_officer').html(td_officer);
    })
</script>
<body>
<div class="container">
    <div class="row">
        <div class="col-md-8">
            <div class="page-header">
                <h4>
                    应用详情&nbsp;&nbsp;&nbsp;
                    <c:if test="${hasAuth}">
                        <button type="button" class="btn btn-info" data-target="#updateAppDetailModal"
                                data-toggle="modal">修改应用信息
                        </button>
                    </c:if>
                </h4>
            </div>
            <c:set value="${fn:split(appDetail.appDesc.officer, ',')}" var="officer_arr"/>
            <input id="officer_input" value="${appDetail.appDesc.officer}" hidden/>
            <table class="table table-striped table-hover">
                <tbody>
                <tr>
                    <td>应用id</td>
                    <td>${appDetail.appDesc.appId}</td>
                    <td>应用名称</td>
                    <td>${appDetail.appDesc.name}</td>
                </tr>
                <tr>
                    <td>申请人</td>
                    <td title="${userMap[appDetail.appDesc.userId].email}">${userMap[appDetail.appDesc.userId].chName}</td>
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
                    <td>报警用户</td>
                    <td>
                        <c:forEach items="${appDetail.alertUsers}" var="appUser" varStatus="stat">
                            <c:if test="${stat.index != 0}">,</c:if>${appUser.chName}(${appUser.name})
                        </c:forEach>
                    </td>
                    <td>负责人</td>
                    <td id="td_officer"></td>
                </tr>
                <tr>
                    <td>内存空间</td>
                    <td><fmt:formatNumber value="${appDetail.mem / 1024 * 1.0}" pattern="0.00"/>G</td>
                    <td>分布机器数</td>
                    <td>${appDetail.machineNum}</td>
                </tr>
                <tr>
                    <td>主节点数</td>
                    <td>${appDetail.masterNum}</td>
                    <td>从节点数</td>
                    <td>${appDetail.slaveNum}</td>
                </tr>
                <tr>
                    <td>appKey</td>
                    <td>
                        <c:choose>
                            <c:when test="${appDetail.appDesc.appKey == null || appDetail.appDesc.appKey == ''}">
                                暂无
                            </c:when>
                            <c:otherwise>
                                ${appDetail.appDesc.appKey}
                            </c:otherwise>
                        </c:choose>
                    </td>
                    <td>redis密码</td>
                    <td>
                        <c:choose>
                            <c:when test="${appDetail.appDesc.pkey != ''}">${password}</c:when>
                            <c:otherwise>无</c:otherwise>
                        </c:choose>
                    </td>
                </tr>
                </tbody>
            </table>
        </div>

        <div class="col-md-4">
            <div class="page-header">
                <h4>
                    报警指标
                    <a target="_blank" href="/admin/app/appAlterConfig?appId=${appDetail.appDesc.appId}" class="btn btn-danger" role="button">应用报警配置</a>
                </h4>
            </div>
            <table class="table table-striped table-hover">
                <thead>
                <tr>
                    <td>id</td>
                    <td>报警key</td>
                    <td>阀值</td>
                    <td>周期</td>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td>1</td>
                    <td>内存使用率大于</td>
                    <td>${appDetail.appDesc.memAlertValue}%</td>
                    <td>每20分钟</td>
                </tr>
                <tr>
                    <td>2</td>
                    <td>客户端连接数大于</td>
                    <td>${appDetail.appDesc.clientConnAlertValue}</td>
                    <td>每20分钟</td>
                </tr>
                <tr>
                    <td>3</td>
                    <td>应用平均命中率小于</td>
                    <td>${appDetail.appDesc.hitPrecentAlertValue}%</td>
                    <c:choose>
                        <c:when test="${appDetail.appDesc.hitPrecentAlertValue > 0}">
                            <td>每20分钟</td>
                        </c:when>
                        <c:otherwise>
                            <td>监控关闭</td>
                        </c:otherwise>
                    </c:choose>
                </tr>
                <tr>
                    <td>4</td>
                    <td>全局报警项邮件通知</td>
                    <td></td>
                    <c:choose>
                        <c:when test="${appDetail.appDesc.isAccessMonitor == 0}">
                            <td>监控关闭</td>
                        </c:when>
                        <c:otherwise>
                            <td>监控开启</td>
                        </c:otherwise>
                    </c:choose>
                </tr>
                </tbody>
            </table>
        </div>
    </div>

    <c:if test="${appDetail.appDesc.isAccessMonitor ==1}">
        <div class="row">
            <div class="col-md-12">
                <div class="portlet box light-grey">

                    <div class="portlet-title">
                        <div class="caption">
                            <h4>
                                应用全局报警项:
                                <button type="button" class="btn btn-danger" data-target="#updateWholeAlertConfigModal"
                                        data-toggle="modal">报警项修改申请
                                </button>
                            </h4>
                        </div>
                        <div class="tools">
                            <a href="javascript:;" class="collapse"></a>
                        </div>
                    </div>
                    <div class="portlet-body">
                        <div class="table-toolbar">
                            <table class="table table-striped table-bordered table-hover" id="tableDataList">
                                <thead>
                                <tr>
                                    <th>id</th>
                                    <th>配置名</th>
                                    <th>配置说明</th>
                                    <th>关系</th>
                                    <th>阀值</th>
                                    <th>周期</th>
                                    <th>最近检测时间</th>
                                </tr>
                                </thead>
                                <tbody>
                                <c:forEach items="${instanceAlertAllList}" var="config">
                                    <tr class="odd gradeX">
                                        <td>
                                                ${config.id}
                                        </td>
                                        <td>
                                                ${config.alertConfig}
                                        </td>
                                        <td>
                                                ${config.configInfo}
                                        </td>
                                        <td>
                                            <c:forEach items="${instanceAlertCompareTypeEnumList}"
                                                       var="instanceAlertCompareTypeEnum">
                                                <c:if test="${config.compareType == instanceAlertCompareTypeEnum.value}">${instanceAlertCompareTypeEnum.info}</c:if>
                                            </c:forEach>
                                        </td>
                                        <td>
                                                ${config.alertValue}
                                        </td>
                                        <td>
                                            <c:forEach items="${instanceAlertCheckCycleEnumList}"
                                                       var="instanceAlertCheckCycleEnum">
                                                <c:if test="${config.checkCycle == instanceAlertCheckCycleEnum.value}">${instanceAlertCheckCycleEnum.info}</c:if>
                                            </c:forEach>
                                        </td>
                                        <td>
                                            <fmt:formatDate value="${config.lastCheckTime}"
                                                            pattern="yyyy-MM-dd HH:mm:ss"/>
                                        </td>
                                    </tr>
                                </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
                <!-- END TABLE PORTLET-->
            </div>
        </div>
    </c:if>

    <div class="row">
        <div class="col-md-12 page-header">
            <h4>
                用户管理&nbsp;&nbsp;&nbsp;
                <c:if test="${hasAuth}">
                    <button type="button" class="btn btn-success" data-target="#appAddUserModal" data-toggle="modal">
                        添加用户
                    </button>
                </c:if>
            </h4>
        </div>
        <div class="col-md-12">
            <table class="table table-striped table-hover">
                <thead>
                <tr>
                    <td>id</td>
                    <td>域账户</td>
                    <td>中文名</td>
                    <td>邮箱</td>
                    <td>手机</td>
                    <td>微信</td>
                    <td>是否报警</td>
                    <c:if test="${hasAuth}">
                        <td>操作</td>
                    </c:if>
                </tr>
                </thead>
                <tbody>
                <c:forEach items="${appDetail.appUsers}" var="user">
                    <tr>
                        <td>${user.id}</td>
                        <td>${user.name}</td>
                        <td>${user.chName}</td>
                        <td>${user.email}</td>
                        <td>${user.mobile}</td>
                        <td>${user.weChat}</td>
                        <td>
                            <c:if test="${user.isAlert==0}">否</c:if>
                            <c:if test="${user.isAlert==1}">是</c:if>
                        </td>
                        <c:if test="${hasAuth}">
                            <td>
                                <a href="javascript;" data-target="#addUserModal${user.id}" data-toggle="modal">[修改]</a>
                                &nbsp;
                                <a href="javascript:void(0);"
                                   onclick="deleteAppUser('${user.id}','${appDetail.appDesc.appId}')">[删除]</a>
                            </td>
                        </c:if>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </div>

    <br/><br/><br/>
    <br/><br/><br/>

</div>

<div id="appAddUserModal" class="modal fade" tabindex="-1" data-width="400">
    <div class="modal-dialog">
        <div class="modal-content">

            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true"></button>
                <h4 class="modal-title">添加用户</h4>
            </div>

            <form class="form-horizontal form-bordered form-row-stripped">
                <div class="modal-body">
                    <div class="row">
                        <!-- 控件开始 -->
                        <div class="col-md-12">
                            <!-- form-body开始 -->
                            <div class="form-body">
                                <div class="form-group">
                                    <label class="control-label col-md-2">
                                        用户名:
                                    </label>
                                    <div class="col-md-7">
                                        <select id="addAppToUser" class="selectpicker bla bla bli col-md-6" multiple
                                                data-live-search="true">
                                            <c:forEach items="${userMap}" var="userEntry">
                                                <c:set value="${userEntry.value}" var="user"/>
                                                <option value="${user.id}">${user.chName}(${user.name})</option>
                                            </c:forEach>
                                            </optgroup>
                                        </select>
                                    </div>
                                </div>
                            </div>
                            <!-- form-body 结束 -->
                        </div>
                        <div id="appAddUserInfo"></div>
                        <!-- 控件结束 -->
                    </div>
                </div>

                <div class="modal-footer">
                    <button type="button" data-dismiss="modal" class="btn">Close</button>
                    <button type="button" id="appAddUserBtn" class="btn red"
                            onclick="appAddUser('${appDetail.appDesc.appId}')">Ok
                    </button>
                </div>

            </form>
        </div>
    </div>
</div>


<div id="appAlertConfigModal" class="modal fade" tabindex="-1" data-width="400">
    <div class="modal-dialog">
        <div class="modal-content">

            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true"></button>
                <h4 class="modal-title">应用报警修改</h4>
            </div>

            <form class="form-horizontal form-bordered form-row-stripped">
                <div class="modal-body">
                    <div class="row">
                        <!-- 控件开始 -->
                        <div class="col-md-12">
                            <!-- form-body开始 -->
                            <div class="form-body">
                                <div class="form-group">
                                    <label class="control-label col-md-3">内存报警阀值:</label>
                                    <div class="col-md-7">
                                        <input type="text" name="memAlertValue"
                                               value="${appDetail.appDesc.memAlertValue}" id="memAlertValue"
                                               placeholder="内存报警阀值" class="form-control" onchange="testisNum(this.id)">
                                        <span class="help-block">例如:如果想内存使用率超过90%报警，填写90<br/><font color="red">(如果不需要报警请填写100以上的数字)</font></span>
                                    </div>
                                </div>

                                <div class="form-group">
                                    <label class="control-label col-md-3">客户端连接数报警阀值:</label>
                                    <div class="col-md-7">
                                        <input type="text" name="clientConnAlertValue"
                                               value="${appDetail.appDesc.clientConnAlertValue}"
                                               id="clientConnAlertValue" placeholder="客户端连接数报警阀值" class="form-control"
                                               onchange="testisNum(this.id)">
                                        <span class="help-block">例如:如果想客户端连接数率超过2000报警，填写2000</span>
                                    </div>
                                </div>

                                <div class="form-group">
                                    <label class="control-label col-md-3">应用平均命中率报警阀值:</label>
                                    <div class="col-md-7">
                                        <input type="text" name="hitPrecentAlertValue"
                                               value="${appDetail.appDesc.hitPrecentAlertValue}"
                                               id="hitPrecentAlertValue" placeholder="平均命中率报警阀值" class="form-control"
                                               onchange="testisNum(this.id)">
                                        <span class="help-block">例如:如果应用平均命中率低于80%报警，填写80</span>
                                    </div>
                                </div>

                                <div class="form-group">
                                    <label class="control-label col-md-3">开启应用全局报警:</label>
                                    <div class="col-md-7">
                                        <select id="isAccessMonitor" name="isAccessMonitor" class="form-control">
                                            <option value="0"
                                                    <c:if test="${appDetail.appDesc.isAccessMonitor == null || appDetail.appDesc.isAccessMonitor == 0}">selected</c:if>>
                                                否
                                            </option>
                                            <option value="1"
                                                    <c:if test="${appDetail.appDesc.isAccessMonitor == 1}">selected</c:if>>
                                                是
                                            </option>
                                        </select>
                                        <span class="help-block">是:接收到全局报警指标报警邮件 否:反之则不接收</span>
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
                            onclick="appAlertConfigChange('${appDetail.appDesc.appId}')">Ok
                    </button>
                </div>

            </form>
        </div>
    </div>
</div>

<div id="updateWholeAlertConfigModal" class="modal fade" tabindex="-1" data-width="400">
    <div class="modal-dialog">
        <div class="modal-content">

            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true"></button>
                <h4 class="modal-title">全局报警项配置修改</h4>
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
                                        <input type="text" name="appConfigKey" id="appMonitorConfigKey" value=""
                                               placeholder="例如:aof_current_size" class="form-control"/>
                                    </div>
                                </div>

                                <div class="form-group">
                                    <label class="control-label col-md-3">配置值:</label>
                                    <div class="col-md-8">
                                        <input type="text" name="appConfigValue" id="appMonitorConfigValue" value=""
                                               placeholder="例如:10000" class="form-control">
                                    </div>
                                </div>

                                <div class="form-group">
                                    <label class="control-label col-md-3">修改原因:</label>
                                    <div class="col-md-8">
                                        <textarea name="appConfigReason" id="appMonitorConfigReason" value=""
                                                  placeholder="例如：修改原因:aof实例内存较大。" class="form-control"></textarea>
                                    </div>
                                </div>

                            </div>
                            <!-- form-body 结束 -->
                        </div>
                        <div id="updateConfigChangeInfo"></div>
                        <!-- 控件结束 -->
                    </div>
                </div>

                <div class="modal-footer">
                    <button type="button" data-dismiss="modal" class="btn">Close</button>
                    <button type="button" id="updateConfigChangeBtn" class="btn red"
                            onclick="updateWholeAlertConfigChange('${appDetail.appDesc.appId}')">Ok
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>

<div id="updateAppDetailModal" class="modal fade" tabindex="-1" data-width="400">
    <div class="modal-dialog">
        <div class="modal-content">

            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true"></button>
                <h4 class="modal-title">应用信息修改</h4>
            </div>

            <form class="form-horizontal form-bordered form-row-stripped">
                <div class="modal-body">
                    <div class="row">
                        <!-- 控件开始 -->
                        <div class="col-md-12">
                            <!-- form-body开始 -->
                            <div class="form-body">
                                <div class="form-group">
                                    <label class="control-label col-md-3">应用名:</label>
                                    <div class="col-md-7">
                                        <input type="text" name="appDescName" value="${appDetail.appDesc.name}"
                                               id="appDescName" class="form-control">
                                    </div>
                                </div>

                                <div class="form-group">
                                    <label class="control-label col-md-3">应用描述:</label>
                                    <div class="col-md-7">
                                        <textarea class="form-control" name="appDescIntro" rows="3" id="appDescIntro"
                                                  placeholder="应用描述">${appDetail.appDesc.intro}</textarea>
                                    </div>
                                </div>

                                <div class="form-group">
                                    <label class="control-label col-md-3">项目负责人:</label>
                                    <input type="text" name="officer" value="${appDetail.appDesc.officer}" id="officer" hidden/>
                                    <div class="col-md-6" style="margin-left: -20px">
                                        <select id="officer_select"
                                                class="selectpicker bla bla bli col-md-6" multiple
                                                data-live-search="true">
                                            <c:forEach items="${userMap}" var="userEntry">
                                                <c:set value="${userEntry.value}" var="user"/>
                                                <option value="${user.id}">${user.chName}(${user.name})</option>
                                            </c:forEach>
                                            </optgroup>
                                        </select>
                                    </div>
                                </div>

                            </div>
                            <!-- form-body 结束 -->
                        </div>
                        <div id="updateAppDetailInfo"></div>
                        <!-- 控件结束 -->
                    </div>
                </div>

                <div class="modal-footer">
                    <button type="button" data-dismiss="modal" class="btn">Close</button>
                    <button type="button" id="updateAppDetailBtn" class="btn red"
                            onclick="updateAppDetailChange('${appDetail.appDesc.appId}')">Ok
                    </button>
                </div>

            </form>
        </div>
    </div>
</div>


<c:forEach items="${appDetail.appUsers}" var="user">
    <div id="addUserModal${user.id}" class="modal fade" tabindex="-1" data-width="400">
        <div class="modal-dialog">
            <div class="modal-content">

                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true"></button>
                    <h4 class="modal-title">管理用户</h4>
                </div>

                <form class="form-horizontal form-bordered form-row-stripped">
                    <div class="modal-body">
                        <div class="row">
                            <!-- 控件开始 -->
                            <div class="col-md-12">
                                <!-- form-body开始 -->
                                <div class="form-body">
                                    <div class="form-group">
                                        <label class="control-label col-md-3">
                                            域账户名:
                                        </label>
                                        <div class="col-md-5">
                                            <input type="text" name="name" id="name${user.id}"
                                                   value="${user.name}" placeholder="域账户名(邮箱前缀)"
                                                   class="form-control"/>
                                        </div>
                                    </div>

                                    <div class="form-group">
                                        <label class="control-label col-md-3">
                                            中文名:
                                        </label>
                                        <div class="col-md-5">
                                            <input type="text" name="chName" id="chName${user.id}"
                                                   value="${user.chName}" placeholder="中文名"
                                                   class="form-control"/>
                                        </div>
                                    </div>

                                    <div class="form-group">
                                        <label class="control-label col-md-3">
                                            邮箱:
                                        </label>
                                        <div class="col-md-5">
                                            <input type="text" name="email" id="email${user.id}"
                                                   value="${user.email}" placeholder="邮箱"
                                                   class="form-control"/>
                                        </div>
                                    </div>

                                    <div class="form-group">
                                        <label class="control-label col-md-3">
                                            手机:
                                        </label>
                                        <div class="col-md-5">
                                            <input type="text" name="mobile" id="mobile${user.id}"
                                                   value="${user.mobile}" placeholder="手机"
                                                   class="form-control"/>
                                        </div>
                                    </div>

                                    <div class="form-group">
                                        <label class="control-label col-md-3">
                                            微信:
                                        </label>
                                        <div class="col-md-5">
                                            <input type="text" name="weChat" id="weChat${user.id}"
                                                   value="${user.weChat}" placeholder="微信"
                                                   class="form-control"/>
                                        </div>
                                    </div>

                                    <div class="form-group">
                                        <label class="control-label col-md-3">
                                            是否收报警:
                                        </label>
                                        <div class="col-md-5">
                                            <select name="isAlert" id="isAlert${user.id}"
                                                    class="form-control select2_category">
                                                <option value="1" <c:if test="${user.isAlert == 1}">selected</c:if>>
                                                    是
                                                </option>
                                                <option value="0" <c:if test="${user.isAlert == 0}">selected</c:if>>
                                                    否
                                                </option>
                                            </select>
                                        </div>
                                    </div>

                                    <input type="hidden" id="type${user.id}" value="${user.type}">
                                    <input type="hidden" id="userId${user.id}" name="userId" value="${user.id}"/>
                                </div>
                                <!-- form-body 结束 -->
                            </div>
                            <div id="info${user.id}"></div>
                            <!-- 控件结束 -->
                        </div>
                    </div>

                    <div class="modal-footer">
                        <button type="button" data-dismiss="modal" class="btn">Close</button>
                        <button type="button" class="btn red"
                                onclick="saveOrUpdateUser('${user.id}','${appDetail.appDesc.appId}')">Ok
                        </button>
                    </div>
                </form>
            </div>
        </div>
    </div>
</c:forEach>
</body>
</html>


