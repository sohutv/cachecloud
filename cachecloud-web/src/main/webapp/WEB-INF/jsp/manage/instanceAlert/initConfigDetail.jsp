<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="com.sohu.cache.redis.enums.RedisAlertConfigEnum"%>
<%@page import="com.sohu.cache.redis.enums.InstanceAlertCompareTypeEnum"%>
<%@page import="com.sohu.cache.redis.enums.InstanceAlertCheckCycleEnum"%>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp"%>

<script type="text/javascript">

//查看实例是否存在
function checkInstanceExist(){
	var instanceHostPort = document.getElementById("instanceHostPort").value;
	if (instanceHostPort == null || instanceHostPort == ""){
		alert("请填写实例ip:port");
		instanceHostPort.focus();
		return false;
	}
	if(instanceHostPort != ''){
		$.post(
			'/manage/instanceAlert/checkInstanceHostPort.json',
			{
				instanceHostPort: instanceHostPort,
			},
	        function(data){
				var success = data.status;
	            if(success==0){
	            		alert(data.message);
	            		document.getElementById("instanceHostPort").focus();
	            }
	        }
	     );
	}
}

function removeAlertConfig(id) {
	$.get(
		'/manage/instanceAlert/remove.json',
		{
			id: id
		},
        function(data){
			var status = data.status;
			if (status == 1) {
           		alert("删除成功!");
			} else {
           		alert("删除失败, msg: " + result.message);
			}
               window.location.reload();
        }
     );
}

function changeAlertConfig(id) {
	var alertValue = document.getElementById("alertValue" + id);
	var checkCycle = document.getElementById("checkCycle" + id);
	var compareType = document.getElementById("compareType" + id);
	var importantLevel = document.getElementById("importantLevel" + id);
	$.get(
		'/manage/instanceAlert/update.json',
		{
			id: id,
			alertValue: alertValue.value,
			checkCycle: checkCycle.value,
			compareType: compareType.value,
			importantLevel: importantLevel.value
		},
        function(data){
			var status = data.status;
			if (status == 1) {
				alert("修改成功！");
                window.location.reload();
			} else {
				alert("修改失败！" + data.message);
			}
			
        }
     );
}

function saveInstanceAlertConfig() {
	var alertConfig = document.getElementById("alertConfig");
	var alertValue = document.getElementById("alertValue");
	if (alertValue.value == ""){
		alert("请填写阈值");
		alertValue.focus();
		return false;
	}
	var compareType = document.getElementById("compareType");
	var checkCycle = document.getElementById("checkCycle");
	var instanceHostPort = document.getElementById("instanceHostPort");
	if (instanceHostPort.value == null || instanceHostPort.value == ""){
		alert("请填写实例ip:port");
		instanceHostPort.focus();
		return false;
	}
	var type = 1;
	if (instanceHostPort.value != null && instanceHostPort.value != '') {
		type = 2;
	}
	$.get(
		'/manage/instanceAlert/add.json',
		{
			alertConfig: alertConfig.value,
			alertValue: alertValue.value,
			configInfo: alertConfig.options[alertConfig.selectedIndex].text,
			compareType: compareType.value,
			checkCycle: checkCycle.value,
			instanceHostPort: instanceHostPort.value,
			type: type
		},
        function(data){
			var status = data.status;
			if (status == 1) {
				alert("添加成功！");
			} else {
				alert("添加失败！" + data.message);
			}
            window.location.reload();
        }
     );
}

//保存全局实例
function saveGlobalInstanceAlertConfig() {
	var alertConfig = document.getElementById("alertConfigGlobal");
	if (alertConfig.value == ""){
		alert("请填写配置名");
		alertConfig.focus();
		return false;
	}
	var alertValue = document.getElementById("alertValueGlobal");
	if (alertValue.value == ""){
		alert("请填写阈值");
		alertValue.focus();
		return false;
	}
	var configInfo = document.getElementById("configInfoGlobal");
	if (configInfo.value == ""){
		alert("请填写配置说明");
		configInfo.focus();
		return false;
	}
	var compareType = document.getElementById("compareTypeGlobal");
	var checkCycle = document.getElementById("checkCycleGlobal");
	var importantLevel = document.getElementById("importantLevel");
	var type = 1;
	$.get(
			'/manage/instanceAlert/add.json',
			{
				alertConfig: alertConfig.value,
				configInfo: configInfo.value,
				alertValue: alertValue.value,
				compareType: compareType.value,
				checkCycle: checkCycle.value,
				importantLevel: importantLevel.value,
				type: type
			},
			function(data){
				var status = data.status;
				if (status == 1) {
					alert("添加成功！");
				} else {
					alert("添加失败！" + data.message);
				}
				window.location.reload();
			}
	);
}

// 应用添加报警
function saveAppAlertConfig() {
    var alertConfig = document.getElementById("alertAppConfig");
    var alertValue = document.getElementById("alertAppValue");
    if (alertValue.value == ""){
        alert("请填写阈值");
        alertValue.focus();
        return false;
    }
    var compareType = document.getElementById("compareAppType");
    var checkCycle = document.getElementById("checkAppCycle");
    var appid = document.getElementById("appid");
	if (appid.value == ""){
		alert("请填写appid");
		appid.focus();
		return false;
	}

    $.get(
        '/manage/instanceAlert/addApp.json',
        {
            alertConfig: alertConfig.value,
            alertValue: alertValue.value,
			configInfo: alertConfig.options[alertConfig.selectedIndex].text,
			compareType: compareType.value,
            checkCycle: checkCycle.value,
            appid: appid.value
        },
        function(data){
            var status = data.status;
            if (status == 1) {
                alert("添加成功！");
            } else {
                alert("添加失败！" + data.message);
            }
            window.location.reload();
        }
    );
}
</script>

<div class="page-container">
	<div class="page-content">
		<div class="table-toolbar">
		</div>

		<div class="row">
			<div class="col-md-12">
				<div class="portlet box light-grey">
						<div class="portlet-title">
							<div class="caption">
								<i class="fa fa-globe"></i>
								全局实例报警项:
								&nbsp;
							</div>
							<div class="tools">
								<button type="button" class="btn btn-success" style="margin-top: 0px" data-target="#addGlobalInstanceAlertModal" data-toggle="modal" href="#">添加全局报警项</button>
								<a href="javascript:;" class="collapse"></a>
							</div>
						</div>
						
						<div class="portlet-body">
	                        <div class="table-toolbar">
								<table class="table table-striped table-bordered table-hover" id="tableDataList">
									<thead>
										<tr>
											<th>id</th>
											<th>配置名</th>
											<th>配置说明</th>
											<th>关系</th>
											<th>阀值</th>
											<th>周期</th>
											<th>重要程度</th>
											<th>最近检测时间</th>
											<th>操作</th>
										</tr>
									</thead>
									<tbody>
										<c:forEach items="${instanceAlertAllList}" var="config">
											<tr class="odd gradeX">
												<td>
													${config.id}
												</td>
												<td>
													${config.alertConfig}
												</td>
												<td>
													${config.configInfo}
												</td>
												<td>
													<c:forEach items="${instanceAlertCompareTypeEnumList}" var="instanceAlertCompareTypeEnum">
														<c:if test="${config.compareType == instanceAlertCompareTypeEnum.value}">${instanceAlertCompareTypeEnum.info}</c:if>
													</c:forEach>
												</td>
												<td>
													${config.alertValue}
												</td>				
												<td>
													<c:forEach items="${instanceAlertCheckCycleEnumList}" var="instanceAlertCheckCycleEnum">
														<c:if test="${config.checkCycle == instanceAlertCheckCycleEnum.value}">${instanceAlertCheckCycleEnum.info}</c:if>
													</c:forEach>
												</td>

												<td>
													<c:if test="${config.importantLevel == 0}">一般</c:if>
													<c:if test="${config.importantLevel == 1}">重要</c:if>
													<c:if test="${config.importantLevel == 2}">紧急</c:if>
												</td>

												<td>
		                    							<fmt:formatDate value="${config.lastCheckTime}" pattern="yyyy-MM-dd HH:mm:ss"/>
												</td>
												<td>
													<button type="button" class="btn btn-info" data-target="#changeInstanceAlertModal${config.id}" data-toggle="modal" href="#">修改</button>
			                    						<button type="button" class="btn btn-info" onclick="if(window.confirm('确认要清除id=${config.id}的配置?!')){removeAlertConfig('${config.id}');return true;}else{return false;}">删除</button>
												</td>
											</tr>
										</c:forEach>
									</tbody>
								</table>
							</div>
						</div>
					</div>
					<!-- END TABLE PORTLET-->
			</div>
		</div>
		
		<div class="row">
			<div class="col-md-12">
				<div class="portlet box light-grey">
						<div class="portlet-title">
							<div class="caption">
								<i class="fa fa-globe"></i>
								特殊实例报警:
								&nbsp;
							</div>
							<div class="tools">
								<div class="btn-group">
									<button type="button" class="btn btn-success" style="margin-top: 0px" data-target="#addAppAlertModal" data-toggle="modal" href="#">添加应用报警项</button>
								</div>
								<div class="btn-group">
									<button type="button" class="btn btn-success" style="margin-top: 0px" data-target="#addInstanceAlertModal" data-toggle="modal" href="#">添加新实例报警项</button>
								</div>
								<a href="javascript:;" class="collapse"></a>
							</div>
						</div>
						
						<div class="portlet-body">
	                        <div class="table-toolbar">
								<table class="table table-striped table-bordered table-hover" id="tableDataList">
									<thead>
										<tr>
											<th>id</th>
											<th>实例信息</th>
											<th>配置名</th>
											<th>配置说明</th>
											<th>关系</th>
											<th>阀值</th>
											<th>周期</th>
											<th>最近检测时间</th>
											<th>操作</th>
										</tr>
									</thead>
									<tbody>
										<c:forEach items="${instanceAlertSpecialList}" var="config">
											<tr class="odd gradeX">
												<td>
													${config.id}
												</td>
		                							<c:set var="instanceId" value="${config.instanceId}"/>
												<td>
													<c:if test="${config.type == 2}">
														${config.instanceInfo.hostPort}
														<a target="_blank" href="/admin/app/index?appId=${config.instanceInfo.appId}">(${config.instanceInfo.appId})</a>
													</c:if>
													<c:if test="${config.type == 3}">
														<a target="_blank" href="/admin/app/index?appId=${config.instanceId}">${config.instanceId}</a>
													</c:if>
												</td>
												<td>
													${config.alertConfig}
												</td>
												<td>
													${config.configInfo}
												</td>
												<td>
													<c:forEach items="${instanceAlertCompareTypeEnumList}" var="instanceAlertCompareTypeEnum">
														<c:if test="${config.compareType == instanceAlertCompareTypeEnum.value}">${instanceAlertCompareTypeEnum.info}</c:if>
													</c:forEach>
												</td>
												<td>
													${config.alertValue}
												</td>
												<td>
													<c:forEach items="${instanceAlertCheckCycleEnumList}" var="instanceAlertCheckCycleEnum">
														<c:if test="${config.checkCycle == instanceAlertCheckCycleEnum.value}">${instanceAlertCheckCycleEnum.info}</c:if>
													</c:forEach>
												</td>
												<td>
		                    							<fmt:formatDate value="${config.lastCheckTime}" pattern="yyyy-MM-dd HH:mm:ss"/>
												</td>
												<td>
													<button type="button" class="btn btn-info" data-target="#changeInstanceAlertModal${config.id}" data-toggle="modal" href="#">修改</button>
			                    						<button type="button" class="btn btn-info" onclick="if(window.confirm('确认要清除id=${config.id}的配置?!')){removeAlertConfig('${config.id}');return true;}else{return false;}">删除</button>
												</td>
											</tr>
										</c:forEach>
									</tbody>
								</table>
							</div>
						</div>
					</div>
					<!-- END TABLE PORTLET-->
			</div>
		</div>
		
	</div>
</div>

<!-- 单实例报警项 -->
<div id="addInstanceAlertModal" class="modal fade" tabindex="-1" data-width="400">
	<div class="modal-dialog">
		<div class="modal-content">
		
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal" aria-hidden="true"></button>
				<h4 class="modal-title">添加实例报警项</h4>
			</div>
			
			<form class="form-horizontal form-bordered form-row-stripped">
				<div class="modal-body">
					<div class="row">
						<!-- 控件开始 -->
						<div class="col-md-12">
							<!-- form-body开始 -->
							<div class="form-body">
								<div class="form-group">
									<label class="control-label col-md-3">
										配置名:
									</label>
									<div class="col-md-5">
										<select name="alertConfig" id="alertConfig" class="form-control select2_category">
											<c:forEach items="${redisUsedGlobalAlertConfigList}" var="redisAlertConfig">
												<option value="${redisAlertConfig.value}">
													${redisAlertConfig.info}
												</option>
											</c:forEach>
										</select>
									</div>
								</div>
								
								<div class="form-group">
									<label class="control-label col-md-3">
										比较:
									</label>
									<div class="col-md-5">
										<select name="compareType" id="compareType" class="form-control select2_category">
											<c:forEach items="${instanceAlertCompareTypeEnumList}" var="instanceAlertCompareTypeEnum">
												<option value="${instanceAlertCompareTypeEnum.value}">
													${instanceAlertCompareTypeEnum.info}
												</option>
											</c:forEach>
										</select>
									</div>
								</div>
								
								
								<div class="form-group">
									<label class="control-label col-md-3">
										阀值:
									</label>
									<div class="col-md-5">
										<input type="text" name="alertValue" id="alertValue"
											class="form-control" />
									</div>
								</div>
								
								<div class="form-group">
									<label class="control-label col-md-3">
										实例:
									</label>
									<div class="col-md-5">
										<input type="text" name="instanceHostPort" id="instanceHostPort"
											class="form-control" placeholder="单个实例ip:port" onchange="checkInstanceExist()"/>
									</div>
								</div>
								
								<div class="form-group">
									<label class="control-label col-md-3">
										周期:
									</label>
									<div class="col-md-5">
										<select name="checkCycle" id="checkCycle" class="form-control select2_category">
											<c:forEach items="${instanceAlertCheckCycleEnumList}" var="instanceAlertCheckCycleEnum">
												<option value="${instanceAlertCheckCycleEnum.value}">
													${instanceAlertCheckCycleEnum.info}
												</option>
											</c:forEach>
										</select>
									</div>
								</div>
							</div>
							<!-- form-body 结束 -->
						</div>
					</div>
				</div>
				
				<div class="modal-footer">
					<button type="button" data-dismiss="modal" class="btn" >Close</button>
					<button type="button" id="configBtn" class="btn red" onclick="saveInstanceAlertConfig()">Ok</button>
				</div>
			</form>
		</div>
	</div>
</div>

<!-- 全局实例报警项 -->
<div id="addGlobalInstanceAlertModal" class="modal fade" tabindex="-1" data-width="400">
	<div class="modal-dialog">
		<div class="modal-content">

			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal" aria-hidden="true"></button>
				<h4 class="modal-title">添加全局实例报警项</h4>
			</div>

			<form class="form-horizontal form-bordered form-row-stripped">
				<div class="modal-body">
					<div class="row">
						<!-- 控件开始 -->
						<div class="col-md-12">
							<!-- form-body开始 -->
							<div class="form-body">
								<div class="form-group">
									<label class="control-label col-md-3">
										配置名:
									</label>
									<div class="col-md-5">
										<input type="text" name="alertConfig" id="alertConfigGlobal"
											   class="form-control" placeholder="参照redis info中的字段名"/>
									</div>
								</div>
								<div class="form-group">
									<label class="control-label col-md-3">
										配置说明:
									</label>
									<div class="col-md-5">
										<input type="text" name="configInfo" id="configInfoGlobal"
											   class="form-control" />
									</div>
								</div>

								<div class="form-group">
									<label class="control-label col-md-3">
										比较:
									</label>
									<div class="col-md-5">
										<select name="compareType" id="compareTypeGlobal" class="form-control select2_category">
											<c:forEach items="${instanceAlertCompareTypeEnumList}" var="instanceAlertCompareTypeEnum">
												<option value="${instanceAlertCompareTypeEnum.value}">
														${instanceAlertCompareTypeEnum.info}
												</option>
											</c:forEach>
										</select>
									</div>
								</div>

								<div class="form-group">
									<label class="control-label col-md-3">
										阀值:
									</label>
									<div class="col-md-5">
										<input type="text" name="alertValue" id="alertValueGlobal"
											   class="form-control" />
									</div>
								</div>

								<div class="form-group">
									<label class="control-label col-md-3">
										周期:
									</label>
									<div class="col-md-5">
										<select name="checkCycle" id="checkCycleGlobal" class="form-control select2_category">
											<c:forEach items="${instanceAlertCheckCycleEnumList}" var="instanceAlertCheckCycleEnum">
												<option value="${instanceAlertCheckCycleEnum.value}">
														${instanceAlertCheckCycleEnum.info}
												</option>
											</c:forEach>
										</select>
									</div>
								</div>

								<div class="form-group">
									<label class="control-label col-md-3">
										重要程度:
									</label>
									<div class="col-md-5">
										<select name="importantLevel" id="importantLevel" class="form-control select2_category">
											<option value="0" selected>一般</option>
											<option value="1">重要</option>
											<option value="2">紧急</option>
										</select>
									</div>
								</div>
							</div>
							<!-- form-body 结束 -->
						</div>
					</div>
				</div>

				<div class="modal-footer">
					<button type="button" data-dismiss="modal" class="btn" >Close</button>
					<button type="button" id="configBtnGlobal" class="btn red" onclick="saveGlobalInstanceAlertConfig()">Ok</button>
				</div>
			</form>
		</div>
	</div>
</div>

<!-- 应用报警项 -->
<div id="addAppAlertModal" class="modal fade" tabindex="-1" data-width="400">
	<div class="modal-dialog">
		<div class="modal-content">

			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal" aria-hidden="true"></button>
				<h4 class="modal-title">添加应用报警项</h4>
			</div>

			<form class="form-horizontal form-bordered form-row-stripped">
				<div class="modal-body">
					<div class="row">
						<!-- 控件开始 -->
						<div class="col-md-12">
							<!-- form-body开始 -->
							<div class="form-body">
								<div class="form-group">
									<label class="control-label col-md-3">
										配置名:
									</label>
									<div class="col-md-5">
										<select name="alertConfig" id="alertAppConfig" class="form-control select2_category">
											<c:forEach items="${redisUsedGlobalAlertConfigList}" var="redisAlertConfig">
												<option value="${redisAlertConfig.value}">
														${redisAlertConfig.info}
												</option>
											</c:forEach>
										</select>
									</div>
								</div>

								<div class="form-group">
									<label class="control-label col-md-3">
										比较:
									</label>
									<div class="col-md-5">
										<select name="compareType" id="compareAppType" class="form-control select2_category">
											<c:forEach items="${instanceAlertCompareTypeEnumList}" var="instanceAlertCompareTypeEnum">
												<option value="${instanceAlertCompareTypeEnum.value}">
														${instanceAlertCompareTypeEnum.info}
												</option>
											</c:forEach>
										</select>
									</div>
								</div>


								<div class="form-group">
									<label class="control-label col-md-3">
										阀值:
									</label>
									<div class="col-md-5">
										<input type="text" name="alertValue" id="alertAppValue"
											   class="form-control" />
									</div>
								</div>

								<div class="form-group">
									<label class="control-label col-md-3">
										应用appid:
									</label>
									<div class="col-md-5">
										<input type="text" name="appid" id="appid"
											   class="form-control" placeholder="应用appid" onchange="checkAppExist()"/>
									</div>
								</div>

								<div class="form-group">
									<label class="control-label col-md-3">
										周期:
									</label>
									<div class="col-md-5">
										<select name="checkCycle" id="checkAppCycle" class="form-control select2_category">
											<c:forEach items="${instanceAlertCheckCycleEnumList}" var="instanceAlertCheckCycleEnum">
												<option value="${instanceAlertCheckCycleEnum.value}">
														${instanceAlertCheckCycleEnum.info}
												</option>
											</c:forEach>
										</select>
									</div>
								</div>
							</div>
							<!-- form-body 结束 -->
						</div>
					</div>
				</div>

				<div class="modal-footer">
					<button type="button" data-dismiss="modal" class="btn" >Close</button>
					<button type="button" id="configAppBtn" class="btn red" onclick="saveAppAlertConfig()">Ok</button>
				</div>
			</form>
		</div>
	</div>
</div>

<c:forEach items="${instanceAlertList}" var="config">
	<div id="changeInstanceAlertModal${config.id}" class="modal fade" tabindex="-1" data-width="400">
		<div class="modal-dialog">
			<div class="modal-content">
			
				<div class="modal-header">
					<button type="button" class="close" data-dismiss="modal" aria-hidden="true"></button>
					<h4 class="modal-title">修改实例报警项</h4>
				</div>
				
				<form class="form-horizontal form-bordered form-row-stripped">
					<div class="modal-body">
						<div class="row">
							<!-- 控件开始 -->
							<div class="col-md-12">
								<!-- form-body开始 -->
								<div class="form-body">
									<div class="form-group">
										<label class="control-label col-md-3">
											比较:
										</label>
										<div class="col-md-5">
											<select name="compareType${config.id}" id="compareType${config.id}" class="form-control select2_category">
												<c:forEach items="${instanceAlertCompareTypeEnumList}" var="instanceAlertCompareTypeEnum">
													<option value="${instanceAlertCompareTypeEnum.value}" <c:if test="${config.compareType == instanceAlertCompareTypeEnum.value}">selected</c:if>>
															${instanceAlertCompareTypeEnum.info}
													</option>
												</c:forEach>
											</select>
										</div>
									</div>

									<div class="form-group">
										<label class="control-label col-md-3">
											阀值:
										</label>
										<div class="col-md-5">
											<input type="text" name="alertValue${config.id}" id="alertValue${config.id}" value="${config.alertValue}"
												class="form-control" />
										</div>
									</div>
									
									<div class="form-group">
										<label class="control-label col-md-3">
											周期:
										</label>
										<div class="col-md-5">
											<select name="checkCycle${config.id}" id="checkCycle${config.id}" class="form-control select2_category">
												<c:forEach items="${instanceAlertCheckCycleEnumList}" var="instanceAlertCheckCycleEnum">
													<option value="${instanceAlertCheckCycleEnum.value}" <c:if test="${config.checkCycle == instanceAlertCheckCycleEnum.value}">selected</c:if>>
														${instanceAlertCheckCycleEnum.info}
													</option>
												</c:forEach>
											</select>
										</div>
									</div>

									<div class="form-group" <c:if test="${config.type != 1}">hidden</c:if>>
										<label class="control-label col-md-3">
											重要程度:
										</label>
										<div class="col-md-5">
											<select name="importantLevel${config.id}" id="importantLevel${config.id}" class="form-control select2_category">
												<option value="0" <c:if test="${config.importantLevel == 0}">selected</c:if>>一般</option>
												<option value="1" <c:if test="${config.importantLevel == 1}">selected</c:if>>重要</option>
												<option value="2" <c:if test="${config.importantLevel == 2}">selected</c:if>>紧急</option>
											</select>
										</div>
									</div>
								</div>
								<!-- form-body 结束 -->
							</div>
						</div>
					</div>
					
					<div class="modal-footer">
						<button type="button" data-dismiss="modal" class="btn" >Close</button>
						<button type="button" id="configBtn${config.id}" class="btn red" onclick="changeAlertConfig('${config.id}')">Ok</button>
					</div>
				</form>
			</div>
		</div>
	</div>
</c:forEach>

