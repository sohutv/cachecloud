<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp" %>
<div class="page-container">
    <div class="page-content">
        <div class="row">
            <div class="col-md-12">
                <h4 class="page-title">
                    	资源总览
                </h4>
            </div>
        </div>

		<div class="row">
			<div class="col-md-12">
				<div class="portlet-body">
					<div class="table-toolbar">
						<table class="table table-striped table-bordered table-hover">
							<tr>
								<td><span style="font-weight:bold">在线应用数</span></td>
								<td><a target="_blank" href="/admin/app/list">${totalRunningApps}</a></td>
								<td><span style="font-weight:bold">在线实例数</span></td>
								<td>${totalRunningInstance}</td>
								<td><span style="font-weight:bold">在线机器数</span></td>
								<td><a target="_blank" href="/manage/machine/index?tabTag=machine">${totalMachineCount}</a></td>
								<td><span style="font-weight:bold">Redis版本数量</span></td>
								<td><a target="_blank" href="/manage/app/resource/index?tab=redis">${redisTypeCount}</a></td>
							</tr>
							<tr>
								<td><span style="font-weight:bold">机器总内存</span></td>
								<td><fmt:formatNumber value="${machineStatsVoList.get(0).totalMachineMem/1024}" pattern="0.00"/>G</td>
								<td><span style="font-weight:bold">机器总分配内存</span></td>
								<td><fmt:formatNumber value="${((machineStatsVoList.get(0).totalMachineMem-machineStatsVoList.get(0).totalMachineFreeMem)/1024)}" pattern="0.00"/>G</td>
								<td><span style="font-weight:bold">实例总内存</span></td>
								<td><fmt:formatNumber value="${machineStatsVoList.get(0).totalInstanceMaxMem/1024/1024/1024}" pattern="0.00"/>G</td>
								<td><span style="font-weight:bold">实例总使用内存</span></td>
								<td><fmt:formatNumber value="${machineStatsVoList.get(0).totalInstanceUsedMem/1024/1024/1024}" pattern="0.00"/>G</td>
							</tr>

						</table>
						<br/>
					</div>
				</div>
			</div>
		</div>

		<div class="col-md-12">
			<div class="portlet box light-grey">
				<div class="portlet-title">
					<div class="caption"><i class="fa fa-globe"></i>分布统计</div>
					<div class="tools">
						<a href="javascript:;" class="collapse"></a>
					</div>
				</div>
				<div class="col-md-6">
					<div id="redisDistributeContainer" style="min-width: 310px; height: 400px; margin: 0 auto"></div>
				</div>
				<div class="col-md-6">
					<div id="roomDistributeContainer" style="min-width: 310px; height: 400px; margin: 0 auto"></div>
				</div>
				<div class="col-md-6">
					<div id="machineMemoryDistributeContainer" style="min-width: 310px; height: 400px; margin: 0 auto"></div>
				</div>
				<div class="col-md-6">
					<div id="maxMemoryDistributeContainer" style="min-width: 310px; height: 400px; margin: 0 auto"></div>
				</div>
			</div>
		</div>

        <div class="row">
            <div class="col-md-12">
                <div class="portlet box light-grey">
                    <div class="portlet-title">
                        <div class="caption"><i class="fa fa-globe"></i>内存统计</div>
                        <div class="tools">
                            <a href="javascript:;" class="collapse"></a>
                        </div>
                    </div>
                    <div class="portlet-body">

                        <div class="table-toolbar">
                            <table class="table table-striped table-bordered table-hover">
                            	<thead>
									<tr>
										<th>机房</th>
										<th>机器使用内存(使用内存/总内存)</th>
										<th>实例分配内存(使用内存/总内存)</th>
									</tr>
								</thead>
								<tbody>
									<c:forEach items="${machineStatsVoList}" var="machineStatsVo">
			                			<tr>
		                                	<td>${machineStatsVo.machineRoom}</td>
		                                	<td>
		                                		<div class="progress margin-custom-bottom0">
		                                            <c:choose>
						                        		<c:when test="${machineStatsVo.machineMemUsedRatio >= 70.00 && machineStatsVo.machineMemUsedRatio <= 90.00}">
															<c:set var="memUsedProgressBarStatus" value="progress-bar-warning"/>
						                        		</c:when>
						                        		<c:when test="${machineStatsVo.machineMemUsedRatio >= 90.00}">
															<c:set var="memUsedProgressBarStatus" value="progress-bar-danger"/>
						                        		</c:when>
						                        		<c:otherwise>
															<c:set var="memUsedProgressBarStatus" value="progress-bar-success"/>
						                        		</c:otherwise>
						                        	</c:choose>
		                                            <div class="progress-bar ${memUsedProgressBarStatus}"
		                                                            role="progressbar" aria-valuenow="${machineStatsVo.machineMemUsedRatio}" aria-valuemax="100"
		                                                            aria-valuemin="0" style="width: ${machineStatsVo.machineMemUsedRatio}%">
		                                                   <label style="color: #000000">
		                                                       <fmt:formatNumber value="${((machineStatsVo.totalMachineMem-machineStatsVo.totalMachineFreeMem)/1024)}" pattern="0.00"/>G&nbsp;&nbsp;Used/<fmt:formatNumber value="${machineStatsVo.totalMachineMem/1024}" pattern="0.00"/>G&nbsp;&nbsp;Total
		                                                   </label>
		                                            </div>
		                                        </div>
		                                	</td>
		                                	<td>
		                                		<div class="progress margin-custom-bottom0">
		                                            <c:choose>
						                        		<c:when test="${machineStatsVo.instanceMemUsedRatio >= 70.00 && machineStatsVo.instanceMemUsedRatio <= 90.00}">
															<c:set var="memUsedProgressBarStatus" value="progress-bar-warning"/>
						                        		</c:when>
						                        		<c:when test="${machineStatsVo.instanceMemUsedRatio >= 90.00}">
															<c:set var="memUsedProgressBarStatus" value="progress-bar-danger"/>
						                        		</c:when>
						                        		<c:otherwise>
															<c:set var="memUsedProgressBarStatus" value="progress-bar-success"/>
						                        		</c:otherwise>
						                        	</c:choose>
		                                            <div class="progress-bar ${memUsedProgressBarStatus}"
		                                                            role="progressbar" aria-valuenow="${machineStatsVo.instanceMemUsedRatio}" aria-valuemax="100"
		                                                            aria-valuemin="0" style="width: ${machineStatsVo.instanceMemUsedRatio}%">
		                                                   <label style="color: #000000">
		                                                       <fmt:formatNumber value="${machineStatsVo.totalInstanceUsedMem/1024/1024/1024}" pattern="0.00"/>G&nbsp;&nbsp;Used/<fmt:formatNumber value="${machineStatsVo.totalInstanceMaxMem/1024/1024/1024}" pattern="0.00"/>G&nbsp;&nbsp;Total
		                                                   </label>
		                                            </div>
		                                        </div>
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

        <div class="row">
            <div class="col-md-12">
                <div class="portlet box light-grey">
                    <div class="portlet-title">
                        <div class="caption"><i class="fa fa-globe"></i>调度统计</div>
                        <div class="tools">
                            <a href="javascript:;" class="collapse"></a>
                        </div>
                    </div>
                    <div class="portlet-body">

                        <div class="table-toolbar">
                            <table class="table table-striped table-bordered table-hover">
                                <tr>
                                    <td>trigger总数:</td>
                                    <td>
                                    	<c:choose>
                                    		<c:when test="${triggerTotalCount > 0}">
	                                    		<a target="_blank" href="/manage/quartz/list">${triggerTotalCount}</a>
	                                    	</c:when>
	                                    	<c:otherwise>
	                                    		0
	                                    	</c:otherwise>
                                    	</c:choose>
                                    </td>
                                    <td>waiting个数:</td>
                                    <td>
                                    	<c:choose>
                                    		<c:when test="${triggerWaitingCount > 0}">
	                                    		<a target="_blank" href="/manage/quartz/list?triggerState=WAITING">${triggerWaitingCount}</a>
	                                    	</c:when>
	                                    	<c:otherwise>
	                                    		0
	                                    	</c:otherwise>
                                    	</c:choose>
                                    </td>
                                    <td>error个数:</td>
                                    <td>
                                    	<c:choose>
                                    		<c:when test="${triggerErrorCount > 0}">
	                                    		<a target="_blank" href="/manage/quartz/list?triggerState=ERROR">${triggerErrorCount}</a>
	                                    	</c:when>
	                                    	<c:otherwise>
	                                    		0
	                                    	</c:otherwise>
                                    	</c:choose>
                                    </td>
                                    <td>paused个数:</td>
                                    <td>
                                    	<c:choose>
                                    		<c:when test="${triggerPausedCount > 0}">
	                                    		<a target="_blank" href="/manage/quartz/list?triggerState=PAUSED">${triggerPausedCount}</a>
	                                    	</c:when>
	                                    	<c:otherwise>
	                                    		0
	                                    	</c:otherwise>
                                    	</c:choose>
                                    </td>
                                </tr>
                                <tr>
                                    <td>acquired个数:</td>
                                    <td>
                                    	<c:choose>
                                    		<c:when test="${triggerAcquiredCount > 0}">
	                                    		<a target="_blank" href="/manage/quartz/list?triggerState=ACQUIRED">${triggerAcquiredCount}</a>
	                                    	</c:when>
	                                    	<c:otherwise>
	                                    		0
	                                    	</c:otherwise>
                                    	</c:choose>
                                    </td>
                                    <td>blocked个数</td>
                                    <td>
                                    	<c:choose>
                                    		<c:when test="${triggerBlockedCount > 0}">
	                                    		<a target="_blank" href="/manage/quartz/list?triggerState=BLOCKED">${triggerBlockedCount}</a>
	                                    	</c:when>
	                                    	<c:otherwise>
	                                    		0
	                                    	</c:otherwise>
                                    	</c:choose>
                                    </td>
                                    <td>misfireCount个数:</td>
                                    <td>
                                    	<c:choose>
                                    		<c:when test="${misfireCount > 0}">
	                                    		<a target="_blank" href="/manage/quartz/list?misFireState=1">${misfireCount}</a>
	                                    	</c:when>
	                                    	<c:otherwise>
	                                    		0
	                                    	</c:otherwise>
                                    	</c:choose>
                                    </td>
                                </tr>

                            </table>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        
        <div class="row">
            <div class="col-md-12">
                <div class="portlet box light-grey">
                    <div class="portlet-title">
                        <div class="caption"><i class="fa fa-globe"></i>任务统计</div>
                        <div class="tools">
                            <a href="javascript:;" class="collapse"></a>
                        </div>
                    </div>
                    <div class="portlet-body">

                        <div class="table-toolbar">
                            <table class="table table-striped table-bordered table-hover">
                                <tr>
                                    <td>任务总数:</td>
                                    <td>
                                    	<c:choose>
                                    		<c:when test="${totalTaskCount > 0}">
	                                    		<a target="_blank" href="/manage/task/list">${totalTaskCount}</a>
	                                    	</c:when>
	                                    	<c:otherwise>
	                                    		0
	                                    	</c:otherwise>
                                    	</c:choose>
                                    </td>
                                    <td>新任务数:</td>
                                    <td>
                                    	<c:choose>
                                    		<c:when test="${newTaskCount > 0}">
	                                    		<a target="_blank" href="/manage/task/list?status=0">${newTaskCount}</a>
	                                    	</c:when>
	                                    	<c:otherwise>
	                                    		0
	                                    	</c:otherwise>
                                    	</c:choose>
                                    </td>
                                    <td>运行中任务数:</td>
                                    <td>
                                    	<c:choose>
                                    		<c:when test="${runningTaskCount > 0}">
	                                    		<a target="_blank" href="/manage/task/list?status=1">${runningTaskCount}</a>
	                                    	</c:when>
	                                    	<c:otherwise>
	                                    		0
	                                    	</c:otherwise>
                                    	</c:choose>
                                    </td>
                                    <td>中断任务数:</td>
                                    <td>
                                    	<c:choose>
                                    		<c:when test="${abortTaskCount > 0}">
	                                    		<a target="_blank" href="/manage/task/list?status=2">${abortTaskCount}</a>
	                                    	</c:when>
	                                    	<c:otherwise>
	                                    		0
	                                    	</c:otherwise>
                                    	</c:choose>
                                    </td>
                                    <td>成功任务数:</td>
                                    <td>
                                    	<c:choose>
                                    		<c:when test="${successTaskCount > 0}">
	                                    		<a target="_blank" href="/manage/task/list?status=4">${successTaskCount}</a>
	                                    	</c:when>
	                                    	<c:otherwise>
	                                    		0
	                                    	</c:otherwise>
                                    	</c:choose>
                                    </td>
                                </tr>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
<script type="text/javascript">
	generateDataPieChart('machineMemoryDistributeContainer', '机器内存使用率',${machineMemoryDistributeList});
	generateDataPieChart('maxMemoryDistributeContainer', '机器内存分配率',${maxMemoryDistributeList});
	generateDataPieChart('redisDistributeContainer', '应用Redis版本分布',${redisDistributeList});
	generateDataPieChart('roomDistributeContainer', '机房机器分布',${roomDistributeList});
</script>
