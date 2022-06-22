    <%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp" %>

<script src="/resources/manage/plugins/jquery-1.10.2.min.js"></script>
<script type="text/javascript" src="/resources/select/bootstrap-select.js"></script>
<link rel="stylesheet" type="text/css" href="/resources/select/bootstrap-select.css">
<!-- 3.0 -->
<link href="/resources/manage/plugins/bootstrap/css/bootstrap.min.css" rel="stylesheet">
<script src="/resources/manage/plugins/bootstrap/js/bootstrap.min.js"></script>
<!-- 提示工具-->
<link href="/resources/css/common.css" rel="stylesheet" type="text/css"/>
<link href="/resources/toastr/toastr.min.css" rel="stylesheet" type="text/css">
<script type="text/javascript" src="/resources/toastr/toastr.min.js"></script>

<script type="text/javascript">
    $(window).on('load', function () {
        $('.selectpicker').selectpicker({
        'selectedText': 'cat'
        });
    });
</script>

<div class="page-container">
    <div class="page-content">
        <div class="modal-dialog" style="width:1150px;">
            <div class="modal-content">
                <div class="modal-header">
                    <h4 class="modal-title">应用迁移工具</h4>
                </div>
                <div class="modal-body">
                    <div class="row bs-wizard" style="border-bottom:0;">
                        <div id="appInfo" class="col-xs-1 bs-wizard-step warn">
                          <div class="text-center bs-wizard-stepnum">1.应用信息</div>
                          <div class="progress"><div class="progress-bar"></div></div>
                          <a href="#" class="bs-wizard-dot"></a>
                          <div class="bs-wizard-info text-center">应用及实例配置</div>
                        </div>

                        <div id="migratePlan" class="col-xs-2 bs-wizard-step disabled">
                          <div class="text-center bs-wizard-stepnum">2.应用迁移计划</div>
                          <div class="progress"><div class="progress-bar"></div></div>
                          <a href="#" class="bs-wizard-dot"></a>
                          <div class="bs-wizard-info text-center">迁移实例计划</div>
                        </div>

                        <div id="slaveChange" class="col-xs-2 bs-wizard-step disabled">
                          <div class="text-center bs-wizard-stepnum">3.新老Slave节点替换</div>
                          <div class="progress"><div class="progress-bar"></div></div>
                          <a href="#" class="bs-wizard-dot"></a>
                          <div class="bs-wizard-info text-center">Slave节点上下线</div>
                        </div>

                        <div id="msFailover" class="col-xs-2 bs-wizard-step disabled">
                          <div class="text-center bs-wizard-stepnum">4.主从Failover</div>
                          <div class="progress"><div class="progress-bar"></div></div>
                          <a href="#" class="bs-wizard-dot"></a>
                          <div class="bs-wizard-info text-center">主从节点切换</div>
                        </div>

                        <div id="addNewSlave" class="col-xs-1 bs-wizard-step disabled">
                          <div class="text-center bs-wizard-stepnum">5.添加Slave</div>
                          <div class="progress"><div class="progress-bar"></div></div>
                          <a href="#" class="bs-wizard-dot"></a>
                          <div class="bs-wizard-info text-center">添加新Slave</div>
                        </div>

                        <div id="instanceCheck" class="col-xs-2 bs-wizard-step disabled">
                          <div class="text-center bs-wizard-stepnum">6.新实例状态检测</div>
                          <div class="progress"><div class="progress-bar"></div></div>
                          <a href="#" class="bs-wizard-dot"></a>
                          <div class="bs-wizard-info text-center">连接、异常、状态检测</div>
                        </div>

                        <div id="downOldSlave" class="col-xs-1 bs-wizard-step disabled">
                          <div class="text-center bs-wizard-stepnum">7.下线slave</div>
                          <div class="progress"><div class="progress-bar"></div></div>
                          <a href="#" class="bs-wizard-dot"></a>
                          <div class="bs-wizard-info text-center">下线老Slave</div>
                        </div>

                        <div id="migrateComplete" class="col-xs-1 bs-wizard-step disabled">
                          <div class="text-center bs-wizard-stepnum">8.迁移完成</div>
                          <div class="progress"><div class="progress-bar"></div></div>
                          <a href="#" class="bs-wizard-dot"></a>
                          <div class="bs-wizard-info text-center">迁移完成</div>
                        </div>
                    </div>
                    <form class="form-horizontal form-bordered form-row-stripped" id="ns">
                        <div class="form-body">
                            <div class="form-group">
                                <label class="control-label col-md-2"> 应用ID: </label>
                                <div class="col-md-3">
                                    <label id="appId" name="appId" class="form-control" readonly>${appDetail.appDesc.appId}<c:if test="${appDetail.appDesc.isTest == 1}"><font style="color:green">(测试)</font></c:if></label>
                                </div>
                                <label class="control-label col-md-2"> Redis类型: </label>
                                <div class="col-md-3">
                                    <label id="type" name="type" class="form-control" readonly>
                                        <c:if test="${appDetail.appDesc.type==2}">Cluster集群</c:if>
                                        <c:if test="${appDetail.appDesc.type==5}">Sentinel</c:if>
                                        <c:if test="${appDetail.appDesc.type==6}">Standalone</c:if>
                                    </label>
                                </div>
                            </div>

                            <div class="form-group">
                                <label class="control-label col-md-2"> 应用总内存: </label>
                                <div class="col-md-3">
                                    <label id="appMem" name="appMem" class="form-control" readonly>${appDetail.mem} MB</label>
                                </div>
                                <label class="control-label col-md-2"> 实例内存: </label>
                                <div class="col-md-3">
                                    <label id="instanceMem" name="instanceMem" class="form-control" readonly><fmt:formatNumber value="${appDetail.mem/appDetail.masterNum}" pattern="0"/> MB</label>
                                </div>
                            </div>

                            <div class="form-group">
                                <label class="control-label col-md-2"> 应用机器数: </label>
                                <div class="col-md-3">
                                    <label id="machineNum" name="machineNum" class="form-control" readonly>${appDetail.machineNum} 台</label>
                                </div>
                                <label class="control-label col-md-2"> Redis节点数: </label>
                                <div class="col-md-3">
                                    <label id="num" name="num" class="form-control" readonly>master:${appDetail.masterNum} &nbsp;slave:${appDetail.slaveNum}</label>
                                </div>
                            </div>

                            <div class="form-group" id="instanceSourceDiv">
                                <label class="control-label col-md-2"> <a target="_blank" href="/admin/app/index?appId=${appDetail.appDesc.appId}">源实例信息</a>: </label>
                                <div class="col-md-8">
                                    <textarea id="instanceSourceInfo" type="text" rows="10" class="form-control" readonly>${instanceSourceInfo}</textarea>
                                </div>
                                <label id="instanceSourceLog" class="control-label"></label>
                            </div>

                            <div class="form-group">
                                <label class="control-label col-md-2"> 迁移目标机房: </label>
                                <div class="col-md-3">
                                    <select name="room" id="room" class="form-control">
                                        <option value="" >
                                            不作限制
                                        </option>
                                        <c:forEach items="${roomList}" var="room">
                                            <option value="${room.name}" <c:if test="${appDetail.appDesc.clientMachineRoom == room.name}">selected="selected"</c:if>>${room.name} (${room.ipNetwork})</option>
                                        </c:forEach>
                                    </select>
                                </div>
                                <label class="control-label col-md-2"> 迁移部署类型: </label>
                                <div class="col-md-3">
                                    <select id="deployType" name="deployType" class="form-control select2_category">
                                        <option value="2" <c:if test="${appDetail.appDesc.isTest == 0}">selected="selected"</c:if>>混合部署</option>
                                        <option value="1" <c:if test="${appDetail.appDesc.isTest == 1}">selected="selected"</c:if>>测试机器部署</option>
                                        <option value="0">专用机器部署</option>
                                    </select>
                                </div>
                                <label class="control-label" style="color:red">('自动挑选机器'选填)</label>
                            </div>



                            <div class="form-group" id="migrateMachineDiv">
                                <label class="control-label col-md-2" style="color:red">Redis迁移机器:</label>
                                <div class="col-md-8">
                                    <label for="id_select"></label>
                                    <select id="id_select" class="selectpicker bla bla bli" multiple data-live-search="true">
                                        <c:forEach items="${machinelist}" var="machine">
                                                <fmt:formatNumber var="usedCpu" value="${machineInstanceCountMap[machine.info.ip]}" pattern="0"/>
                                                <fmt:formatNumber var="cpu" value="${machine.info.cpu}" pattern="0"/>
                                                <fmt:formatNumber var="cpuUsage" value="${usedCpu/cpu*100}" pattern="0"/>
                                                <fmt:formatNumber var="usedMemRss" value="${((machine.machineMemInfo.usedMemRss)/1024/1024/1024)}" pattern="0.0"/>
                                                <fmt:formatNumber var="mem" value="${ machine.info.mem}" pattern="0.0"/>
                                                <fmt:formatNumber var="memUsage" value="${usedMemRss/mem*100}" pattern="0"/>
                                                <c:if test="${machine.info.useType==0}">
                                                    <option value="${machine.ip}">${machine.ip}：${usedCpu}/${cpu}核(${cpuUsage}%) ${usedMemRss}/${mem}G(${memUsage}%) 【${machine.info.realIp}<c:if test="${machine.info.rack !=null && machine.info.rack != ''}">-${machine.info.rack}</c:if>】【专用-${machine.info.extraDesc}】</option>
                                                </c:if>
                                                <c:if test="${machine.info.useType==1}">
                                                    <option value="${machine.ip}">${machine.ip}：${usedCpu}/${cpu}核(${cpuUsage}%) ${usedMemRss}/${mem}G(${memUsage}%) 【${machine.info.realIp}<c:if test="${machine.info.rack !=null && machine.info.rack != ''}">-${machine.info.rack}</c:if>】【测试-${machine.info.extraDesc}】</option>
                                                </c:if>
                                                <c:if test="${machine.info.useType==2}">
                                                    <option value="${machine.ip}">${machine.ip}：${usedCpu}/${cpu}核(${cpuUsage}%) ${usedMemRss}/${mem}G(${memUsage}%) 【${machine.info.realIp}<c:if test="${machine.info.rack !=null && machine.info.rack != ''}">-${machine.info.rack}</c:if>】【混合-${machine.info.extraDesc}】</option>
                                                </c:if>
                                        </c:forEach>
                                    </select>
                                </div>
                                <div class="col-md-2">
                                    <button type="button" id="chooseRedis" class="btn green btn-sm" style="float: left;background-color:rgb(53, 170, 71);" onclick="autoSelectMachine(${appDetail.appDesc.type},${appDetail.machineNum},${appDetail.mem},${appDetail.masterNum},${appDetail.slaveNum})" >
                                     	自动挑选机器
                                    </button>
                                </div>
                            </div>

                            <div class="form-group" id="redisMachineInfoDiv" style="display:none">
                                <label class="control-label col-md-2">
                                    <%--所选Redis机器信息:--%>
                                </label>
                                <div class="col-md-8">
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
                                    <c:if test="${appDetail.appDesc.isTest == 1}">
                                        <div><font style="color: green">测试应用对剩余内存和剩余核数不作要求</font></div>
                                    </c:if>
                                </div>
                            </div>

                            <c:if test="${appDetail.appDesc.type==5}">
                                 <div class="form-group" id="migrateMachineDiv">
                                    <label class="control-label col-md-2" style="color:red">Sentinel迁移机器:</label>
                                    <div class="col-md-8">
                                        <label for="id_select2"></label>
                                        <select id="id_select2" class="selectpicker bla bla bli" multiple data-live-search="true">
                                            <c:forEach items="${machinelist}" var="machine">
                                                <%--<c:if test="${machine.useType=='3'}">--%>
                                                <option value="${machine.ip}">${machine.ip}：${usedCpu}/${cpu}核(${cpuUsage}%) 【${machine.info.realIp}<c:if test="${machine.info.rack !=null && machine.info.rack != ''}">-${machine.info.rack}</c:if>】<c:if test="${machine.info.extraDesc != null && machine.info.extraDesc.length()>0}">(${machine.info.extraDesc})</c:if></option>
                                                <%--</c:if>--%>
                                            </c:forEach>
                                        </select>
                                    </div>
                                    <div class="col-md-2">
                                        <button type="button" id="chooseSentinel" class="btn green btn-sm" style="float: left;background-color:rgb(53, 170, 71);" onclick="selectSentinelMachine(${appDetail.appDesc.appId})" >
                                             	自动挑选机器
                                        </button>
                                    </div>
                                </div>

                                <div class="form-group" id="sentinelMachineInfoDiv" style="display:none">
                                    <label class="control-label col-md-2">
                                        <%--所选Sentinel机器信息:--%>
                                    </label>
                                    <div class="col-md-8">
                                        <table class="table table-striped table-bordered table-hover" id="sentinelTableList">
                                            <thead>
                                            <tr>
                                                <th>ip</th>
                                                <th>机房</th>
                                                <th>总内存</th>
                                                <th>分配内存</th>
                                                <th>使用内存</th>
                                                <th>剩余内存</th>
                                                <th>实例数/核数</th>
                                                <th>详情</th>
                                            </tr>
                                            </thead>
                                            <tbody>
                                            </tbody>
                                        </table>
                                    </div>
                                </div>
                            </c:if>

                             <div class="form-group" id="nodeChangeDiv" hidden="hidden">
                                <label class="control-label col-md-2" style="color:red"> 节点变更信息:</label>
                                <div class="col-md-8">
                                    <textarea id="nodeChangeInfo" type="text" rows="<c:if test="${appDetail.appDesc.type == 5}">${appDetail.masterNum+appDetail.slaveNum+5}</c:if><c:if test="${appDetail.appDesc.type != 5}">${appDetail.masterNum+appDetail.slaveNum}</c:if>" class="form-control" readonly></textarea>
                                </div>
                            </div>



                            <div class="form-group" id="instanceTargetDiv" hidden="hidden">
                                <label class="control-label col-md-2"> <a target="_blank" href="/admin/app/index?appId=${appDetail.appDesc.appId}" style="color:red">最新实例信息:</a></label>
                                <div class="col-md-8">
                                    <textarea id="instanceTargetInfo" type="text" rows="<c:if test="${appDetail.appDesc.type == 5}">${appDetail.masterNum+appDetail.slaveNum+5}</c:if><c:if test="${appDetail.appDesc.type != 5}">${appDetail.masterNum*2}</c:if>" class="form-control" style="background-color:#BC8F8F" readonly></textarea>
                                </div>
                                <label id="instanceTargetLog" class="control-label"></label>
                            </div>

                            <div class="form-group">
                                <label class="control-label col-md-3"> 提示: </label>
                                <div class="col-md-8">
                                    <div class="form-control-static">如果在迁移过程中遇到错误警告，请登录服务器查看日志解决后再<b style="color:cornflowerblue	">继续</b>执行</div>
                                </div>
                            </div>
                        </div>
                    </form>
                </div>

                <div class="modal-footer">
                     <button type="button" id="skipbutton" class="btn" data-toggle="modal" onclick="" style="display:none;"><span id="skip">跳过</span></button>
                     <button type="button" class="btn btn-primary" data-toggle="modal" onclick="migrate(${appDetail.appDesc.appId})"><span id="install">开始迁移</span></button>
                </div>

                <input type="hidden" id="downInstanceIds" name="downInstanceIds" value="${downInstanceIds}">
                <input type="hidden" id="downSentinelIds" name="downSentinelIds" value="${downSentinelIds}">
                <input type="hidden" id="appType" name="appType" value="${appDetail.appDesc.type}">

            </div>
        </div>
    </div>
</div>



<script>
var migrateId = "";
// step forward method
var method = "";

var machineInfo = "";
var machineSentinelInfo = "";



/**
 * 自动挑选机器
 */
function autoSelectMachine(type,machineNum,mem,masterNum,slaveNum) {
    $('#tableList tbody tr').empty();

    $.post('/manage/app/migrate/selectMachine',
        {
            type: type,
            useType: $('#deployType').val(),
            room: $('#room').val(),
            machineNum: machineNum,
            mem: mem,
            masterNum: masterNum,
            slaveNum: slaveNum
        },
        function (data) {
            if(data.status == 1){
                var selectedMachineList=data.resMachineList;
                var selectedMachineArray=new Array();
                selectedMachineList.forEach(function (machine) {
                    selectedMachineArray.push(machine.ip);
                    $("#tableList tbody").prepend(
                        '<tr class="odd gradeX">\n' +
                        "   <td> <a target='_blank' href='/manage/machine/machineInstances?ip="+machine.ip+" '>"+machine.ip+"</a>\n" +
                        '   <td>'+machine.mem+'G</td>\n' +
                        '   <td ><font color="#FF0000">'+(machine.mem-(machine.applyMem/1024/1024/1024)).toFixed(2)+'G </font> </td>\n' +
                        '   <td>'+machine.instanceNum+"&nbsp;&nbsp;/&nbsp;&nbsp;"+machine.cpu+'</td>\n' +
                        '   <td>'+machineDeployStat.masterNum+"&nbsp;&nbsp;/&nbsp;&nbsp;"+machineDeployStat.slaveNum+"&nbsp;&nbsp;/&nbsp;&nbsp;"+machineDeployStat.sentinelNum+'</td>\n' +
                        '   <td>'+machine.realIp+"&nbsp;&nbsp;/&nbsp;&nbsp;"+machine.rack+'</td>\n' +
                        "   <td> <a target='_blank' href='/manage/machine/index?tabTag=machine&ipLike="+machine.ip+" '>查看</a>\n" +
                        '</tr>'
                    )
                    $("#redisMachineInfoDiv").removeAttr("style");
                });
                $('#id_select').selectpicker('val',selectedMachineArray);

            } else if(data.status == -1){
                toastr.error("异常信息："+data.message);
                warn(migratePlanId);
                goOn("migratePlan");
            } else {
                toastr.error("操作失败,请查看日志!");
                warn(migratePlanId);
                goOn("migratePlan");
            }
        }
    );
}

function selectSentinelMachine(appId) {

    $('#sentinelTableList tbody tr').empty();

    $.post('/manage/app/migrate/selectSentinelMachine',
        {
            appId: appId
        },
        function (data) {
            if(data.status == 1){
                var selectedMachineList=data.sentinelMachineList;
                var selectedMachineArray=new Array();
                selectedMachineList.forEach(function (machine) {
                    selectedMachineArray.push(machine.ip);
                    $("#sentinelTableList tbody").prepend(
                        '<tr class="odd gradeX">\n' +
                        "   <td> <a target='_blank' href='/manage/machine/machineInstances?ip="+machine.ip+" '>"+machine.ip+"</a>\n" +
                        '   <td>'+machine.room+'G</td>\n' +
                        '   <td>'+machine.mem+'</td>\n' +
                        '   <td>'+(machine.applyMem/1024/1024/1024).toFixed(2)+'G</td>\n' +
                        '   <td>'+(machine.usedMem/1024/1024/1024).toFixed(2)+'G</td>\n' +
                        '   <td ><font color="#FF0000">'+(machine.mem-(machine.applyMem/1024/1024/1024)).toFixed(2)+'G </font> </td>\n' +
                        '   <td>'+machine.instanceNum+"&nbsp;&nbsp;/&nbsp;&nbsp;"+machine.cpu+'</td>\n' +
                        "   <td> <a target='_blank' href='/manage/machine/index?tabTag=machine&ipLike="+machine.ip+" '>查看</a>\n" +
                        '</tr>'
                    )
                    $("#sentinelMachineInfoDiv").removeAttr("style");
                });
                $('#id_select2').selectpicker('val',selectedMachineArray);

            } else if(data.status == -1){
                toastr.error("异常信息："+data.message);
                warn(migratePlanId);
                goOn("migratePlan");
            } else {
                toastr.error("操作失败,请查看日志!");
                warn(migratePlanId);
                goOn("migratePlan");
            }
        }
    );

}

 /**
 * 一键迁移
 */
function migrate(appId){
    migrateId = appId;

    // 迁移机器验证
    if(machineInfo == ''){
        $("#id_select option:selected").each(function(){
            if(this.value != '' ){
            machineInfo += this.value +";";
            }
        });
    }
    if(machineSentinelInfo == ''){
        $("#id_select2 option:selected").each(function(){
            if(this.value != ''){
                machineSentinelInfo += this.value +";";
            }
        });
    }

    // 迁移redis机器验证
    if($("#appType").attr("value") == '5'){
        if(machineInfo==''){
            toastr.error("未添加机器，请选择Redis迁移机器");
            return ;
        }
    }
    // 迁移sentinel机器验证
    if($("#appType").attr("value") == '5'){
        if(machineSentinelInfo==''){
        toastr.error("未添加机器，请选择Sentinel迁移机器");
        return ;
        }
    }

    complete("appInfo");

	if($('#install').html() == "继续"){
        // step forward
		setTimeout(method+'()', 500);
	}else if($('#install').html() == "迁移完成"){
        // step complete
        window.location.reload();
    }else{
        migratePlan();
    }
}
/**
 * 继续 step forward
 */
function goOn(m){
	$("#install").html("继续");
	method = m;
}

/**
*  跳过步骤
*/
function skip(currentMethod,nextMethod){
    active(currentMethod);
    complete(currentMethod);
    goOn(nextMethod);
    $("#skipbutton").attr("style","display:none;");
}

/**
* 2.应用迁移计划
*/
function migratePlan(){
    var migratePlanId = "migratePlan";
    active(migratePlanId);
    $("#install").html("迁移检查中...");


    $.post('/manage/app/migrate/checkPlan',
        {
        "appId":migrateId,
        "machineInfo":machineInfo,
        "machineSentinelInfo":machineSentinelInfo,
        "type":$("#appType").attr("value")
    },
        function(data){
            if(data.status == 1){
                // 选择迁移机器
                $("#id_select").attr("disabled","disabled");
                $("#id_select2").attr("disabled","disabled");
                $("#downSlaveDiv").removeAttr("hidden");
                $("#nodeChangeDiv").removeAttr("hidden");
                $("#downInstanceIds").attr("value",data.downInstanceIds);
                $("#downSentinelIds").attr("value",data.downSentinelIds);
                $("#nodeChangeInfo").html(data.newInstanceInfo+data.downInstanceInfo);
                // 隐藏机器挑选细节
                $("#redisMachineInfoDiv").attr("style","display:none");
                $("#sentinelMachineInfoDiv").attr("style","display:none");
                $("#chooseRedis").attr("style","display:none");
                $("#chooseSentinel").attr("style","display:none");
                complete(migratePlanId);
                goOn("slaveChange");
            } else if(data.status == -1){
                toastr.error("异常信息："+data.message);
                warn(migratePlanId);
                goOn("migratePlan");
            } else {
                toastr.error("操作失败,请查看日志!");
                warn(migratePlanId);
                goOn("migratePlan");
            }
        }
    );
}

/**
* 3.新老Slave节点替换
*/
function slaveChange(){
    var slaveChangeId = "slaveChange";
    active(slaveChangeId);
    $("#install").html("slave节点替换中...");
    $.post('/manage/app/migrate/nodeReplace',
    {
    "appId":migrateId,
    //"machineInfo":$("button[data-id='id_select']").attr("title"),
    //"machineSentinelInfo":$("button[data-id='id_select2']").attr("title"),
    "machineInfo":machineInfo,
    "machineSentinelInfo":machineSentinelInfo,
    "downInstanceIds":$("#downInstanceIds").attr("value"),
    "type":$("#appType").attr("value")
    },
    function(data){
        if(data.status == 1){
            // master-slave 日志
            $("#instanceTargetDiv").removeAttr("hidden");
            $("#instanceTargetInfo").html(data.instanceTargetInfo);
            $("#instanceTargetLog").html(data.instanceTargetLog);
            $("#nodeChangeDiv").attr("hidden","hidden");
            complete(slaveChangeId);
            goOn("msFailover");
        } else if(data.status == -1){
            toastr.error("异常信息："+data.message);
            warn(slaveChangeId);
            goOn("slaveChange");
        } else {
            toastr.error("操作失败,请查看日志!");
            warn(slaveChangeId);
            goOn("slaveChange");
        }
    });
}

/**
* 4.主从Failover
*/
function msFailover(){
    var failoverId = "msFailover";
    active(failoverId);
    $("#install").html("主从切换中...");
    $.post('/manage/app/migrate/msFailover',
    {
        "appId":migrateId,
        "type":$("#appType").attr("value")
    },
    function(data){
        if(data.status == 1){
            // master-slave 日志
            $("#instanceTargetDiv").removeAttr("hidden");
            $("#instanceTargetInfo").html(data.instanceTargetInfo);
            $("#instanceTargetLog").html(data.instanceTargetLog);
            // 老redis实例下线
            $("#downInstanceIds").attr("value",data.downInstanceIds);
            complete(failoverId);
            // 下一步添加新的从节点
            goOn("addNewSlave");
            // 可跳过步骤
            $("#skipbutton").removeAttr("style");
            $("#skipbutton").attr("onclick","skip('addNewSlave','instanceCheck')");
        } else if(data.status == -1){
            toastr.error("异常信息："+data.message);
            warn(failoverId);
            goOn("msFailover");
        } else {
            toastr.error("操作失败,请查看日志!");
            warn(failoverId);
            goOn("msFailover");
        }
    });
}

/**
* 5.new Slave配置替换重启
*/
function addNewSlave(){
    var addNewSlaveId = "addNewSlave";
    active(addNewSlaveId);
    $("#install").html("添加新slave...");
    $("#skipbutton").attr("style","display:none;");
    $.post('/manage/app/migrate/addSlave',
    {
        "appId":migrateId,
        //"machineInfo":$("button[data-id='id_select']").attr("title"),
        "machineInfo":machineInfo,
        "type":$("#appType").attr("value")
    },
    function(data){
        if(data.status == 1){
            $("#instanceTargetInfo").html(data.instanceTargetInfo);
            $("#instanceTargetLog").html(data.instanceTargetLog);
            complete(addNewSlaveId);
            goOn("instanceCheck");
        } else if(data.status == -1){
            toastr.error("异常信息："+data.message);
            warn(addNewSlaveId);
            goOn("addNewSlave");
        } else {
            toastr.error("操作失败,请查看日志!");
            warn(addNewSlaveId);
            goOn("addNewSlave");
        }
    });
}



/**
* 6.验证当前实例状态
*/
function instanceCheck(){
    var instanceCheckId = "instanceCheck";
    active(instanceCheckId);
    $("#install").html("实例检测中...");
    // 显示下线实例
    //$("#nodeChangeDiv").removeAttr("hidden","hidden");
    $.post('/manage/app/migrate/appCheck',
    {
        "appId":migrateId,
        "type":$("#appType").attr("value")
    },
    function(data){
        if(data.status == 1){
            complete(instanceCheckId);
            goOn("downOldSlave");
        } else if(data.status == -1){
            toastr.error("异常信息："+data.message);
            warn(instanceCheckId);
        } else {
            toastr.error("操作失败,请查看日志!");
            warn(instanceCheckId);
        }
    });

}

/**
* 7.下线老实例
*/
function downOldSlave(){
    var downOldSlaveId = "downOldSlave";
    active(downOldSlaveId);
    $("#install").html("下线老slave实例...");
    var downInstanceIds = $("#downInstanceIds").attr("value");
    // 如果是sentinel
    if($("#appType").attr("value") == '5'){
        downInstanceIds = $("#downInstanceIds").attr("value")+","+$("#downSentinelIds").attr("value")
    }
    $.post('/manage/app/migrate/downSlave',
    {
        "appId":migrateId,
        "downInstanceIds":downInstanceIds,
        "type":$("#appType").attr("value")
    },
    function(data){
        if(data.status == 1){
            $("#instanceTargetInfo").html(data.instanceTargetInfo);
            $("#instanceTargetLog").html(data.instanceTargetLog);
            complete(downOldSlaveId);
            goOn("migrateComplete");
        } else if(data.status == -1){
            toastr.error("异常信息："+data.message);
            warn(downOldSlaveId);
        } else {
            toastr.error("操作失败,请查看日志!");
            warn(downOldSlaveId);
        }
    });

}

/**
* 8.迁移完成
*/
function migrateComplete(){
    var upgradeCompleteId = "migrateComplete";
    active(upgradeCompleteId);
    $("#install").html("迁移完成");
    $.post('/manage/app/migrate/complete',
    {
        "appId":migrat,
        "upgradeVersionId":$('#versionSelect'+installId+" option:selected").attr('versionid')
    },
    function(data){
        if(data.status == 1){
            complete(upgradeCompleteId);
        } else if(data.status == -1){
            toastr.error("异常信息："+data.message);
            warn(upgradeCompleteId);
        } else {
            toastr.error("操作失败,请查看日志!");
            warn(upgradeCompleteId);
        }
    });
}

function warn(id){
	$("#"+id).addClass("warn");
}
function disable(id){
	$("#"+id).removeClass("active").addClass("disabled");
}
function active(id){
	$("#"+id).removeClass("disabled").removeClass("warn").addClass("active");
}
function complete(id){
	$("#"+id).removeClass("active").removeClass("warn").addClass("complete");
}
</script>