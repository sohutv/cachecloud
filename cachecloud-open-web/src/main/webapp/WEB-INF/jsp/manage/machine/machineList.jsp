<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp"%>
<div class="page-container">
	<div class="page-content">
		<div class="row">
			<div class="col-md-12">
				<h3 class="page-title">
					机器管理
				</h3>
			</div>
		</div>
		<div class="row">
			<div class="col-md-12">
				<div class="portlet box light-grey">
					<div class="portlet-title">
						<div class="caption"><i class="fa fa-globe"></i>机器列表</div>
						<div class="tools">
							<a href="javascript:;" class="collapse"></a>
						</div>
					</div>
					<div class="portlet-body">
                        <div class="table-toolbar">
                            <div class="btn-group">
                                <button id="sample_editable_1_new" class="btn green" data-target="#addMachineModal" data-toggle="modal">
                                    	添加新机器 <i class="fa fa-plus"></i>
                                </button>
                            </div>
                            <div class="btn-group" style="float:right">
                                <form action="/manage/machine/list.do" method="post" class="form-horizontal form-bordered form-row-stripped">
                                    <label class="control-label">
                                        	机器ip:
                                    </label>
                                    &nbsp;<input type="text" name="ipLike" id="ipLike" value="${ipLike}" placeholder="机器ip"/>
                                    &nbsp;<button type="submit" class="btn blue btn-sm">查询</button>
                                </form>
                            </div>
                        </div>
						<table class="table table-striped table-bordered table-hover" id="tableDataList">
							<thead>
								<tr>
									<th>ip</th>
									<th>内存使用率</th>
									<th>已分配内存</th>
									<th>CPU使用率</th>
									<th>网络流量</th>
									<th>机器负载</th>
									<th>核数/实例数</th>
									<th>最后统计时间</th>
									<th>是否虚机</th>
									<th>机房</th>
									<th>额外说明</th>
									<th>状态收集</th>
                                    <th>操作</th>
								</tr>
							</thead>
							<tbody>
								<c:forEach items="${list}" var="machine">
									<tr class="odd gradeX">
										<td>
											<a target="_blank" href="/manage/machine/machineInstances.do?ip=${machine.info.ip}">${machine.info.ip}</a>
										</td>
										<td>
											<c:choose>
												<c:when test="${machine.memoryUsageRatio == null || machine.memoryUsageRatio == ''}">
													收集中..${collectAlert}
												</c:when>
												<c:otherwise>
													<span style="display:none"><fmt:formatNumber value="${machine.memoryUsageRatio / 100}" pattern="0.00"/></span>
		                                            <div class="progress margin-custom-bottom0">
			                                            <c:choose>
							                        		<c:when test="${fmtMemoryUsageRatio >= 80.00}">
																<c:set var="memUsedProgressBarStatus" value="progress-bar-danger"/>
							                        		</c:when>
							                        		<c:otherwise>
																<c:set var="memUsedProgressBarStatus" value="progress-bar-success"/>
							                        		</c:otherwise>
							                        	</c:choose>
		                                                <fmt:formatNumber var="fmtMemoryUsageRatio" value="${machine.memoryUsageRatio}" pattern="0.00"/>
		                                                <div class="progress-bar ${memUsedProgressBarStatus}"
		                                                             role="progressbar" aria-valuenow="${machine.memoryUsageRatio}" aria-valuemax="100"
		                                                             aria-valuemin="0" style="width: ${machine.memoryUsageRatio}%">
		                                                    <label style="color: #000000">
		                                                        <fmt:formatNumber value="${((machine.memoryTotal-machine.memoryFree)/1024/1024/1024)}" pattern="0.00"/>G&nbsp;&nbsp;Used/
		                                                        <fmt:formatNumber value="${ machine.memoryTotal/1024/1024/1024}" pattern="0.00"/>G&nbsp;&nbsp;Total
		                                                    </label>
		                                              	</div>
		                                             </div>
												</c:otherwise>
											</c:choose>
											
                                              
										</td>
                                        <td>
                                        <c:choose>
											<c:when test="${machine.memoryUsageRatio == null || machine.memoryUsageRatio == ''}">
												收集中..${collectAlert}
											</c:when>
											<c:otherwise>
												<fmt:formatNumber var="fmtMemoryAllocatedRatio" value="${((machine.memoryAllocated)/1024)*100.0/(machine.memoryTotal/1024/1024/1024)}" pattern="0.00"/>
	                                        	<span  style="display:none"><fmt:formatNumber value="${fmtMemoryAllocatedRatio / 100}" pattern="0.00"/></span>
	                                            <div class="progress margin-custom-bottom0">
	                                            	<c:choose>
						                        		<c:when test="${fmtMemoryAllocatedRatio >= 80.00}">
															<c:set var="memAllocateProgressBarStatus" value="progress-bar-danger"/>
						                        		</c:when>
						                        		<c:otherwise>
															<c:set var="memAllocateProgressBarStatus" value="progress-bar-success"/>
						                        		</c:otherwise>
						                        	</c:choose>
	                                                    <div class="progress-bar ${memAllocateProgressBarStatus}"
	                                                         role="progressbar" aria-valuenow="${fmtMemoryAllocatedRatio}" aria-valuemax="100"
	                                                         aria-valuemin="0" style="width: ${fmtMemoryAllocatedRatio}%">
	                                                        <label style="color: #000000">
	                                                            <fmt:formatNumber value="${((machine.memoryAllocated)/1024)}" pattern="0.00"/>G&nbsp;&nbsp;Used/
	                                                            <fmt:formatNumber value="${ machine.memoryTotal/1024/1024/1024}" pattern="0.00"/>G&nbsp;&nbsp;Total
	                                                        </label>
	                                                    </div>
	                                                </div>
											</c:otherwise>
										</c:choose>
                                            
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
											<fmt:formatNumber value="${machine.traffic / 1024 / 1024}" pattern="0.00"/>M
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
										<td>
											<fmt:formatNumber var="fmtInstanceCpuRatio" value="${machineInstanceCountMap[machine.info.ip] * 100.0 /machine.info.cpu}" pattern="0.00"/>
	                                        	<span style="display:none"><fmt:formatNumber value="${fmtInstanceCpuRatio / 100}" pattern="0.00"/></span>
	                                            <div class="progress margin-custom-bottom0">
	                                            	<c:choose>
						                        		<c:when test="${fmtInstanceCpuRatio >= 80.00}">
															<c:set var="instanceCpuProgressBarStatus" value="progress-bar-danger"/>
						                        		</c:when>
						                        		<c:otherwise>
															<c:set var="instanceCpuProgressBarStatus" value="progress-bar-success"/>
						                        		</c:otherwise>
						                        	</c:choose>
	                                                    <div class="progress-bar ${instanceCpuProgressBarStatus}"
	                                                         role="progressbar" aria-valuenow="${fmtInstanceCpuRatio}" aria-valuemax="100"
	                                                         aria-valuemin="0" style="width: ${fmtInstanceCpuRatio}%">
	                                                        <label style="color: #000000">
	                                                            <fmt:formatNumber value="${machineInstanceCountMap[machine.info.ip]}"/>&nbsp;&nbsp;实例/
	                                                            <fmt:formatNumber value="${machine.info.cpu}"/>&nbsp;&nbsp;核
	                                                        </label>
	                                                    </div>
	                                                </div>
										</td>
										<td><fmt:formatDate value="${machine.modifyTime}" type="time" timeStyle="full" pattern="yyyy-MM-dd HH:mm"/></td>
                                        <th>
                                        	<c:choose>
                                        		<c:when test="${machine.info.virtual == 1}">
                                        			是
                                        			<br/>
                                        			物理机:${machine.info.realIp}
                                        		</c:when>
                                        		<c:otherwise>
                                        			否
                                        		</c:otherwise>
                                        	</c:choose>
                                        </th>
										<th>${machine.info.room}</th>
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
                                        	<a href="/server/index.do?ip=${machine.info.ip}" class="btn btn-info" target="_blank">监控</a>
                                        	&nbsp;
                                            <a href="javascript;" data-target="#addMachineModal${machine.info.id}" class="btn btn-info" data-toggle="modal">修改</a>
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
		</div>
	</div>
    <c:forEach items="${list}" var="machine">
        <%@include file="addMachine.jsp" %>
    </c:forEach>
    <%@include file="addMachine.jsp"%>
</div>
