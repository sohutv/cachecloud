<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<div id="appRefuseModal${item.id}" class="modal fade" tabindex="-1" data-width="400">
	<div class="modal-dialog">
		<div class="modal-content">
		
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal" aria-hidden="true"></button>
				<h4 class="modal-title">审批驳回意见</h4>
			</div>
			
			<form class="form-horizontal form-bordered form-row-stripped">
				<div class="modal-body">
					<div class="row">
						<!-- 控件开始 -->
						<div class="col-md-12">
							<!-- form-body开始 -->
							<div class="form-body">
								
								<div class="form-group">
									<label class="control-label col-md-3">驳回原因:</label>
									<div class="col-md-7">
										<textarea rows="5"  name="refuseReason${item.id}" id="refuseReason${item.id}" placeholder="驳回原因 " class="form-control"></textarea>
									</div>
								</div>
							</div>
							<!-- form-body 结束 -->
						</div>
						<div id="appRefuseInfo${item.id}"></div>
						<!-- 控件结束 -->
					</div>
				</div>
				
				<div class="modal-footer">
					<button type="button" data-dismiss="modal" class="btn" >Close</button>
					<button type="button" id="appRefuseBtn${item.id}" class="btn red" onclick="appRefuse('${item.id}', '${item.type}')">提交</button>
				</div>
			
			</form>
		</div>
	</div>
</div>