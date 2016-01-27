<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp"%>


<div class="page-container">
	<div class="page-content">
		
		<%@include file="machineForHorizontalScaleList.jsp" %>
		
		<div class="row">
			<div class="col-md-12">
				<h3 class="page-title">
					应用水平扩容(申请详情:<font color="red">${appAudit.info}</font>)
				</h3>
			</div>
		</div>
		<div class="row">
			<div class="col-md-12">
				<div class="portlet box light-grey">
					<div class="portlet-title">
						<div class="caption">
							<i class="fa fa-globe"></i>
							填写扩容配置:
							&nbsp;
						</div>
						<div class="tools">
							<a href="javascript:;" class="collapse"></a>
							<a href="javascript:;" class="remove"></a>
						</div>
					</div>
					<div class="portlet-body">
						<div class="form">
								<!-- BEGIN FORM-->
								<form action="/manage/app/addAppClusterSharding.do" method="post"
									class="form-horizontal form-bordered form-row-stripped" onsubmit="return checkAddShardParam()">
									<div class="form-body">
										<div class="form-group">
											<label class="control-label col-md-3">主从分片配置:</label>
											<div class="col-md-5">
												<input type="text" name="masterSizeSlave" id="masterSizeSlave" placeholder="materIp:memSize:slaveIp" class="form-control">
											</div>
										</div>
										<input type="hidden" name="appAuditId" value="${appAudit.id}">
										
										<div class="form-actions fluid">
											<div class="row">
												<div class="col-md-12">
													<div class="col-md-offset-3 col-md-3">
														<button type="submit" class="btn green">
															<i class="fa fa-check"></i>
															提交
														</button>
														&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
														<a target="_blank" class="btn green" href="/manage/app/handleHorizontalScale?appAuditId=${appAudit.id}">ReShard页面</a>
													</div>
												</div>
											</div>
										</div>


									</div>
								</form>
								<!-- END FORM-->
							</div>
					</div>
				</div>
				<!-- END EXAMPLE TABLE PORTLET-->
			</div>
		</div>
	</div>
</div>

