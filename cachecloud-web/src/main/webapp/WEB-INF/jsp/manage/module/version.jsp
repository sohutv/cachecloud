<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<script type="text/javascript" src="/resources/bootstrap/jquery/jquery-1.11.0.js"></script>
<%@include file="/WEB-INF/jsp/manage/commons/taglibs.jsp" %>
<%@include file="/WEB-INF/jsp/manage/include/cache_cloud_main_js_include.jsp" %>
<%@include file="/WEB-INF/jsp/manage/include/cache_cloud_main_css.jsp" %>


<script type="text/javascript">

    // 编译&推送
    function compile(resourceId,repositoryId){

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

</script>

<div class="row">
    <div class="col-md-12">
        <h4 class="glyphicon glyphicon-folder-open" >
            版本管理
        </h4>
        <div class="form-group col-md-1" style="float:right; width: max-content">
            <button id="version" class="btn green btn-sm" style="float: right;" data-target="#addVersionModal${moduleInfo.id}" data-toggle="modal" >
                 <i class="fa fa-plus"></i>新建版本
            </button>
        </div>
    </div>
</div>

<div class="row">
    <div class="col-md-12">
        <div style="float:left">
            <form class="form-inline" role="form" method="post" action="/manage/app/resource/index?tab=redis"
                  id="appList" name="ec">
                <%--<div class="form-group">--%>
                    <%--<input type="text" class="form-control" id="searchName" name="searchName" value="${searchName}" placeholder="版本名称">--%>
                <%--</div>--%>
                <%--<button type="submit" class="btn btn-info">查询</button>--%>
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
                    <th>版本id</th>
                    <th>tag信息</th>
                    <th>关联redis版本</th>
                    <th>最后更新时间</th>
                    <th>是否可用</th>
                    <th>so地址</th>
                    <th>操作</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach items="${moduleInfo.versions}" var="version">
                    <tr>
                        <td>${version.id}</td>
                        <td>${version.tag}</td>
                        <td>
                            <c:forEach items="${versionList}" var="ver">
                                <c:if test="${ver.id == version.versionId}">${ver.name}</c:if>
                            </c:forEach>
                        </td>
                        <td><fmt:formatDate value="${version.createTime}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
                        <td>
                            <c:if test="${version.status==0}">
                                <label style="color:red">不可用</label>
                            </c:if>
                            <c:if test="${version.status==1}">
                                <label style="color:green">可用</label>
                            </c:if>
                        </td>
                        <td>${version.soPath}</td>
                        <td>
                            <button type="button" class="btn btn-warning" data-target="#updateVersionModal${version.id}" data-toggle="modal">修改</button>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </div>
</div>

<c:forEach items="${allModules}" var="module">
    <%@include file="addVersion.jsp" %>
</c:forEach>

<c:forEach items="${moduleInfo.versions}" var="version">
    <%@include file="updateVersion.jsp" %>
</c:forEach>




