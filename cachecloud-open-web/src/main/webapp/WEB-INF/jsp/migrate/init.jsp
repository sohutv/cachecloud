<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp"%>

<!DOCTYPE html>
<html lang="en">
<head>
    <title>迁移数据</title>
    <jsp:include page="/WEB-INF/include/head.jsp"/>
    <script type="text/javascript" src="/resources/js/myPopover.js"></script>
    <script type="text/javascript">
    
    	function changeDataType(appIdId,serversId, choose) {
    		var dataType = choose.options[choose.selectedIndex].value;
    		var appId = document.getElementById(appIdId);
    		var servers = document.getElementById(serversId);
    		if (dataType == 0) {
    			appId.disabled = true;
    			servers.disabled = false;
    		} else if(dataType == 1) {
    			appId.disabled = false;
    			servers.disabled = true;
    		}
    	}
    	
    	function fillAppInstanceList(instanceDetailId,redisMigrateIndexId,appIdInputId) {
    		var appId = document.getElementById(appIdInputId).value;
    		if (appId == "") {
    			//不能为空
    			return;
    		}
    		var instanceDetail = document.getElementById(instanceDetailId);
    		$.get(
       			'/data/migrate/appInstanceList.json',
       			{
       				appId: appId,
       			},
       	        function(data){
       				var instances = data.instances;
       				instanceDetail.value = instances;
       				
       				var appType = data.appType;
       				var redisMigrateIndex = document.getElementById(redisMigrateIndexId);
       				//修改select
       				if (appType == 2) {
       					var options = redisMigrateIndex.options;
       					for(var i = 0;i < options.length; i++){
       						if (1 == options[i].value){
       							options[i].selected = 'selected';
       							break;
       						}
       					}
       				} else if(appType == 5 || appType == 6) {
       					var options = redisMigrateIndex.options;
       					for(var i = 0;i < options.length; i++){
       						if (0 == options[i].value){
       							options[i].selected = 'selected';
       							break;
       						}
       					}
       				}
       				
       	        }
       	     );
    		
    	}
    
    	function checkMigrateFormat() {
    		var sourceRedisMigrateIndex = document.getElementById("sourceRedisMigrateIndex").value;
    		var targetRedisMigrateIndex = document.getElementById("targetRedisMigrateIndex").value;
    		var sourceServers = document.getElementById("sourceServers");
    		var sourceAppId = document.getElementById("sourceAppId");
    		var sourceDataType = document.getElementById("sourceDataType").value;
    		var migrateMachineIp = document.getElementById("migrateMachineIp").value;
    		var redisSourcePass = document.getElementById("redisSourcePass");
    		var redisTargetPass = document.getElementById("redisTargetPass");

    		
			//非cachecloud
    		if (sourceDataType == 0 && sourceServers.value == "") {
   				alert("源实例信息不能为空!");
   				sourceServers.focus();
   				return false;
    		//cachecloud
    		} else if(sourceDataType == 1 && sourceAppId.value == "") {
   				alert("源appId不能为空!");
   				sourceAppId.focus();
   				return false;
    		} else if(sourceDataType == 1 && sourceServers.value == "") {
   				alert("请确保appId=" + sourceAppId.value + "下有实例信息");
   				sourceAppId.focus();
   				return false;
    		}
    		
    		var targetAppId = document.getElementById("targetAppId");
    		var targetServers = document.getElementById("targetServers");
    		var targetDataType = document.getElementById("targetDataType").value;
			//非cachecloud
    		if(targetDataType == 1 && targetAppId.value == "") {
   				alert("目标appId不能为空!");
   				targetAppId.focus();
   				return false;
    		} else if(targetDataType == 1 && targetServers.value == "") {
   				alert("请确保appId=" + targetAppId.value + "下有实例信息");
   				targetAppId.focus();
   				return false;
    		}
    		
    		$.get(
    			'/data/migrate/check.json',
    			{
    				sourceRedisMigrateIndex: sourceRedisMigrateIndex,
    				targetRedisMigrateIndex: targetRedisMigrateIndex,
    				sourceServers:sourceServers.value,
    				targetServers:targetServers.value,
    				migrateMachineIp:migrateMachineIp,
    				redisSourcePass:redisSourcePass.value,
    				redisTargetPass:redisTargetPass.value
    			},
    	        function(data){
    				var status = data.status;
    				alert(data.message);
    				if (status == 1) {
    					var submitButton = document.getElementById("submitButton");
    		    		submitButton.disabled = false;
    		    		
    		    		var checkButton = document.getElementById("checkButton");
    		    		checkButton.disabled = true;
    				}
    	        }
    	     );
    	}
    	
    	function startMigrate() {
    		var sourceRedisMigrateIndex = document.getElementById("sourceRedisMigrateIndex").value;
    		var targetRedisMigrateIndex = document.getElementById("targetRedisMigrateIndex").value;
    		var sourceServers = document.getElementById("sourceServers");
    		var targetServers = document.getElementById("targetServers");
    		var migrateMachineIp = document.getElementById("migrateMachineIp").value;
    		var sourceAppId = document.getElementById("sourceAppId");
    		var targetAppId = document.getElementById("targetAppId");
    		var redisSourcePass = document.getElementById("redisSourcePass");
    		var redisTargetPass = document.getElementById("redisTargetPass");

    		$.get(
    			'/data/migrate/start.json',
    			{
    				sourceRedisMigrateIndex: sourceRedisMigrateIndex,
    				targetRedisMigrateIndex: targetRedisMigrateIndex,
    				sourceServers: sourceServers.value,
    				targetServers: targetServers.value,
    				migrateMachineIp: migrateMachineIp,
    				sourceAppId: sourceAppId.value,
    				targetAppId: targetAppId.value,
    				redisSourcePass: redisSourcePass.value,
    				redisTargetPass:redisTargetPass.value
    			},
    	        function(data){
    				var status = data.status;
    				if (status == 1) {
    					alert("迁移程序已经启动，请返回迁移列表关注迁移进度!");
    					location.href = "/data/migrate/list";
    				} else {
    					alert("迁移失败,请查看日志分析原因!");
    				}
		    		
		    		var checkButton = document.getElementById("checkButton");
		    		checkButton.disabled = true;
    	        }
    	     );
    	}
    	
    </script>
    
</head>
<body role="document">
	<div class="container">
	    <jsp:include page="/WEB-INF/include/headMenu.jsp"/>
	    <div id="systemAlert">
	    </div>
		<div class="page-content">
			<div class="portlet box light-grey">
				<div class="portlet-body">
					<div class="form">
						<form action="" method="post"
							class="form-horizontal form-bordered form-row-stripped">
							<div class="form-body">
								<div class="row">
									<div class="col-md-12">
										<h4 class="page-header">
											迁移工具配置
											<button class="btn btn-success btn-sm" 
										      data-container="body" data-toggle="popover" data-placement="top" 
										      data-content="<a href='http://cachecloud.github.io/2016/06/28/1.2.%20%E8%BF%81%E7%A7%BB%E5%B7%A5%E5%85%B7%E4%BD%BF%E7%94%A8%E8%AF%B4%E6%98%8E/'>使用文档</a>" style="border-radius:100%">
										      ?
										   </button>
										</h4>
									</div>
								</div>
								<div class="row">
									<div class="col-md-12">
										<div class="col-md-6">
											<div class="form-group">
												<label class="control-label col-md-3">
													迁移工具机器:
												</label>
												<div class="col-md-5">
													<select id="migrateMachineIp" name="migrateMachineIp" class="form-control select2_category">
														<c:forEach items="${machineInfoList}" var="machineInfo">
															<option value="${machineInfo.ip}">
	                                                            ${machineInfo.ip}
															</option>
														</c:forEach>
													</select>
												</div>
											</div>
										</div>
									</div>
								</div>
							
								<div class="row">
									<div class="col-md-12">
										<h4 class="page-header">
											源和目标配置
										</h4>
									</div>
								</div>
							
								<div class="row">
									<div class="col-md-12">
										<div class="col-md-6">
											<div class="form-group">
												<label class="control-label col-md-3">
													源类型:
												</label>
												<div class="col-md-5">
													<select id="sourceRedisMigrateIndex" name="sourceRedisMigrateIndex" class="form-control select2_category">
														<option value="0">
															Redis普通节点
														</option>
														<option value="1">
															Redis-cluster
														</option>
														<option value="2">
                                                            RDB-file
														</option>
														<option value="4">
                                                            AOF-file
														</option>
													</select>
												</div>
											</div>
										</div>
										
										<div class="col-md-6">
											<div class="form-group">
												<label class="control-label col-md-3">
													目标类型:
												</label>
												<div class="col-md-5">
													<select id="targetRedisMigrateIndex" name="targetRedisMigrateIndex" class="form-control select2_category">
														<option value="0">
															Redis普通节点
														</option>
														<option value="1">
															Redis-cluster
														</option>
														<option value="2">
                                                            RDB-file
														</option>
													</select>
												</div>
											</div>
										</div>
									</div>
								</div>
								
								<div class="row">
									<div class="col-md-12">
										<div class="col-md-6">
											<div class="form-group">
												<label class="control-label col-md-3">
													数据源:
												</label>
												<div class="col-md-5">
													<select id="sourceDataType" name="sourceDataType" class="form-control select2_category" onchange="changeDataType('sourceAppId','sourceServers',this)">
														<option value="0" selected="selected">
															非cachecloud
														</option>
														<option value="1">
															cachecloud
														</option>
													</select>
												</div>
											</div>
										</div>
										
										<div class="col-md-6">
											<div class="form-group">
												<label class="control-label col-md-3">
													数据源:
												</label>
												<div class="col-md-5">
													<select id="targetDataType" name="targetDataType" class="form-control select2_category" onchange="changeDataType('targetAppId','targetServers',this)">
														<option value="1" selected="selected">
															cachecloud
														</option>
														<option value="0">
															非cachecloud
														</option>
													</select>
												</div>
											</div>
										</div>
									</div>
								</div>
								
								<div class="row">
									<div class="col-md-12">
										<div class="col-md-6">
											<div class="form-group" id="sourceAppIdDiv">
												<label class="control-label col-md-3">
													源appId:
												</label>
												<div class="col-md-5">
													<input disabled="disabled" type="text" id="sourceAppId" class="form-control" onchange="fillAppInstanceList('sourceServers', 'sourceRedisMigrateIndex',this.id)"/>
												</div>
											</div>
										</div>
										
										<div class="col-md-6">
											<div class="form-group" id="targetAppIdDiv">
												<label class="control-label col-md-3">
													目标appId:
												</label>
												<div class="col-md-5">
													<input type="text" id="targetAppId" class="form-control"  onchange="fillAppInstanceList('targetServers', 'targetRedisMigrateIndex',this.id)"/>
												</div>
											</div>
										</div>
									</div>
								</div>
								
								<div class="row">
									<div class="col-md-12">
										<div class="col-md-6">
											<div class="form-group">
												<label class="control-label col-md-3">
													源密码:
												</label>
												<div class="col-md-5">
													<input type="text" id="redisSourcePass" name="redisSourcePass" placeholder="没有无需填写" class="form-control"/>
												</div>
											</div>
										</div>
										
										<div class="col-md-6">
											<div class="form-group">
												<label class="control-label col-md-3">
													目标密码:
												</label>
												<div class="col-md-5">
													<input type="text" id="redisTargetPass" name="redisTargetPass" placeholder="没有无需填写" class="form-control"/>
												</div>
											</div>
										</div>
									</div>
								</div>
								
								<div class="row">
									<div class="col-md-12">
										<div class="col-md-6">
											<div class="form-group" id="sourceServersDiv">
												<label class="control-label col-md-3">
													源实例详情:
												</label>
												<div class="col-md-8">
													<textarea rows="10" name="sourceServers" id="sourceServers" placeholder="节点详情" class="form-control"></textarea>
													<span class="help-block">
														每行格式都是:&nbsp;&nbsp;ip:port(例如：10.10.xx.xx:6379)<br/>
														1. standalone类型：<br/> 
														&nbsp;&nbsp;&nbsp;&nbsp;masterIp:masterPort<br/>
														2. sentinel类型：<br/>
														&nbsp;&nbsp;&nbsp;&nbsp;masterIp:masterPort<br/>
														3. cluster类型：<br/>
														&nbsp;&nbsp;&nbsp;&nbsp;masterIp1:masterPort1<br/>
														&nbsp;&nbsp;&nbsp;&nbsp;slaveIp1:slavePort1<br/>
														&nbsp;&nbsp;&nbsp;&nbsp;masterIp2:masterPort2<br/>
														&nbsp;&nbsp;&nbsp;&nbsp;slaveIp2:slavePort2<br/>
														(可以是多对主从，只要把所有的cluster节点都按照格式写就可以，程序会自动判断)<br/>
													</span>
												</div>
											</div>
										</div>
									
										<div class="col-md-6">
											<div class="form-group" id="targetServersDiv">
												<label class="control-label col-md-3">
													目标实例详情:
												</label>
												<div class="col-md-8">
													<textarea disabled="disabled" rows="10" name="targetServers" id="targetServers" placeholder="节点详情" class="form-control"></textarea>
													<span class="help-block">
														每行格式都是:&nbsp;&nbsp;ip:port(例如：10.10.xx.xx:6379)<br/>
														1. standalone类型：<br/> 
														&nbsp;&nbsp;&nbsp;&nbsp;masterIp:masterPort<br/>
														2. sentinel类型：<br/>
														&nbsp;&nbsp;&nbsp;&nbsp;masterIp:masterPort<br/>
														3. cluster类型：<br/>
														&nbsp;&nbsp;&nbsp;&nbsp;masterIp1:masterPort1<br/>
														&nbsp;&nbsp;&nbsp;&nbsp;slaveIp1:slavePort1<br/>
														&nbsp;&nbsp;&nbsp;&nbsp;masterIp2:masterPort2<br/>
														&nbsp;&nbsp;&nbsp;&nbsp;slaveIp2:slavePort2<br/>
														(可以是多对主从，只要把所有的cluster节点都按照格式写就可以，程序会自动判断)<br/>
													</span>
												</div>
											</div>
										</div>
									</div>
								</div>
								
								<div class="form-actions fluid">
									<div class="row">
										<div class="col-md-12">
											<div class="col-md-offset-5 col-md-9">
												<button id="submitButton" type="button" onclick="startMigrate()" class="btn green" disabled="disabled">
													<i class="fa fa-check"></i>
													开始迁移
												</button>
												<button id="checkButton" type="button" class="btn green" onclick="checkMigrateFormat()">
													<i class="fa fa-check"></i>
													检查格式
												</button>
											</div>
										</div>
									</div>
								</div>
							</div>
						</form>
					</div>
				</div>
			</div>
		</div>
	</div>
	<br/><br/><br/><br/><br/><br/><br/>
	<jsp:include page="/WEB-INF/include/foot.jsp"/>
</body>
</html>

