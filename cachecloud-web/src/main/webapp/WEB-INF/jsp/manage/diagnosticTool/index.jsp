<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp" %>

<script type="text/javascript" src="/resources/bootstrap/bootstrap3/js/bootstrap.js"></script>
<script type="text/javascript" src="/resources/js/docs.min.js"></script>

<div class="page-container">
    <div class="page-content">

        <div class="row">
            <div class="col-md-12">
                <h2 class="page-header">
                    应用诊断工具
                </h2>
            </div>
        </div>
        <div class="tabbable-custom">
            <ul class="nav nav-tabs" id="app_tabs">
                <li id="redis-cli" class="active" data-url="/manage/app/tool/diagnostic/tool?tabTag=redis-cli">
                    <a href="?tabTag=redis-cli">redis-cli工具</a>
                </li>
                <li id="scan"
                    data-url="/manage/app/tool/diagnostic/tool?appId=${appId}&parentTaskId=${parentTaskId}&auditId=${auditId}&diagnosticStatus=${diagnosticStatus}&tabTag=scan">
                    <a href="?tabTag=scan">scan检测</a>
                </li>
                <li id="memoryUsed" data-url="/manage/app/tool/diagnostic/tool?appId=${appId}&parentTaskId=${parentTaskId}&auditId=${auditId}&diagnosticStatus=${diagnosticStatus}&tabTag=memoryUsed">
                    <a href="?tabTag=memoryUsed">memoryUsed诊断</a>
                </li>
                <li id="idlekey" data-url="/manage/app/tool/diagnostic/tool?appId=${appId}&parentTaskId=${parentTaskId}&auditId=${auditId}&diagnosticStatus=${diagnosticStatus}&tabTag=idlekey">
                    <a href="?tabTag=idlekey">idlekey诊断</a>
                </li>
                <li id="hotkey" data-url="/manage/app/tool/diagnostic/tool?appId=${appId}&parentTaskId=${parentTaskId}&auditId=${auditId}&diagnosticStatus=${diagnosticStatus}&tabTag=hotkey">
                    <a href="?tabTag=hotkey">hotkeys/bigkeys/memkeys诊断</a>
                </li>
                <li id="deleteKey" data-url="/manage/app/tool/diagnostic/tool?appId=${appId}&parentTaskId=${parentTaskId}&auditId=${auditId}&diagnosticStatus=${diagnosticStatus}&tabTag=deleteKey">
                    <a href="?tabTag=deleteKey">删除任务</a>
                </li>
                <li id="slotAnalysis" data-url="/manage/app/tool/diagnostic/tool?appId=${appId}&parentTaskId=${parentTaskId}&auditId=${auditId}&diagnosticStatus=${diagnosticStatus}&tabTag=slotAnalysis">
                    <a href="?tabTag=slotAnalysis">集群slot分析</a>
                </li>
            </ul>
            <div class="tab-content">
                <div class="tab-pane active" id="redis-cliTab">
                </div>
                <div class="tab-pane" id="scanTab">
                </div>
                <div class="tab-pane" id="memoryUsedTab">
                </div>
                <div class="tab-pane" id="idlekeyTab">
                </div>
                <div class="tab-pane" id="hotkeyTab">
                </div>
                <div class="tab-pane" id="deleteKeyTab">
                </div>
                <div class="tab-pane" id="slotAnalysisTab">
                </div>
            </div>
        </div>
    </div>
</div>
<script type="text/javascript">
    function showTab(tab) {
        $.get($("#" + tab).attr("data-url"), function (result) {
            $("#" + tab + "Tab").html(result);
        });
    }

    function refreshActiveTab() {
        var tab = getQueryString("tabTag");
        if (tab) {
            $("#" + tab).addClass("active").siblings().removeClass("active");
            $("#" + tab + "Tab").addClass("active").siblings().removeClass("active");
        } else {
            tab = "redis-cli";
        }
        console.log("tab:" + tab)
        showTab(tab);
        $("#tabs li a").tooltip({placement: "bottom"});
    }

    $(function () {
        refreshActiveTab();
    });

    function getQueryString(name) {
        var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
        var r = window.location.search.substr(1).match(reg);
        if (r != null) return unescape(r[2]);
        return null;
    }
</script>

