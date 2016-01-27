<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp"%>
<c:forEach items="${apps}" var="item">
	<div id="appScaleApply${item.appId}" class="modal fade" tabindex="-1" data-width="400">
		<div class="modal-dialog">
			<div class="modal-content">
			
				<div class="modal-header">
					<button type="button" class="close" data-dismiss="modal" aria-hidden="true"></button>
					<h4 class="modal-title">申请扩容</h4>
				</div>
				
				<form class="form-horizontal form-bordered form-row-stripped">
					<div class="modal-body">
						<div class="row">
							<!-- 控件开始 -->
							<div class="col-md-12">
								<!-- form-body开始 -->
								<div class="form-body">
									
									<div class="form-group">
										<label class="control-label col-md-3">
											每个分片容量:
										</label>
										<div class="col-md-5">
											<input type="text" name="memSize" id="memSize${item.appId}"
												value="${memSize}"  placeholder="每个分片容量"
												class="form-control" />
											<span class="help-block">
												追加的单位容量（256，512，1024）
											</span>
										</div>
									</div>
									<input type="hidden" id="appId" name="appId" value="${item.appId}"/>
								</div>
								<!-- form-body 结束 -->
							</div>
							<div id="info${item.appId}"></div>
							<!-- 控件结束 -->
						</div>
					</div>
					
					<div class="modal-footer">
						<button type="button" data-dismiss="modal" class="btn" >Close</button>
						<button type="button" class="btn red" onclick="appScaleApply('${item.appId}')">Ok</button>
					</div>
				
				</form>
			</div>
		</div>
	</div>
</c:forEach>
