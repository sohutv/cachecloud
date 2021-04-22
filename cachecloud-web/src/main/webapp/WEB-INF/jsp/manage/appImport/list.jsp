<%@ taglib prefix="html" uri="http://struts.apache.org/tags-html" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp" %>
<%@include file="/WEB-INF/jsp/manage/include/cache_cloud_main_css.jsp" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>CacheCloud管理后台</title>
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <link href="/resources/css/mem-cloud.css" rel="stylesheet"/>
    <link href="/resources/css/common.css" rel="stylesheet" type="text/css"/>
</head>

<body class="page-header-fixed">
<%@include file="/WEB-INF/jsp/manage/include/head.jsp" %>

<%@include file="/WEB-INF/jsp/manage/include/left.jsp" %>

<%@include file="index.jsp" %>

<%@include file="/WEB-INF/jsp/manage/include/foot.jsp" %>

<%@include file="/WEB-INF/jsp/manage/include/cache_cloud_paginator_js.jsp" %>

<script type="text/javascript">
    var TableManaged = function () {
        return {
            //main function to initiate the module
            init: function () {
                if (!jQuery().dataTable) {
                    return;
                }
                console.log("jQuery().dataTable");
                $('#tableDataList').dataTable({
                    "searching": true,
                    "scrollX": true,
                    "autoWidth": true,
                    "bSort": true,
                    "bLengthChange": false,
                    "iDisplayLength": 10,
                    "sPaginationType": "bootstrap",
                    "aaSorting": [[6,'desc']],
                    "oLanguage": {
                        "oPaginate": {
                            "sFirst": "首页",
                            "sPrevious": "前一页",
                            "sNext": "后一页",
                            "sLast": "尾页"
                        },
                        "sLengthMenu": "每页显示 _MENU_条",
                        "sZeroRecords": "没有找到符合条件的数据",
                        "sInfo": "当前第 _START_ - _END_ 条　共计 _TOTAL_ 条",
                        "sSearch": "搜索：",
                    }
                });
            }
        };
    }();

    $(function () {
        TableManaged.init();
    });
</script>

</body>
</html>

