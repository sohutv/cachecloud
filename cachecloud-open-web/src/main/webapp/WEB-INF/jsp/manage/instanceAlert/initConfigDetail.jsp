<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="com.sohu.cache.redis.enums.RedisAlertConfigEnum"%>
<%@page import="com.sohu.cache.redis.enums.InstanceAlertCompareTypeEnum"%>
<%@page import="com.sohu.cache.redis.enums.InstanceAlertCheckCycleEnum"%>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp"%>

<script type="text/javascript">

//查看实例是否存在
function checkInstanceExist(){
	var instanceHostPort = document.getElementById("instanceHostPort").value;
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
	$.get(
		'/manage/instanceAlert/update.json',
		{
			id: id,
			alertValue: alertValue.value,
			checkCycle: checkCycle.value
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
	var type = 1;
	if (instanceHostPort.value != null && instanceHostPort.value != '') {
		type = 2;
	}
	$.get(
		'/manage/instanceAlert/add.json',
		{
			alertConfig: alertConfig.value,
			alertValue: alertValue.value,
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
</script>

<div class="page-container">
	<div class="page-content">
		<div class="table-toolbar">
			<div class="btn-group">
				<button id="sample_editable_1_new" class="btn green" data-target="#addInstanceAlertModal" data-toggle="modal">
				添加新实例报警项 <i class="fa fa-plus"></i>
				</button>
			</div>
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
													${config.instanceInfo.hostPort}
													<a target="_blank" href="/admin/app/index.do?appId=${config.instanceInfo.appId}">(${config.instanceInfo.appId})</a>
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
											<c:forEach items="${redisAlertConfigEnumList}" var="redisAlertConfig">
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
											class="form-control" placeholder="全部则为空,单个实例ip:port" onchange="checkInstanceExist()"/>
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

