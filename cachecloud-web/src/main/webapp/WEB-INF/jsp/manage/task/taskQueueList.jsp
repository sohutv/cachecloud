<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp"%>

<div class="page-container">
	<div class="page-content">
		<div class="row">
			<div class="col-md-12">
				<h3 class="page-title">
					任务管理
				</h3>
			</div>
		</div>
		<div class="row">
			<div class="col-md-12">
                        <div>
                            <div class="btn-group" style="float:right">
                                <form action="/manage/task/list" method="get" id="taskQueueList" class="form-horizontal form-bordered form-row-stripped">
                                	<label class="control-label">
                                        任务id:
                                    </label>
                                    &nbsp;<input type="text" name="searchTaskId" id="searchTaskId" value="${searchTaskId}" placeholder="" onchange="testisNum(this.id)"/>
                                	
                                	<label class="control-label">
                                        appId:
                                    </label>
                                    &nbsp;<input type="text" name="appId" id="appId" value="${taskSearch.appId}" placeholder="" onchange="testisNum(this.id)"/>
                                    
                                    <label class="control-label">
                                        类名:
                                    </label>
                                    &nbsp;<input type="text" name="className" id="className" value="${taskSearch.className}" placeholder="" />
                                    
                                    <label class="control-label">
										状态:
									</label>
									<select name="status">
										<option value="-1">
											全部
										</option>
										<option value="0" <c:if test="${taskSearch.status == 0}">selected="selected"</c:if>>
											新任务
										</option>
										<option value="1" <c:if test="${taskSearch.status == 1}">selected="selected"</c:if>>
											运行中
										</option>
										<option value="2" <c:if test="${taskSearch.status == 2}">selected="selected"</c:if>>
											中断
										</option>
										<option value="4" <c:if test="${taskSearch.status == 4}">selected="selected"</c:if>>
											成功
										</option>
										<option value="5" <c:if test="${taskSearch.status == 5}">selected="selected"</c:if>>
											准备
										</option>
									</select>
                                    <input type="hidden" name="pageNo" id="pageNo">
                                    &nbsp;<button type="submit" class="btn blue btn-sm">查询</button>
                                </form>
                            </div>
                        </div>
                        <br/><br/><br/>
						<table class="table table-striped table-bordered table-hover">
							<thead>
								<tr>
									<th>任务id</th>
									<th>集群id</th>
									<th>类名</th>
									<th>信息</th>
									<th>状态</th>
									<th>进度</th>
									<th>父任务id</th>
									<th>耗时(s)</th>
									<th>开始时间</th>
									<th>结束时间</th>
									<th>创建时间</th>
									<th>备注</th>
									<th>操作</th>
								</tr>
							</thead>
							<tbody>
								<c:forEach items="${taskQueueList}" var="taskQueue">
									<tr class="odd gradeX">
										<td>${taskQueue.id}</td>
										<td>${taskQueue.appId}</td>
										<td>${taskQueue.className}</td>
										<td>
											<c:choose>
												<c:when test="${taskQueue.className == 'TwemproxyToTwemproxyTaskV2'}">
													<a target="_blank" href="/manage/migrate/list">${taskQueue.importantInfo}</a>
												</c:when>
												<c:otherwise>
													${taskQueue.importantInfo}
												</c:otherwise>
											</c:choose>
										
										</td>
										<td>
											<c:choose>
												<c:when test="${taskQueue.status == 2}">
													<font color='red'>${taskQueue.statusDesc}</font>
												</c:when>
												<c:otherwise>
													${taskQueue.statusDesc}
												</c:otherwise>
											</c:choose>
										</td>
										<td>${taskQueue.progress}</td>
										<td>
											<c:choose>
												<c:when test="${taskQueue.parentTaskId != 0}">
													${taskQueue.parentTaskId}
												</c:when>
											</c:choose>
										</td>
										<td>${taskQueue.costSeconds}</td>
										<td>
											<c:if test="${taskQueue.status != 0}">
												<fmt:formatDate pattern="yyyy-MM-dd HH:mm:ss" value="${taskQueue.startTime}"/>
											</c:if>
										</td>
										<td>
											<c:if test="${taskQueue.status == 4}">
												<fmt:formatDate pattern="yyyy-MM-dd HH:mm:ss" value="${taskQueue.endTime}"/>
											</c:if>
										</td>
										<td>
											<fmt:formatDate pattern="yyyy-MM-dd HH:mm:ss" value="${taskQueue.createTime}"/>
										</td>
										<td>${taskQueue.taskNote}</td>
										<td>
											<a href="/manage/task/flow?taskId=${taskQueue.id}">[执行步骤]</a>
											<c:if test="${taskQueue.status == 2}">
												<a href="/manage/task/execute?taskId=${taskQueue.id}">[重试任务]</a>
											</c:if>
										</td>
									</tr>
								</c:forEach>
							</tbody>
						</table>
			</div>
			<div style="margin-bottom: 10px;float: right;margin-right: 15px">
                <span>
                    <ul id='ccPagenitor' style="margin-bottom: 0px;margin-top: 0px"></ul>
                    <div id="pageDetail" style="float:right;padding-top:7px;padding-left:8px;color:#4A64A4;display: none">共${page.totalPages}页,${page.totalCount}条</div>
                </span>
            </div>
		</div>
	</div>
</div>
<script src="/resources/bootstrap/paginator/bootstrap-paginator.js"></script>
<script src="/resources/bootstrap/paginator/custom-pagenitor.js"></script>
<script type="text/javascript">
    $(function(){
            //分页点击函数
            var pageClickedFunc = function (e, originalEvent, type, page){
                //form传参用pageSize
                document.getElementById("pageNo").value=page;
                document.getElementById("taskQueueList").submit();
            };
            //分页组件
            var element = $('#ccPagenitor');
            //当前page号码
            var pageNo = '${page.pageNo}';
            //总页数
            var totalPages = '${page.totalPages}';
            //显示总页数
            var numberOfPages = '${page.numberOfPages}';
            var options = generatePagenitorOption(pageNo, numberOfPages, totalPages, pageClickedFunc);
            if(totalPages > 0){
                element.bootstrapPaginator(options);
                document.getElementById("pageDetail").style.display = "";
            }else{
                element.html("未查询到相关记录！");
            }
    });
</script>
