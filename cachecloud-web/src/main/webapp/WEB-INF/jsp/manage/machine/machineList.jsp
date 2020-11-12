<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<script type="text/javascript" src="/resources/bootstrap/jquery/jquery-1.11.0.js"></script>
<%@include file="/WEB-INF/jsp/manage/commons/taglibs.jsp" %>
<%@include file="/WEB-INF/jsp/manage/include/cache_cloud_main_js_include.jsp" %>
<%@include file="/WEB-INF/jsp/manage/include/cache_cloud_main_css.jsp" %>

<script type="text/javascript">
	var TableManaged = function () {
		return {
			//main function to initiate the module
			init: function () {

				if (!jQuery().dataTable) {
					return;
				}

				$('#tableDataList').dataTable({
					"searching": true,
					"bLengthChange": false,
					"iDisplayLength": 15,
					"sPaginationType": "bootstrap",
					"oLanguage": {
						"sLengthMenu": "_MENU_ records",
						"oPaginate": {
							"sPrevious": "Prev",
							"sNext": "Next"
						}
					}
				});

				jQuery('#tableDataList_wrapper>div:first-child').css("display", "none");
			}
		};
	}();

	$(function () {
		$('.selectpicker').selectpicker({
			'selectedText': 'cat',
			'size': 8,
			'dropupAuto': false
		});
		$('.selectpicker').selectpicker('refresh');
		// $('.dropdown-toggle').on('click', function () {
		//     $('.dropdown-toggle').dropdown();
		// });
		App.init(); // initlayout and core plugins
		TableManaged.init();
	});

	$('#modal-diagnosticResult').on('shown.bs.modal', function (e) {
		$('#modal-title').html('');
		$('#diagnosticResultCount').html('');
		$('#diagnosticResultTable').html('');

		var redisKey = $(e.relatedTarget).data('rediskey');
		var title = $(e.relatedTarget).data('title');
		$('#modal-title').html(title);
		$.get(
				'/manage/app/tool/diagnostic/data.json',
				{
					redisKey: redisKey,
					type: 0
				},
				function (data) {
					$('#diagnosticResultCount').append(
							'<tr>' +
							'<td>key (共计' + data.count + '个）</td>' +
							'</tr>'
					);
					var diagnosticResultList = data.result;
					diagnosticResultList.forEach(function (diagnosticResult, index) {
						$('#diagnosticResultTable').append(
								'<tr>' +
								'<td>' + diagnosticResult + '</td>' +
								'</tr>'
						);
					})
				}
		);

	});

	function changeAppIdSelect(appId, instance_select) {
		console.log('instance_select:' + instance_select);
		console.log(appId);

		document.getElementById(instance_select).options.length = 0;

		$.post('/manage/app/tool/diagnostic/appInstances',
				{
					appId: appId,
				},
				function (data) {
					var status = data.status;
					if (status == 1) {
						var appInstanceList = data.appInstanceList;
						$('#' + instance_select).append("<option value=''>所有主节点</option>");
						for (var i = 0; i < appInstanceList.length; i++) {
							var val = appInstanceList[i].hostPort;
							var term = appInstanceList[i].hostPort + '（角色：' + appInstanceList[i].roleDesc + '）'
							$('#' + instance_select).append("<option value='" + val + "'>" + term + "</option>");
						}
						$('#' + instance_select).selectpicker('refresh');
						$('#' + instance_select).selectpicker('render');
					} else {
						console.log('data.status:' + status);
					}
				}
		);
	}

	function submitDiagnostic(type) {
		var appId;
		var size;
		var nodes;
		var params = [];
		if (type == 0) {
			appId = $('#scan-select').selectpicker('val');
			if (appId == null || appId == '') {
				alert("请选择应用");
				return;
			}

			nodes = $('#scan_instance-select').selectpicker('val');

			size = $('#scan_size-select').selectpicker('val');

			params.push($('#scan-pattern').val());
			params.push(size);
		}
		$.post(
				'/manage/app/tool/diagnostic/submit.json',
				{
					type: type,
					appId: appId,
					nodes: nodes == null ? "" : nodes.toString(),
					params: params.toString()
				},
				function (data) {
					var status = data.status;
					if (status == 'success') {
						alert("检测任务提交成功，任务id：" + data.taskId);
						location.href = "/manage/app/tool/index?tabTag=scan";
					} else {
						toastr.error("检测任务提交失败,请查看系统日志确认相关原因!");
					}
				}
		);
	}
</script>


<div class="row">
	<div class="col-md-12">
		<button id="sample_editable_1_new" class="btn green" data-target="#addMachineModal" data-toggle="modal">
			添加新机器 <i class="fa fa-plus"></i>
		</button>

		<div style="float:right">
			<form action="/manage/machine/index?tabTag=machine" method="post" class="form-horizontal form-bordered form-row-stripped">
				<label class="control-label">
					机器ip:
				</label>
				&nbsp;<input type="text" name="ipLike" id="ipLike" value="${ipLike}" placeholder="机器ip"/>
				&nbsp;<input type="text" name="realip" id="realip" value="${realip}" placeholder="宿主机ip"/>
				<select name="useType" id="useType">
					<option value="-1">使用类型</option>
					<option value="0" <c:if test="${useType == 0}">selected</c:if>>Redis专用机器</option>
					<option value="1" <c:if test="${useType == 1}">selected</c:if>>Redis测试机器</option>
					<option value="2" <c:if test="${useType == 2}">selected</c:if>>混合部署机器</option>
				</select>
				<select name="type" id="type">
					<option value="-1">机器类型</option>
					<option value="0" <c:if test="${type == 0}">selected</c:if>>Redis机器</option>
					<option value="3" <c:if test="${type == 3}">selected</c:if>>Sentinel机器</option>
					<option value="2" <c:if test="${type == 2}">selected</c:if>>Redis迁移工具</option>
					<option value="4" <c:if test="${type == 4}">selected</c:if>>Twemproxy机器</option>
					<option value="5" <c:if test="${type == 5}">selected</c:if>>Pika机器</option>
				</select>
				<select name="k8sType" id="k8sType">
					<option value="-1">容器类型</option>
					<option value="0" <c:if test="${k8sType == 0}">selected</c:if>>普通容器</option>
					<option value="1" <c:if test="${k8sType == 1}">selected</c:if>>k8s容器</option>
					<option value="2" <c:if test="${k8sType == 2}">selected</c:if>>物理机</option>
					<option value="3" <c:if test="${k8sType == 3}">selected</c:if>>虚拟机</option>
				</select>
				&nbsp;<button type="submit" class="btn blue btn-sm">查询</button>
			</form>
		</div>
	</div>
</div>

<br/>
<div class="row">
	<div class="col-md-12">
		<div class="portlet box light-grey" id="clientIndex">

			<table class="table table-striped table-bordered table-hover" id="tableDataList">
				<thead>
				<tr>
					<th>ip</th>
					<th>宿主机ip</th>
					<th>实例数/核数</th>
					<th>内存使用率</th>
					<th>rss内存使用</th>
					<th>已分配内存</th>
					<th>CPU使用率</th>
					<th>机器负载</th>
					<th>最后统计时间</th>
					<th>机房</th>
					<th>机器类型</th>
					<th>使用类型</th>
					<th>额外说明</th>
					<th>状态收集</th>
					<th>操作</th>
				</tr>
				</thead>
				<tbody>
				<c:forEach items="${list}" var="machine">
					<tr class="odd gradeX">
						<td>
							<a target="_blank" href="/manage/machine/machineInstances?ip=${machine.info.ip}">${machine.info.ip}</a>
							<c:if test="${machine.info.k8sType==1}">(<a target="_blank" href="/manage/machine/pod/changelist?ip=${machine.info.ip}" title="查看pod变更记录">k8s</a>)</c:if>
						</td>
						<th>
								${machine.info.realIp}
						</th>
						<td>
							<fmt:formatNumber var="v1" value="${machineInstanceCountMap[machine.info.ip]}" pattern="0.00"/>
							<fmt:formatNumber var="v2" value="${machine.info.cpu}" pattern="0.00"/>
							<c:if test='${v1/v2*100.0>100}'>
								<fmt:formatNumber var="fmtInstanceCpuRatio" value="100" pattern="0.00"/>
							</c:if>
							<c:if test='${v1/v2*100.0<=100}'>
								<fmt:formatNumber var="fmtInstanceCpuRatio" value="${v1/v2*100.0}" pattern="0.00"/>
							</c:if>
							<span style="display:none"><fmt:formatNumber value="${fmtInstanceCpuRatio / 100}" pattern="0.00"/></span>
							<div class="progress margin-custom-bottom0">
								<c:choose>
									<c:when test='${v1/v2*100.0>100}'><c:set var="instanceCpuProgressBarStatus" value="progress-bar-danger"/></c:when>
									<c:when test="${fmtInstanceCpuRatio >= 80.00}"><c:set var="instanceCpuProgressBarStatus" value="progress-bar-warning"/></c:when>
									<c:otherwise><c:set var="instanceCpuProgressBarStatus" value="progress-bar-success"/></c:otherwise>
								</c:choose>
								<c:choose>
									<c:when test="${fmtInstanceCpuRatio == 0.00}">
										<c:set var="instanceCount" value="0"/>
									</c:when>
									<c:otherwise>
										<c:set var="instanceCount" value="${machineInstanceCountMap[machine.info.ip]}"/>
									</c:otherwise>
								</c:choose>
								<div class="progress-bar ${instanceCpuProgressBarStatus}"
									 role="progressbar" aria-valuenow="${fmtInstanceCpuRatio}" aria-valuemax="100"
									 aria-valuemin="0" style="width: ${fmtInstanceCpuRatio}%">
									<label style="color: #000000">
											${instanceCount}&nbsp;&nbsp;/
										<fmt:formatNumber value="${machine.info.cpu}"/>&nbsp;&nbsp;
									</label>
								</div>
							</div>
						</td>
						<td>
							<fmt:formatNumber var="usedMem" value="${((machine.machineMemInfo.usedMem)/1024/1024/1024)}" pattern="0.00"/>
							<fmt:formatNumber var="mem" value="${ machine.info.mem}" pattern="0.00"/>
							<fmt:formatNumber var="fmtMemoryUsageRatio" value="${usedMem/mem*100.0}" pattern="0.00"/>
							<div class="progress margin-custom-bottom0">
								<c:choose>
									<c:when test="${fmtMemoryUsageRatio >= 80.00}"><c:set var="memUsedProgressBarStatus" value="progress-bar-danger"/></c:when>
									<c:when test="${fmtMemoryUsageRatio >= 60.00}"><c:set var="memUsedProgressBarStatus" value="progress-bar-warning"/></c:when>
									<c:otherwise><c:set var="memUsedProgressBarStatus" value="progress-bar-success"/></c:otherwise>
								</c:choose>
								<div class="progress-bar ${memUsedProgressBarStatus}"
									 role="progressbar" aria-valuenow="${fmtMemoryUsageRatio}" aria-valuemax="100"
									 aria-valuemin="0" style="width: ${fmtMemoryUsageRatio}%">
									<label style="color: #000000">
											${usedMem}G&nbsp;&nbsp;Used/
											${mem}G&nbsp;&nbsp;Total
									</label>
								</div>
							</div>
						</td>
						<td>
							<fmt:formatNumber var="usedMemRss" value="${((machine.machineMemInfo.usedMemRss)/1024/1024/1024)}" pattern="0.00"/>
							<fmt:formatNumber var="mem" value="${ machine.info.mem}" pattern="0.00"/>
							<fmt:formatNumber var="fmtMemoryUsageRssRatio" value="${usedMemRss/mem*100.0}" pattern="0.00"/>
							<div class="progress margin-custom-bottom0">
								<c:choose>
									<c:when test="${fmtMemoryUsageRssRatio >= 80.00}"><c:set var="memUsedRssProgressBarStatus" value="progress-bar-danger"/></c:when>
									<c:when test="${fmtMemoryUsageRssRatio >= 70.00}"><c:set var="memUsedRssProgressBarStatus" value="progress-bar-warning"/></c:when>
									<c:otherwise><c:set var="memUsedRssProgressBarStatus" value="progress-bar-success"/></c:otherwise>
								</c:choose>
								<div class="progress-bar ${memUsedRssProgressBarStatus}"
									 role="progressbar" aria-valuenow="${fmtMemoryUsageRssRatio}" aria-valuemax="100"
									 aria-valuemin="0" style="width: ${fmtMemoryUsageRatio}%">
									<label style="color: #000000">
											${usedMemRss}G&nbsp;&nbsp;Used/
											${mem}G&nbsp;&nbsp;Total
									</label>
								</div>
							</div>
						</td>
						<td>
							<fmt:formatNumber var="applyMem" value="${((machine.machineMemInfo.applyMem)/1024/1024/1024)}" pattern="0.00"/>
							<fmt:formatNumber var="mem" value="${ machine.info.mem}" pattern="0.00"/>
							<c:if test='${applyMem/mem*100.0>100}'>
								<fmt:formatNumber var="fmtMemoryAllocatedRatio" value="100" pattern="0.00"/>
							</c:if>
							<c:if test='${applyMem/mem*100.0<=100}'>
								<fmt:formatNumber var="fmtMemoryAllocatedRatio" value="${applyMem/mem*100.0}" pattern="0.00"/>
							</c:if>
							<div class="progress margin-custom-bottom0">
								<c:choose>
									<c:when test="${applyMem/mem*100.0 > 100.00}"><c:set var="memAllocateProgressBarStatus" value="progress-bar-danger"/></c:when>
									<c:when test="${fmtMemoryAllocatedRatio >= '80.00'}"><c:set var="memAllocateProgressBarStatus" value="progress-bar-warning"/></c:when>
									<c:otherwise><c:set var="memAllocateProgressBarStatus" value="progress-bar-success"/></c:otherwise>
								</c:choose>
								<div class="progress-bar ${memAllocateProgressBarStatus}"
									 role="progressbar" aria-valuenow="${fmtMemoryAllocatedRatio}" aria-valuemax="100"
									 aria-valuemin="0" style="width: ${fmtMemoryAllocatedRatio}%">
									<label style="color: #000000">
											${applyMem}G&nbsp;&nbsp;Used/
											${mem}G&nbsp;&nbsp;Total
									</label>
								</div>
							</div>
						</td>
						<td>
							<c:choose>
								<c:when test="${machine.cpuUsage == null || machine.cpuUsage == ''}">
									收集中..${collectAlert}
								</c:when>
								<c:otherwise>
									${machine.cpuUsage}
								</c:otherwise>
							</c:choose>
						</td>
						<td>
							<c:choose>
								<c:when test="${machine.load == null || machine.load == ''}">
									收集中..${collectAlert}
								</c:when>
								<c:otherwise>
									${machine.load}
								</c:otherwise>
							</c:choose>
						</td>
						<td><fmt:formatDate value="${machine.modifyTime}" type="time" timeStyle="full" pattern="yyyy-MM-dd HH:mm"/></td>
						<th>${machine.info.room}</th>
						<th>
							<c:if test="${machine.info.type==0}">Redis机器</c:if>
							<c:if test="${machine.info.type==3}">sentinel机器</c:if>
							<c:if test="${machine.info.type==2}">Redis迁移工具</c:if>
							<c:if test="${machine.info.type==4}">Twemproxy机器</c:if>
							<c:if test="${machine.info.type==5}">Pika机器</c:if>
						</th>
						<th>
							<c:if test="${machine.info.useType==0}">专用机器</c:if>
							<c:if test="${machine.info.useType==1}">测试机器</c:if>
							<c:if test="${machine.info.useType==2}">混合部署机器</c:if>
						</th>
						<th>
								${machine.info.extraDesc}
							<c:if test="${machine.info.type == 2}">
								<font color='red'>(迁移工具机器)</font>
							</c:if>
						</th>
						<c:choose>
							<c:when test="${machine.info.collect == 1}">
								<td>开启</td>
							</c:when>
							<c:otherwise>
								<th>关闭</th>
							</c:otherwise>
						</c:choose>
						<td>
							<a href="/server/index?ip=${machine.info.ip}" class="btn btn-info" target="_blank">监控</a>
							&nbsp;
							<a href="javascript:void(0);" data-target="#addMachineModal${machine.info.id}" class="btn btn-info" data-toggle="modal">修改</a>
							&nbsp;

							<button id="removeMachineBtn${machine.info.id}" onclick="removeMachine(this.id,'${machine.info.ip}')" type="button" class="btn btn-info">删除</button>

						</td>
					</tr>
				</c:forEach>
				</tbody>
			</table>
		</div>
	</div>
</div>

<c:forEach items="${list}" var="machine">
	<%@include file="addMachine.jsp" %>
</c:forEach>
<%@include file="addMachine.jsp" %>
