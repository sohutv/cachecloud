<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@include file="/WEB-INF/jsp/manage/commons/taglibs.jsp" %>
<%@include file="/WEB-INF/jsp/manage/include/cache_cloud_main_js_include.jsp" %>
<%@include file="/WEB-INF/jsp/manage/include/cache_cloud_main_css.jsp" %>

<script type="text/javascript">

    $('#modal-script').on('shown.bs.modal', function (e) {

        $('#editor2').html('');
        var resourceName = $(e.relatedTarget).data('resource_name');
        var resourceId = $(e.relatedTarget).data('resource_id');
        var respoitoryId = $('#repositoryId').html();
        $('#resource_id').html(resourceId);
        $('#resource_name').html(resourceName);

        $.post(
            '/manage/app/resource/script/load',
            {
                resourceId: resourceId,
                respositoryId:respoitoryId
            },
            function (data) {
                if(data.status == 1){
                    $('#editor2').html(data.content);
                    if(data.source == 1){
                         $('#file_source').html("<span style=color:red>(临时保存)</span>");
                    }else if(data.source == 2){
                         $('#file_source').html("<span style=color:green>(最新文件)</span>");
                    }
                }
            }
        );
    });

    function pushResource(resourceId){

        var resource_id = (typeof resourceId == 'undefined') ? $('#resource_id').html() : resourceId;
        $.post(
            '/manage/app/resource/push.json',
            {
                repositoryId: $('#repositoryId').html(),
                resourceId: resource_id,
                content: $('#editor2').val()
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

    function temporarySave(){
        $.post(
            '/manage/app/resource/temporarySave.json',
            {
                resourceId: $('#resource_id').html(),
                content: $('#editor2').val()
            },
            function (data) {
                var status = data.status;
                if (status == 1) {
                    alert("保存成功");
                } else {
                    alert("保存失败！" + data.message);
                }
                window.location.reload();
            }
        );
    }

</script>



<div class="row">
    <div class="col-md-12">
        <h4 class="glyphicon glyphicon-file" >
            脚本管理
        </h4>
        <div class="form-group col-md-1" style="float:right; width: max-content">
            <button id="redis_version" class="btn green btn-sm" style="float: right;" data-target="#addResourceModal" data-toggle="modal" >
                 <i class="fa fa-plus"></i>新建脚本
            </button>
        </div>
    </div>
</div>



<div class="row">
    <div class="col-md-12">
        <div style="float:left">
            <form class="form-inline" role="form" method="post" action="/manage/app/resource/index?tab=script"
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
                    <th>脚本名称</th>
                    <th>脚本说明</th>
                    <th>目录</th>
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
                             <a target="_blank" href="${repository.url}${resource.dir}/${resource.name}">${resource.name}</a>
                        </td>
                        <td>
                            ${resource.intro}
                        </td>
                        <td>
                            ${resource.dir}
                        </td>
                        <td>
                            <fmt:formatDate value="${resource.lastmodify}" pattern="yyyy-MM-dd HH:mm:ss"/>
                        </td>
                        <td>
                            ${resource.username}
                        </td>
                        <td>
                                <c:if test="${resource.ispush==1}"><span style="color:green">已推送</span></c:if>
                                <c:if test="${resource.ispush==3}"><span style="color:green">已推送</span><span style="color:red">(有新修改)</span></c:if>
                                <c:if test="${resource.ispush==2}">未推送<span style="color:red">(有新修改)</span></c:if>
                                <c:if test="${resource.ispush==0}">未推送</c:if>
                        </td>
                        <td>
                            <button type="button" class="btn btn-info" data-target="#addResourceModal" data-toggle="modal"
                            data-resource_id="${resource.id}" >
                                修改
                            </button>
                            <button type="button" class="btn btn-info" data-target="#modal-script" data-toggle="modal"
                                    data-resource_id="${resource.id}" data-resource_name="${resource.name}" >
                                    编辑内容
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


<div id="modal-script" class="modal fade" tabindex="-1">
    <div class="modal-dialog" style="width: max-content">
        <div class="modal-content">

            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true"></button>
                <h4 class="modal-title">
                    <label id="resource_name"></label>脚本内容:
                    <label id="file_source"></label>
                    <small><label id="modal-title" style="color: #00BE67"></label></small>
                </h4>

            </div>

            <form class="form-horizontal form-bordered form-row-stripped">
                <div class="modal-body" style="width:800px;height:400px; overflow:scroll;">
                    <label id="resource_id" style="display:none"></label>
                    <!-- 控件开始 -->
                    <textarea rows="20"  name="editor2" id="editor2" placeholder="" class="form-control"></textarea>
                </div>

                <div class="modal-footer">
                    <button class="btn btn-info" onclick="pushResource()" data-toggle="modal">推送</button>
                    <button class="btn btn-info" onclick="temporarySave()" data-toggle="modal">临时保存</button>
                    <button type="button" data-dismiss="modal" class="btn">关闭</button>
                </div>
            </form>
        </div>
    </div>
</div>


<%@ include file="addScript.jsp" %>




