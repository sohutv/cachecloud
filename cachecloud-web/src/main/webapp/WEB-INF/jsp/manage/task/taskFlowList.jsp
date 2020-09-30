<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp"%>

<script type="text/javascript">

function skipTaskFlow(taskFlowId) {
	var targetStatus = 5;
	if(confirm("确认要将id=" + taskFlowId + "任务流跳过吗?")){
		$.post(
			'/manage/task/changeTaskFlowStatus.json',
			{
				taskFlowId: taskFlowId,
				status: targetStatus
			},
	        function(data){
	            if(data.result){
	            	alert("操作成功");
	            }else{
	            	alert("操作失败,message=" + data.message);
	            	alert(data.message);
	            }
	        }
	     );
           window.location.reload();
	}
}


function changeParam(){
	var prettyParamText = document.getElementById("prettyParam");
    if(prettyParamText.value == ""){
        alert("配置参数不能为空");
        prettyParamText.focus();
        return false;
    }
    var changeParamBtn = document.getElementById("changeParamBtn");
    changeParamBtn.disabled = true;
    
	$.post(
		'/manage/task/changeParam.json',
		{
			taskId: taskId.value,
			prettyParamText: prettyParamText.value
		},
        function(data){
            if(data.result){
                $("#changeParamInfo").html("<div class='alert alert-error' ><button class='close' data-dismiss='alert'>×</button><strong>Success!</strong>更新成功，窗口会自动关闭</div>");
                var targetId = "#changeParamModal";
                setTimeout("$('" + targetId +"').modal('hide');window.location.reload();",1000);
            }else{
            	alert(data.message);
            	changeParamBtn.disabled = false;
                $("#changeParamInfo").html("<div class='alert alert-error' ><button class='close' data-dismiss='alert'>×</button><strong>Error!</strong>更新失败！</div>");
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
					任务流详情
				</h3>
			</div>
		</div>
		
		<div class="row">
			<div class="col-md-12">
				<div class="portlet box light-grey">
					<div class="portlet-title">
						<div class="caption"><i class="fa fa-globe"></i>基本信息</div>
						<div class="tools">
							<a href="javascript:;" class="collapse"></a>
						</div>
					</div>
					<div class="portlet-body">
						<table class="table table-striped table-bordered table-hover">
								<tr>
					                <td>任务id</td>
					                <td>${taskQueue.id}</td>
					                <td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>
								</tr>
								<tr>
					                <td>集群id</td>
					                <td>
					                	<a target="_blank" href="/manage/task/list?appId=${taskQueue.appId}">${taskQueue.appId}</a>
					                	&nbsp;<a target="_blank" href="/admin/app/index?appId=${taskQueue.appId}">[前台]</a>
					                	&nbsp;<a target="_blank" href="/manage/app/index?appId=${taskQueue.appId}">[运维]</a>
										<c:if test="${taskQueue.status == 2}">
											&nbsp;<a href="/manage/task/execute?taskId=${taskQueue.id}">[重试任务]</a>
										</c:if>
										<c:if test="${appDesc.type == 1 or appDesc.type == 4}">
				                           &nbsp;<a target="_blank" href="/manage/app/appNutCrackerStat?appId=${taskQueue.appId}">[代理]</a>
                        				</c:if>
					                </td>
					                <td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>
								</tr>
								<tr>
					                <td>重要信息</td>
					                <td>${taskQueue.importantInfo}</td>
					                <td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>
								</tr>
								<tr>
					                <td>执行机器</td>
					                <td>
					                	${taskQueue.executeIpPort}
					                </td>
					                <td></td>
								</tr>
								<tr>
					                <td>任务名</td>
					                <td>${taskQueue.className}</td>
					                <td></td>
								</tr>
								<tr>
					                <td>进度</td>
					                <td>
					                	${taskQueue.progress}
					                </td>
					                <td></td>
								</tr>
								<tr>
					                <td>任务初始参数</td>
					                <td>
					                	${taskQueue.initParam}
					                </td>
					                <td>
					                </td>
								</tr>
								<tr>
					                <td>任务动态参数</td>
					                <td>
					                	${taskQueue.prettyParam}
					                </td>
					                <td>
					                	<button type="button" class="btn btn-success" data-target="#changeParamModal" data-toggle="modal">修改</button>
					                </td>
								</tr>
						</table>
					</div>
				</div>
			</div>
		</div>
		
		
		<div class="row">
			<div class="col-md-12">
				<div class="portlet box light-grey">
					<div class="portlet-title">
						<div class="caption"><i class="fa fa-globe"></i>任务列表</div>
						<div class="tools">
							<a href="javascript:;" class="collapse"></a>
						</div>
					</div>
					<div class="portlet-body">
						<table class="table table-striped table-bordered table-hover" id="tableDataList">
							<thead>
								<tr>
									<th>序号</th>
									<th>任务流id</th>
									<th>步骤名称</th>
									<th>超时时间(s)</th>
									<th>执行时间(s)</th>
									<th>执行IP</th>
									<th>状态</th>
									<th>日志</th>
									<th>开始时间</th>
									<th>结束时间</th>
									<th>步骤描述</th>
									<th>运维建议</th>
									<th>操作</th>
								</tr>
							</thead>
							<tbody>
								<c:forEach items="${taskStepFlowList}" var="taskStepFlow">
									<tr class="odd gradeX">
										<td>${taskStepFlow.orderNo}</td>
										<td>${taskStepFlow.id}</td>
										<td>${taskStepFlow.stepName}</td>
										<td>${taskStepFlow.taskStepMeta.timeout}</td>
										<td>${taskStepFlow.costSeconds}</td>
										<td>${taskStepFlow.executeIpPort}</td>
										<td>${taskStepFlow.statusDesc}</td>
										<td>
											<a href="#${taskStepFlow.stepName}">查看</a>
										</td>
										<td>
											<fmt:formatDate pattern="yyyy-MM-dd HH:mm:ss" value="${taskStepFlow.startTime}"/>
										</td>
										<td>
											<fmt:formatDate pattern="yyyy-MM-dd HH:mm:ss" value="${taskStepFlow.endTime}"/>
										</td>
										<td>${taskStepFlow.taskStepMeta.stepDesc}</td>
										<td>${taskStepFlow.taskStepMeta.opsDevice}</td>
										<td>
											<c:if test="${taskStepFlow.status == 0 || taskStepFlow.status == 1 || taskStepFlow.status == 2}">
					                			<button type="button" class="btn btn-success btn-sm" onclick="skipTaskFlow('${taskStepFlow.id}')">跳过</button>
											</c:if>
										</td>
										
									</tr>
								</c:forEach>
							</tbody>
						</table>
					</div>
				</div>
			</div>
		</div>
		
		
		<div class="row">
			<div class="col-md-12">
				<div class="portlet box light-grey">
					<div class="portlet-title">
						<div class="caption"><i class="fa fa-globe"></i>详细日志</div>
						<div class="tools">
							<a href="javascript:;" class="collapse"></a>
						</div>
					</div>
					<div class="portlet-body">
						<table class="table table-striped table-bordered table-hover" id="tableDataList">
							<thead>
								<tr>
									<th>序号</th>
									<th>步骤名称</th>
									<th>日志</th>
								</tr>
							</thead>
							<tbody>
								<c:forEach items="${taskStepFlowList}" var="taskStepFlow" varStatus="stat">
									<c:set var="stepName" value="${taskStepFlow.stepName}"/>
									<tr class="odd gradeX" id="${stepName}">
										<td>${stat.index + 1}</td>
										<td>${stepName}</td>
										<td>
										<c:forEach items="${stepLogListMap[stepName]}" var="line">
											${line}<br/>
										</c:forEach>
										</td>
									</tr>
								</c:forEach>
							</tbody>
						</table>
					</div>
				</div>
			</div>
		</div>
		
	</div>
</div>

<div id="changeParamModal" class="modal fade" tabindex="-1" data-width="400">
	<div class="modal-dialog">
		<div class="modal-content">
		
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal" aria-hidden="true"></button>
				<h4 class="modal-title">动态配置修改</h4>
			</div>
			
			<form class="form-horizontal form-bordered form-row-stripped">
				<div class="modal-body">
					<div class="row">
						<!-- 控件开始 -->
						<div class="col-md-12">
							<!-- form-body开始 -->
							<div class="form-body">
								<div class="form-group">
										<textarea rows="20" name="prettyParam" id="prettyParam" class="form-control">${taskQueue.prettyParam}</textarea>
								</div>
								<input type="hidden" name="taskId" id="taskId" value="${taskQueue.id}"/>
							</div>
							<!-- form-body 结束 -->
						</div>
						<div id="changeParamInfo"></div>
						<!-- 控件结束 -->
					</div>
				</div>
				
				<div class="modal-footer">
					<button type="button" data-dismiss="modal" class="btn" >Close</button>
					<button type="button" id="changeParamBtn" class="btn red" onclick="changeParam()">Ok</button>
				</div>
			
			</form>
		</div>
	</div>
</div>

