<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp"%>
<div class="page-container">
	<div class="page-content">
		<div class="row">
			<div class="col-md-12">
				<div class="portlet box light-grey">
					<div class="portlet-title">
						<div class="caption"><i class="fa fa-globe"></i>故障列表</div>
						<div class="tools">
							<a href="javascript:;" class="collapse"></a>
						</div>
					</div>
					<div class="portlet-body">
						<table class="table table-striped table-bordered table-hover" id="tableDataList">
							<thead>
								<tr>
									<th>AppID</th>
									<th>名称</th>
									<th>IP</th>
									<th>PORT</th>
									<th>M/S</th>
									<th>Time</th>
									<th>联系人</th>
									<th>IsMemCloud</th>
									<th>IsRecover</th>
									<th>原因</th>
								</tr>
							</thead>
							<tbody>
								<c:forEach items="${faults}" var="item">
									<tr class="odd gradeX">
										<td>${item.appId}</td>
										<td>2</td>
										<td>${item.ip}</td>
										<td>${item.port}</td>
										<td>5</td>
										<td>${item.createTime}</td>
										<td>7</td>
										<td>${item.isMemcloud}</td>
										<td>7</td>
										<td>${item.reason}</td>
									</tr>
								</c:forEach>
								
							</tbody>
						</table>
					</div>
				</div>
				<!-- END EXAMPLE TABLE PORTLET-->
			</div>
		</div>
	</div>
</div>