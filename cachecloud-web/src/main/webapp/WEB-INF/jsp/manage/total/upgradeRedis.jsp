<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>


<!-- 小版本升级 -->
<div id="upgradeRedisVersionModal${appDetail.appDesc.appId}" class="modal fade" tabindex="-1">
	<div class="modal-dialog" style="width:1000px;">
		<div class="modal-content">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
				<h4 class="modal-title">Redis版本升级流程</h4><b style="font-size:12px;color:red"><i>只支持小版本号升级</i></b>
			</div>
			<div class="modal-body">
				<div class="row bs-wizard" style="border-bottom:0;">
	                <div id="versionUpgrade${appDetail.appDesc.appId}" class="col-xs-2 bs-wizard-step warn complete">
	                  <div class="text-center bs-wizard-stepnum">1.升级版本选择</div>
	                  <div class="progress"><div class="progress-bar"></div></div>
	                  <a href="#" class="bs-wizard-dot"></a>
	                  <div class="bs-wizard-info text-center">版本检查</div>
	                </div>

	                <div id="instanceCheck${appDetail.appDesc.appId}" class="col-xs-2 bs-wizard-step disabled">
	                  <div class="text-center bs-wizard-stepnum">2.实例配置检查</div>
	                  <div class="progress"><div class="progress-bar"></div></div>
	                  <a href="#" class="bs-wizard-dot"></a>
	                  <div class="bs-wizard-info text-center">配置检查</div>
	                </div>

	                <div id="slaveUpdate${appDetail.appDesc.appId}" class="col-xs-2 bs-wizard-step disabled">
	                  <div class="text-center bs-wizard-stepnum">3.Slave配置更新</div>
	                  <div class="progress"><div class="progress-bar"></div></div>
	                  <a href="#" class="bs-wizard-dot"></a>
	                  <div class="bs-wizard-info text-center">更新配置并重启</div>
	                </div>

	                <div id="msFailover${appDetail.appDesc.appId}" class="col-xs-2 bs-wizard-step disabled">
	                  <div class="text-center bs-wizard-stepnum">4.主从节点Failover</div>
	                  <div class="progress"><div class="progress-bar"></div></div>
	                  <a href="#" class="bs-wizard-dot"></a>
	                  <div class="bs-wizard-info text-center">主从节点切换</div>
	                </div>

	                <div id="newSlaveUpdate${appDetail.appDesc.appId}" class="col-xs-2 bs-wizard-step disabled">
	                  <div class="text-center bs-wizard-stepnum">5.新Slave配置更新</div>
	                  <div class="progress"><div class="progress-bar"></div></div>
	                  <a href="#" class="bs-wizard-dot"></a>
	                  <div class="bs-wizard-info text-center">更新配置并重启</div>
	                </div>
	                <div id="upgradeComplete${appDetail.appDesc.appId}" class="col-xs-1 bs-wizard-step disabled">
	                  <div class="text-center bs-wizard-stepnum">6.升级完成</div>
	                  <div class="progress"><div class="progress-bar"></div></div>
	                  <a href="#" class="bs-wizard-dot"></a>
	                  <div class="bs-wizard-info text-center">集群实例及版本信息</div>
	                </div>
	            </div>
				<form class="form-horizontal form-bordered form-row-stripped" id="ns">
					<div class="form-body">
						<div class="form-group">
							<label class="control-label col-md-3"> 应用ID: </label>
					        <div class="col-md-8">
								<label id="appId${appDetail.appDesc.appId}" name="appId" class="form-control" >${appDetail.appDesc.appId}</label>
							</div>
						</div>
						<div class="form-group">
							<label class="control-label col-md-3"> 当前版本号: </label>
					        <div class="col-md-4">
								<label id="appVersion${appDetail.appDesc.appId}" name="appVersion" class="form-control" >${appDetail.appDesc.versionName}</label>
							</div>

						</div>
						<div class="form-group" hidden="hidden" id="div${appDetail.appDesc.appId}">
							<label class="control-label col-md-3"> <a target="_blank" href="/admin/app/index?appId=${appDetail.appDesc.appId}">实例信息</a>: </label>
                            <div class="col-md-8">
                                <textarea id="instanceInfo${appDetail.appDesc.appId}" type="text" rows="8" class="form-control" readonly></textarea>
                            </div>
							<label id="instanceLog${appDetail.appDesc.appId}" class="control-label"></label>
						</div>
						<div class="form-group">
                            <c:set var="bigVersionName" value="${appDetail.appDesc.versionName.substring(0,appDetail.appDesc.versionName.lastIndexOf('.'))}"></c:set>
                            <c:set var="currentTag" value="${appDetail.appDesc.versionName.substring(appDetail.appDesc.versionName.lastIndexOf('.')+1)}"></c:set>
                            <label class="control-label col-md-3"> 可升级Redis版本:</label>
					        <div class="col-md-4">
                                <select id="versionSelect${appDetail.appDesc.appId}" class="form-control select2_category" title="请选择" data-live-search-placeholder="搜索" name="ip" data-live-search="true">
									<c:forEach items="${redisVersionList}" var="version">
                                        <c:if test="${version.groups == bigVersionName && (currentTag - version.name.substring(version.name.lastIndexOf('.')+1) < 0 )}">
                                            <option versionid="${version.id}">${version.name}</option>
                                        </c:if>
                                    </c:forEach>
                                </select>
                            </div>
							<label class="control-label"><a target="_blank" onclick="configPreview(${appDetail.appDesc.appId})"> Redis配置预览</a></label>&nbsp;&nbsp;&nbsp;
							<label class="control-label"><a target="_blank" onclick="configContrast('${appDetail.appDesc.versionId}','${appDetail.appDesc.appId}')"> 升级配置对比</a></label>
						</div>
						<div class="form-group">
							<label class="control-label col-md-3"> 提示: </label>
					        <div class="col-md-8">
								<div class="form-control-static">如果在升级过程中遇到错误警告，请登录服务器解决后再<b style="color:cornflowerblue	">继续</b>执行</div>
							</div>
						</div>
					</div>
				</form>
			</div>
			<div class="modal-footer">
				<button type="button" class="btn btn-primary" data-toggle="modal" onclick="install(${appDetail.appDesc.appId})"><span id="install${appDetail.appDesc.appId}">一键安装</span></button>
			</div>
		</div>
	</div>
</div>