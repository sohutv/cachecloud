    <%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp"%>
<script src="/resources/manage/plugins/jquery-1.10.2.min.js"></script>
<script type="text/javascript">
    var jQuery_1_10_2 = $;
</script>
<script type="text/javascript" src="/resources/select/bootstrap-select.js"></script>
<link rel="stylesheet" type="text/css" href="/resources/select/bootstrap-select.css"/>
<div class="container">

    <div class="row">
        <div class="col-md-12">
            <h3 class="page-header">
                拓扑问题诊断
                <c:if test="${checkInfo.tips.size() == 0}">
                    <a class="btn btn-success">正常</a>
                </c:if>
                <c:if test="${checkInfo.tips.size() > 0}">
                    <a class="btn btn-danger">异常</a>
                </c:if>
            </h3>
        </div>
    </div>

    <div class="col-md-12">
        <h5 style="font:bold;color:green;">
            应用ID:${checkInfo.appDesc.appId}  &nbsp;&nbsp;&nbsp; 应用名:${checkInfo.appDesc.name} &nbsp;&nbsp;&nbsp; 应用类型:${checkInfo.appDesc.typeDesc}
        </h5>
        <table class="table table-striped table-bordered table-hover">
            <thead>
                <tr>
                    <th>序号</th>
                    <th>诊断问题</th>
                    <th>应用信息</th>

                </tr>
            </thead>
            <tbody>
                <c:set var="index" value="0" />
                <c:if test="${checkInfo.tips.size() == 0}">
                    <tr>
                        <td colspan="3" align="center">无</td>
                    </tr>
                </c:if>
                <c:if test="${checkInfo.tips.size() > 0}">
                    <c:forEach items="${checkInfo.tips}" var="tip">
                        <tr class="odd gradeX">
                            <td>
                                <c:set var="index" value="${index+1}" />
                                ${index}
                            </td>
                            <td>
                                ${tip.status}
                            </td>
                            <td>
                                ${tip.desc}
                            </td>
                        </tr>
                    </c:forEach>
                </c:if>
            </tbody>
        </table>
    </div>

    <c:if test="${checkInfo.appDesc.type==2 || checkInfo.appDesc.type==5}">

        <div class="row">
            <div class="col-md-12">
                <h3 class="page-header">
                    诊断1:同网段分析
                    <c:choose>
                        <c:when test="${checkInfo.sameNetSegment == 'false'}">
                            <a class="btn btn-danger">异常</a>
                        </c:when>
                        <c:otherwise>
                            <a class="btn btn-success">正常</a>
                        </c:otherwise>
                    </c:choose>
                </h3>
            </div>
        </div>

        <div class="row">
            <div class="col-md-12">
                <h3 class="page-header">
                    诊断2:主从节点分析
                    <c:choose>
                        <c:when test="${checkInfo.master_slaves.size() == checkInfo.slaveNum && checkInfo.msFlag == 'false'}">
                            <a class="btn btn-success">正常</a>
                        </c:when>
                        <c:otherwise>
                            <a class="btn btn-danger">异常</a>
                        </c:otherwise>
                    </c:choose>
                </h3>
            </div>
        </div>

        <div class="col-md-12">
            <div>
                <c:if test="${checkInfo.appDesc.type==5}">sentinel节点数量：${checkInfo.sentinels.size()}</c:if>
                主节点数量：${checkInfo.master_slaves.size()} &nbsp;
                从节点数量：${checkInfo.slaveNum}  &nbsp;<br/>
                <c:if test="${checkInfo.msFlag=='true'}"><span style="color:red;font:bold">【异常诊断】：主从节点有分布同一台物理机</span><br/></c:if>
                <c:if test="${checkInfo.master_slaves.size() != checkInfo.slaveNum}"><span style="color:red;font:bold">【异常诊断】：主从节点数量不一致</span><br/></c:if>
                <c:if test="${checkInfo.appDesc.type==5 && checkInfo.sentinels.size() < 3}"><span style="color:red;font:bold">【异常诊断】：sentinel节点数量少于3个</span><br/></c:if>
            </div>
            <c:if test="${checkInfo.appDesc.type==5}">
                ${instanceInfoMap}
                <table class="table table-striped table-bordered table-hover">
                    <thead>
                        <tr>
                            <th width="20px">序号</th>
                            <th width="40px">sentinel实例</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:set var="index" value="0" />
                        <c:forEach items="${checkInfo.sentinels}" var="sentinel">
                            <tr class="odd gradeX">
                                <td width="20px">
                                    <c:set var="index" value="${index+1}" />
                                    ${index}
                                </td>
                                <td width="40px">
                                    ${sentinel.ip}:${sentinel.port}
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </c:if>

            <table class="table table-striped table-bordered table-hover">
                <thead>
                    <tr>
                        <th>master</th>
                        <th>slave</th>
                        <th>是否同一物理机</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach items="${checkInfo.master_slaves}" var="master">
                        <tr class="odd gradeX">
                            <td>
                                ${master.key.ip}:${master.key.port}
                            </td>
                            <td>
                                <c:forEach items="${master.value}" var="slave">
                                    ${slave.ip}:${slave.port} <br/>
                                </c:forEach>
                            </td>
                            <td>
                                <c:if test="${master.value.size() > 0}">
                                    <c:if test="${checkInfo.instanceInfoMap.get(master.key.ip).realIp.equals(checkInfo.instanceInfoMap.get(master.value.get(0).ip).realIp)}"><a style="color:red">是</a></c:if>
                                    <c:if test="${!checkInfo.instanceInfoMap.get(master.key.ip).realIp.equals(checkInfo.instanceInfoMap.get(master.value.get(0).ip).realIp)}">否</c:if>
                                </c:if>
                                <c:if test="${master.value.size() == 0}"> <a style="color:red">无slave</a> </c:if>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>

        </div>

        <!--  机器分布 -->
        <div class="row">
            <div class="col-md-12">
                <h3 class="page-header">
                    诊断3:物理机/机架分布
                    <c:choose>
                        <c:when test="${checkInfo.machineInfoMap.size() >= 3 && checkInfo.failoverStatus != false}">
                            <a class="btn btn-success">正常</a>
                        </c:when>
                        <c:otherwise>
                            <a class="btn btn-danger">异常</a>
                        </c:otherwise>
                    </c:choose>
                </h3>
            </div>
        </div>

        <div class="col-md-12">
            <div>
                物理机节点数：${checkInfo.machineInfoMap.size()} &nbsp;<br/>
                <c:if test="${checkInfo.machineInfoMap.size() < 3}">
                    <span style="color:red;font:bold;">【异常诊断】：物理机分布数量少于3组</span><br/>
                </c:if>
                <c:if test="${checkInfo.failoverStatus == false}">
                    <span style="color:red;font:bold;">【异常诊断】：主节点分布少于3台物理机,其中一台物理机宕机不满足故障转移条件</span>
                </c:if>
            </div>
            <table class="table table-striped table-bordered table-hover">
                <thead>
                    <tr>
                        <th>宿主机</th>
                        <th>机房/机架信息</th>
                        <th>实例信息</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach items="${checkInfo.machineInstancesMap}" var="machine">
                        <tr class="odd gradeX">
                            <td>${machine.key}</td>
                            <td>${checkInfo.machineInfoMap.get(machine.key).room}:${checkInfo.machineInfoMap.get(machine.key).rack}</td>
                            <td>
                                <c:forEach items="${machine.value}" var="instance">
                                    ${instance.ip}:${instance.port} (${instance.roleDesc})<br/>
                                </c:forEach>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </div>


    </c:if>

</div>

<script type="text/javascript">
    $(window).on('load', function () {
        jQuery_1_10_2('.selectpicker').selectpicker({
            'selectedText': 'cat'
        });
    });
    $(function(){
        $('#searchType_bigKey').change(function () {
            if($(this).val()==1)
            {
                $("#instanceInfo").show();
            }else{
                $("#instanceInfo").hide();
            }
        });
        $('#searchType_idleKey').change(function () {
            if($(this).val()==1)
            {
                $("#instanceInfo_idle").show();
            }else{
                $("#instanceInfo_idle").hide();
            }
        });
        $('#searchType_delKey').change(function () {
            if($(this).val()==1)
            {
                $("#instanceInfo_del").show();
            }else{
                $("#instanceInfo_del").hide();
            }
        });
    });

    var appId=$('#appId').val();
    function findInstancePatternKeys() {
        var flag=1;
        var instance=document.getElementById('inst_scanKey');
        var pattern=document.getElementById('pattern_scanKey');
        var scanKeyListText=document.getElementById('scanKeyList');
        scanKeyListText.value='';

        var note_inst_scanKey=document.getElementById('note_inst_scanKey');
        var note_pattern_scanKey=document.getElementById('note_pattern_scanKey');
        note_inst_scanKey.style.display='none';
        note_pattern_scanKey.style.display='none';

        if(instance.value==''){
            note_inst_scanKey.style.display='';
            flag=0;
        }
        if(pattern.value==''){
            note_pattern_scanKey.style.display='';
            flag=0;
        }
        if(flag==0) return;
        //清除上次记录
        $('#scanKeyList').empty();
        var ip=instance.value.split(':')[0];
        var port=instance.value.split(':')[1];
        $.post(
            '/manage/app/tool/findInstancePatternKeys.json',
            {
                appId: appId,
                ip: ip,
                port: port,
                pattern: pattern.value
            },
            function (data) {
                var instancePatternKeyList=data.instancePatternKeyList;
                if(instancePatternKeyList==null||instancePatternKeyList.length==0){
                    alert("无满足条件的key!");
                }else {
                    instancePatternKeyList.forEach(function (key) {
                        scanKeyListText.value=scanKeyListText.value+key+"\n";
                    })
                }
            }
        );

    }
    function findBigKey() {
        var searchType_bigKey=$('#searchType_bigKey').val();
        var startBytes=document.getElementById('startBytes');
        var endBytes=document.getElementById('endBytes');
        var bigKeyListText=document.getElementById('bigKeyList');
        bigKeyListText.value='';

        var note_endBytes=document.getElementById('note_endBytes');
        if(startBytes.value==''||endBytes.value==''){
            note_endBytes.style.display='';
            return;
        }
        if(searchType_bigKey==1){
            var instance=document.getElementById('inst_bigKey');
            var ip=instance.value.split(':')[0];
            var port=instance.value.split(':')[1];
            $.post(
                '/manage/app/tool/findInstanceBigKey.json',
                {
                    appId: appId,
                    ip: ip,
                    port: port,
                    startBytes: startBytes.value,
                    endBytes: endBytes.value
                },
                function (data) {
                    var instanceBigKeyList=data.instanceBigKeyList;
                    if(instanceBigKeyList==null||instanceBigKeyList.length==0){
                        alert("无满足条件的key!");
                    }else {
                        instanceBigKeyList.forEach(function (key) {
                            bigKeyListText.value=bigKeyListText.value+key+"\n";
                        })
                    }
                }
            );
        }else if(searchType_bigKey==0){
            $.post(
                '/manage/app/tool/findClusterBigKey.json',
                {
                    appId: appId,
                    startBytes: startBytes.value,
                    endBytes: endBytes.value
                },
                function (data) {
                    var clusterBigKeyList=data.clusterBigKeyList;
                    if(clusterBigKeyList==null||clusterBigKeyList.length==0){
                        alert("无满足条件的key!");
                    }else {
                        clusterBigKeyList.forEach(function (key) {
                            bigKeyListText.value=bigKeyListText.value+key+"\n";
                        })
                    }
                }
            );
        }
    }
    function findIdleKey() {
        var searchType_idleKey=$('#searchType_idleKey').val();
        var idleDays=document.getElementById('idleDays');
        var idleKeyList=document.getElementById('idleKeyList');
        idleKeyList.value='';

        var note_idleDays=document.getElementById('note_idleDays');
        if(idleDays.value==''){
            note_idleDays.style.display='';
            return;
        }
        if(searchType_idleKey==1){
            var instance=document.getElementById('inst_idleKey');
            var ip=instance.value.split(':')[0];
            var port=instance.value.split(':')[1];
            $.post(
                '/manage/app/tool/findInstanceIdleKeys.json',
                {
                    appId: appId,
                    ip: ip,
                    port: port,
                    idleDays: idleDays.value
                },
                function (data) {
                    var instanceIdleKeyList=data.instanceIdleKeyList;
                    if(instanceIdleKeyList==null||instanceIdleKeyList.length==0){
                        alert("无满足条件的key!");
                    }else {
                        instanceIdleKeyList.forEach(function (key) {
                            idleKeyList.value=idleKeyList.value+key+"\n";
                        })
                    }
                }
            );
        }else if(searchType_idleKey==0){
            $.post(
                '/manage/app/tool/findClusterIdleKeys.json',
                {
                    appId: appId,
                    idleDays: idleDays.value
                },
                function (data) {
                    var clusterIdleKeyList=data.clusterIdleKeyList;
                    if(clusterIdleKeyList==null||clusterIdleKeyList.length==0){
                        alert("无满足条件的key!");
                    }else {
                        clusterIdleKeyList.forEach(function (key) {
                            idleKeyList.value=idleKeyList.value+key+"\n";
                        })
                    }
                }
            );
        }
    }
    function deleteKey() {
        var searchType_delKey=$('#searchType_delKey').val();
        var pattern=document.getElementById('pattern_delKey');
        var note_pattern=document.getElementById('note_pattern_delKey');
        if(pattern.value==''){
            note_pattern.style.display='';
            return;
        }

        if(searchType_delKey==1){
            var instance=document.getElementById('inst_delKey');
            var ip=instance.value.split(':')[0];
            var port=instance.value.split(':')[1];
            $.post(
                '/manage/app/tool/delInstancePatternKeys.json',
                {
                    appId: appId,
                    ip: ip,
                    port: port,
                    pattern: pattern.value
                },
                function(data){
                    var result=data.result;
                    if(result==1){
                        alert("匹配"+pattern.value+"的keys删除成功");
                    }else {
                        alert("匹配"+pattern.value+"的keys删除失败");
                    }
                }
            );

        }else if(searchType_delKey==0){
            $.post(
                '/manage/app/tool/delClusterPatternKey.json',
                {
                    appId: appId,
                    pattern: pattern.value
                },
                function(data){
                    var result=data.result;
                    if(result==1){
                        alert("匹配"+pattern.value+"的keys删除成功");
                    }else {
                        alert("匹配"+pattern.value+"的keys删除失败");
                    }
                }
            );
        }
    }
    function appTopologyExam(){

        $.post(
            '/manage/tool/topologyExam.json',
            {
                appId: appId,
                examType: 3
            },
            function(data){
                var taskId=data.taskId;
                if(taskId>0){
                    alert("应用检查任务提交成功，任务ID："+taskId+"，请查看结果邮件！");
                }else {
                    alert("应用检查任务提交失败！");
                }
            }
        );
    }
</script>
