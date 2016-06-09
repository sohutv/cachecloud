<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp"%>

<!DOCTYPE html>
<html lang="en">
<head>
    <title>迁移数据</title>
    <jsp:include page="/WEB-INF/include/head.jsp"/>
    <script type="text/javascript">
    
    	function changeDataType(appIdDivId,serversDivId, choose) {
    		var dataType = choose.options[choose.selectedIndex].value;
    		var appIdDiv = document.getElementById(appIdDivId);
    		var serversDiv = document.getElementById(serversDivId);
    		if (dataType == 0) {
    			appIdDiv.style.display = "none";
    			serversDiv.style.display = "";
    		} else if(dataType == 1) {
    			appIdDiv.style.display = "";
    			serversDiv.style.display = "none";
    		}
    	}
    	
    	function fillAppInstanceList(instanceDetailId, appIdInputId) {
    		var appId = document.getElementById(appIdInputId).value;
    		if (appId == "") {
    			//不能为空
    			return;
    		}
    		var instanceDetail = document.getElementById(instanceDetailId);
    		$.get(
       			'/migrate/appInstanceList.json',
       			{
       				appId: appId,
       			},
       	        function(data){
       				var instances = data.instances;
       				instanceDetail.value = instances;
       	        }
       	     );
    		
    	}
    
    	function checkMigrateFormat() {
    		var sourceRedisMigrateIndex = document.getElementById("sourceRedisMigrateIndex").value;
    		var targetRedisMigrateIndex = document.getElementById("targetRedisMigrateIndex").value;
    		
    		var sourceServers = document.getElementById("sourceServers");
    		var sourceAppId = document.getElementById("sourceAppId");
    		var sourceDataType = document.getElementById("sourceDataType").value;
    		
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
    		if (targetDataType == 0 && targetServers.value == "") {
   				alert("目标实例信息不能为空!");
   				targetServers.focus();
   				return false;
    		//cachecloud
    		} else if(targetDataType == 1 && targetAppId.value == "") {
   				alert("目标appId不能为空!");
   				targetAppId.focus();
   				return false;
    		} else if(targetDataType == 1 && targetServers.value == "") {
   				alert("请确保appId=" + targetAppId.value + "下有实例信息");
   				targetAppId.focus();
   				return false;
    		}
    		
    		$.get(
    			'/migrate/check.json',
    			{
    				sourceRedisMigrateIndex: sourceRedisMigrateIndex,
    				targetRedisMigrateIndex: targetRedisMigrateIndex,
    				sourceServers:sourceServers.value,
    				targetServers:targetServers.value,
    				migrateMachineIp:'10.10.53.159'
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
    		$.get(
    			'/migrate/start.json',
    			{
    				sourceRedisMigrateIndex: sourceRedisMigrateIndex,
    				targetRedisMigrateIndex: targetRedisMigrateIndex,
    				sourceServers:sourceServers.value,
    				targetServers:targetServers.value,
    				migrateMachineIp:'10.10.53.159'
    			},
    	        function(data){
    				var status = data.status;
    				if (status == 1) {
    					alert("迁移程序已经启动，请返回迁移列表关注迁移进度!");
    					//location.href = "/admin/app/list";
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
			<div class="row">
				<div class="col-md-12">
					<h3 class="page-header">
						迁移数据
					</h3>
				</div>
			</div>
			<div class="portlet box light-grey">
				<div class="portlet-body">
					<div class="form">
						<form action="/import/app/add" method="post"
							class="form-horizontal form-bordered form-row-stripped">
							<div class="form-body">
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
													<select id="sourceDataType" name="sourceDataType" class="form-control select2_category" onchange="changeDataType('sourceAppIdDiv','sourceServersDiv',this)">
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
													<select id="targetDataType" name="targetDataType" class="form-control select2_category" onchange="changeDataType('targetAppIdDiv','targetServersDiv',this)">
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
											<div class="form-group" id="sourceAppIdDiv" style="display:none">
												<label class="control-label col-md-3">
													源appId:
												</label>
												<div class="col-md-5">
													<input type="text" id="sourceAppId" class="form-control" onchange="fillAppInstanceList('sourceServers', this.id)"/>
												</div>
											</div>
										</div>
										
										<div class="col-md-6">
											<div class="form-group" id="targetAppIdDiv">
												<label class="control-label col-md-3">
													目标appId:
												</label>
												<div class="col-md-5">
													<input type="text" id="targetAppId" class="form-control"  onchange="fillAppInstanceList('targetServers', this.id)"/>
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
											<div class="form-group" id="targetServersDiv" style="display:none">
												<label class="control-label col-md-3">
													目标实例详情:
												</label>
												<div class="col-md-8">
													<textarea rows="10" name="targetServers" id="targetServers" placeholder="节点详情" class="form-control"></textarea>
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

