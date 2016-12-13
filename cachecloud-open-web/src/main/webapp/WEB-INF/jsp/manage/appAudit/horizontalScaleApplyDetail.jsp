<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp"%>
<script type="text/javascript">
	function checkNodes(){
		var masterSizeSlave = document.getElementById("masterSizeSlave");
		if(masterSizeSlave.value == ""){
			alert("节点信息不能为空");
			masterSizeSlave.focus();
			return false;
		}
		var appAuditId = document.getElementById("appAuditId");
		$.get(
			'/manage/app/checkHorizontalNodes.json',
			{
				appAuditId: appAuditId.value,
				masterSizeSlave: masterSizeSlave.value
			},
	        function(data){
				var status = data.status;
				alert(data.message);
				if (status == 1) {
					var nodeDeployBtn = document.getElementById("nodeDeployBtn");
					nodeDeployBtn.disabled = false;
		    		
		    		var nodeCheckBtn = document.getElementById("nodeCheckBtn");
		    		nodeCheckBtn.disabled = true;
		    		
		    		masterSizeSlave.disabled = true;
				} else {
					masterSizeSlave.focus();
				}
	        }
	     );
	}
	
	function deployNodes(){
		var masterSizeSlave = document.getElementById("masterSizeSlave");
		var appAuditId = document.getElementById("appAuditId");
		
		var nodeDeployBtn = document.getElementById("nodeDeployBtn");
		nodeDeployBtn.disabled = true;
		
		$.get(
			'/manage/app/addHorizontalNodes.json',
			{
				appAuditId: appAuditId.value,
				masterSizeSlave: masterSizeSlave.value
			},
			function(data){
				var status = data.status;
				if (status == 1) {
					alert("添加部署成功,确认后将跳转到ReShard页面!");
					window.location.href="/manage/app/handleHorizontalScale?appAuditId=" + appAuditId.value;
				} else {
					alert("节点部署失败,请查看系统日志确认相关原因!");
				}
	        }
	     );
	}

</script>

<div class="page-container">
	<div class="page-content">
		
		<div class="row">
			<div class="col-md-12">
				<h3 class="page-title">
				添加新的节点(不分配slot，只meet到集群)
				</h3>
			</div>
		</div>
		<div class="row">
			<div class="col-md-12">
				<div class="portlet box light-grey">
					<div class="portlet-title">
						<div class="caption">
							<i class="fa fa-globe"></i>
							填写扩容配置:
							&nbsp;
						</div>
						<div class="tools">
							<a href="javascript:;" class="collapse"></a>
							<a href="javascript:;" class="remove"></a>
						</div>
					</div>
					<div class="portlet-body">
						<div class="form">
								<!-- BEGIN FORM-->
								<form class="form-horizontal form-bordered form-row-stripped">
									<div class="form-body">
										<div class="form-group">
											<label class="control-label col-md-3">主从分片配置:</label>
											<div class="col-md-5">
												<input type="text" name="masterSizeSlave" id="masterSizeSlave" placeholder="materIp:memSize:slaveIp" class="form-control">
											</div>
										</div>
										<input type="hidden" id="appAuditId" name="appAuditId" value="${appAudit.id}">
										
										<div class="form-actions fluid">
											<div class="row">
												<div class="col-md-12">
													<div class="col-md-offset-3 col-md-6">
														<button disabled="disabled" id="nodeDeployBtn" type="button" class="btn green" onclick="deployNodes()">
															<i class="fa fa-check"></i>
															部署节点
														</button>
														&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
														<button type="button" id="nodeCheckBtn" class="btn green" onclick="checkNodes()">
															<i class="fa fa-check"></i>
															验证格式
														</button>
														&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
														<a target="_blank" class="btn green" href="/manage/app/handleHorizontalScale?appAuditId=${appAudit.id}">ReShard页面</a>
													</div>
												</div>
											</div>
										</div>
									</div>
								</form>
								<!-- END FORM-->
							</div>
					</div>
				</div>
				<!-- END EXAMPLE TABLE PORTLET-->
			</div>
		</div>
	</div>
</div>

