<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<script type="text/javascript">

    function updateVersion(module_id,versionId){

        $.post(
            '/manage/app/resource/addVersion.json',
            {
                moduleId: module_id,
                versionId: versionId,//插件版本
                tag: $('#tag'+versionId).val(),
                version_id: $("#versionId"+versionId+" option:selected").attr("versionid"),//redis版本
                status: $("input[name=status"+versionId+"]:checked").val(),
                so_path : $('#so_path'+versionId).val()
            },
            function (data) {
                var status = data.status;
                if (status == 1) {
                    alert("修改成功");
                    window.location.reload();
                } else {
                    $("#tips").html(data.message);
                }

            }
        );
    }

</script>


<div id="updateVersionModal${version.id}" class="modal fade" tabindex="-1" data-width="400">
    <div class="modal-dialog">
        <div class="modal-content">

            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true"></button>
                <h4 id="version-title">${version.tag}版本修改</h4>
            </div>

            <form class="form-horizontal form-bordered form-row-stripped">
                <div class="modal-body">
                    <div class="row">
                        <!-- 控件开始 -->
                        <div class="col-md-12">
                            <!-- form-body开始 -->
                            <div class="form-body">
                                <label id="moduleId" style="display:none"></label>
                                <div class="form-group">
                                    <label class="control-label col-md-3">
                                        模块名:
                                    </label>
                                    <div class="col-md-6">
                                        <input type="text" value="${moduleInfo.name}"  class="form-control"  readonly="readonly" />
                                    </div>
                                </div>

                                <div class="form-group">
                                    <label class="control-label col-md-3">
                                        tag版本:
                                    </label>
                                    <div class="col-md-6">
                                        <input type="text" name="tag" id="tag${version.id}" class="form-control"  value="${version.tag}" />
                                    </div>
                                </div>

                                <div class="form-group">
                                    <label class="control-label col-md-3">
                                        关联Redis版本:
                                    </label>
                                    <div class="col-md-6">
                                        <select name="type" id="versionId${version.id}" class="form-control select2_category">
                                            <c:forEach items="${versionList}" var="ver">
                                                    <option <c:if test="${ver.id == version.versionId}">selected</c:if> versionid="${ver.id}">${ver.name} </option>
                                            </c:forEach>
                                        </select>
                                    </div>
                                </div>

                                <div class="form-group">
                                    <label class="control-label col-md-3">
                                        so路径:
                                    </label>
                                    <div class="col-md-8">
                                        <input type="text" name="so_path" id="so_path${version.id}" class="form-control" value="${version.soPath}"  />
                                    </div>
                                </div>

                                <div class="form-group">
                                    <label class="control-label col-md-3">
                                        是否可用:
                                    </label>
                                    <div class="col-md-6">
                                        <label class="radio-inline">
                                            <input type="radio" name="status${version.id}" value="0" <c:if test="${version.status==0}">checked</c:if>> 不可用
                                        </label>
                                        <label class="radio-inline">
                                            <input type="radio" name="status${version.id}" value="1" <c:if test="${version.status==1}">checked</c:if>> 可用
                                        </label>
                                    </div>
                                </div>

                            </div>
                        </div>
                        <!-- 控件结束 -->
                    </div>
                </div>

                <div class="modal-footer">
                    <button type="button" data-dismiss="modal" class="btn" >Close</button>
                    <button type="button" id="versionBtn" class="btn red" onclick="updateVersion('${moduleInfo.id}','${version.id}')">Ok</button>
                </div>

            </form>
        </div>
    </div>
</div>



