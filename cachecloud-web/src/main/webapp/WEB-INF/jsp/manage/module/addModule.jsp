<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<script type="text/javascript">

    function addModule(){
        $.post(
            '/manage/app/resource/addModule.json',
            {
                moduleName: $('#moduleName').val(),
                moduleInfo: $('#moduleInfo').val(),
                giturl: $('#giturl').val()
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

<div id="addModuleModal" class="modal fade" tabindex="-1" data-width="400">
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
                                        <input type="text" name="moduleName" id="moduleName"
                                            class="form-control" />
                                    </div>
                                    <div><span id="tips" style="color:red"></span></div>
                                </div>

                                <div class="form-group">
                                    <label class="control-label col-md-3">
                                        模块信息:
                                    </label>
                                    <div class="col-md-6">
                                        <textarea rows="5"  name="moduleInfo" id="moduleInfo" placeholder="模块说明" class="form-control"></textarea>
                                    </div>
                                </div>

                                <div class="form-group">
                                    <label class="control-label col-md-3">
                                        Git地址:
                                    </label>
                                    <div class="col-md-9">
                                        <input type="text" name="giturl" id="giturl" value=""
                                            class="form-control"/>
                                    </div>
                                </div>

                            </div>
                            <div id="info"></div>
                            <!-- 控件结束 -->
                        </div>
                    </div>
                </div>

                <div class="modal-footer">
                    <button type="button" data-dismiss="modal" class="btn" >Close</button>
                    <button type="button" id="moduleBtn" class="btn red" onclick="addModule()">Ok</button>
                </div>

            </form>
        </div>
    </div>
</div>




