<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp" %>

<script type="text/javascript" src="/resources/bootstrap/bootstrap3/js/bootstrap.js"></script>
<script type="text/javascript" src="/resources/js/docs.min.js"></script>

<div class="page-container">
    <div class="page-content">

        <div class="row">
            <div class="col-md-20">
                <h2 class="page-header">
                    <image src="https://docs.redislabs.com/latest/images/icon_logo/logo-redis-2.svg" width="100px" height="60px"/>模块管理
                </h2>
                <div>
                    <button id="delete_module" class="btn gray btn-sm" style="float: right;" onclick="deleteModule()" data-toggle="modal" >
                         <i class="fa fa-minus"></i>移除模块
                    </button>
                    <button id="add_module" class="btn green btn-sm" style="float: right;" data-target="#addModuleModal" data-toggle="modal" >
                        <i class="fa fa-plus"></i>新增模块
                    </button>
                </div>
            </div>
        </div>

        <%@ include file="addModule.jsp" %>

        <div class="tabbable-custom">
            <ul class="nav nav-tabs" id="tabs">
                <c:forEach items="${allModules}" var="module">
                    <li moduleid="${module.id}" id="${module.name}"  name="moduleTab" <c:if test="${allModules.indexOf(module)==0}">class="active"</c:if>
                     data-url="/manage/app/resource/module/${module.name}" ><a href="?tab=${module.name}&">${module.name}</a></li>
                </c:forEach>
            </ul>
            <div class="tab-content" id="tabContent">
                 <c:forEach items="${allModules}" var="module">
                     <div class="tab-pane active" id="${module.name}Tab"/>
                </c:forEach>
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
    		tab = "RediSearch";
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

    function deleteModule(moduleId){
        if (confirm("确认要移除模块吗?")) {
            $.post(
                '/manage/app/resource/deleteModule.json',
                {
                    moduleId: $("#tabs li[class='active']").attr("moduleId")
                },
                function (data) {
                    var status = data.status;
                    if (status == 1) {
                        window.location.reload();
                    } else {
                        $("#tips").html(data.message);
                    }

                }
            );
        }
    }

</script>
