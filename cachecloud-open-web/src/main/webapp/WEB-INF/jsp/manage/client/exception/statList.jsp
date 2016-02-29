<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp"%>
<div class="page-container">
	<div class="page-content">
		<div class="row">
			<div class="col-md-12">
				<h3 class="page-title">
					近一个月客户端异常统计
				</h3>
			</div>
		</div>
		<div class="row">
			<div class="col-md-12">
				<div class="portlet box light-grey">
					<div class="portlet-title">
						<div class="caption"><i class="fa fa-globe"></i>客户端异常统计</div>
						<div class="tools">
							<a href="javascript:;" class="collapse"></a>
						</div>
					</div>
					<div class="portlet-body">
                        <div class="table-toolbar">
                            <div class="btn-group" style="float:right">
                                <form action="/manage/client/exception" method="post" class="form-horizontal form-bordered form-row-stripped">
                                    <label class="control-label">
                                        	机器ip:
                                    </label>
                                    &nbsp;<input type="text" name="ip" id="ip" value="${ip}" placeholder="机器ip"/>
                                    &nbsp;<button type="submit" class="btn blue btn-sm">查询</button>
                                </form>
                            </div>
                        </div>
						<table class="table table-striped table-bordered table-hover" id="tableDataList">
							<thead>
								<tr>
									<th>应用id</th>
									<th>实例id</th>
									<th>实例ip</th>
									<th>实例port</th>
									<th>异常数</th>
									<th>应用负责人</th>
								</tr>
							</thead>
							<tbody>
								<c:forEach items="${clientInstanceExceptionList}" var="clientInstanceException">
									<tr class="odd gradeX">
										<td>
											<a target="_blank" href="/admin/app/index.do?appId=${clientInstanceException.appId}">${clientInstanceException.appId}</a>
										</td>
										<td>
											<a href="/admin/instance/index.do?instanceId=${clientInstanceException.instanceId}" target="_blank">${clientInstanceException.instanceId}</a>
										</td>
										<td>${clientInstanceException.instanceHost}</td>
										<td>${clientInstanceException.instancePort}</td>
										<td>${clientInstanceException.exceptionCount}</td>
										<td>${appIdOwnerMap[clientInstanceException.appId]}</td>
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
