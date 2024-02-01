<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp"%>

<script src="/resources/manage/plugins/jquery-1.10.2.min.js"></script>
<script type="text/javascript">
    var jQuery_1_10_2 = $;
</script>
<script type="text/javascript" src="/resources/select/bootstrap-select.js"></script>
<link rel="stylesheet" type="text/css" href="/resources/select/bootstrap-select.css"/>
<!-- 提示工具-->
<link href="/resources/css/common.css" rel="stylesheet" type="text/css"/>
<link href="/resources/toastr/toastr.min.css" rel="stylesheet" type="text/css">
<script type="text/javascript" src="/resources/toastr/toastr.min.js"></script>
<!-- app deploy -->
<script src="/resources/manage/manage/appDeploy.js?<%=System.currentTimeMillis()%>" type="text/javascript"></script>


<link href="/resources/manage/css/style-metronic.css" rel="stylesheet" type="text/css"/>

<script type="text/javascript">
    $(window).on('load', function () {
        jQuery_1_10_2('.selectpicker').selectpicker({
            'selectedText': 'cat'
        });
    });
</script>
<div class="page-container">
	<div class="page-content">

		<input type="hidden" id="hiddenAppId" name="hiddenAppId" value="${appId}">
		<input type="hidden" id="appAuditId" name="appAuditId" value="${appAuditId}">
		<%--部署流程化--%>
		<div class="modal-dialog" style="width:1150px;">
			<div class="modal-content">
				<div class="modal-header">
					<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
					<h3 class="modal-title">应用部署 ID:${appDesc.appId}</h3>
				</div>
				<div class="modal-body">
					<div class="row">
						<div class="col-md-12">
							<table class="table table-striped table-bordered table-hover" id="tableDataList">
								<tr>
									<td>应用id</td>
									<td>${appDesc.appId}</td>
									<td>应用名称</td>
									<td>${appDesc.name}</td>
								</tr>
								<tr>
									<td>存储种类</td>
									<td>
										<c:choose>
											<c:when test="${appDesc.type == 2}">RedisCluster</c:when>
											<c:when test="${appDesc.type == 5}">Redis+Sentinel</c:when>
											<c:when test="${appDesc.type == 6}">RedisStandalone</c:when>
											<%--<c:when test="${appDesc.type == 7}">Redis+Twemproxy</c:when>--%>
											<%--<c:when test="${appDesc.type == 8}">Rika+Sentinel</c:when>--%>
											<%--<c:when test="${appDesc.type == 9}">Rika+Twemproxy</c:when>--%>
										</c:choose>
									</td>
									<td>内存申请详情</td>
									<td><font color="red">${appAudit.info}</font></td>
								</tr>
								<tr>
									<td>是否需要热备</td>
									<td>
										<c:choose>
											<c:when test="${appDesc.needHotBackUp == 1}">是</c:when>
											<c:when test="${appDesc.needHotBackUp == 0}">否</c:when>
										</c:choose>
									</td>
									<td>是否有后端数据源</td>
									<td>
										<c:choose>
											<c:when test="${appDesc.hasBackStore == 1}">有</c:when>
											<c:when test="${appDesc.hasBackStore == 0}">无</c:when>
										</c:choose>
									</td>
								</tr>
								<tr>
									<td>是否测试</td>
									<td>
										<c:choose>
											<c:when test="${appDesc.isTest == 1}">是</c:when>
											<c:when test="${appDesc.isTest == 0}">否</c:when>
										</c:choose>
									</td>
									<td>是否需要持久化</td>
									<td>
										<c:choose>
											<c:when test="${appDesc.needPersistence == 1}">是</c:when>
											<c:when test="${appDesc.needPersistence == 0}">否</c:when>
										</c:choose>
									</td>
								</tr>
								<tr>
									<td>预估QPS</td>
									<td>${appDesc.forecaseQps}</td>
									<td>预估条目数量</td>
									<td>${appDesc.forecastObjNum}</td>
								</tr>
								<tr>
									<td>客户端机房信息</td>
									<td>${appDesc.clientMachineRoom}</td>
									<td>Redis版本</td>
									<td>${version.name}</td>
								</tr>
								<tr>
									<td>申请安装Redis模块</td>
									<td class="col-md-4">${appAudit.param3}</td>
									<td>淘汰策略</td>
									<td>${appDesc.maxmemoryPolicyDesc}</td>
								</tr>
							</table>
						</div>
					</div>

					<div class="row" id="redisVersionDiv">
						<div class="col-md-12">
							<div class="portlet box light-grey">
								<div class="portlet-title">
									<div class="caption"><i class="fa fa-globe"></i>应用基础信息</div>
									<div class="tools">
										<a href="javascript:;" class="collapse"></a>
									</div>
								</div>
								<div class="portlet-body">
									<div class="form">
										<!-- BEGIN FORM-->
										<form class="form-horizontal form-bordered form-row-stripped">
											<div class="form-group">
												<label class="control-label col-md-3">
													应用级别
												</label>
												<div class="col-md-5">
													<select id="importantLevel" name="importantLevel" class="form-control">
														<option <c:if test="${appDesc.importantLevel == 1}">selected</c:if> value="1">
															S
														</option>
														<option <c:if test="${appDesc.importantLevel == 2}">selected</c:if> value="2">
															A
														</option>
														<option <c:if test="${appDesc.importantLevel == 3}">selected</c:if> value="3">
															B
														</option>
														<option <c:if test="${appDesc.importantLevel == 4}">selected</c:if> value="4">
															C
														</option>
													</select>
												</div>
											</div>
											<div class="form-group">
												<label class="control-label col-md-3">
													Redis版本:
												</label>
												<div class="col-md-5">
													<select name="type" id="versionId" class="form-control select2_category">
														<option versionid="-1"> --- 请选择Redis版本 ---</option>
														<c:forEach items="${versionList}" var="version">
															<option <c:if test="${version.id == appDesc.versionId}">selected</c:if> versionid="${version.id}">${version.name} </option>
														</c:forEach>
													</select>
												</div>
											</div>
											<div class="form-group">
												<label class="control-label col-md-3">
													Redis密码：
												</label>
												<div class="col-md-5">
													<input type="text" name="md5Password" id="md5Password" class="form-control" value="${md5password}" readonly/>
												</div>
												<div class="col-md-2">
													<input type="checkbox" id="isSetCustomPwd" name="isSetCustomPwd" onchange="changePwd('${md5password}')"/>设置自定义密码
												</div>
											</div>
										</form>
										<!-- END FORM-->
									</div>
								</div>

							</div>
							<!-- END TABLE PORTLET-->
						</div>
					</div>

					<div class="row" id="moduleVersionDiv">
						<div class="col-md-12">
							<div class="portlet box light-grey">
								<div class="portlet-title">
									<div class="caption"><i class="fa fa-globe"></i>Redis模块信息</div>
									<div class="tools">
										<a href="javascript:;" class="collapse"></a>
									</div>
									<c:if test="${appAudit.param3 != ''}">
										<label style="color:red">申请安装模块:${appAudit.param3}</label>
									</c:if>
								</div>
								<div class="portlet-body">
									<div class="form">
										<!-- BEGIN FORM-->
										<form class="form-horizontal form-bordered form-row-stripped">
											<div class="form-group" id="moduleInfo">
												<label class="control-label col-md-3">
													选择安装Redis模块:
												</label>
												<div class="col-md-5" id="checkboxs">
														<c:forEach items="${allModuleVersions}" var="module">
															<div>
																<input id="checkbox-${module.id}" moduleId="${module.id}" type="checkbox" onclick="selectModule(${module.id})">
																<label id="check-${module.id}" value="${module.id}">${module.name} (${module.info})</label>
															</div>
															<div>
																<select name="type" id="moduleVersionId-${module.id}" class="form-control select2_category">
																	<option versionid="-1">不安装</option>
																	<c:forEach items="${module.versions}" var="version">
																		<c:if test="${version.status ==1}">
																			<option versionid="${version.id}">${version.tag} </option>
																		</c:if>
																	</c:forEach>
																</select>
															</div>
														</c:forEach>
												</div>
											</div>
										</form>
										<!-- END FORM-->
									</div>
								</div>

							</div>
							<!-- END TABLE PORTLET-->
						</div>
					</div>

					<div class="row" id="appDeployDiv">
						<div class="col-md-12">
							<div class="portlet box light-grey">
								<div class="portlet-title">
									<div class="caption">
										<i class="fa fa-globe"></i>
										应用部署信息
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
										<form id="getDeployInfo" action="/manage/app/generateDeployInfo" method="post"
											  class="form-horizontal form-bordered form-row-stripped">
											<div class="form-body">
												<div class="form-group">
													<label class="control-label col-md-2">
														应用类型<font color='red'>*</font>：
													</label>
													<div class="col-md-2">
														<select id="appType" name="type" class="form-control select2_category">
															<option value="2" <c:if test="${appDesc.type == 2}">selected</c:if>>
																RedisCluster
															</option>
															<option value="5" <c:if test="${appDesc.type == 5}">selected</c:if>>
																Sentinel+Redis
															</option>
															 <option value="8" <c:if test="${appDesc.type == 8}">selected</c:if>>
																Sentinel+Pika
															</option>
															<option value="7" <c:if test="${appDesc.type == 7}">selected</c:if>>
																Twemproxy+Redis
															</option>
															<option value="9" <c:if test="${appDesc.type == 9}">selected</c:if>>
																Twemproxy+Pika
															</option>
															<option value="6" <c:if test="${appDesc.type == 6}">selected</c:if>>
																Redis-Standalone
															</option>

														</select>
													</div>
													<label class="control-label col-md-2">
														maxMemory<font color='red'>*</font>：
													</label>
													<div class="col-md-2">
														<input type="text" name="maxMemory" id="maxMemory" placeholder="实例内存(MB)" class="form-control" value=""/>
														<p id="notenum" style="color: red; display: none">
															<i class="ace-icon fa fa-exclamation-triangle bigger-120"></i>
															实例内存(MB)
														</p>
													</div>
												</div>

												<%--sentinelMachines--%>
												<div id="sentinelMachines" class="form-group">
													<label class="control-label col-md-2">
														Sentinel机器:
													</label>
													<div class="col-md-8">
														<label for="sentinelMachineList"></label>
														<select id="sentinelMachineList" class="selectpicker bla bla bli" multiple data-live-search="true">
															<c:forEach items="${machineList}" var="machine">
																<c:if test="${machine.info.type==3}">
																	<fmt:formatNumber var="usedCpu" value="${machineInstanceCountMap[machine.info.ip]}" pattern="0"/>
																	<fmt:formatNumber var="cpu" value="${machine.info.cpu}" pattern="0"/>
																	<fmt:formatNumber var="cpuUsage" value="${usedCpu/cpu*100}" pattern="0"/>
																	<fmt:formatNumber var="usedMemRss" value="${((machine.machineMemInfo.usedMemRss)/1024/1024/1024)}" pattern="0.0"/>
																	<fmt:formatNumber var="mem" value="${ machine.info.mem}" pattern="0.0"/>
																	<fmt:formatNumber var="memUsage" value="${usedMemRss/mem*100}" pattern="0"/>

																	<option value="${machine.ip}">${machine.ip}：${usedCpu}/${cpu}核(${cpuUsage}%) ${usedMemRss}/${mem}G(${memUsage}%) 【${machine.info.realIp}-${machine.info.rack}】【sentinel】</option>
																</c:if>
															</c:forEach>
															</optgroup>
														</select>
														<p id="noteSentinelMachines" style="color: red; display: none">
															sentinel机器为奇数个(建议默认:3个）
														</p>
													</div>
													<div class="col-md-2">
														&nbsp;&nbsp;部署实例数：
														<select name="sentinelNum" id="sentinelNum">
															<option value="1">1</option>
															<option value="2">2</option>
															<option value="3">3</option>
															<option value="4">4</option>
															<option value="5">5</option>
															<option value="6">6</option>
															<option value="7">7</option>
															<option value="8">8</option>
														</select>
													</div>
												</div>
												<%--proxy Machines--%>
												<div id="twemproxyMachines" class="form-group">
													<label class="control-label col-md-2">
														Twemproxy机器:
													</label>
													<div class="col-md-8">
														<label for="twemproxyMachineList"></label>
														<select id="twemproxyMachineList" class="selectpicker bla bla bli" multiple data-live-search="true">
															<c:forEach items="${machineList}" var="machine">
																<c:if test="${machine.info.type==4}">
																	<option value="${machine.ip}">${machine.ip}【twemproxy】</option>
																</c:if>
															</c:forEach>
															</optgroup>
														</select>
													</div>
													<div class="col-md-2">
														&nbsp;&nbsp;部署实例数：
														<select name="twemproxyNum" id="twemproxyNum">
															<option value="1">1</option>
															<option value="2">2</option>
															<option value="3">3</option>
															<option value="4">4</option>
															<option value="5">5</option>
															<option value="6">6</option>
															<option value="7">7</option>
															<option value="8">8</option>
														</select>
													</div>
												</div>

												<!-- redis machines -->
												 <div id="redisMachines" class="form-group" >
													<label class="control-label col-md-2">
														Redis机器：
													</label>
													<div class="col-md-8">
														<label for="redisMachineList"></label>
														<select id="redisMachineList" class="selectpicker bla bla bli" multiple data-live-search="true">
															<c:forEach items="${machineList}" var="machine">
																<c:if test="${machine.info.type==0}">
																	<fmt:formatNumber var="usedCpu" value="${machineInstanceCountMap[machine.info.ip]}" pattern="0"/>
																	<fmt:formatNumber var="cpu" value="${machine.info.cpu}" pattern="0"/>
																	<fmt:formatNumber var="cpuUsage" value="${usedCpu/cpu*100}" pattern="0"/>
																	<fmt:formatNumber var="usedMemRss" value="${((machine.machineMemInfo.usedMemRss)/1024/1024/1024)}" pattern="0.0"/>
																	<fmt:formatNumber var="mem" value="${ machine.info.mem}" pattern="0.0"/>
																	<fmt:formatNumber var="memUsage" value="${usedMemRss/mem*100}" pattern="0"/>

																	<c:if test="${machine.info.useType==0}">
																		<option value="${machine.ip}">${machine.ip}：${usedCpu}/${cpu}核(${cpuUsage}%) ${usedMemRss}/${mem}G(${memUsage}%) 【${machine.info.realIp}-${machine.info.rack}】【专用:${machine.info.extraDesc}】</option>
																	</c:if>
																	<c:if test="${machine.info.useType==1}">
																		<option value="${machine.ip}">${machine.ip}：${usedCpu}/${cpu}核(${cpuUsage}%) ${usedMemRss}/${mem}G(${memUsage}%) 【${machine.info.realIp}-${machine.info.rack}】【测试:${machine.info.extraDesc}】</option>
																	</c:if>
																	<c:if test="${machine.info.useType==2}">
																		<option value="${machine.ip}">${machine.ip}：${usedCpu}/${cpu}核(${cpuUsage}%) ${usedMemRss}/${mem}G(${memUsage}%) 【${machine.info.realIp}-${machine.info.rack}】【混合:${machine.info.extraDesc}】</option>
																	</c:if>
																</c:if>
															</c:forEach>
															</optgroup>
														</select>
													</div>
													<div class="col-md-2">
														&nbsp;&nbsp;实例数：
														<select name="redisNum" id="redisNum">
															<option value="1">1主1从</option>
															<option value="0">1个(主/从)</option>
															<option value="2">2主2从</option>
															<option value="3">3主3从</option>
															<option value="4">4主4从</option>
															<option value="5">5主5从</option>
															<option value="6">6主6从</option>
															<option value="7">7主7从</option>
															<option value="8">8主8从</option>
															<option value="9">9主9从</option>
															<option value="10">10主10从</option>
														</select>
													</div>
												</div>

												<%--pika machines--%>
												<div id="pikaMachines" class="form-group">
													<label class="control-label col-md-2">
														Pika机器:
													</label>
													<div class="col-md-8">
														<label for="pikaMachineList"></label>
														<select id="pikaMachineList" class="selectpicker bla bla bli" multiple data-live-search="true">
															<c:forEach items="${machineList}" var="machine">
																<c:if test="${machine.info.type==5}">
																	<option value="${machine.ip}">${machine.ip} 【pika】</option>
																</c:if>
															</c:forEach>
															</optgroup>
														</select>
													</div>
													<div class="col-md-2">
														&nbsp;&nbsp;部署实例数：
														<select name="pikaNum" id="pikaNum">
															<option value="1">1</option>
															<option value="2">2</option>
															<option value="3">3</option>
															<option value="4">4</option>
															<option value="5">5</option>
															<option value="6">6</option>
															<option value="7">7</option>
															<option value="8">8</option>
														</select>
													</div>
												</div>


												<div class="form-group" style="display:none" id="selectMachineId">
													<label class="control-label col-md-2">
														部署机器信息
													</label>
													<div class="col-md-10">
														<table class="table table-striped table-bordered table-hover" id="tableList">
															<thead>
															<tr>
																<th>ip</th>
																<th>实例数/核数</th>
																<th>已使用/剩余/总内存</th>
																<th>已使用/剩余/总磁盘</th>
																<th>master/salve/sentinel/twemproxy分配</th>
																<th>宿主机/机架信息</th>
																<%--<th>详情</th>--%>
															</tr>
															</thead>
															<tbody>
															</tbody>
														</table>
													</div>
												</div>
												<div class="form-group">
													<label class="control-label col-md-2">
														<br/><br/><br/>部署信息预览:<font color='red'>(*)</font>:
													</label>
													<div class="col-md-5">
														<textarea rows="10" name="appDeployInfo" id="appDeployInfo" placeholder="部署详情" class="form-control" disabled="disabled"></textarea>
													</div>

													<div class="col-md-4">
														<br/>
														<button id="clearInfo" class="btn btn-info" onclick="clearinfo()" data-toggle="modal" style="background:#CCCCCC;display:none;">清除</button>
														<br>
														<button id="manualSwitch" class="btn btn-info" onclick="manualSwitchFunc()" title="可启用编辑，修改部署信息，将按此信息进行部署"  data-toggle="modal" style="background:#FF0000;display:none;">编辑</button>
													</div>
												</div>

												<div class="modal-footer">
													<button type="button" class="btn btn-primary" data-toggle="modal" onclick="generateDeployInfo()">
														<span id="deployPreview">生成部署预览</span>
													</button>
													<button type="button" d="appDeployBtn" class="btn btn-primary" data-toggle="modal" onclick="addAppDeployTask()">
														<span id="deploy">开始部署</span>
													</button>
												</div>
												<input id="importId" type="hidden" value="${importId}">
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
		</div>
	</div>
</div>

<script type="text/javascript">
        // 初始化应用类型
        $(function() {
            appType($('#appType').val());
        });

        // 根据类型选择机器
        $('#appType').change(function () {
             appType($('#appType').val());
        });
        // 根据应用类型获取不同资源
        function appType(type){
            if(type==5){
                // sentinel +redis
                $("#sentinelMachines").show();
                $("#redisMachines").show();
                $("#twemproxyMachines").hide();
                $("#pikaMachines").hide();
            }else if(type==7){
                // twemproxy+Redis
                $("#sentinelMachines").show();
                $("#redisMachines").show();
                $("#twemproxyMachines").show();
                $("#pikaMachines").hide();
            }else if(type==8){
                // sentinel+Pika
                $("#sentinelMachines").show();
                $("#redisMachines").hide();
                $("#twemproxyMachines").hide();
                $("#pikaMachines").show();
            }else if(type==9){
                // twemproxy+Pika
                $("#sentinelMachines").show();
                $("#redisMachines").hide();
                $("#twemproxyMachines").show();
                $("#pikaMachines").show();
            }else {
                // standalone | rediscluster
                $("#redisMachines").show();
                $("#sentinelMachines").hide();
                $("#twemproxyMachines").hide();
                $("#pikaMachines").hide();
            }
        }

		//选择安装相关模块信息
		function selectModule(moduleId){
			if($("#checkbox-"+moduleId).is(':checked') == true){
				$("#check-"+moduleId).attr("style","font-weight:bold;color:green");
			}else{
				$("#check-"+moduleId).removeAttr("style");
			}

			<%--var moduleinfos = "";--%>
			<%--// 插件信息--%>
			<%--$("#checkboxs input:checkbox").each(function(){--%>
				<%--var moduleId = $(this).attr("moduleId");--%>
				<%--if($(this).is(':checked')){--%>
				<%--moduleinfos += moduleId+":"+$("#moduleVersionId-"+moduleId+" option:selected").attr("versionid")+";";--%>
				<%--}--%>
			<%--})--%>
			<%--alert(moduleinfos);--%>
		}
</script>
