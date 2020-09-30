<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp"%>
<!-- 一键生成实例配置 -->
<div id="assignRedisModal" class="modal fade" tabindex="-1" data-width="400">
    <div class="modal-dialog" style="width:1200px;">
        <div class="modal-content" >
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true"></button>
                <h4 class="modal-title">实例自动分配</h4>
            </div>

            <div class="form">
                <!-- BEGIN FORM-->
                <form id="getDeployInfo" action="/manage/app/getDeployInfo" method="post"
                      class="form-horizontal form-bordered form-row-stripped">
                    <div class="form-body">
                        <div class="form-group">
                            <label class="control-label col-md-2">
                                应用ID：
                            </label>
                            <div class="col-md-2">
                                <input type="text" name="appid" id="id" class="form-control" readonly="readonly" value="${appDesc.appId}"/>
                            </div>

                            <label class="control-label col-md-2">
                                应用名称：
                            </label>
                            <div class="col-md-2">
                                <input type="text" name="name" id="name" class="form-control" readonly="readonly" value="${appDesc.name}"/>
                            </div>
                            <label class="control-label col-md-2">
                                存储种类<font color='red'>*</font>：
                            </label>
                            <div class="col-md-2">
                                <select id="appType" name="type" class="form-control select2_category">
                                    <option value="2" <c:if test="${appDesc.type == 2}">selected</c:if>>
                                        Redis-cluster
                                    </option>
                                    <option value="5" <c:if test="${appDesc.type == 5}">selected</c:if>>
                                        Redis-sentinel
                                    </option>
                                    <option value="6" <c:if test="${appDesc.type == 6}">selected</c:if>>
                                        Redis-standalone
                                    </option>
                                </select>
                            </div>
                        </div>

                        <div class="form-group">
                            <label class="control-label col-md-2">
                                部署类型<font color='red'>*</font>：
                            </label>
                            <div class="col-md-2">
                                <select id="useType" name="useType" class="form-control">
                                    <option value="0">
                                        专用部署
                                    </option>
                                    <option value="1" <c:if test="${appDesc.isTest == 1}">selected="selected"</c:if>>
                                        测试部署
                                    </option>
                                    <option value="2" <c:if test="${appDesc.isTest == 0}">selected="selected"</c:if>>
                                        混合部署
                                    </option>
                                </select>
                            </div>

                            <label class="control-label col-md-2">
                                客户端机房：
                            </label>
                            <div class="col-md-2">
                                <select name="room" id="room" class="form-control">
                                    <option value="" >
                                        不作限制
                                    </option>
                                    <c:forEach items="${roomList}" var="room">
                                        <option value="${room.name}" <c:if test="${appDesc.clientMachineRoom == room.name}">selected="selected"</c:if>>${room.name} (${room.ipNetwork})</option>
                                    </c:forEach>
                                </select>
                            </div>

                            <label class="control-label col-md-2">
                                是否有从节点<font color='red'>*</font>：
                            </label>
                            <div class="col-md-2">
                                <select id="isSalve" name="isSalve" class="form-control">
                                    <option value="1" <c:if test="${appDesc.isTest == 0}">selected="selected"</c:if>>
                                        有
                                    </option>
                                    <option value="0" <c:if test="${appDesc.isTest == 1}">selected="selected"</c:if>>
                                        无
                                    </option>
                                </select>
                            </div>
                        </div>

                        <c:set var="memSize" value="${fn:replace(appAudit.param1, 'G', '')}"/>

                        <div class="form-group">
                            <label class="control-label col-md-2">
                                内存总量(G)<font color='red'>*</font>：
                            </label>
                            <div class="col-md-2">
                                <input type="text" name="size" id="size" placeholder="内存总量，单G" class="form-control" value="${memSize}"/>
                                <p id="notesize" style="color: red; display: none">
                                    <i class="ace-icon fa fa-exclamation-triangle bigger-120"></i>请填写内存总量
                                </p>
                            </div>

                            <%--redis standalone--%>
                            <c:if test="${appDesc.type == 6}">
                                <c:set var="masterNumber" value="1"/>
                                <c:choose>
                                    <c:when test="${appDesc.isTest == 0}">
                                        <c:set var="machineNumber" value="2"/>
                                    </c:when>
                                    <c:otherwise>
                                        <c:set var="machineNumber" value="1"/>
                                    </c:otherwise>
                                </c:choose>
                            </c:if>
                            <%--redis sentinel--%>
                            <c:if test="${appDesc.type == 5}">
                                <c:set var="masterNumber" value="1"/>
                                <c:set var="machineNumber" value="4"/>
                            </c:if>
                            <%--redis cluster--%>
                            <c:if test="${appDesc.type == 2}">
                                <fmt:formatNumber var="val" type="number" value="${memSize}" maxFractionDigits="2"/>
                                <c:choose>
                                    <c:when test="${val<6.00}">
                                        <c:set var="machineNumber" value="3"/>
                                        <c:set var="masterNumber" value="4"/>
                                    </c:when>
                                    <c:otherwise>
                                        <fmt:formatNumber var="val1" type="number" value="${val/2}" maxFractionDigits="0"/>
                                        <fmt:formatNumber var="val2" type="number" value="${memSize}" maxFractionDigits="0"/>
                                        <c:set var="machineNumber" value="${val1}"/>
                                        <c:set var="masterNumber" value="${val2}"/>
                                    </c:otherwise>
                                </c:choose>
                            </c:if>

                            <label class="control-label col-md-2">
                                分配机器数<font color='red'>*</font>：
                            </label>
                            <div class="col-md-2">
                                <input type="text" name="machineNum" id="machineNum" placeholder="分配机器数" class="form-control" value="${machineNumber}"/>
                                <p id="noteMachineNum" style="color: red; display: none">
                                    <i class="ace-icon fa fa-exclamation-triangle bigger-120"></i>
                                    请填写机器数
                                </p>
                            </div>

                            <label class="control-label col-md-2">
                                主节点数<font color='red'>*</font>：
                            </label>
                            <div class="col-md-2">
                                <input type="text" name="instanceNum" id="instanceNum" placeholder="分配实例数" class="form-control" value="${masterNumber}"/>
                                <p id="notenum" style="color: red; display: none">
                                    <i class="ace-icon fa fa-exclamation-triangle bigger-120"></i>
                                    请填写实例数
                                </p>
                            </div>
                        </div>


                        <%--specialMachines--%>
                        <div id="specialMachines" class="form-group" style="display:none;">
                            <label class="control-label col-md-2">
                                专用部署机器：

                            </label>
                            <div class="col-md-8">
                                <label for="machines"></label>
                                <select id="machines" class="selectpicker bla bla bli" multiple data-live-search="true">
                                    <c:forEach items="${machineList}" var="machine">
                                        <fmt:formatNumber var="usedCpu" value="${machineInstanceCountMap[machine.info.ip]}" pattern="0"/>
                                        <fmt:formatNumber var="cpu" value="${machine.info.cpu}" pattern="0"/>
                                        <fmt:formatNumber var="cpuUsage" value="${usedCpu/cpu*100}" pattern="0"/>
                                        <fmt:formatNumber var="usedMemRss" value="${((machine.machineMemInfo.usedMemRss)/1024/1024/1024)}" pattern="0.0"/>
                                        <fmt:formatNumber var="mem" value="${ machine.info.mem}" pattern="0.0"/>
                                        <fmt:formatNumber var="memUsage" value="${usedMemRss/mem*100}" pattern="0"/>
                                        <c:if test="${machine.info.useType==0}">
                                            <option value="${machine.ip}">${machine.ip}：${usedCpu}/${cpu}核(${cpuUsage}%) ${usedMemRss}/${mem}G(${memUsage}%) 【${machine.info.realIp}-${machine.info.rack}】【专用-${machine.info.extraDesc}】</option>
                                        </c:if>
                                        <c:if test="${machine.info.useType==1}">
                                            <option value="${machine.ip}">${machine.ip}：${usedCpu}/${cpu}核(${cpuUsage}%) ${usedMemRss}/${mem}G(${memUsage}%) 【${machine.info.realIp}-${machine.info.rack}】【测试-${machine.info.extraDesc}】</option>
                                        </c:if>
                                        <c:if test="${machine.info.useType==2}">
                                            <option value="${machine.ip}">${machine.ip}：${usedCpu}/${cpu}核(${cpuUsage}%) ${usedMemRss}/${mem}G(${memUsage}%) 【${machine.info.realIp}-${machine.info.rack}】【混合-${machine.info.extraDesc}】</option>
                                        </c:if>
                                    </c:forEach>
                                    </optgroup>
                                </select>
                                <p id="notemachines" style="color: red; display: none">
                                    <i class="ace-icon fa fa-exclamation-triangle bigger-120"></i>
                                    请选择专用机器
                                </p>
                            </div>
                        </div>
                        <%--sentinelMachines--%>
                        <div id="sentinelMachines" class="form-group" <c:if test="${appDesc.type != 5}">style="display:none;"</c:if>>
                            <label class="control-label col-md-2">
                                sentinel机器<font color='red'>(*至少为3个，且为奇数个)</font>：
                            </label>
                            <div class="col-md-8">
                                <label for="sentinelMachineList"></label>
                                <select id="sentinelMachineList" class="selectpicker bla bla bli" multiple data-live-search="true">
                                    <c:forEach items="${machineList}" var="machine">
                                        <c:if test="${machine.info.useType==3}">
                                            <option value="${machine.ip}">${machine.ip}【Redis-sentinel机器】</option>
                                        </c:if>
                                    </c:forEach>
                                    </optgroup>
                                </select>
                                <p id="noteSentinelMachines" style="color: red; display: none">
                                    <i class="ace-icon fa fa-exclamation-triangle bigger-120"></i>
                                    sentinel机器数至少为3个，且为奇数个
                                </p>
                            </div>
                        </div>




                        <div class="form-group">
                            <label class="control-label col-md-2">
                                所选机器信息
                            </label>
                            <div class="col-md-10">
                                <table class="table table-striped table-bordered table-hover" id="tableList">
                                    <thead>
                                    <tr>
                                        <th>ip</th>
                                        <th>总内存</th>
                                        <th>剩余内存</th>
                                        <th>实例数/核数</th>
                                        <th>master数/salve数/sentinel数</th>
                                        <th>宿主机/机架信息</th>
                                        <th>详情</th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    </tbody>
                                </table>
                                <c:if test="${appDesc.isTest == 1}">
                                    <div><font style="color: green">测试应用对剩余内存和剩余核数不作要求</font></div>
                                </c:if>
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="control-label col-md-2">
                                <br/><br/><br/>部署信息预览:<font color='red'>(*)</font>:
                            </label>
                            <div class="col-md-5">
                                <textarea rows="10" name="appDeployInfo" id="appDeployInfo" placeholder="部署详情" class="form-control"></textarea>
                            </div>
                            <label id="startDeployInfoLabel" style="color: green"></label>
                        </div>

                        <div class="modal-footer">
                            <button type="button" class="btn btn-primary" data-toggle="modal" onclick="getDeployInfo()">
                                <span id="deployPreview">实例部署预览</span>
                            </button>
                            <button type="button" class="btn btn-primary" data-toggle="modal" onclick="generatePreview()">
                                <span id="deploy">确定部署信息</span>
                            </button>
                        </div>

                    </div>
                </form>
                <!-- END FORM-->
            </div>
        </div>
    </div>
</div>

<script type="text/javascript">
    $(function(){
        $("select[name='useType']").change(function(){
            if($(this).val()==0)
            {
                $("#specialMachines").show();
            }else{
                $("#specialMachines").hide();
            }
        });

        $('#appType').change(function () {
            if($(this).val()==5)
            {
                $("#sentinelMachines").show();
            }else{
                $("#sentinelMachines").hide();
            }
        });
    });
</script>