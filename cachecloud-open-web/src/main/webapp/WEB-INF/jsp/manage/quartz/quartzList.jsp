<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp"%>
<div class="page-container">
	<div class="page-content">
		<div class="row">
			<div class="col-md-12">
				<h3 class="page-title">
					Quartz管理
				</h3>
			</div>
		</div>
		<div class="row">
			<div class="col-md-12">
				<div class="portlet box light-grey">
					<div class="portlet-title">
						<div class="caption"><i class="fa fa-globe"></i>trigger列表</div>
						<div class="tools">
							<a href="javascript:;" class="collapse"></a>
						</div>
					</div>
					<div class="portlet-body">
                        <div class="table-toolbar">
                            <div class="btn-group" style="float:right">
                                <form action="/manage/quartz/list.do" method="post" class="form-horizontal form-bordered form-row-stripped">
                                    <label class="control-label">
                                        查询:
                                    </label>
                                    &nbsp;<input type="text" name="query" id="ipLike" value="${query}" placeholder=""/>
                                    &nbsp;<button type="submit" class="btn blue btn-sm">查询</button>
                                </form>
                            </div>
                        </div>
						<table class="table table-striped table-bordered table-hover" id="tableDataList">
							<thead>
								<tr>
									<th>triggerName</th>
									<th>triggerGroup</th>
                                    <th>cron</th>
                                    <th>nextFireDate</th>
									<th>prevFireDate</th>
                                    <th>startDate</th>
									<th>triggerState</th>
									<th>操作</th>
								</tr>
							</thead>
							<tbody>
								<c:forEach items="${triggerList}" var="t">
									<tr class="odd gradeX">
										<td>${t.triggerName}</td>
										<td>${t.triggerGroup}</td>
                                        <td>${t.cron}</td>
                                        <td>${t.nextFireDate}</td>
										<td>${t.prevFireDate}</td>
                                        <td>${t.startDate}</td>
										<td>${t.triggerState}</td>
										<td>
                                        <c:if test="${t.triggerState == 'PAUSED'}">
                                            <a onclick="if(window.confirm('确认恢复吗?!')){return true;}else{return false;}"
                                               href="/manage/quartz/resume.do?name=${t.triggerName}&group=${t.triggerGroup}">[恢复]
                                            </a>
                                        </c:if>
                                        <c:if test="${t.triggerState != 'PAUSED'}">
                                            <a onclick="if(window.confirm('确认暂停吗?!')){return true;}else{return false;}"
                                               href="/manage/quartz/pause.do?name=${t.triggerName}&group=${t.triggerGroup}">[暂停]
                                            </a>
                                        </c:if>
                                        <a onclick="if(window.confirm('确认删除吗?!')){return true;}else{return false;}"
                                               href="/manage/quartz/remove.do?name=${t.triggerName}&group=${t.triggerGroup}">[删除]
                                        </a>
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
