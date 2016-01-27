<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<div id="addUserModal${user.id}" class="modal fade" tabindex="-1" data-width="400">
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
										<input type="text" name="name" id="name${user.id}"
											value="${user.name}" placeholder="域账户名(邮箱前缀)"
											class="form-control" />
									</div>
								</div>
								
								<div class="form-group">
									<label class="control-label col-md-3">
										中文名:
									</label>
									<div class="col-md-5">
										<input type="text" name="chName" id="chName${user.id}"
											value="${user.chName}" placeholder="中文名"
											class="form-control" />
									</div>
								</div>
								
								<div class="form-group">
									<label class="control-label col-md-3">
										邮箱:
									</label>
									<div class="col-md-5">
										<input type="text" name="email" id="email${user.id}"
											value="${user.email}" placeholder="邮箱"
											class="form-control" />
									</div>
								</div>
								
								<div class="form-group">
									<label class="control-label col-md-3">
										手机:
									</label>
									<div class="col-md-5">
										<input type="text" name="mobile" id="mobile${user.id}"
											value="${user.mobile}" placeholder="手机"
											class="form-control" />
									</div>
								</div>
								
								<div class="form-group">
									<label class="control-label col-md-3">
										类型:
									</label>
									<div class="col-md-5">
										<select name="type" id="type${user.id}" class="form-control select2_category">
											<option value="0" <c:if test="${user.type == 0}">selected="selected"</c:if>>
												管理员
											</option>
											<option value="2" <c:if test="${user.type == 2}">selected="selected"</c:if>>
												普通用户
											</option>
										</select>
									</div>
								</div>
								<input type="hidden" id="userId${user.id}" name="userId" value="${user.id}"/>
							</div>
							<!-- form-body 结束 -->
						</div>
						<div id="info${user.id}"></div>
						<!-- 控件结束 -->
					</div>
				</div>
				
				<div class="modal-footer">
					<button type="button" data-dismiss="modal" class="btn" >Close</button>
					<button type="button" id="userBtn${user.id}" class="btn red" onclick="saveOrUpdateUser('${user.id}')">Ok</button>
				</div>
			
			</form>
		</div>
	</div>
</div>
