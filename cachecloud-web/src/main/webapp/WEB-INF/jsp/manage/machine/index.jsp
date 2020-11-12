<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp" %>

<script type="text/javascript" src="/resources/bootstrap/bootstrap3/js/bootstrap.js"></script>
<script type="text/javascript" src="/resources/js/docs.min.js"></script>

<div class="page-container">
    <div class="page-content">

        <div class="row">
            <div class="col-md-12">
                <h2 class="page-header">
                   机器管理
                </h2>
            </div>
        </div>
        <div class="tabbable-custom">
            <ul class="nav nav-tabs" id="app_tabs">
                <li id="machine" class="active"
                    data-url="/manage/machine/list?tabTag=machine&ipLike=${ipLike}&useType=${useType}&type=${type}&versionId=${versionId}&isInstall=${isInstall}&k8sType=${k8sType}&realip=${realip}">
                    <a href="?tabTag=machine">机器管理</a>
                </li>
                <li id="room"
                    data-url="/manage/machine/list?tabTag=room">
                    <a href="?tabTag=room">机房管理</a>
                </li>
            </ul>
            <div class="tab-content">
                <div class="tab-pane active" id="machineTab">
                </div>
                <div class="tab-pane" id="roomTab">
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
            tab = "machine";
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

