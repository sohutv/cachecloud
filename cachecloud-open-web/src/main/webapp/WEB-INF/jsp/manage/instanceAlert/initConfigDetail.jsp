<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp"%>

<script type="text/javascript">

function removeConfig(configKey) {
	if (confirm("确认要删除key="+configKey+"配置?")) {
		$.get(
			'/manage/instanceAlert/remove.json',
			{
				configKey: configKey
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
}

function changeConfig(configKey) {
	var alertValue = document.getElementById("alertValue" + configKey);
	var info = document.getElementById("info" + configKey);
	var status = document.getElementById("status" + configKey);
	var compareType = document.getElementById("compareType" + configKey);
	var valueType = document.getElementById("valueType" + configKey);
	$.get(
		'/manage/instanceAlert/update.json',
		{
			configKey: configKey,
			alertValue: alertValue.value,
			info: info.value,
			status: status.value,
			compareType: compareType.value,
			valueType: valueType.value
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

function saveInstanceAlert() {
	var configKey = document.getElementById("configKey");
	if (configKey.value == ""){
		alert("请填写配置名");
		configKey.focus();
		return false;
	}
	var alertValue = document.getElementById("alertValue");
	var info = document.getElementById("configInfo");
	if (info.value == "") {
		alert("请填写配置说明");
		info.focus();
		return false;
	}
	var orderId = document.getElementById("orderId");
	var compareType = document.getElementById("compareType");
	var valueType = document.getElementById("valueType");
	$.get(
		'/manage/instanceAlert/add.json',
		{
			configKey: configKey.value,
			alertValue: alertValue.value,
			info: info.value,
			orderId: orderId.value,
			compareType: compareType.value,
			valueType: valueType.value
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
								填写实例报警项:
								&nbsp;
							</div>
							<div class="tools">
								<a href="javascript:;" class="collapse"></a>
							</div>
						</div>
						
						
						<c:forEach items="${instanceAlertList}" var="config" varStatus="stats">
							<div class="form">
								<form class="form-horizontal form-bordered form-row-stripped">
									<div class="form-body">
										<div class="form-group">
											<label class="control-label col-md-2">
												<c:choose>
													<c:when test="${config.status == 0}">
														<font color='red'>（无效配置）</font>
													</c:when>
												</c:choose>
												${config.configKey}:
											</label>
											<div class="col-md-3">
												<input id="info${config.configKey}" type="text" name="info" class="form-control" value="${config.info}" />
											</div>
											
											<div class="col-md-1">
												<select id="compareType${config.configKey}" name="compareType" class="form-control">
													<option value="-1" <c:if test="${config.compareType == -1}">selected</c:if>>
														小于
													</option>
													<option value="0" <c:if test="${config.compareType == 0}">selected</c:if>>
														等于
													</option>
													<option value="1" <c:if test="${config.compareType == 1}">selected</c:if>>
														大于
													</option>
													<option value="2" <c:if test="${config.compareType == 2}">selected</c:if>>
														不等于
													</option>
												</select>
											</div>
											
											<div class="col-md-2">
												<select id="valueType${config.configKey}" name="valueType" class="form-control">
													<option value="1" <c:if test="${config.valueType == 1}">selected</c:if>>
														固定值
													</option>
													<option value="2" <c:if test="${config.valueType == 2}">selected</c:if>>
														差值
													</option>
												</select>
											</div>
											
											<div class="col-md-1">
												<input id="alertValue${config.configKey}" type="text" name="info" class="form-control" value="${config.alertValue}" />
											</div>
											
											
											<div class="col-md-1">
												<select id="status${config.configKey}" name="status" class="form-control">
													<option value="1" <c:if test="${config.status == 1}">selected</c:if>>
														有效
													</option>
													<option value="0" <c:if test="${config.status == 0}">selected</c:if>>
														无效
													</option>
												</select>
											</div>
											<div class="col-md-2">
												<button type="button" class="btn btn-small" onclick="changeConfig('${config.configKey}')">
													修改
												</button>
												<button type="button" class="btn btn-small" onclick="removeConfig('${config.configKey}')">
													删除
												</button>
											</div>
										</div>
									</div>
									<input type="hidden" name="configKey" value="${config.configKey}">
								</form>
								<!-- END FORM-->
							</div>
						</c:forEach>
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
										<input type="text" name="configKey" id="configKey"
											class="form-control" />
									</div>
								</div>
								
								<div class="form-group">
									<label class="control-label col-md-3">
										比较:
									</label>
									<div class="col-md-5">
										<select name="compareType" id="compareType" class="form-control select2_category">
											<option value="-1">
												小于
											</option>
											<option value="0">
												等于
											</option>
											<option value="1" >
												大于
											</option>
											<option value="2" >
												不等于
											</option>
										</select>
									</div>
								</div>
								
								<div class="form-group">
									<label class="control-label col-md-3">
										类型:
									</label>
									<div class="col-md-5">
										<select name="valueType" id="valueType" class="form-control select2_category">
											<option value="1">
												固定值
											</option>
											<option value="2">
												差值
											</option>
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
										说明:
									</label>
									<div class="col-md-5">
										<input type="text" name="info" id="configInfo"
											class="form-control" />
									</div>
								</div>
								
								<div class="form-group">
									<label class="control-label col-md-3">
										序号:
									</label>
									<div class="col-md-5">
										<input type="text" name="orderId" id="orderId"
											class="form-control" />
									</div>
								</div>
								
								
							</div>
							<!-- form-body 结束 -->
						</div>
					</div>
				</div>
				
				<div class="modal-footer">
					<button type="button" data-dismiss="modal" class="btn" >Close</button>
					<button type="button" id="configBtn" class="btn red" onclick="saveInstanceAlert()">Ok</button>
				</div>
			
			</form>
		</div>
	</div>
</div>

