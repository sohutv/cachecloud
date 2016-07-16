<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<script type="text/javascript">
	function reloadInstanceDetailPage(appId,instanceId){
		location.href = "/admin/instance/index.do?instanceId="+instanceId + "&appId="+appId + "&tabTag=instance_configSelect";
	}
	function instanceConfigChange(appId, instanceId){
		var instanceConfigKey = document.getElementById("instanceConfigKey");
		if(instanceConfigKey.value == ""){
			alert("配置项不能为空");
			instanceConfigKey.focus();
			return false;
		}
		
		var instanceConfigValue = document.getElementById("instanceConfigValue");
		if(instanceConfigValue.value == ""){
			alert("配置值不能为空");
			instanceConfigValue.focus();
			return false;
		}

        var instanceConfigReason = document.getElementById("instanceConfigReason");
        if(instanceConfigReason.value == ""){
            alert("配置原因不能为空");
            instanceConfigReason.focus();
            return false;
        }
        
        var instanceConfigChangeBtn = document.getElementById("instanceConfigChangeBtn");
        instanceConfigChangeBtn.disabled = true;
		
		$.post(
			'/admin/app/changeInstanceConfig.do',
			{
				appId: appId,
				instanceId: instanceId,
				instanceConfigKey: instanceConfigKey.value,
				instanceConfigValue: instanceConfigValue.value,
                instanceConfigReason: instanceConfigReason.value
			},
	        function(data){
	            if(data==1){
	                alert("申请成功，请在邮件中关注申请状况.");
	            	$("#instanceConfigChangeInfo").html("<div class='alert alert-error' ><button class='close' data-dismiss='alert'>×</button><strong>Success!</strong>更新成功，窗口会自动关闭</div>");
	                setTimeout("$('instanceConfigChangeModal').modal('hide');reloadInstanceDetailPage("+appId+","+instanceId+");",1000);
	            }else{
	            	instanceConfigChangeBtn.disabled = false;
	                $("#instanceConfigChangeInfo").html("<div class='alert alert-error' ><button class='close' data-dismiss='alert'>×</button><strong>Error!</strong>更新失败！</div>");
	            }
	        }
	     );
	}
</script>
<div class="container">
    <br/>
    <div class="row">
        <div class="col-md-12">
            <div class="page-header">
                <h4>
               		 实例当前配置信息
                     <button type="button" class="btn btn-info" data-target="#instanceConfigChangeModal" data-toggle="modal" href="#">配置修改申请(当前实例)</button>
                </h4>
                <h6>
                </h6>
            </div>
            <table class="table table-striped table-hover">
                <thead>
                <tr>
                    <td>配置项</td>
                    <td>配置值</td>
                </tr>
                </thead>
                <tbody>
                <c:forEach items="${redisConfigList}" var="redisConfig" varStatus="status">
                    <tr>
                        <td>${redisConfig.key}</td>
                        <td>${redisConfig.value}</td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </div>
</div>

<div id="instanceConfigChangeModal" class="modal fade" tabindex="-1" data-width="400">
	<div class="modal-dialog">
		<div class="modal-content">
		
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal" aria-hidden="true"></button>
				<h4 class="modal-title">实例id=${instanceId}配置修改</h4>
			</div>
			
			<form class="form-horizontal form-bordered form-row-stripped">
				<div class="modal-body">
					<div class="row">
						<!-- 控件开始 -->
						<div class="col-md-12">
							<!-- form-body开始 -->
							<div class="form-body">
								
								<div class="form-group">
									<label class="control-label col-md-3">配置项:</label>
									<div class="col-md-8">
										<input type="text" name="instanceConfigKey" id="instanceConfigKey" placeholder="例如:maxclients" class="form-control" />
									</div>
								</div>
								
								<div class="form-group">
									<label class="control-label col-md-3">配置值:</label>
									<div class="col-md-8">
										<input type="text" name="instanceConfigValue" id="instanceConfigValue" placeholder="例如:15000" class="form-control">
									</div>
								</div>

                                <div class="form-group">
                                    <label class="control-label col-md-3">修改原因:</label>
                                    <div class="col-md-8">
                                        <textarea name="instanceConfigReason" id="instanceConfigReason" placeholder="例如：修改原因:1.需要更多的连接数。" class="form-control"></textarea>
                                    </div>
                                </div>
								
							</div>
							<!-- form-body 结束 -->
						</div>
						<div id="instanceConfigChangeInfo"></div>
						<!-- 控件结束 -->
					</div>
				</div>
				
				<div class="modal-footer">
					<button type="button" data-dismiss="modal" class="btn" >Close</button>
					<button type="button" id="instanceConfigChangeBtn" class="btn red" onclick="instanceConfigChange('${appId}','${instanceId}')">Ok</button>
				</div>
			
			</form>
		</div>
	</div>
</div>

