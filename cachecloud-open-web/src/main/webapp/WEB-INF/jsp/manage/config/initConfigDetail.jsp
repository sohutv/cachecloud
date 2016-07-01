<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp"%>
<div class="page-container">
	<div class="page-content">
		<div class="row">
			<div class="col-md-12">
				<h3 class="page-title">
					配置修改 
					<c:choose>
						<c:when test="${success == 1}">
							<font color="red">更新成功</font>
						</c:when>
						<c:when test="${success == 0}">
							<font color="red">更新失败</font>
						</c:when>
					</c:choose>
				</h3>
			</div>
		</div>
		
		<div class="row">
				<div class="col-md-12">
					<div class="portlet box light-grey">
							<div class="portlet-title">
								<div class="caption">
									<i class="fa fa-globe"></i>
									填写配置:
									&nbsp;
								</div>
								<div class="tools">
									<a href="javascript:;" class="collapse"></a>
								</div>
							</div>
							
							<div class="form">
									<!-- BEGIN FORM-->
									<form action="/manage/config/update.do" method="post" class="form-horizontal form-bordered form-row-stripped">
										<div class="form-body">
											
											<c:forEach items="${configList}" var="config" varStatus="stats">
												<div class="form-group">
													<label class="control-label col-md-3">
														${config.info}<font color='red'>(*)</font>:
													</label>
													<div class="col-md-5">
														<c:choose>
															<c:when test="${config.configKey == 'cachecloud.whether.schedule.clean.data'}">
																<select name="${config.configKey}" class="form-control">
																	<option value="false" <c:if test="${config.configValue == 'false'}">selected</c:if>>
																		否
																	</option>
																	<option value="true" <c:if test="${config.configValue == 'true'}">selected</c:if>>
																		是
																	</option>
																</select>
															</c:when>
															<c:when test="${config.configKey == 'cachecloud.user.login.type'}">
																<select name="${config.configKey}" class="form-control">
																	<option value="1" <c:if test="${config.configValue == '1'}">selected</c:if>>
																		session
																	</option>
																	<option value="2" <c:if test="${config.configValue == '2'}">selected</c:if>>
																		cookie
																	</option>
																</select>
															</c:when>
															<c:otherwise>
																<input type="text" name="${config.configKey}" class="form-control" value="${config.configValue}" />
															</c:otherwise>
														</c:choose>
													</div>
												</div>
											</c:forEach>
											
											<div class="form-actions fluid">
												<div class="row">
													<div class="col-md-12">
														<div class="col-md-offset-3 col-md-9">
															<button type="submit" class="btn green">
																<i class="fa fa-check"></i>
																确认修改
															</button>
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
					<!-- END TABLE PORTLET-->
				</div>
			</div>
		
	</div>
</div>
