<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
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

<body>
<!-- 一键生成实例配置 -->
<div id="assignRedisModal" tabindex="-1" data-width="400">
    <div class="" style="width:1400px;">
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

                        <div class="form-group">
                            <label class="control-label col-md-2">
                                内存总量<font color='red'>*</font>：
                            </label>
                            <div class="col-md-2">
                                <input type="text" name="size" id="size" placeholder="内存总量，单位M" class="form-control"/>
                                <p id="notesize" style="color: red; display: none">
                                    <i class="ace-icon fa fa-exclamation-triangle bigger-120"></i>请填写内存总量
                                </p>
                            </div>

                            <label class="control-label col-md-2">
                                分配机器数<font color='red'>*</font>：
                            </label>
                            <div class="col-md-2">
                                <input type="text" name="machineNum" id="machineNum" placeholder="分配机器数" class="form-control"/>
                                <p id="noteMachineNum" style="color: red; display: none">
                                    <i class="ace-icon fa fa-exclamation-triangle bigger-120"></i>
                                    请填写机器数
                                </p>
                            </div>

                            <label class="control-label col-md-2">
                                主节点数<font color='red'>*</font>：
                            </label>
                            <div class="col-md-2">
                                <input type="text" name="instanceNum" id="instanceNum" placeholder="分配实例数" class="form-control"/>
                                <p id="notenum" style="color: red; display: none">
                                    <i class="ace-icon fa fa-exclamation-triangle bigger-120"></i>
                                    请填写实例数
                                </p>
                            </div>
                        </div>


                        <%--excludeMachines--%>
                        <div class="form-group">
                            <label class="control-label col-md-2">
                                排除机器：
                            </label>
                            <div class="col-md-8">
                                <label for="excludeMachines"></label>
                                <select id="excludeMachines" class="selectpicker bla bla bli" multiple data-live-search="true">
                                    <c:forEach items="${machineList}" var="machine">
                                        <c:if test="${machine.info.useType==0}">
                                            <option value="${machine.ip}">${machine.ip}：${machine.info.extraDesc}【Redis专用机器】</option>
                                        </c:if>
                                        <c:if test="${machine.info.useType==1}">
                                            <option value="${machine.ip}">${machine.ip}：${machine.info.extraDesc}【Redis测试机器】</option>
                                        </c:if>
                                        <c:if test="${machine.info.useType==2}">
                                            <option value="${machine.ip}">${machine.ip}：${machine.info.extraDesc}【Redis混合部署机器】</option>
                                        </c:if>
                                    </c:forEach>
                                    </optgroup>
                                </select>
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
                                        <c:if test="${machine.info.useType==0}">
                                            <option value="${machine.ip}">${machine.ip}：${machine.info.extraDesc}【Redis专用机器】</option>
                                        </c:if>
                                        <c:if test="${machine.info.useType==1}">
                                            <option value="${machine.ip}">${machine.ip}：${machine.info.extraDesc}【Redis测试机器】</option>
                                        </c:if>
                                        <c:if test="${machine.info.useType==2}">
                                            <option value="${machine.ip}">${machine.ip}：${machine.info.extraDesc}【Redis混合部署机器】</option>
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
                                <span id="deployPreview">生成实例部署</span>
                            </button>
                            <%--<button type="button" class="btn btn-primary" data-toggle="modal" onclick="generatePreview()">--%>
                                <%--<span id="deploy">确定部署信息</span>--%>
                            <%--</button>--%>
                        </div>

                    </div>
                </form>
                <!-- END FORM-->
            </div>
        </div>
    </div>
</div>
</body>
</html>

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

    //生成部署详情
function getDeployInfo() {
    var flag=1;

	var appType=document.getElementById("appType");
	var isSlave=document.getElementById("isSalve");
	var size=document.getElementById("size");
	var machineNum=document.getElementById("machineNum");
    var instanceNum=document.getElementById("instanceNum");
	var useType=document.getElementById("useType");
	var room=document.getElementById("room");
    var machines=$('#machines').val();
    var excludeMachines=$('#excludeMachines').val();
    var sentinelMachineList=$('#sentinelMachineList').val();

	var notemachines=document.getElementById("notemachines");
	var notesize=document.getElementById("notesize");
	var noteMachineNum=document.getElementById("noteMachineNum");
	var noteSentinelMachines=document.getElementById('noteSentinelMachines');
	var notenum=document.getElementById("notenum");
	notemachines.style.display='none';
	notesize.style.display='none';
    noteMachineNum.style.display='none';
    noteSentinelMachines.style.display='none';
	notenum.style.display='none';

	if(size.value==''){
        notesize.style.display='';
        flag=0;
	}
    if(machineNum.value==''){
        noteMachineNum.style.display='';
        flag=0;
    }
	if(instanceNum.value==''){
        notenum.style.display='';
		flag=0;
	}
	if(useType.value==0){//专用
		if(machines==null){
            notemachines.style.display='';
            flag=0;
		}
	}
	if(appType.value==5){//sentinel
		var count=sentinelMachineList==null?0:sentinelMachineList.length;
        // alert("count: "+count);
        if(count<3||count%2==0){
            noteSentinelMachines.style.display='';
            flag=0;
        }

    }

    if(appType.value!=6&&isSlave.value==1&&machineNum.value<2){
		alert("需要生成从节点，则机器数需大于1")
		flag=0;
	}

	if(flag==0) return;

	//清除上次记录
	$('#tableList tbody tr').empty();
    <%--var appDeployText = document.getElementById("appDeployText");--%>
    <%--appDeployText.value="";--%>
    var appDeployInfo = document.getElementById("appDeployInfo");
    appDeployInfo.value="";

    var startDeployInfoLabel = document.getElementById("startDeployInfoLabel");
    startDeployInfoLabel.innerHTML = '<br/>正在生成部署预览,请等待...';
	$.post(
        '/manage/app/getDeployInfo.json',
		{
			type: appType.value,
			isSalve: isSlave.value,
            room: room.value,
            size: size.value,
            machineNum: machineNum.value,
			instanceNum: instanceNum.value,
            useType: useType.value,
            machines: machines==null?null:machines.toString(),
            excludeMachines: excludeMachines==null?null:excludeMachines.toString(),
            sentinelMachines: sentinelMachineList==null?null:sentinelMachineList.toString()
		},
		function (data) {
            startDeployInfoLabel.innerHTML='';
        	if(data.result!='success'){
        		alert("生成部署预览发生错误："+data.result);
			}
			else {
                /**
				 * 展示机器详情
                 */
                var resMachines=data.resMachines;
                var machineDeployStatMap=data.machineDeployStatMap;
                resMachines.forEach(function (machine) {
                    var machineDeployStat=machineDeployStatMap[machine.ip];
                    $("#tableList tbody").prepend(
                        '<tr class="odd gradeX">\n' +
                        "   <td> <a target='_blank' href='/manage/machine/machineInstances?ip="+machine.ip+" '>"+machine.ip+"</a>\n" +
                        '   <td>'+machine.mem+'G</td>\n' +
                        '   <td ><font color="#FF0000">'+(machine.mem-(machine.applyMem/1024/1024/1024)).toFixed(2)+'G </font> </td>\n' +
                        '   <td>'+machine.instanceNum+"&nbsp;&nbsp;/&nbsp;&nbsp;"+machine.cpu+'</td>\n' +
                        '   <td>'+machineDeployStat.masterNum+"&nbsp;&nbsp;/&nbsp;&nbsp;"+machineDeployStat.slaveNum+"&nbsp;&nbsp;/&nbsp;&nbsp;"+machineDeployStat.sentinelNum+'</td>\n' +
                        '   <td>'+machine.mem+"&nbsp;&nbsp;/&nbsp;&nbsp;"+machine.rack+'</td>\n' +
                        "   <td> <a target='_blank' href='/manage/machine/list?ipLike="+machine.ip+" '>查看</a>\n" +
                        '</tr>'
                    )
                })
                /**
				 * 展示部署详情
                 */
                var deployInfos=data.deployInfoList;
                if(deployInfos.length==0){
                    alert('可用机器数不足，请重新部署生成预览');
                }
                if(appType.value==2){//Redis-cluster
                    deployInfos.forEach(function (deployInfo) {
                        var masterIp=deployInfo.masterIp;
                        var memSize=deployInfo.memSize;
                        var slaveIp=deployInfo.slaveIp;
                        appDeployInfo.value=appDeployInfo.value+masterIp+":"+memSize+(slaveIp==''?"\n":":"+slaveIp+"\n");
                    });
                }else if(appType.value==5){//Redis-sentinel
                    deployInfos.forEach(function (deployInfo) {
                        if(deployInfo.masterIp!=null&&deployInfo.masterIp!=''){
                            var masterIp=deployInfo.masterIp;
                            var memSize=deployInfo.memSize;
                            var slaveIp=deployInfo.slaveIp;
                            appDeployInfo.value=appDeployInfo.value+masterIp+":"+memSize+(slaveIp==''?"\n":":"+slaveIp+"\n");
                        }else {
                            var sentinelIp=deployInfo.sentinelIp;
                            appDeployInfo.value=appDeployInfo.value+sentinelIp+"\n";
                        }
                    });
                }else if(appType.value==6){//Redis-standalone
                    deployInfos.forEach(function (deployInfo) {
                        var masterIp=deployInfo.masterIp;
                        var memSize=deployInfo.memSize;
                        appDeployInfo.value=masterIp+":"+memSize;
                    });
                }
			}
        }
	);
}
</script>