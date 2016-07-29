<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp"%>

<script type="text/javascript">

function removeConfig(id, configKey) {
	if (confirm("确认要删除key="+configKey+"配置?")) {
		$.get(
			'/manage/redisConfig/remove.json',
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
}

function changeConfig(id, configKey) {
	var configValue = document.getElementById("configValue" + id);
	var info = document.getElementById("info" + id);
	var status = document.getElementById("status" + id);
	$.get(
		'/manage/redisConfig/update.json',
		{
			id: id,
			configKey: configKey,
			configValue: configValue.value,
			info: info.value,
			status: status.value
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

function saveRedisConfig() {
	var configKey = document.getElementById("configKey");
	if (configKey.value == ""){
		alert("请填写配置名");
		configKey.focus();
		return false;
	}
	var configValue = document.getElementById("configValue");
	var info = document.getElementById("info");
	if (info.value == "") {
		alert("请填写配置说明");
		info.focus();
		return false;
	}
	var type = document.getElementById("type");
	$.get(
		'/manage/redisConfig/add.json',
		{
			configKey: configKey.value,
			configValue: configValue.value,
			info: info.value,
			type: type.value
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
				<button id="sample_editable_1_new" class="btn green" data-target="#addRedisConfigModal" data-toggle="modal">
				添加新配置 <i class="fa fa-plus"></i>
				</button>
			</div>
			<div class="btn-group" style="float:right">
				<form action="/manage/redisConfig/init.do" method="post" class="form-horizontal form-bordered form-row-stripped">
					<label class="control-label">
						Redis类型:
					</label>
					<select name="type">
						<option value="2" <c:if test="${type == 2}">selected</c:if>>
							Redis-cluster
						</option>
						<option value="5" <c:if test="${type == 5}">selected</c:if>>
							Redis-sentinel
						</option>
						<option value="6" <c:if test="${type == 6}">selected</c:if>>
	                       Redis-standalone
						</option>
					</select>
					&nbsp;<button type="submit" class="btn green btn-sm">查询</button>
				</form>
			</div>
		</div>
		<div class="row">
			<div class="col-md-12">
				<h3 class="page-title">
					<c:choose>
						<c:when test="${type==6}">Redis普通配置</c:when>
						<c:when test="${type==2}">Redis Cluster</c:when>
						<c:when test="${type==5}">Redis Sentinel</c:when>
					</c:choose>
					<a target="_blank" href="/manage/redisConfig/preview?type=${type}" class="btn btn-info" role="button">配置模板 预览</a>
					
				</h3>
			</div>
		</div>
		<div class="alert alert-warning" role="alert">
		        1. 此功能是Redis全局配置模板(每次开启应用时用到)，请谨慎修改.<br/>
		        2. 配置中的%d,%s代表Cachecloud会动态配置，最好不要修改.<br/>
		        3. 使用方法详见<a target="_blank" href='http://cachecloud.github.io/2016/07/13/1.2.%20Redis%E9%85%8D%E7%BD%AE%E6%A8%A1%E6%9D%BF%E4%BD%BF%E7%94%A8%E6%96%B9%E6%B3%95/'>Redis配置模板使用方法</a>
		</div>
		
		
		<div class="row">
			<div class="col-md-12">
				<div class="portlet box light-grey">
						<div class="portlet-title">
							<div class="caption">
								<i class="fa fa-globe"></i>
								填写配置:
								&nbsp;
							</div>
							<div class="tools">
								<a href="javascript:;" class="collapse"></a>
							</div>
						</div>
						
						
						<c:forEach items="${redisConfigList}" var="config" varStatus="stats">
							<div class="form">
								<form class="form-horizontal form-bordered form-row-stripped">
									<div class="form-body">
										<div class="form-group">
											<label class="control-label col-md-3">
												<c:choose>
													<c:when test="${config.status == 0}">
														<font color='red'>（无效配置）</font>
													</c:when>
												</c:choose>
												${config.configKey}:
											</label>
											<div class="col-md-2">
												<input id="configValue${config.id}" type="text" name="configValue" class="form-control" value="${config.configValue}" />
											</div>
											<div class="col-md-3">
												<input id="info${config.id}" type="text" name="info" class="form-control" value="${config.info}" />
											</div>
											<div class="col-md-2">
												<select id="status${config.id}" name="status" class="form-control">
													<option value="1" <c:if test="${config.status == 1}">selected</c:if>>
														有效
													</option>
													<option value="0" <c:if test="${config.status == 0}">selected</c:if>>
														无效
													</option>
												</select>
											</div>
											<div class="col-md-2">
												<button type="button" class="btn btn-small" onclick="changeConfig('${config.id}','${config.configKey}')">
													修改
												</button>
												<button type="button" class="btn btn-small" onclick="removeConfig('${config.id}','${config.configKey}')">
													删除
												</button>
											</div>
										</div>
									</div>
									<input type="hidden" name="configKey" value="${config.configKey}">
									<input type="hidden" name="id" value="${config.id}">
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

<div id="addRedisConfigModal" class="modal fade" tabindex="-1" data-width="400">
	<div class="modal-dialog">
		<div class="modal-content">
		
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal" aria-hidden="true"></button>
				<h4 class="modal-title">添加Redis配置</h4>
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
										配置值:
									</label>
									<div class="col-md-5">
										<input type="text" name="configValue" id="configValue"
											class="form-control" />
									</div>
								</div>
								
								<div class="form-group">
									<label class="control-label col-md-3">
										配置说明:
									</label>
									<div class="col-md-5">
										<input type="text" name="info" id="info"
											class="form-control" />
									</div>
								</div>
								
								
								<div class="form-group">
									<label class="control-label col-md-3">
										类型:
									</label>
									<div class="col-md-5">
										<select name="type" id="type" class="form-control select2_category">
											<option value="6">
												Redis普通配置
											</option>
											<option value="2">
												Redis Cluster配置
											</option>
											<option value="5" >
												Redis Sentinel配置
											</option>
											
										</select>
									</div>
								</div>
							</div>
							<!-- form-body 结束 -->
						</div>
						<div id="info"></div>
						<!-- 控件结束 -->
					</div>
				</div>
				
				<div class="modal-footer">
					<button type="button" data-dismiss="modal" class="btn" >Close</button>
					<button type="button" id="configBtn" class="btn red" onclick="saveRedisConfig()">Ok</button>
				</div>
			
			</form>
		</div>
	</div>
</div>

