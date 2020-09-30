<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<script type="text/javascript" src="/resources/bootstrap/jquery/jquery-1.11.0.js"></script>
<%@include file="/WEB-INF/jsp/manage/commons/taglibs.jsp" %>
<%@include file="/WEB-INF/jsp/manage/include/cache_cloud_main_js_include.jsp" %>
<%@include file="/WEB-INF/jsp/manage/include/cache_cloud_main_css.jsp" %>


<script type="text/javascript">

    // 编译&推送
    function compile(resourceId,repositoryId,ispush){

        if(ispush == 1 && !window.confirm("资源已推送，确认覆盖？")){
            return ;
        }

        $.post(
            '/manage/app/resource/compile.json',
            {
                repositoryId: repositoryId,
                resourceId: resourceId
            },
            function (data) {
                var status = data.status;
                if (status == 1) {
                    alert("编译成功");
                } else {
                    alert("编译失败！" + data.message);
                }
                window.location.reload();
            }
        );
    }

    function search(){
        $.post(
            '/manage/app/resource/redis/tool',
            {
                searchName: $("#searchName").val()
            },
            function (data) {
                var status = data.status;
                if (status == 1) {
                    alert("编译成功");
                } else {
                    alert("编译失败！" + data.message);
                }
                window.location.reload();
            }
        );
    }
</script>

<div class="row">
    <div class="col-md-12">
        <h4 class="glyphicon glyphicon-folder-open" >
            迁移工具管理
        </h4>
        <div class="form-group col-md-1" style="float:right; width: max-content">
            <button id="redis_version" class="btn green btn-sm" style="float: right;" data-target="#addResourceModal" data-toggle="modal" >
                 <i class="fa fa-plus"></i>新建资源包
            </button>
        </div>
    </div>
</div>

<div class="row">
    <div class="col-md-12">
        <div style="float:left">
            <form class="form-inline" role="form" method="post" action="/manage/app/resource/index?tab=tool"
                  id="appList" name="ec">
                <div class="form-group">
                    <input type="text" class="form-control" id="searchName" name="searchName"
                           value="${searchName}" placeholder="资源名称">
                </div>
                <button type="submit" class="btn btn-success" onclick="search()">查询</button>
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
                    <td>资源id</td>
                    <th>资源名</th>
                    <th>说明</th>
                    <th>目录</th>
                    <th>源地址</th>
                    <th>最后修改时间</th>
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
                            <c:if test="${resource.ispush==1 || resource.ispush==3 }"><a target="_blank" href="${repository.url}${resource.dir}">${resource.name}</a></c:if>
                            <c:if test="${resource.ispush==0 || resource.ispush==2 || resource.ispush==4}">${resource.name}</c:if>
                        </td>
                        <td>
                            ${resource.intro}
                        </td>
                        <td>
                            ${resource.dir}
                        </td>
                        <td>
                            <c:if test="${resource.url != null && resource.url != ''}">
                                <a target="_blank" href="${resource.url}" title="${resource.url}">[下载]</a>
                            </c:if>
                        </td>
                        <td>
                            <fmt:formatDate value="${resource.lastmodify}" pattern="yyyy-MM-dd HH:mm:ss"/>
                        </td>
                        <td>
                            ${resource.username}
                        </td>
                         <td>
                            <c:if test="${resource.ispush == 0}">未推送</c:if>
                            <c:if test="${resource.ispush == 1}"><span style="color:green"><a href="/manage/task/flow?taskId=${resource.taskId}" target="_blank">已推送</a></span></c:if>
                             <c:if test="${resource.ispush == 4}"><span style="color:orange">编译中<a href="/manage/task/flow?taskId=${resource.taskId}" target="_blank">[查看任务]</a></span></c:if>
                        </td>
                        <td>
                            <button type="button" class="btn btn-info" data-target="#addResourceModal" data-toggle="modal"
                                    data-resource_id="${resource.id}" >
                                    修改
                            </button>
                            <button class="btn btn-success" onclick="compile('${resource.id}','${repository.id}','${resource.ispush}')" data-toggle="modal">推送</button>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </div>
</div>

<%@ include file="addTool.jsp" %>




