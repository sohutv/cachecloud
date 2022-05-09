<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<script type="text/javascript">

    function addVersion(module_id){

        $.post(
            '/manage/app/resource/addVersion.json',
            {
                moduleId: module_id,
                tag: $('#tag_'+module_id).val(),
                version_id: $("#versionId_"+module_id+" option:selected").attr("versionid"),
                status: $("input[name=status_"+module_id+"]:checked").val(),
                so_path : $('#so_path_'+module_id).val(),
            },
            function (data) {
                var status = data.status;
                if (status == 1) {
                    alert("创建成功");
                    window.location.reload();
                } else {
                    $("#tips").html(data.message);
                }

            }
        );
    }

</script>


<div id="addVersionModal${module.id}" class="modal fade" tabindex="-1" data-width="400">
    <div class="modal-dialog">
        <div class="modal-content">

            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true"></button>
                <h4 id="version-title">添加版本</h4>
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
                                        <input type="text" name="tag" id="tag_${module.id}" class="form-control"   />
                                    </div>
                                </div>

                                <div class="form-group">
                                    <label class="control-label col-md-3">
                                        关联Redis版本:
                                    </label>
                                    <div class="col-md-6">
                                        <select name="type" id="versionId_${module.id}" class="form-control select2_category">
                                            <c:forEach items="${versionList}" var="ver">
                                                    <option <c:if test="${ver.id == version.version_id}">selected</c:if> versionid="${ver.id}">${ver.name} </option>
                                            </c:forEach>
                                        </select>
                                    </div>
                                </div>

                                <div class="form-group">
                                    <label class="control-label col-md-3">
                                        so路径:
                                    </label>
                                    <div class="col-md-8">
                                        <input type="text" name="so_path" id="so_path_${module.id}" class="form-control"   />
                                    </div>
                                </div>

                                <div class="form-group">
                                    <label class="control-label col-md-3">
                                        是否可用:
                                    </label>
                                    <div class="col-md-6">
                                        <label class="radio-inline">
                                            <input type="radio" name="status_${module.id}" value="0" checked> 不可用
                                        </label>
                                        <label class="radio-inline">
                                            <input type="radio" name="status_${module.id}" value="1"> 可用
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
                    <button type="button" id="versionBtn" class="btn red" onclick="addVersion(${moduleInfo.id})">Ok</button>
                </div>

            </form>
        </div>
    </div>
</div>



