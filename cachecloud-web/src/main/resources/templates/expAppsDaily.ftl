<!DOCTYPE html>
<head>
    <meta charset=UTF-8/>
    <title>应用日报</title>
</head>
<body>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<p>

<table style="width:100%; font-size:12px;" width="100%" cellpadding="0" cellspacing="0">
    <colgroup>
        <col style="width: 5px;">
    </colgroup>

    <tr>
        <td>
        </td>
        <td style="padding-top:20px; padding-left:27px;">

            <ul>
                <li>
                    <span style="font-weight: bold; padding-top:20px; color:#3f3f3f;">
                        应用异常情况
                        <a target="_blank" href="${ccDomain}/manage/app/stat/list?tabId=0&searchDate=${searchDate}">【查看更多--后台】</a>
                    </span>
                </li>
            </ul>
            <table style="table-layout:fixed;width: 872px;border-collapse: collapse;word-break: break-all;word-wrap:break-word;border-top: 1px dotted #676767;text-align: center;color: #000; font-family:'宋体'; font-size:12px; margin-top:10px; margin-left: 24px">
                <tr>
                    <td style="border-right: 1px dotted #676767; border-bottom: 1px dotted #676767; height:33px; width: 50px;">
                        应用id
                    </td>
                    <td style="border-right: 1px dotted #676767; border-bottom: 1px dotted #676767; height:33px; width: 50px;">
                        应用名
                    </td>
                    <td style="border-right: 1px dotted #676767; border-bottom: 1px dotted #676767; height:33px; width: 150px;/">
                        应用负责人
                    </td>
                    <td style="border-right: 1px dotted #676767; border-bottom: 1px dotted #676767; height:33px; width: 50px;">
                        异常总量(全天)
                    </td>
                    <td style="border-right: 1px dotted #676767; border-bottom: 1px dotted #676767; height:33px; width: 50px;">
                        连接异常(全天)
                    </td>
                    <td style="border-right: 1px dotted #676767; border-bottom: 1px dotted #676767; height:33px; width: 50px;">
                        命令超时(全天)
                    </td>
                    <td style="border-right: 1px dotted #676767; border-bottom: 1px dotted #676767; height:33px; width: 50px;">
                        延迟事件(全天)
                    </td>
                    <td style="border-right: 1px dotted #676767; border-bottom: 1px dotted #676767; height:33px; width: 50px;">
                        慢查询(全天)
                    </td>
                </tr>
                <#assign expAppStats=appClientGatherStatGroup["expAppStats"]>
                <#list expAppStats as item>
                    <tr>
                        <#assign appid=item["app_id"]?c>
                        <td style="border-right: 1px dotted #676767; border-bottom: 1px dotted #676767; height:33px; width: 50px;">
                            <a target="_blank" href="${ccDomain}/admin/app/index?appId=${appid}">
                                ${appid}
                            </a>
                        </td>
                        <td style="border-right: 1px dotted #676767; border-bottom: 1px dotted #676767; height:33px; width: 50px;">
                            ${appDescMap[appid].name!}
                        </td>
                        <td style="border-right: 1px dotted #676767; border-bottom: 1px dotted #676767; height:33px; width: 100px;">
                            ${appDescMap[appid].officer!}
                        </td>
                        <td style="border-right: 1px dotted #676767; border-bottom: 1px dotted #676767; height:33px; width: 50px;">
                            <a target="_blank"
                               href="${ccDomain}/client/show/index?searchDate=${searchDate}&appId=${appid}"
                               &tabTag=app_client_exception_statistics">
                                ${item["exp_count"]}
                            </a>
                        </td>
                        <td style="border-right: 1px dotted #676767; border-bottom: 1px dotted #676767; height:33px; width: 50px;">
                            <a target="_blank"
                               href="${ccDomain}/client/show/exceptionStatistics/client?searchDate=${searchDate}&appId=${appid}&exceptionType=0">
                                次数:${item["conn_exp_count"]}
                            </a>
                            <br/>
                            平均耗时(ms):${item['avg_conn_exp_cost']}
                        </td>
                        <td style="border-right: 1px dotted #676767; border-bottom: 1px dotted #676767; height:33px; width: 50px;">
                            <a target="_blank"
                               href="${ccDomain}/client/show/exceptionStatistics/client?searchDate=${searchDate}&appId=${appid}&exceptionType=1">
                                次数:${item["cmd_exp_count"]}
                            </a>
                            <br/>
                            平均耗时(ms):${item["avg_cmd_exp_cost"]}
                        </td>
                        <td style="border-right: 1px dotted #676767; border-bottom: 1px dotted #676767; height:33px; width: 50px;">
                            <a target="_blank"
                               href="${ccDomain}/admin/app/index?searchDate=${searchDate}&appId=${appid}&tabTag=app_latency">
                                ${item["latency_count"]}
                            </a>
                        </td>
                        <td style="border-right: 1px dotted #676767; border-bottom: 1px dotted #676767; height:33px; width: 50px;">
                            <a target="_blank"
                               href="${ccDomain}/admin/app/index?searchDate=${searchDate}&appId=${appid}&tabTag=app_latency">
                                ${item["slow_log_count"]}
                            </a>
                        </td>
                    </tr>
                </#list>
            </table>


            <ul>
                <li>
                    <span style="font-weight: bold; padding-top:20px; color:#3f3f3f;">
                    应用延迟事件（top 10）
                        <a target="_blank" href="${ccDomain}/manage/app/stat/list?tabId=0&searchDate=${searchDate}">【查看更多--后台】</a>
                    </span>
                </li>
            </ul>
            <table style="table-layout:fixed;width: 872px;border-collapse: collapse;word-break: break-all;word-wrap:break-word;border-top: 1px dotted #676767;text-align: center;color: #000; font-family:'宋体'; font-size:12px; margin-top:10px; margin-left: 24px">
                <tr>
                    <td style="border-right: 1px dotted #676767; border-bottom: 1px dotted #676767; height:33px; width: 50px;">
                        应用id
                    </td>
                    <td style="border-right: 1px dotted #676767; border-bottom: 1px dotted #676767; height:33px; width: 50px;">
                        应用名
                    </td>
                    <td style="border-right: 1px dotted #676767; border-bottom: 1px dotted #676767; height:33px; width: 150px;/">
                        应用负责人
                    </td>
                    <td style="border-right: 1px dotted #676767; border-bottom: 1px dotted #676767; height:33px; width: 50px;">
                        延迟事件(全天)
                    </td>
                    <td style="border-right: 1px dotted #676767; border-bottom: 1px dotted #676767; height:33px; width: 50px;">
                        慢查询(全天)
                    </td>
                </tr>
                <#assign latencyAppStats=appClientGatherStatGroup["latencyAppStats"]>
                <#list latencyAppStats as item>
                    <tr>
                        <#assign appid=item["app_id"]?c>
                        <td style="border-right: 1px dotted #676767; border-bottom: 1px dotted #676767; height:33px; width: 50px;">
                            <a target="_blank"
                               href="${ccDomain}/admin/app/index?appId=${appid}">
                                ${appid}
                            </a>
                        </td>
                        <td style="border-right: 1px dotted #676767; border-bottom: 1px dotted #676767; height:33px; width: 50px;">
                            ${appDescMap[appid].name!}
                        </td>
                        <td style="border-right: 1px dotted #676767; border-bottom: 1px dotted #676767; height:33px; width: 100px;">
                            ${appDescMap[appid].officer!}
                        </td>
                        <td style="border-right: 1px dotted #676767; border-bottom: 1px dotted #676767; height:33px; width: 50px;">
                            <a target="_blank"
                               href="${ccDomain}/admin/app/index?searchDate=${searchDate}&appId=${appid}&tabTag=app_latency">
                                ${item['latency_count']}
                            </a>
                        </td>
                        <td style="border-right: 1px dotted #676767; border-bottom: 1px dotted #676767; height:33px; width: 50px;">
                            <a target="_blank"
                               href="${ccDomain}/admin/app/index?searchDate=${searchDate}&appId=${appid}&tabTag=app_latency">
                                ${item['slow_log_count']}
                            </a>
                        </td>
                    </tr>
                </#list>
            </table>

            <ul>
                <li>
                    <span style="font-weight: bold; padding-top:20px; color:#3f3f3f;">
                    应用拓扑诊断
                        <a target="_blank"
                           href="${ccDomain}/manage/app/stat/list/server?tabId=3&searchDate=${searchDate}">【查看更多--后台】</a>
                    </span>
                </li>
            </ul>
            <table style="table-layout:fixed;width: 872px;border-collapse: collapse;word-break: break-all;word-wrap:break-word;border-top: 1px dotted #676767;text-align: center;color: #000; font-family:'宋体'; font-size:12px; margin-top:10px; margin-left: 24px">
                <tr>
                    <td style="border-right: 1px dotted #676767; border-bottom: 1px dotted #676767; height:33px; width: 50px;">
                        应用id
                    </td>
                    <td style="border-right: 1px dotted #676767; border-bottom: 1px dotted #676767; height:33px; width: 50px;">
                        应用名
                    </td>
                    <td style="border-right: 1px dotted #676767; border-bottom: 1px dotted #676767; height:33px; width: 150px;/">
                        应用负责人
                    </td>
                    <td style="border-right: 1px dotted #676767; border-bottom: 1px dotted #676767; height:33px; width: 150px;/">
                        redis类型
                    </td>
                    <td style="border-right: 1px dotted #676767; border-bottom: 1px dotted #676767; height:33px; width: 50px;">
                        redis版本
                    </td>
                    <td style="border-right: 1px dotted #676767; border-bottom: 1px dotted #676767; height:33px; width: 50px;">
                        拓扑诊断结果
                    </td>
                </tr>
                <#assign topologyAppStats=appClientGatherStatGroup["topologyAppStats"]>
                <#list topologyAppStats as item>
                    <tr>
                        <#assign appid=item["app_id"]?c>
                        <td style="border-right: 1px dotted #676767; border-bottom: 1px dotted #676767; height:33px; width: 50px;">
                            <a target="_blank"
                               href="${ccDomain}/admin/app/index?appId=${appid}">
                                ${appid}
                            </a>
                        </td>
                        <td style="border-right: 1px dotted #676767; border-bottom: 1px dotted #676767; height:33px; width: 50px;">
                            ${appDescMap[appid].name!}
                        </td>
                        <td style="border-right: 1px dotted #676767; border-bottom: 1px dotted #676767; height:33px; width: 100px;">
                            ${appDescMap[appid].officer!}
                        </td>
                        <td style="border-right: 1px dotted #676767; border-bottom: 1px dotted #676767; height:33px; width: 50px;">
                            ${appDescMap[appid].typeDesc!}
                        </td>
                        <td style="border-right: 1px dotted #676767; border-bottom: 1px dotted #676767; height:33px; width: 50px;">
                            ${appDescMap[appid].versionName!}
                        </td>
                        <td style="border-right: 1px dotted #676767; border-bottom: 1px dotted #676767; height:33px; width: 50px;">
                            <span style="color:red">异常</span>
                            [<a target="_blank"
                                href="${ccDomain}/manage/app/index?appId=${appid}&tabTag=app_ops_tool">查看诊断</a>]
                        </td>
                    </tr>
                </#list>
            </table>

            <ul>
                <li>
                    <span style="font-weight: bold; padding-top:20px; color:#3f3f3f;">
                        应用内存使用情况（bottom 10）
                        <a target="_blank"
                           href="${ccDomain}/manage/app/stat/list/server?tabId=1&searchDate=${searchDate}">【查看更多--后台】</a>
                </span>
                </li>
            </ul>
            <table style="table-layout:fixed;width: 872px;border-collapse: collapse;word-break: break-all;word-wrap:break-word;border-top: 1px dotted #676767;text-align: center;color: #000; font-family:'宋体'; font-size:12px; margin-top:10px; margin-left: 24px">
                <tr>
                    <td style="border-right: 1px dotted #676767; border-bottom: 1px dotted #676767; height:33px; width: 50px;">
                        应用id
                    </td>
                    <td style="border-right: 1px dotted #676767; border-bottom: 1px dotted #676767; height:33px; width: 50px;">
                        应用名
                    </td>
                    <td style="border-right: 1px dotted #676767; border-bottom: 1px dotted #676767; height:33px; width: 150px;/">
                        应用负责人
                    </td>
                    <td style="border-right: 1px dotted #676767; border-bottom: 1px dotted #676767; height:33px; width: 50px;">
                        redis类型
                    </td>
                    <td style="border-right: 1px dotted #676767; border-bottom: 1px dotted #676767; height:33px; width: 50px;">
                        内存
                    </td>
                    <td style="border-right: 1px dotted #676767; border-bottom: 1px dotted #676767; height:33px; width: 50px;">
                        使用内存
                    </td>
                    <td style="border-right: 1px dotted #676767; border-bottom: 1px dotted #676767; height:33px; width: 50px;">
                        内存使用率
                    </td>
                    <td style="border-right: 1px dotted #676767; border-bottom: 1px dotted #676767; height:33px; width: 50px;">
                        应用客户端连接
                    </td>
                </tr>
                <#assign memAlterAppStats=appClientGatherStatGroup["memAlterAppStats"]>
                <#list memAlterAppStats as item>
                    <tr>
                        <#assign appid=item["app_id"]?c>
                        <td style="border-right: 1px dotted #676767; border-bottom: 1px dotted #676767; height:33px; width: 50px;">
                            <a target="_blank"
                               href="${ccDomain}/admin/app/index?appId=${appid}">
                                ${appid}
                            </a>
                        </td>
                        <td style="border-right: 1px dotted #676767; border-bottom: 1px dotted #676767; height:33px; width: 50px;">
                            ${appDescMap[appid].name!}
                        </td>
                        <td style="border-right: 1px dotted #676767; border-bottom: 1px dotted #676767; height:33px; width: 100px;">
                            ${appDescMap[appid].officer!}
                        </td>
                        <td style="border-right: 1px dotted #676767; border-bottom: 1px dotted #676767; height:33px; width: 50px;">
                            ${appDescMap[appid].typeDesc!}
                        </td>
                        <td style="border-right: 1px dotted #676767; border-bottom: 1px dotted #676767; height:33px; width: 50px;">
                            ${item["format_mem"]} G
                        </td>
                        <td style="border-right: 1px dotted #676767; border-bottom: 1px dotted #676767; height:33px; width: 50px;">
                            ${item["format_used_memory"]}
                        </td>
                        <td style="border-right: 1px dotted #676767; border-bottom: 1px dotted #676767; height:33px; width: 50px;">
                            ${item["mem_used_ratio"]} %
                        </td>
                        <td style="border-right: 1px dotted #676767; border-bottom: 1px dotted #676767; height:33px; width: 50px;">
                            <a href="${ccDomain}/admin/app/index?appId=${appid}&tabTag=app_clientList"
                               target="_blank">
                                ${item["connected_clients"]}
                            </a>
                        </td>
                    </tr>
                </#list>
            </table>


            <ul>
                <li>
                    <span style="font-weight: bold; padding-top:20px; color:#3f3f3f;">
                        应用碎片率情况（top 10）
                        <a target="_blank"
                           href="${ccDomain}/manage/app/stat/list/server?tabId=2&searchDate=${searchDate}">【查看更多--后台】</a>
                </span>
                </li>
            </ul>
            <table style="table-layout:fixed;width: 872px;border-collapse: collapse;word-break: break-all;word-wrap:break-word;border-top: 1px dotted #676767;text-align: center;color: #000; font-family:'宋体'; font-size:12px; margin-top:10px; margin-left: 24px">
                <tr>
                    <td style="border-right: 1px dotted #676767; border-bottom: 1px dotted #676767; height:33px; width: 50px;">
                        应用id
                    </td>
                    <td style="border-right: 1px dotted #676767; border-bottom: 1px dotted #676767; height:33px; width: 50px;">
                        应用名
                    </td>
                    <td style="border-right: 1px dotted #676767; border-bottom: 1px dotted #676767; height:33px; width: 150px;/">
                        应用负责人
                    </td>
                    <td style="border-right: 1px dotted #676767; border-bottom: 1px dotted #676767; height:33px; width: 50px;">
                        redis版本
                    </td>
                    <td style="border-right: 1px dotted #676767; border-bottom: 1px dotted #676767; height:33px; width: 50px;">
                        内存使用
                    </td>
                    <td style="border-right: 1px dotted #676767; border-bottom: 1px dotted #676767; height:33px; width: 50px;">
                        rss内存使用
                    </td>
                    <td style="border-right: 1px dotted #676767; border-bottom: 1px dotted #676767; height:33px; width: 50px;">
                        平均碎片率
                    </td>
                </tr>
                <#assign fragRatioAppStats=appClientGatherStatGroup["fragRatioAppStats"]>
                <#list fragRatioAppStats as item>
                    <tr>
                        <#assign appid=item["app_id"]?c>
                        <td style="border-right: 1px dotted #676767; border-bottom: 1px dotted #676767; height:33px; width: 50px;">
                            <a target="_blank"
                               href="${ccDomain}/admin/app/index?appId=${appid}">
                                ${appid}
                            </a>
                        </td>
                        <td style="border-right: 1px dotted #676767; border-bottom: 1px dotted #676767; height:33px; width: 50px;">
                            ${appDescMap[appid].name!}
                        </td>
                        <td style="border-right: 1px dotted #676767; border-bottom: 1px dotted #676767; height:33px; width: 100px;">
                            ${appDescMap[appid].officer!}
                        </td>
                        <td style="border-right: 1px dotted #676767; border-bottom: 1px dotted #676767; height:33px; width: 50px;">
                            ${appDescMap[appid].versionName!}
                        </td>
                        <td style="border-right: 1px dotted #676767; border-bottom: 1px dotted #676767; height:33px; width: 50px;">
                            ${item["format_used_memory"]} G
                        </td>
                        <td style="border-right: 1px dotted #676767; border-bottom: 1px dotted #676767; height:33px; width: 50px;">
                            ${item["format_used_memory_rss"]} G
                        </td>
                        <td style="border-right: 1px dotted #676767; border-bottom: 1px dotted #676767; height:33px; width: 50px;">
                            ${item["avg_mem_frag_ratio"]}
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