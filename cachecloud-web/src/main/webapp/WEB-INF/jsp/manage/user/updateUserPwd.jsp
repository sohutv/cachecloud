<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<div id="updateUserPwdModal${user.id}" class="modal fade" tabindex="-1" data-width="400">
	<div class="modal-dialog">
		<div class="modal-content">

			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal" aria-hidden="true"></button>
				<h4 class="modal-title">管理用户</h4>
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
										域账户名:
									</label>
									<div class="col-md-5">
										<input type="text" name="name" id="updateUserPwdName${user.id}"
											   value="${user.name}" placeholder="域账户名(邮箱前缀)"
											   class="form-control" disabled/>
									</div>
								</div>

								<div class="form-group">
									<label class="control-label col-md-3">
										密码:
									</label>
									<div class="col-md-5">
										<input type="password" name="password" id="updateUserPwdPassword${user.id}" placeholder="输入密码"
											   class="form-control" onchange="checkPassword('${user.id}')"/>
										<span class="help-block">密码中必须包含字母、数字，至少8个字符</span>
									</div>
								</div>

								<div class="form-group">
									<label class="control-label col-md-3">
										确认密码:
									</label>
									<div class="col-md-5">
										<input type="password" name="password0" id="updateUserPwdPassword0${user.id}" placeholder="再次输入密码"
											   class="form-control" onchange="checkConfirmPassword('${user.id}')"/>
									</div>
								</div>

								<input type="hidden" id="updateUserPwdUserId${user.id}" name="userId" value="${user.id}"/>
							</div>
							<!-- form-body 结束 -->
						</div>
						<div id="updateUserPwdInfo${user.id}"></div>
						<!-- 控件结束 -->
					</div>
				</div>

				<div class="modal-footer">
					<button type="button" data-dismiss="modal" class="btn" >Close</button>
					<button type="button" id="updateUserPwdBtn${user.id}" class="btn red" onclick="updateUserPwd('${user.id}')">Ok</button>
				</div>

			</form>
		</div>
	</div>
</div>
