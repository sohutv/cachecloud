<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp"%>
<div class="page-container">
	<div class="page-content">
		<div class="row">
			<div class="col-md-12">
				<h3 class="page-title">
					容器:${ip} [宿主机:${realIp}]变更历史
				</h3>
			</div>
		</div>
		<div class="row">
			<div class="col-md-12">
				<div class="portlet box light-grey">
					<div class="portlet-title">
						<div class="caption"><i class="fa fa-globe"></i>变更列表</div>
						<div class="tools">
							<a href="javascript:;" class="collapse"></a>
						</div>
					</div>
					<div class="portlet-body">
						<table class="table table-striped table-bordered table-hover" id="tableDataList">
							<thead>
								<tr>
									<th>ip</th>
									<th>宿主机ip</th>
									<th>pod最后变更时间</th>
									<th>pod部署名称</th>
									<th>操作状态</th>
									<th>是否同步</th>
                                    <th>操作</th>
								</tr>
							</thead>
							<tbody>
								<c:forEach items="${relationList}" var="relation">
									<tr class="odd gradeX">
										<td>
											<a target="_blank" href="/manage/machine/machineInstances?ip=${relation.ip}">${relation.ip}</a>
										</td>
                                        <td>
											${relation.realIp}
										</td>
                                        <td><fmt:formatDate value="${relation.updateTime}" type="time" timeStyle="full" pattern="yyyy-MM-dd HH:mm:ss"/></td>
										<td>
                                            ${relation.extraDesc}
										</td>
                                        <td>
                                        	<c:choose>
                                        		<c:when test="${relation.status == 0}">
													pod下线
                                        		</c:when>
                                                <c:when test="${relation.status == 1}">
													pod上线
                                        		</c:when>
                                        	</c:choose>
                                        </td>
                                        <td>
                                        	<c:choose>
												<c:when test="${relation.isSync == 0 && relation.realIp != realIp}">
													未同步
												</c:when>
												<c:when test="${relation.isSync == -1 && relation.realIp != realIp}">
													同步中...
												</c:when>
												<c:when test="${relation.isSync == -2 && relation.realIp != realIp}">
													<label style="color:red">同步失败</label>
												</c:when>
                                                <c:when test="${relation.isSync == 1}">
													<label style="color:green">已同步</label>
                                        		</c:when>
                                        	</c:choose>
                                        </td>
                                        <td>
											<c:if test="${relation.realIp != realIp && relation.isSync ==0 && relation.status == 1}">
												<button id="syncDaTa${relation.id}" onclick="syncData('${relation.realIp}','${realIp}','${ip}','${relation.id}')" type="button" class="btn btn-info">同步数据</button>
											</c:if>
											<c:if test="${relation.taskid >0}">
												<button id="syncDaTa${relation.id}" onclick="jumpTask('${relation.taskid}')" type="button" class="btn btn-success">查看同步任务</button>
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
	</div>

</div>
<script type="text/javascript">

	function syncData(sourceIp,targetIp,containerIp,relationId){

		if(sourceIp == targetIp){
			toastr.error("警告:源主机:"+sourceIp+" 和目标主机:"+targetIp+" 是同一台机器，不能同步!");
			return;
		}
		// 确认是否同步
		if(confirm("确认将源主机:"+sourceIp+" 数据目录同步到目标主机 :"+targetIp+" ？")){
			$.post('/manage/machine/pod/add/syncTask',
			{
				relationId: relationId,
				containerIp: containerIp,
				sourceIp: sourceIp,
				targetIp: targetIp
			},function(data) {
				var status = data.status;
				if (status == 1) {
					toastr.success("添加同步任务成功,请查看!");
				} else {
					toastr.error("添加同步任务失败！" + data.message);
				}
				setTimeout("window.location.reload()",100);
			});
		}else{
			toastr.error("取消同步!");
			return;
		}
    }

	//跳转任务详情页面
	function jumpTask(taskid){
		window.open("/manage/task/flow?taskId=" + taskid,'_blank');
	}

</script>