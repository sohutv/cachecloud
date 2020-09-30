<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<script type="text/javascript" src="/resources/bootstrap/jquery/jquery-1.11.0.js"></script>
<%@include file="/WEB-INF/jsp/manage/commons/taglibs.jsp" %>
<%@include file="/WEB-INF/jsp/manage/include/cache_cloud_main_js_include.jsp" %>
<%@include file="/WEB-INF/jsp/manage/include/cache_cloud_main_css.jsp" %>

<script>

    function setConfig(){
        $.post(
            '/manage/app/resource/add.json',
            {
                resourceId: $('#resourceId').html(),
                resourceName: $('#resourceName').val(),
                resourceDir: $('#resourceDir').val(),
                resourceUrl: $('#resourceUrl').val(),
                resourceType:   1,
                resourceStatus: 1
            },
            function (data) {
                var status = data.status;
                if (status == 1 || status == 2) {
                    alert("设置成功");
                } else {
                    alert("设置异常！");
                }
                window.location.reload();
            }
        );
    }

    function check(){

        $.post(
            '/manage/app/resource/add.json',
            {
                resourceId: $('#resourceId').html(),
                resourceName: $('#resourceName').val(),
                resourceDir: $('#resourceDir').val(),
                resourceUrl: $('#resourceUrl').val()
            },
            function (data) {
                var status = data.status;
                if (status == 1) {
                    alert("设置成功");
                } else {
                    alert("设置异常！");
                }
                window.location.reload();
            }
        );
    }
</script>

<div class="row">
    <div class="col-md-12">
        <h4 class="glyphicon glyphicon-home" >
            资源仓库管理
        </h4>
    </div>
</div>
<div class="row">
    <div id="respo-div" class="col-md-15">
        <form class="form-inline" role="form" name="ec">
            <label id="resourceId" class="control-label" style="display:none">${repository.id}</label>
            <div class="form-group col-md-2">
                <label id="respo">&nbsp;&nbsp;&nbsp;远程仓库地址：</label>
            </div>
            <div class="col-md-7">
                ip：<input id="resourceName" style="width: 20%" type="text"
                    value="${repository.name}" class="form-control" name="pattern" placeholder="仓库ip">
                根目录：<input id="resourceDir" style="width: 55%" type="text"
                value="${repository.dir}" class="form-control" name="pattern" placeholder="路径">
            </div>

            <div class="form-group col-md-1" style="float:left; width: max-content">
                <%--<button type="button" class="form-control btn yellow" onclick="check()">校验</button>--%>
            </div>
            <div class="form-group col-md-4" style="float:left; width: max-content">
                <button type="button" class="form-control btn green" onclick="setConfig()">设置</button>
            </div>

            <div class="col-md-2">
                <label>&nbsp;&nbsp;&nbsp;资源下载地址：</label>
            </div>
            <div class="col-md-9">
               <input id="resourceUrl" style="width: 70%" type="text"
                    value="${repository.url}" class="form-control" name="pattern" placeholder="域名地址">
            </div>
        </form>
    </div>
</div>

<div class="row">
    <div class="col-md-12">
        <div class="portlet box light-grey" id="clientIndex">

            <table class="table table-striped table-bordered table-hover" id="tableDataList">
                <thead>
                <tr>
                    <th>目录</th>
                    <th>资源名</th>
                    <th>资源说明</th>
                    <th>状态</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach items="${resourceList}" var="resource">
                    <c:if test="${resource.type != 1 && resource.type != 6}">
                        <tr>
                            <td>
                                <a target="_blank" href="${repository.url}${resource.dir}">${resource.dir}</a>
                            </td>
                            <td>
                                <c:if test="${resource.ispush==1 || resource.ispush==3 }"><a target="_blank" href="${repository.url}${resource.dir}/${resource.name}">${resource.name}</a></c:if>
                                <c:if test="${resource.ispush==0 || resource.ispush==2 || resource.ispush==4}">${resource.name}</c:if>
                            </td>
                            <td>
                                ${resource.intro}
                            </td>
                            <td>
                                <c:if test="${resource.ispush==1}"><span style="color:green">已推送</span></c:if>
                                <c:if test="${resource.ispush==3}"><span style="color:green">已推送</span><span style="color:red">(有新修改)</span></c:if>
                                <c:if test="${resource.ispush==2}">未推送<span style="color:red">(有新修改)</span></c:if>
                                <c:if test="${resource.ispush==0}">未推送</c:if>
                                <c:if test="${resource.ispush==4}"><span style="color:orange">编译中...</span></c:if>
                            </td>
                        </tr>
                    </c:if>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </div>
</div>






