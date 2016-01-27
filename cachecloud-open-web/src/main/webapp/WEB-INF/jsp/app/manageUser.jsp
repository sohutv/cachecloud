<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<div id="manageUser" class="modal fade" tabindex="-1" data-width="400">
	<div class="modal-dialog">
		<div class="modal-content">
		
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal" aria-hidden="true"></button>
				<h4 class="modal-title">添加用户</h4>
			</div>
			
			<form action="/user/add.do" method="post" class="form-horizontal form-bordered form-row-stripped">
				<div class="modal-body">
					<div class="row">
						<!-- 控件开始 -->
						<div class="col-md-12">
							<div class="form-body">
								<div class="form-group">
									<label class="control-label col-md-3">
										用户名:
									</label>
									<div class="col-md-5">
										<input type="text" name="name" id="user.name"
											value="${user.name}" placeholder="用户名"
											class="form-control" />
									</div>
								</div>
								
								<div class="form-group">
									<label class="control-label col-md-3">
										邮箱:
									</label>
									<div class="col-md-5">
										<input type="text" name="email" id="user.email"
											value="${user.email}" placeholder="邮箱"
											class="form-control" />
									</div>
								</div>
								
								<div class="form-group">
									<label class="control-label col-md-3">
										手机:
									</label>
									<div class="col-md-5">
										<input type="text" name="mobile" id="user.mobile"
											value="${user.mobile}" placeholder="手机"
											class="form-control" />
									</div>
								</div>
							</div>
						</div>
						<!-- 控件结束 -->
					</div>
				</div>
				
				<div class="modal-footer">
					<button type="button" data-dismiss="modal" class="btn">Close</button>
					<button type="submit" class="btn red">Ok</button>
				</div>
			
			</form>
		</div>
	</div>
</div>