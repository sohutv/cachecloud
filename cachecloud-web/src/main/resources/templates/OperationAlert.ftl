<!DOCTYPE html>
<head>
    <meta charset=UTF-8/>
    <title>CacheCloud运维操作通知</title>
</head>
<body>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<p>
<table style="width:100%; font-size:12px;" width="100%" cellpadding="0" cellspacing="0">
    <colgroup>
        <col style="width: 5px;">
    </colgroup>
    <tr>
        <td></td>
        <td style="padding-top:20px; padding-left:27px;">
            <ul>
                <li><span style="font-weight: bold; padding-top:20px; color:#3f3f3f;">运维操作机器通知：</span></li>
            </ul>
            <table style="table-layout:fixed;width: 872px;border-collapse: collapse;word-break: break-all;word-wrap:break-word;border-top: 1px dotted #676767;text-align: center;color: #000; font-family:'宋体'; font-size:12px; margin-top:10px; margin-left: 24px">
                <tr>
                    <td style="border-right: 1px dotted #676767; border-bottom: 1px dotted #676767; height:33px; width: 50px;">
                        机器ip
                    </td>
                    <td style="border-right: 1px dotted #676767; border-bottom: 1px dotted #676767; height:33px; width: 50px;">
                        宿主机ip
                    </td>
                    <td style="border-right: 1px dotted #676767; border-bottom: 1px dotted #676767; height:33px; width: 50px;">
                        机器详情
                    </td>
                    <td style="border-right: 1px dotted #676767; border-bottom: 1px dotted #676767; height:33px; width: 50px;">
                        操作
                    </td>
                    <td style="border-right: 1px dotted #676767; border-bottom: 1px dotted #676767; height:33px; width: 50px;">
                        Status
                    </td>
                    <td style="border-right: 1px dotted #676767; border-bottom: 1px dotted #676767; height:33px; width: 50px;">
                        Message
                    </td>
                </tr>
                <#list operationAlertValueResultList as item>
                    <tr>
                        <td style="border-right: 1px dotted #676767; border-bottom: 1px dotted #676767; height:33px; width: 50px;">
                            ${item.ip!}
                        </td>
                        <td style="border-right: 1px dotted #676767; border-bottom: 1px dotted #676767; height:33px; width: 50px;">
                            <#if item.machineInfo??>
                                ${item.machineInfo.realIp!}
                            </#if>
                        </td>
                        <td style="border-right: 1px dotted #676767; border-bottom: 1px dotted #676767; height:33px; width: 50px;">
                            <#if item.machineInfo??>
                                内存：${item.machineInfo.mem!} G<br/>
                                cpu：${item.machineInfo.cpu!}<br/>
                                备注说明：${item.machineInfo.extraDesc!}
                            </#if>
                        </td>
                        <td style="border-right: 1px dotted #676767; border-bottom: 1px dotted #676767; height:33px; width: 50px;">
                            ${item.type!}
                        </td>
                        <td style="border-right: 1px dotted #676767; border-bottom: 1px dotted #676767; height:33px; width: 50px;">
                            ${item.status!}
                        </td>
                        <td style="border-right: 1px dotted #676767; border-bottom: 1px dotted #676767; height:33px; width: 50px;">
                            ${item.message!}
                        </td>
                    </tr>
                </#list>

            </table>
        </td>
    </tr>

</table>
</p>
</body>
</html>