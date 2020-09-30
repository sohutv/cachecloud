<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp" %>
<div class="col-md-2" style="background: aliceblue">
    <ul class="nav nav-list" id="app_tabs">
        <li class="active">
            <a href="/admin/app/jobs">
                <i class="glyphicon glyphicon-list-alt"></i> 我的工单
            </a>
        </li>

        <li>
            <a href="#">
                <i class="glyphicon glyphicon-plus"></i> 创建工单
            </a>
        </li>

        <li>
            <a href="/admin/app/init">
                &nbsp;&nbsp;&nbsp;<i class="glyphicon glyphicon-cloud"></i> 申请应用
            </a>
        </li>
        <li>
            <a href="/admin/app/appDel">
                &nbsp;&nbsp;&nbsp;<i class="glyphicon glyphicon-trash"></i>  数据清理
            </a>
        </li>
        <li>
            <a href="/admin/app/appOffline">
                &nbsp;&nbsp;&nbsp;<i class="glyphicon glyphicon-remove"></i> 下线应用
            </a>
        </li>
        <li>
            <a href="/admin/app/appDataMigrate">
                &nbsp;&nbsp;&nbsp;<i class="glyphicon glyphicon-retweet"></i> 数据迁移
            </a>
        </li>
        <li>
            <a href="/admin/app/appDiagnostic">
                &nbsp;&nbsp;&nbsp;<i class="glyphicon glyphicon-ok-sign"></i> 诊断应用
            </a>
        </li>
        <li><a href="/admin/app/appKeyAnalysis?appId=${appId}">
            &nbsp;&nbsp;&nbsp;<i class="glyphicon glyphicon-star-empty"></i> 键值分析
        </a></li>
        <li>
            <a href="/admin/app/appScale?appId=${appId}">
                &nbsp;&nbsp;&nbsp;<i class="glyphicon glyphicon-resize-full"></i> 扩容/缩容
            </a>
        </li>
        <li><a href="/admin/app/appConfig?appId=${appId}&instanceId=${instanceId}">
            &nbsp;&nbsp;&nbsp;<i class="glyphicon glyphicon-edit"></i> 修改应用配置
        </a></li>
        <li>
            <a href="/admin/app/appAlterConfig?appId=${appId}">
                &nbsp;&nbsp;&nbsp;<i class="glyphicon glyphicon-bell"></i> 修改报警
            </a>
        </li>
    </ul>
</div>


<div class="col-md-1"></div>