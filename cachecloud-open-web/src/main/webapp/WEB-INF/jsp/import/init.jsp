<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp"%>

<!DOCTYPE html>
<html lang="en">
<head>
    <title>应用导入</title>
    <jsp:include page="/WEB-INF/include/head.jsp"/>
    <script type="text/javascript">
    	function checkAppInstanceFormat() {
    		//应用名
    		var appName = document.getElementById("appName");
    		if(appName.value == ""){
    			alert("应用名不能为空");
    			appName.focus();
    			return false;
    		}
    		
    		//应用描述
    		var appIntro = document.getElementById("appIntro");
    		if(appIntro.value == ""){
    			alert("应用描述不能为空");
    			appIntro.focus();
    			return false;
    		}
    		
    		//项目负责人
    		var officer = document.getElementById("officer");
    		if(officer.value == ""){
    			alert("项目负责人不能为空");
    			officer.focus();
    			return false;
    		}
    		
    		//内存报警阀值
    		var memAlertValue = document.getElementById("memAlertValue");
    		if(memAlertValue.value == ""){
    			alert("内存报警阀值不能为空");
    			memAlertValue.focus();
    			return false;
    		}
    		
    		var appInstanceInfo = document.getElementById("appInstanceInfo");
    		if(appInstanceInfo.value == ""){
    			alert("实例详情不能为空");
    			appInstanceInfo.focus();
    			return false;
    		}
    		var password = document.getElementById("password");
    		
    		$.post(
    			'/import/app/check.json',
    			{
    				name: appName.value,
    				password: password.value,
    				appInstanceInfo: appInstanceInfo.value
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
    	
    	function importApp() {
    		//应用名
    		var appName = document.getElementById("appName").value;
    		//应用描述
    		var appIntro = document.getElementById("appIntro").value;
    		//项目负责人
    		var officer = document.getElementById("officer").value;
    		//内存报警阀值
    		var memAlertValue = document.getElementById("memAlertValue").value;
    		//redis密码
    		var password = document.getElementById("password").value;
    		//实例详情
    		var appInstanceInfo = document.getElementById("appInstanceInfo").value;
    		//应用类型
    		var appType = document.getElementById("appType").value;
    		//是否测试
    		var isTest = document.getElementById("isTest").value;
    		
    		var submitButton = document.getElementById("submitButton");
    		submitButton.disabled = true;
    		
    		$.post(
    			'/import/app/add.json',
    			{
    				name: appName,
    				intro: appIntro,
    				officer: officer,
    				memAlertValue: memAlertValue,
    				password: password,
    				type: appType,
    				isTest: isTest,
    				appInstanceInfo: appInstanceInfo
    			},
    	        function(data){
    				var status = data.status;
    				if (status == 1) {
    					alert("应用导入成功，请查看应用列表!");
    					location.href = "/admin/app/list";
    				} else {
    					alert("应用导入失败!");
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
						应用导入
						<font color='red' size="4">
							<c:choose>
								<c:when test="${success == 1}">(更新成功)</c:when>
							</c:choose>
						</font>
					</h3>
				</div>
			</div>
			<div class="row">
				<div class="col-md-12">
					<div class="portlet box light-grey">
						<div class="portlet-body">
							<div class="form">
									<!-- BEGIN FORM-->
									<form action="/import/app/add" method="post"
										class="form-horizontal form-bordered form-row-stripped">
										<div class="form-body">
											<div class="form-group">
												<label class="control-label col-md-3">
													应用名称<font color='red'>(*)</font>:
												</label>
												<div class="col-md-5">
													<input type="text" name="name" id="appName"
														class="form-control" onchange="checkAppNameExist()"/>
												</div>
											</div>
											
											<div class="form-group">
												<label class="control-label col-md-3">
													应用描述<font color='red'>(*)</font>:
												</label>
												<div class="col-md-5">
													<textarea class="form-control" name="intro" 
														rows="3" id="appIntro" placeholder="应用描述"></textarea>
													<span class="help-block">
														应用描述（必填，不超过128个字符，可以包含中文）
													</span>
												</div>
											</div>
											
											<div class="form-group">
												<label class="control-label col-md-3">
													存储种类:
												</label>
												<div class="col-md-5">
													<select id="appType" name="type" class="form-control select2_category">
														<option value="2">
															Redis-cluster
														</option>
														<option value="5">
															Redis-sentinel
														</option>
														<option value="6">
                                                            Redis-standalone
														</option>
													</select>
												</div>
											</div>
											
											<div class="form-group">
												<label class="control-label col-md-3">
													项目负责人<font color='red'>(*)</font>:
												</label>
												<div class="col-md-5">
													<input type="text" name="officer" id="officer" placeholder="项目负责人(中文必填)"
														class="form-control" />
												</div>
											</div>
											
											<div class="form-group">
												<label class="control-label col-md-3">
													测试:
												</label>
												<div class="col-md-5">
													<select id="isTest" name="isTest" class="form-control">
														<option value="0">
															否
														</option>
														<option value="1">
															是
														</option>
													</select>
												</div>
											</div>
											
											<div class="form-group">
												<label class="control-label col-md-3">
													redis密码:
												</label>
												<div class="col-md-5">
													<input type="text" name="password" id="password" placeholder="redis密码" class="form-control"/>
													<span class="help-block">
														redis密码，如果没有则为空
													</span>
												</div>
											</div>
											
											<div class="form-group">
												<label class="control-label col-md-3">
													内存报警阀值<font color='red'>(*)</font>:
												</label>
												<div class="col-md-5">
													<input type="text" name="memAlertValue" id="memAlertValue" placeholder="内存报警阀值" class="form-control" onchange="testisNum(this.id)"/>
													<span class="help-block">
														例如内存使用率超过90%就报警，请填写90(<font color="red">如果不需要报警请填写100以上的数字</font>)
													</span>
												</div>
											</div>
											
											<div class="form-group">
												<label class="control-label col-md-3">
													实例详情:<font color='red'>(*)</font>:
												</label>
												<div class="col-md-5">
													<textarea rows="10" name="appInstanceInfo" id="appInstanceInfo" placeholder="节点详情" class="form-control"></textarea>
													<span class="help-block">
														每行格式都是:&nbsp;&nbsp;ip:port:maxMemory(单位:M)或者masterName}<br/>
														1. standalone类型：<br/> 
														&nbsp;&nbsp;&nbsp;&nbsp;masterIp:masterPort:maxMemory(例如：10.10.xx.xx:6379:2048)<br/>
														2. sentinel类型：<br/>
														&nbsp;&nbsp;&nbsp;&nbsp;masterIp:masterPort:maxMemory<br/>
														&nbsp;&nbsp;&nbsp;&nbsp;slaveIp:slavePort:maxMemory<br/>
														(可以是多个slave)<br/>
														&nbsp;&nbsp;&nbsp;&nbsp;sentinelIp1:sentinelPor1:masterName<br/>
														&nbsp;&nbsp;&nbsp;&nbsp;sentinelIp2:sentinelPor2:masterName<br/>
														&nbsp;&nbsp;&nbsp;&nbsp;sentinelIp3:sentinelPor3:masterName<br/>
														(可以是多个sentinel)<br/>
														3. cluster类型：<br/>
														&nbsp;&nbsp;&nbsp;&nbsp;masterIp1:masterPort1:maxMemory1<br/>
														&nbsp;&nbsp;&nbsp;&nbsp;slaveIp1:slavePort1:maxMemory1<br/>
														&nbsp;&nbsp;&nbsp;&nbsp;masterIp2:masterPort2:maxMemory2<br/>
														&nbsp;&nbsp;&nbsp;&nbsp;slaveIp2:slavePort2:maxMemory2<br/>
														(可以是多对主从，只要把所有的cluster节点都按照格式写就可以，程序会自动判断)<br/>
													</span>
												</div>
											</div>
											
											<input name="userId" id="userId" value="${userInfo.id}" type="hidden" />
											<input id="appExist" value="0" type="hidden" />
											
											<div class="form-actions fluid">
												<div class="row">
													<div class="col-md-12">
														<div class="col-md-offset-3 col-md-9">
															<button id="submitButton" type="button" onclick="importApp()" class="btn green" disabled="disabled">
																<i class="fa fa-check"></i>
																开始导入
															</button>
															<button id="checkButton" type="button" class="btn green" onclick="checkAppInstanceFormat()">
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
					<!-- END TABLE PORTLET-->
				</div>
			</div>
		</div>
	</div>
	<br/><br/><br/><br/><br/><br/><br/>
	<jsp:include page="/WEB-INF/include/foot.jsp"/>
</body>
</html>

