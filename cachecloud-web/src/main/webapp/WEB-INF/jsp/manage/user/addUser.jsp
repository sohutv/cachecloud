<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.sohu.cache.utils.EnvCustomUtil"%>
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
										<input type="text" name="name" id="user_name${user.id}"
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
										微信:
									</label>
									<div class="col-md-5">
										<input type="text" name="weChat" id="weChat${user.id}"
											   value="${user.weChat}" placeholder="微信"
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

								<div class="form-group">
									<label class="control-label col-md-3">
										是否收报警:
									</label>
									<div class="col-md-5">
										<select name="isAlert" id="isAlert${user.id}" class="form-control select2_category">
											<option value="1" <c:if test="${user.isAlert == 1}">selected</c:if>>
												是
											</option>
											<option value="0" <c:if test="${user.isAlert == 0}">selected</c:if>>
												否
											</option>
										</select>
									</div>
								</div>

								<c:if test="<%=EnvCustomUtil.openswitch%>">
									<div class="form-group">
										<label class="control-label col-md-3">
											公司名称:
										</label>
										<div class="col-md-5">
											<input type="text" id="company${user.id}" name="company"
												   value="${user.company}" placeholder="公司名称"
												   class="form-control"/>
										</div>
									</div>

									<div class="form-group">
										<label class="control-label col-md-3">
											使用目的:
										</label>
										<div class="col-md-5">
											<input type="text" id="purpose${user.id}" name="purpose"
												   value="${user.purpose}" placeholder="使用目的"
												   class="form-control"/>
										</div>
									</div>
								</c:if>

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
					<button type="button" id="userBtn${user.id}" class="btn red" onclick="saveOrUpdateUser('${user.id}', <%=EnvCustomUtil.openswitch%>)">Ok</button>
				</div>
			
			</form>
		</div>
	</div>
</div>
