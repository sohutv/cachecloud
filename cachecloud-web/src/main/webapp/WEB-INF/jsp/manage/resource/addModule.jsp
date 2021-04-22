    <%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<script type="text/javascript">

    function addResource(){
        $.post(
            '/manage/app/resource/add.json',
            {
                resourceId: $('#resourceId').html(),
                resourceName: $('#resourceName').val(),
                resourceDesc: $('#resourceDesc').val(),
                resourceDir: $('#resourceDir').val(),
                resourceType: $('#resourceType').val(),
                resourceUrl: $('#resourceUrl').val(),
                copyVersion: $('#copyVersion option:selected').attr("versionid"),
                resourceStatus: $('#resourceStatus').val()
            },
            function (data) {
                var status = data.status;
                if (status == 1) {
                    alert("新建成功");
                    window.location.reload();
                } else if(status == 2){
                    alert("修改成功");
                    window.location.reload();
                }else {
                    $("#tips").html(data.message);
                }

            }
        );
    }

    $('#addResourceModal').on('shown.bs.modal', function (e) {

        $("#resourceName").removeAttr("readonly", "readonly");
        $('#config').attr("style","display:none");
        $("#resourceName").val("");
        $('#resourceDesc').val("");
        $("#resourceDir option[value='/redis']").prop("selected", true);
        $('#resourceUrl').val("");
        $('#resourceType').val("8");
        $('#resourceStatus').val("1");
        $('#modal-title').html("新建模块");
        $('#resourceId').html("");

        var resourceId = $(e.relatedTarget).data('resource_id');
        if(typeof resourceId == 'undefined'){
            $('#config').attr("style","display:block");
            return ;
        }
        $('#modal-title').html("修改模块");
        $('#resourceId').html(resourceId);
        $("#resourceName").attr("readonly", "readonly");

        $.post(
            '/manage/app/resource/get.json',
            {
                resourceId: resourceId
            },
            function (data) {
                if(data.status == 1){
                     $('#resourceName').val(data.resource.name);
                     $('#resourceDesc').val(data.resource.intro);
                     $("#resourceDir option[value='"+data.resource.dir+"']").prop("selected", true);
                     $('#resourceUrl').val(data.resource.url);
                     $('#resourceType').val(data.resource.type);
                     $('#resourceStatus').val(data.resource.status);
                }
            }
        );
    });

</script>

<div id="addResourceModal" class="modal fade" tabindex="-1" data-width="400">
    <div class="modal-dialog">
        <div class="modal-content">

            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true"></button>
                <h4 id="modal-title">新建模块</h4>
            </div>

            <form class="form-horizontal form-bordered form-row-stripped">
                <div class="modal-body">
                    <div class="row">
                        <!-- 控件开始 -->
                        <div class="col-md-12">
                            <!-- form-body开始 -->
                            <div class="form-body">
                                <label id="resourceId" style="display:none"></label>
                                <div class="form-group">
                                    <label class="control-label col-md-3">
                                        模块名称:
                                    </label>
                                    <div class="col-md-6">
                                        <input type="text" name="resourceName" id="resourceName"
                                            class="form-control"  onchange="updateUrl()" />
                                    </div>
                                    <div><span id="tips" style="color:red"></span></div>
                                </div>

                                <div class="form-group">
                                    <label class="control-label col-md-3">
                                        描述:
                                    </label>
                                    <div class="col-md-6">
                                        <textarea rows="5"  name="resourceDesc" id="resourceDesc" placeholder="模块说明" class="form-control"></textarea>
                                    </div>
                                </div>

                                <div class="form-group">
                                    <label class="control-label col-md-3">
                                        状态:
                                    </label>
                                    <div class="col-md-6">
                                        <select name="resourceStatus" id="resourceStatus" class="form-control select2_category">
                                            <option value="1">
                                                有效
                                            </option>
                                            <option value="0">
                                                无效
                                            </option>
                                        </select>
                                    </div>
                                </div>

                                <div class="form-group">
                                    <label class="control-label col-md-3">
                                        目录:
                                    </label>
                                    <div class="col-md-6">
                                        <select id="resourceDir" name="resourceDir" class="form-control">
                                            <c:forEach items="${dirList}" var="dir">
                                                <option value="${dir.name}">${dir.name}(${dir.intro})</option>
                                            </c:forEach>
                                        </select>
                                    </div>
                                </div>

                                <div class="form-group">
                                    <label class="control-label col-md-3">
                                        类型:
                                    </label>
                                    <div class="col-md-6">
                                        <select name="resourceType" id="resourceType" class="form-control select2_category">
                                            <option value="2">
                                                脚本
                                            </option>
                                            <option value="3">
                                                Redis资源包
                                            </option>
                                            <option value="6">
                                                目录
                                            </option>
                                            <option value="7">
                                                迁移工具
                                            </option>
                                            <option value="8">
                                                Redis模块
                                            </option>
                                        </select>
                                    </div>
                                </div>

                                <div class="form-group">
                                    <label class="control-label col-md-3">
                                        源地址:
                                    </label>
                                    <div class="col-md-9">
                                        <input type="text" name="resourceUrl" id="resourceUrl" value=""
                                            class="form-control"/>
                                    </div>
                                </div>

                            </div>
                        </div>
                        <div id="info"></div>
                        <!-- 控件结束 -->
                    </div>
                </div>

                <div class="modal-footer">
                    <button type="button" data-dismiss="modal" class="btn" >Close</button>
                    <button type="button" id="versionBtn" class="btn red" onclick="addResource()">Ok</button>
                </div>

            </form>
        </div>
    </div>
</div>



