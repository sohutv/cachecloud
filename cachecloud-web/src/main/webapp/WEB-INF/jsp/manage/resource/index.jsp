<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp" %>

<script type="text/javascript" src="/resources/bootstrap/bootstrap3/js/bootstrap.js"></script>
<script type="text/javascript" src="/resources/js/docs.min.js"></script>

<div class="page-container">
    <div class="page-content">

        <div class="row">
            <div class="col-md-6">
                <h2 class="page-header">
                    系统资源管理
                </h2>
            </div>
        </div>

        <div class="tabbable-custom">
            <ul class="nav nav-tabs" id="tabs"><li id="respo" class="active" data-url="/manage/app/resource/redis/respo">
                    <a href="?tab=respo&">仓库配置</a>
                </li>
                <li id="dir" data-url="/manage/app/resource/redis/dir?searchName=${searchName}"><a href="?tab=dir">目录管理</a></li>
                <li id="script" data-url="/manage/app/resource/redis/script?searchName=${searchName}"><a href="?tab=script">脚本管理</a></li>
                <li id="redis" data-url="/manage/app/resource/redis/redis?searchName=${searchName}"><a href="?tab=redis">Redis资源管理</a></li>
                <li id="tool" data-url="/manage/app/resource/redis/tool?searchName=${searchName}"><a href="?tab=tool">迁移工具管理</a></li>
                <%--<li id="module" data-url="/manage/app/resource/redis/module?searchName=${searchName}"><a href="?tab=module">Redis模块管理</a></li>--%>
                <%--<li id="sshkey" data-url="/manage/app/resource/redis/sshkey"><a href="?tab=sshkey">SSH Keys</a></li>--%>
            </ul>
            <div class="tab-content" id="tabContent">
                <div class="tab-pane active" id="respoTab">
                </div>
                <div class="tab-pane" id="dirTab">
                </div>
                <div class="tab-pane" id="scriptTab">
                </div>
                <div class="tab-pane" id="redisTab">
                </div>
                <div class="tab-pane" id="toolTab">
                </div>
                <%--<div class="tab-pane" id="sshkeyTab">--%>
                <%--</div>--%>
                <%--<div class="tab-pane" id="moduleTab">--%>
            </div>
        </div>
    </div>

</div>

<script type="text/javascript">
    function showTab(tab){
    	$.get($("#"+tab).attr("data-url"), function (result) {
        	$("#"+tab+"Tab").html(result);
        });
    }
    function refreshActiveTab(){
    	var tab = getQueryString("tab");
    	if(tab){
    		$("#"+tab).addClass("active").siblings().removeClass("active");
    		$("#"+tab+"Tab").addClass("active").siblings().removeClass("active");
    	} else {
    		tab = "respo";
    	}
    	showTab(tab);
    	$("#tabs li a").tooltip({placement:"bottom"});
    }
    $(function(){
    	refreshActiveTab();
    });

    function getQueryString(name){
        var reg = new RegExp("(^|&)"+ name +"=([^&]*)(&|$)");
        var r = window.location.search.substr(1).match(reg);
        if(r!=null)return  unescape(r[2]); return null;
    }

</script>