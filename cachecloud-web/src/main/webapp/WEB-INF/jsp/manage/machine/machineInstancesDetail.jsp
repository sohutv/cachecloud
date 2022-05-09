<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp"%>
<link rel="stylesheet" type="text/css" href="/resources/select/bootstrap-select.css"/>
	<script src="/resources/manage/plugins/jquery-1.10.2.min.js"></script>
	<script type="text/javascript">
	var jQuery_1_10_2 = $;
	</script>
	<script type="text/javascript" src="/resources/select/bootstrap-select.js"></script>
	<link rel="stylesheet" type="text/css" href="/resources/select/bootstrap-select.css"/>

<script type="text/javascript">
	$(window).on('load', function () {
        jQuery_1_10_2('.selectpicker').selectpicker({
            'selectedText': 'cat'
        });
    });
</script>

<script type="text/javascript">
	function startInstance(appId, instanceId){
		if(confirm("确认要开启"+instanceId+"实例吗?")){
			$.ajax({
                type: "get",
                url: "/manage/instance/startInstance.json",
                data: 
                {
                	appId: appId,
                	instanceId: instanceId
                },
                success: function (result) {
                	if(result.success == 1){
                		alert("开启成功!");
                	}else{
                		alert("开启失败, msg: " + result.message)
                	}
                    window.location.reload();
                }
            });
        }
	}

	function scrollStartInstance(machineIp){
		if(confirm("确认滚动重启容器"+machineIp+"下所有实例吗?")){
			$.ajax({
                type: "get",
                url: "/manage/instance/scrollStartInstance.json",
                data:
                {
					machineIp: machineIp
                },
                success: function (result) {
                	if(result.success == 1){
                		alert(result.message);
                	}else{
                		alert("重启失败, msg: " + result.message)
                	}
                    window.location.reload();
                }
            });
        }
	}

	function migrateInstance(machineIp){

		var content = "";
		// 1.判断是全部实例迁移还是部分实例
		var instanceIds ="";
		$('#selectInstance option:selected').each(function(){
			var instanceId = parseInt($(this).attr("value"));
			if(instanceId > 0){
				instanceIds += instanceId+",";
			}else{
				instanceIds = -1;
				return false;;
			}
		});
		// 2.目标机器判断
		var targetIp = $('#targetContainer option:selected').attr("value");
		if(typeof(targetIp) == "undefined" || targetIp == ''){
			alert("请选择目标机器！");
			return;
		}
		//3.确认实例
		<%--alert("instanceIds:"+isEmpty(instanceIds));--%>
		if(isEmpty(instanceIds)){
			alert("请选择要迁移的实例！");
			return;
		}

		if(parseInt(instanceIds) == -1){
			content = "确认将容器"+machineIp+"全部实例迁移到"+targetIp+"容器?";
		}else{
			content = "确认将容器"+machineIp+"实例ID("+instanceIds+")迁移到"+targetIp+"容器?";
		}

		// 4.资源是否充足判断
		if(parseFloat($("#containerMM").attr("value")).toFixed(2) * 0.9 < parseFloat($("#needMM").attr("value")).toFixed(2)){
			alert("目标机器内存使用率将超过90%,请重新分配实例或选择其他目标机器！");
			return;
		}

		// 5.发起迁移
		if(confirm(content)){
			$.ajax({
                type: "get",
                url: "/manage/instance/migrate.json",
                data:
                {
					sourceIp: machineIp,
					targetIp: targetIp,
					instanceIds: instanceIds // -1:迁移所有实例 ，部分实例迁移,部分实例迁移存这些实例id
                },
                success: function (result) {
                	if(result.status == 1){
						$("#migrateInstanceBtn").click();
						if(parseInt(instanceIds) == -1){
							$("#tips").html("<label style=\"color:red\">当前机器实例正在迁移到新容器:<a target=\"blank\" href=\"/manage/machine/machineInstances?ip="+targetIp+"\">"+targetIp+"</a></label>");
						}else{
							$("#tips").html("<label style=\"color:red\">当前机器实例("+instanceIds+")正在迁移到新容器:<a target=\"blank\" href=\"/manage/machine/machineInstances?ip="+targetIp+"\">"+targetIp+"</a></label>");
						}
					}
				}
            });
        }
	}

	function isEmpty(property) {
		return (property === null || property === "" || typeof property === "undefined");
	}

	// 计算迁移实例需要内存
	function instanceChange(){

		var totalUsed = 0.00;
		$('#selectInstance option:selected').each(function(){
			totalUsed += parseFloat($(this).attr("memoryUsed"));
		});
		$("#needMM").html("<label style=\"color:darkgray\">需要内存:"+totalUsed.toFixed(2)+"G<label>");
		$("#needMM").attr("value",totalUsed);
	}

	// 迁移机器内存剩余内存
	function containerChange(){

		var totalUsed = 0.00;
		$('#targetContainer option:selected').each(function(){
			totalUsed += parseFloat($(this).attr("memoryUsed"));
		});
		$("#containerMM").attr("value",totalUsed);

		//内存提示
		if(parseFloat(totalUsed).toFixed(2) - parseFloat($("#needMM").attr("value")).toFixed(2) > 0){
			$("#containerMM").html("<label style=\"color:green\">剩余内存:"+totalUsed.toFixed(2)+"G<label>");
		}else{
			$("#containerMM").html("<label style=\"color:red\">内存不够:"+totalUsed.toFixed(2)+"G<label>");
		}
	}

	function shutdownInstance(appId, instanceId){
		if(confirm("确认要下线"+instanceId+"实例吗?")){
			$.ajax({
                type: "get",
                url: "/manage/instance/shutdownInstance.json",
                data: 
                {
                	appId: appId,
                	instanceId: instanceId
                },
                success: function (result) {
                	if(result.success == 1){
                		alert("关闭成功!");
                	}else{
                		alert("关闭失败, msg: " + result.message)
                	}
                    window.location.reload();
                }
            });
        }
	}	
	
</script>
<div class="page-container">
    <div class="page-content">
        <div class="row">
		    <div class="col-md-12">
		        <h3 class="page-title">
				   <c:choose>
					 <c:when test="${machineInfo.k8sType==1}">
						 机器(ip=<a target="_blank" href="/manage/machine/pod/changelist?ip=${machineInfo.ip}" title="查看pod变更记录">${machineInfo.ip}</a>)实例列表
					 </c:when><c:otherwise>
						 机器(ip=${machineInfo.ip})实例列表
					 </c:otherwise>
				   </c:choose>
					<a target="_blank" onclick="scrollStartInstance('${machineInfo.ip}')" class="btn btn-success">
						滚动重启
					</a>
					<button id="sample_editable_1_new" class="btn green" data-target="#migrateInstanceModal" data-toggle="modal" onclick="instanceChange()">
						一键迁移
					</button>
					<label id="tips"></label>
		        </h3>
		    </div>
		</div>
		<div class="row">
		    <div class="col-md-12">
		        <div class="portlet box light-grey">
		            <div class="portlet-title">
		                <div class="caption"><i class="fa fa-globe"></i>实例列表</div>
		                <div class="tools">
		                    <a href="javascript:;" class="collapse"></a>
		                </div>
		            </div>
		            <div class="portlet-body">
		                <table class="table table-striped table-bordered table-hover" id="tableDataList">
		                    <thead>
		                    <tr>
		                        <td>ID</td>
		                        <th>应用ID</th>
		                        <th>应用名</th>
		                        <th>应用类型</th>
		                        <th>负责人</th>
		                        <td>服务器ip:port</td>
		                        <td>实例空间使用情况</td>
		                        <td>连接数</td>
		                        <td>角色</td>
		                        <td>实例所在机器信息可用内存(G)</td>
		                        <td>日志</td>
		                        <td>实例操作</td>
		                    </tr>
		                    </thead>
		                    <tbody>
		                    <c:forEach var="instance" items="${instanceList}" varStatus="status">
		                        <tr>
		                            <td><a href="/admin/instance/index?instanceId=${instance.id}"
		                                   target="_blank">${instance.id}</a></td>
		                            <c:set var="instanceStatsMapKey" value="${instance.ip}:${instance.port}"></c:set>
		                            <c:set var="curAppId" value="${instance.appId}"></c:set>
		                            <td>
		                            	<c:if test="${curAppId > 0}">
			                            	<a target="_blank" href="/manage/app/index?appId=${curAppId}">
			                            		${curAppId}
			                            	</a>
		                            	</c:if>
		                            </td>
		                            <td>
										<c:if test="${curAppId > 0}">
											<a target="_blank" href="/admin/app/index?appId=${curAppId}">
												${(appInfoMap[curAppId]).name}
											</a>
										</c:if>
									</td>
									<td>
										<c:if test="${curAppId > 0}">
											<a target="_blank" href="/admin/app/index?appId=${curAppId}">
											${(appInfoMap[curAppId]).typeDesc}
											</a>
										</c:if>
									</td>
		                            <td>${(appInfoMap[curAppId]).officer}</td>
		                            <td>${instance.ip}:${instance.port}</td>
		                            <td>
		                                <div class="progress margin-custom-bottom0">
		                                	<c:choose>
				                        		<c:when test="${(instanceStatsMap[instanceStatsMapKey]).memUsePercent >= 80}">
													<c:set var="progressBarStatus" value="progress-bar-danger"/>
				                        		</c:when>
				                        		<c:otherwise>
													<c:set var="progressBarStatus" value="progress-bar-success"/>
				                        		</c:otherwise>
				                        	</c:choose>
		                                    <div class="progress-bar ${progressBarStatus}"
		                                         role="progressbar"
		                                         aria-valuenow="${(instanceStatsMap[instanceStatsMapKey]).memUsePercent }"
		                                         aria-valuemax="100"
		                                         aria-valuemin="0"
		                                         style="width: ${(instanceStatsMap[instanceStatsMapKey]).memUsePercent }%">
		                                            <label style="color: #000000">
		                                                <fmt:formatNumber
		                                                        value="${(instanceStatsMap[instanceStatsMapKey]).usedMemory / 1024 / 1024 / 1024}"
		                                                        pattern="0.00"/>G&nbsp;&nbsp;Used/<fmt:formatNumber value="${(instanceStatsMap[instanceStatsMapKey]).maxMemory / 1024 / 1024 / 1024}" pattern="0.00"/>G&nbsp;&nbsp;Total
		                                            </label>
		                                     </div>
		                                </div>
		                            </td>
                            		   <td>${(instanceStatsMap[instanceStatsMapKey]).currConnections}</td>
                            		   <td>${instance.roleDesc}</td>
		                            <td><fmt:formatNumber
		                                    value="${(machineCanUseMem[instance.ip])/1024/1024/1024}"
		                                    pattern="0.00"/>
		                            </td>
									<td>
										<a target="_blank" href="/manage/instance/log?instanceId=${instance.id}">查看</a>
									</td>
		                            <td>
										<c:if test="${instance.status == 0}">
											<a target="_blank" onclick="startInstance('${curAppId}', '${instance.id}')" class="btn btn-success">
												启动实例
											</a>
										</c:if>
										<a target="_blank" onclick="shutdownInstance('${curAppId}', '${instance.id}')" class="btn btn-danger">
											下线实例
										</a>
		                            </td>
		                        </tr>
		                    </c:forEach>
		                    </tbody>
		                </table>
		            </div>
		        </div>
		    </div>
		</div>
    </div>
</div>

<div id="migrateInstanceModal" class="modal fade" tabindex="-1" data-width="600">
	<div class="modal-dialog">
		<div class="modal-content">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal" aria-hidden="true"></button>
				<h4 class="modal-title">实例替换迁移</h4>
				<b style="font-size:12px;color:blue"><i>支持所有/部分cluster实例迁移</i></b>
			</div>

			<form class="form-horizontal form-bordered form-row-stripped">
				<div class="modal-body">
					<div class="row">
						<!-- 控件开始 -->
						<div class="col-md-15">
							<!-- form-body开始 -->
							<div class="form-body">
								<div class="form-group">
									<label class="control-label col-md-3">
										当前机器ip：
									</label>
									<div class="col-md-8">
										${machineInfo.ip}
											<label class="label label-info">宿主:${machineInfo.realIp} cpu:${machineInfo.cpu}核  mem:${machineInfo.mem}G</label>
									</div>
								</div>

								<div class="form-group">
									<label class="control-label col-md-3">
										<font color="red">*</font>选择实例：
									</label>
									<div class="col-md-5">
										<c:set var="instanceMemoryUsed" value="0"/>
										<c:forEach items="${instanceList}" var="instance">
											<c:if test="${instance.type==2}">
												<c:set var="instanceStatsMapKey" value="${instance.ip}:${instance.port}"></c:set>
												<c:set var="instanceMemoryUsed" value="${(instanceStatsMap[instanceStatsMapKey]).usedMemory / 1024 / 1024 / 1024 + instanceMemoryUsed}"/>
											</c:if>
										</c:forEach>

										<select id="selectInstance" class="selectpicker bla bla bli col-md-6" multiple data-live-search="true" onchange="instanceChange()">
											<option value="-1" selected="true" memoryUsed="${instanceMemoryUsed}">全部cluster实例(默认)</option>
											<c:forEach items="${instanceList}" var="instance">
												<c:set var="instanceStatsMapKey" value="${instance.ip}:${instance.port}"></c:set>
												<c:if test="${instance.type==2}">
													<option value="${instance.id}" memoryUsed="${(instanceStatsMap[instanceStatsMapKey]).usedMemory / 1024 / 1024 / 1024}"> 【${instance.id}】 ${instance.ip}:${instance.port}
													（Used:<fmt:formatNumber value="${(instanceStatsMap[instanceStatsMapKey]).usedMemory / 1024 / 1024 / 1024}" pattern="0.00"/>G/
													<fmt:formatNumber value="${(instanceStatsMap[instanceStatsMapKey]).maxMemory / 1024 / 1024 / 1024}" pattern="0.00"/>G)
													</option>
												</c:if>
											</c:forEach>
										</select>
									</div>
									<label class="control-label col-md-4" id="needMM" value="0">
									</label>
								</div>

								<div id="redisMachines" class="form-group" >
									<label class="control-label col-md-3">
										<font color="red">*</font>目标机器ip：
									</label>
									<div class="col-md-5">
										<select id="targetContainer" class="selectpicker bla bla bli col-md-6" data-live-search="true" onchange="containerChange()">
											<c:forEach items="${machineList}" var="machine">
												<c:if test="${machine.info.type==0}">
													<fmt:formatNumber var="usedCpu" value="${machineInstanceCountMap[machine.info.ip]}" pattern="0"/>
													<fmt:formatNumber var="cpu" value="${machine.info.cpu}" pattern="0"/>
													<fmt:formatNumber var="cpuUsage" value="${usedCpu/cpu*100}" pattern="0"/>
													<fmt:formatNumber var="usedMemRss" value="${((machine.machineMemInfo.usedMemRss)/1024/1024/1024)}" pattern="0.0"/>
													<fmt:formatNumber var="mem" value="${ machine.info.mem}" pattern="0.0"/>
													<fmt:formatNumber var="memUsage" value="${usedMemRss/mem*100}" pattern="0"/>

													<c:if test="${machine.info.useType==0}">
														<option value="${machine.ip}" memoryUsed="${mem-usedMemRss}">${machine.ip}：${usedCpu}/${cpu}核(${cpuUsage}%) ${usedMemRss}/${mem}G(${memUsage}%) 【${machine.info.realIp}-${machine.info.rack}】</option>
													</c:if>
													<c:if test="${machine.info.useType==1}">
														<option value="${machine.ip}" memoryUsed="${mem-usedMemRss}">${machine.ip}：${usedCpu}/${cpu}核(${cpuUsage}%) ${usedMemRss}/${mem}G(${memUsage}%) 【${machine.info.realIp}-${machine.info.rack}】</option>
													</c:if>
													<c:if test="${machine.info.useType==2}">
														<option value="${machine.ip}" memoryUsed="${mem-usedMemRss}">${machine.ip}：${usedCpu}/${cpu}核(${cpuUsage}%) ${usedMemRss}/${mem}G(${memUsage}%) 【${machine.info.realIp}-${machine.info.rack}】</option>
													</c:if>
												</c:if>
											</c:forEach>
											</optgroup>
										</select>
									</div>
									<label class="control-label col-md-4" id="containerMM" value="0">
									</label>
								</div>

							</div>
							<!-- form-body 结束 -->
						</div>
						<div id="machineInfo${machine.info.id}"></div>
						<!-- 控件结束 -->
					</div>
				</div>

				<div class="modal-footer">
					<button type="button" id="migrateInstanceBtn"  data-dismiss="modal" class="btn" >Close</button>
					<button type="button" class="btn red" onclick="migrateInstance('${machineInfo.ip}')">Ok</button>
				</div>

			</form>
		</div>
	</div>
</div>