<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<div class="row">
    <div id="console" class="console"></div>
    <script type="text/javascript">
        $(document).ready(function () {
            var console = $('#console');
            var controller = console.console({
                promptLabel: 'appId:${appId}> ',
                commandValidate: function (line) {
                    if (line == "") return false;
                    else return true;
                },
                commandHandle: function (line,report) {
                    $.ajax({
                        url: "/admin/app/commandExecute.json",
                        data: {appId: $('#appId').val(), command: line},
                        dataType: "json",
                        success: function (result) {
                            report([
                                {msg: result.result,
                                    className: "jquery-console-message-value"}
                            ]);
                        }
                    });
                },
                autofocus: true,
                animateScroll: true,
                promptHistory: true
            });
        });
    </script>
    <center><h4><font color='red'>注意：非测试应用只可以执行只读命令，如有需要清理数据请联系管理员！</font></h4></center>

    <input type="hidden" id="appId" value="${appId}">

</div>