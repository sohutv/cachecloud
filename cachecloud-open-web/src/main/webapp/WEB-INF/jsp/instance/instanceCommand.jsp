<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<div class="row">
    <br />
    <div id="console" class="col-md-12 console"></div>
    <script type="text/javascript">
        $(document).ready(function () {
            var console = $('#console');
            var controller = console.console({
                welcomeMessage: '欢迎使用缓存云平台控制台',
                promptLabel: 'instanceId:${instanceId}> ',
                commandValidate: function (line) {
                    if (line == "") return false;
                    else return true;
                },
                commandHandle: function (line, report) {
                    $.ajax({
                        url: "/admin/instance/commandExecute.json",
                        data: {instanceId: $('#instanceId').val(), command: line},
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

    <input type="hidden" id="instanceId" value="${instanceId}">


</div>