<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<script type="text/javascript" src="/resources/bootstrap/jquery/jquery-1.11.0.js"></script>
<%@include file="/WEB-INF/jsp/manage/commons/taglibs.jsp" %>
<%@include file="/WEB-INF/jsp/manage/include/cache_cloud_main_js_include.jsp" %>
<%@include file="/WEB-INF/jsp/manage/include/cache_cloud_main_css.jsp" %>
<script type="text/javascript" src="/resources/js/jquery-console.js"></script>

<script type="text/javascript">
    var container = $('#console');
    $(function () {
        $('.selectpicker').selectpicker({
            'selectedText': 'cat',
            'size': 8,
            'dropupAuto': false
        });
        $('.selectpicker').selectpicker('refresh');
        $('.dropdown-toggle').on('click', function () {
            $('.dropdown-toggle').dropdown();
        });

        var controller = container.console({
            promptLabel: 'redis-cli > ',
            commandValidate: function (line) {
                if (line == "") return false;
                else return true;
            },
            commandHandle: function (line, report) {
                var appId = $('#cli-select').selectpicker('val');
                if (appId == null || appId == '') {
                    alert("请选择应用");
                    return false;
                }

                var node = $('#cli_instance-select').selectpicker('val');
                if (node == null || node == '') {
                    alert("请选择实例");
                    return false;
                }

                var timeout = $('#timeout').val();


                $.post('/manage/app/tool/commandExecute',
                    {
                        appId: appId,
                        node: node,
                        command: line,
                        timeout: timeout
                    },
                    function (data) {
                        report([
                            {msg: data.result, className: "jquery-console-message-value"}
                        ]);
                    }
                );
            },
            autofocus: true,
            animateScroll: true,
            promptHistory: true
        });
    });

    function changeAppIdSelect(appId, instance_select) {
        console.log('instance_select:' + instance_select);
        console.log(appId);

        document.getElementById(instance_select).options.length = 0;

        $.post('/manage/app/tool/diagnostic/appInstances',
            {
                appId: appId,
            },
            function (data) {
                var status = data.status;
                if (status == 1) {
                    var appInstanceList = data.appInstanceList;
                    for (var i = 0; i < appInstanceList.length; i++) {
                        var val = appInstanceList[i].hostPort;
                        var term = appInstanceList[i].hostPort + '（角色：' + appInstanceList[i].roleDesc + '）'
                        $('#' + instance_select).append("<option value='" + val + "'>" + term + "</option>");
                    }
                    $('#' + instance_select).selectpicker('refresh');
                    $('#' + instance_select).selectpicker('render');
                } else {
                    console.log('data.status:' + status);
                }
            }
        );
    }

    function testisNum(id) {
        var value = document.getElementById(id).value;
        if (value != "" && isNaN(value)) {
            alert("超时时间请填入整数，单位为ms");
            document.getElementById(id).value = "";
            document.getElementById(id).focus();
        }
    }
</script>


<div class="row">
    <div id="scan-div" class="col-md-12">
        <form class="form-inline" role="form" name="ec">
            <div class="form-group col-md-2">
                <label for="cli-select">应用&nbsp;&nbsp;</label>
                <select id="cli-select" name="appId" class="selectpicker show-tick form-control"
                        data-live-search="true" title="选择应用" data-width="35%"
                        onchange="changeAppIdSelect(this.value,'cli_instance-select')">
                    <option value="">选择应用</option>
                    <c:forEach items="${appDescMap}" var="appDescEntry">
                        <c:set value="${appDescEntry.value}" var="appDesc"></c:set>
                        <option value="${appDesc.appId}" title="${appDesc.appId} ${appDesc.name}">
                            【${appDesc.appId}】&nbsp;名称：${appDesc.name}&nbsp;类型：${appDesc.typeDesc}&nbsp;版本：${appDesc.versionName}
                        </option>
                    </c:forEach>
                </select>
            </div>
            <div class="form-group col-md-2 col-md-offset-1">
                <label for="cli_instance-select">实例&nbsp;&nbsp;</label>
                <select id="cli_instance-select" name="nodes"
                        class="selectpicker show-tick form-control"
                        data-live-search="true" title="选择实例" data-width="35%" data-size="8">
                </select>
            </div>
            <div class="form-group col-md-2 col-md-offset-1">
                <label class="fa fa-question-circle" aria-hidden="true" title="超时时间，单位ms，默认30000ms"></label>
                <input id="timeout" style="width: 70%" type="text" class="form-control" name="timeout"
                       placeholder="超时时间（默认30000ms）" onchange="testisNum(this.id)">
            </div>
        </form>
    </div>
</div>

<br/>
<div class="col-md-12">
    <a class="fa fa-file-text">
        输入 --help 查看可用命令
    </a>

</div>
<br/>
<div class="col-md-12">
    <form class="form-inline" role="form">
        <div id="console" class="console"></div>
    </form>
</div>

