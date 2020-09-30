<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@include file="/WEB-INF/jsp/manage/commons/taglibs.jsp" %>
<%@include file="/WEB-INF/jsp/manage/include/cache_cloud_main_js_include.jsp" %>
<%@include file="/WEB-INF/jsp/manage/include/cache_cloud_main_css.jsp" %>

<script>

    function pushResource(resourceId){

        var resource_id = (typeof resourceId == 'undefined') ? $('#resource_id').html() : resourceId;
        $.post(
            '/manage/app/resource/push.json',
            {
                repositoryId: $('#repositoryId').html(),
                resourceId: resource_id
            },
            function (data) {
                var status = data.status;
                if (status == 1) {
                    alert("推送成功");
                } else {
                    alert("推送失败！" + data.message);
                }
                window.location.reload();
            }
        );
    }

</script>

<div class="row">
    <div class="col-md-12">
        <h4 class="glyphicon glyphicon-th-list" >
            目录管理
        </h4>
        <div class="form-group col-md-1" style="float:right; width: max-content">
            <button id="redis_version" class="btn green btn-sm" style="float: right;" data-target="#addResourceModal" data-toggle="modal" >
                 <i class="fa fa-plus"></i>新建目录
            </button>
        </div>
    </div>
</div>

<div class="row">
    <div class="col-md-12">
        <div style="float:left">
            <form class="form-inline" role="form" method="post" action="/manage/app/resource/index?tab=dir"
                  id="appList" name="ec">

                <div class="form-group">
                     <label id="repositoryId" class="control-label" style="display:none">${repository.id}</label>

                    <input type="text" class="form-control" id="searchName" name="searchName"
                           value="${searchName}" placeholder="脚本名称">
                </div>
                <button type="submit" class="btn btn-success">查询</button>
            </form>
        </div>
    </div>
</div>
<br/>
<div class="row">
    <div class="col-md-12">
        <div class="portlet box light-grey" id="clientIndex">

            <table class="table table-striped table-bordered table-hover" id="tableDataList">
                <thead>
                <tr>
                    <td>序号</td>
                    <th>目录名称</th>
                    <th>说明</th>
                    <th>最后更新时间</th>
                    <th>操作人</th>
                    <th>状态</th>
                    <th>操作</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach items="${resourceList}" var="resource">
                    <tr>
                        <td>${resource.id}</td>
                        <td>
                            <c:if test="${resource.ispush == 0}">${resource.name}</c:if>
                            <c:if test="${resource.ispush == 1}"><a target="_blank" href="${repository.url}${resource.name}">${resource.name}</a></c:if>
                        </td>
                        <td>
                            ${resource.intro}
                        </td>
                        <td>
                            <fmt:formatDate value="${resource.lastmodify}" pattern="yyyy-MM-dd HH:mm:ss"/>
                        </td>
                        <td>
                            ${resource.username}
                        </td>
                          <td>
                            <c:if test="${resource.ispush == 0}">未推送</c:if>
                            <c:if test="${resource.ispush == 1}"><span style="color:green">已推送</span></c:if>
                        </td>
                        <td>
                            <button type="button" class="btn btn-info" data-target="#addResourceModal" data-toggle="modal"
                            data-resource_id="${resource.id}" >
                                修改
                            </button>
                            <c:if test="${resource.ispush == 0}">
                                <button id="resource" class="btn btn-success" onclick="pushResource(${resource.id})" data-toggle="modal">推送</button>
                            </c:if>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </div>
</div>

<%@ include file="addDir.jsp" %>




