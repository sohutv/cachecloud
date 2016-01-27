<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<script type="text/javascript">
	function reloadAppDetailPage(appId,instanceId){
		location.href = "/admin/instance/index.do?instanceId="+instanceId + "&appId="+appId + "&tabTag=instance_configSelect";
	}
	function appConfigChange(appId, instanceId){
		var appConfigKey = document.getElementById("appConfigKey");
		if(appConfigKey.value == ""){
			alert("配置项不能为空");
			appConfigKey.focus();
			return false;
		}
		
		var appConfigValue = document.getElementById("appConfigValue");
		if(appConfigValue.value == ""){
			alert("配置值不能为空");
			appConfigValue.focus();
			return false;
		}

        var appConfigReason = document.getElementById("appConfigReason");
        if(appConfigReason.value == ""){
            alert("配置原因不能为空");
            appConfigReason.focus();
            return false;
        }
        
        var appConfigChangeBtn = document.getElementById("appConfigChangeBtn");
        appConfigChangeBtn.disabled = true;
		
		$.post(
			'/admin/app/changeAppConfig.do',
			{
				appId: appId,
				instanceId: instanceId,
				appConfigKey: appConfigKey.value,
				appConfigValue: appConfigValue.value,
                appConfigReason: appConfigReason.value
			},
	        function(data){
	            if(data==1){
	                alert("申请成功，请在邮件中关注申请状况.");
	            	$("#appConfigChangeInfo").html("<div class='alert alert-error' ><button class='close' data-dismiss='alert'>×</button><strong>Success!</strong>更新成功，窗口会自动关闭</div>");
	                setTimeout("$('appScaleApplyModal').modal('hide');reloadAppDetailPage("+appId+","+instanceId+");",1000);
	            }else{
	            	appConfigChangeBtn.disabled = false;
	                $("#appConfigChangeInfo").html("<div class='alert alert-error' ><button class='close' data-dismiss='alert'>×</button><strong>Error!</strong>更新失败！</div>");
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
                <h4>实例当前配置信息</h4>
                <h6>
                    <button type="button" class="btn default" data-target="#appConfigChangeModal" data-toggle="modal" href="#">申请修改配置</button>
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

<div id="appConfigChangeModal" class="modal fade" tabindex="-1" data-width="400">
	<div class="modal-dialog">
		<div class="modal-content">
		
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal" aria-hidden="true"></button>
				<h4 class="modal-title">应用配置修改</h4>
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
										<input type="text" name="appConfigKey" id="appConfigKey" placeholder="例如:maxclients" class="form-control" />
									</div>
								</div>
								
								<div class="form-group">
									<label class="control-label col-md-3">配置值:</label>
									<div class="col-md-8">
										<input type="text" name="appConfigValue" id="appConfigValue" placeholder="例如:15000" class="form-control">
									</div>
								</div>

                                <div class="form-group">
                                    <label class="control-label col-md-3">修改原因:</label>
                                    <div class="col-md-8">
                                        <textarea name="appConfigReason" id="appConfigReason" placeholder="例如：修改原因:1.需要更多的连接数。" class="form-control"></textarea>
                                        <%--<input type="text" name="appConfigReason" id="appConfigReason" placeholder="例如：修改原因:1.需要更多的连接数。" class="form-control">--%>
                                    </div>
                                </div>
								
							</div>
							<!-- form-body 结束 -->
						</div>
						<div id="appConfigChangeInfo"></div>
						<!-- 控件结束 -->
					</div>
				</div>
				
				<div class="modal-footer">
					<button type="button" data-dismiss="modal" class="btn" >Close</button>
					<button type="button" id="appConfigChangeBtn" class="btn red" onclick="appConfigChange('${appId}','${instanceId}')">Ok</button>
				</div>
			
			</form>
		</div>
	</div>
</div>

