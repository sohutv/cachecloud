<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp"%>
<div class="page-container">
	<div class="page-content">
		<div class="row">
			<div class="col-md-12">
				<h3 class="page-title">
					客户端版本统计
				</h3>
			</div>
		</div>
		<div class="row">
			<div class="col-md-12">
				<div class="portlet box light-grey">
					<div class="portlet-title">
						<div class="caption"><i class="fa fa-globe"></i>客户端版本统计</div>
						<div class="tools">
							<a href="javascript:;" class="collapse"></a>
						</div>
					</div>
					<div class="portlet-body">
						<div class="table-toolbar">
                            <div class="btn-group" style="float:right">
                                <form action="/manage/client/version" method="post" class="form-horizontal form-bordered form-row-stripped">
                                    <label class="control-label">
                                        	应用id:
                                    </label>
                                    &nbsp;<input type="text" name="appId" id="appId" value="${appId}" placeholder="应用id"/>
                                    &nbsp;<button type="submit" class="btn blue btn-sm">查询</button>
                                </form>
                            </div>
                        </div>
						<table class="table table-striped table-bordered table-hover" id="tableDataList">
							<thead>
								<tr>
									<th>应用id</th>
									<th>应用名</th>
									<th>客户端ip</th>
									<th>客户端版本</th>
									<th>上报时间</th>
									<th>应用负责人</th>
								</tr>
							</thead>
							<tbody>
								<c:forEach items="${appClientVersionList}" var="appClientVersion">
									<tr class="odd gradeX">
										<td>
											<a target="_blank" href="/admin/app/index.do?appId=${appClientVersion.appId}">${appClientVersion.appId}</a>
										</td>
										<td>
											${appIdNameMap[appClientVersion.appId]}
										</td>
										<td>${appClientVersion.clientIp}</td>
										<td>${appClientVersion.clientVersion}</td>
										<td>
											<fmt:formatDate value="${appClientVersion.reportTime}" type="time" timeStyle="full" pattern="yyyy-MM-dd HH:mm"/></td>
										</td>
										<td>${appIdOwnerMap[appClientVersion.appId]}</td>
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
