<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp"%>
<script type="text/javascript" src="/resources/bootstrap/jquery/jquery-1.11.0.js"></script>
<script type="text/javascript" src="/resources/select/bootstrap-select.js"></script>
<link rel="stylesheet" type="text/css" href="/resources/select/bootstrap-select.css"/>
<script type="text/javascript" src="/resources/js/selectpicker.js?<%=System.currentTimeMillis()%>"></script>
<div class="col-md-9">
	<div class="row">
		<div class="col-md-12">
			<h3 class="page-header">
				申请应用
				<font color='red' size="4">
					<c:choose>
						<c:when test="${success == 1}">(更新成功)</c:when>
					</c:choose>
				</font>
			</h3>
		</div>
	</div>
	<div class="row">
		<div class="col-md-12">
			<div class="portlet box light-grey">
				<div class="portlet-body">
					<div class="form">
							<!-- BEGIN FORM-->
							<form action="/admin/app/add" method="post"
								class="form-horizontal form-bordered form-row-stripped" onsubmit="return saveAppDesc()">
								<div class="form-body">
									<div class="form-group">
										<label class="control-label col-md-3">
											应用名称<font color='red'>(*)</font>:
										</label>
										<div class="col-md-5">
											<input type="text" name="name" id="appName" placeholder="\${服务名}-\${机房:js/tc}-\${环境:online/test}"
												class="form-control" onchange="checkAppNameExist()"/>
											<span class="help-block">
												如：cachecloud-js-online，全局唯一
											</span>
										</div>
									</div>

									<div class="form-group">
										<label class="control-label col-md-3">
											应用描述<font color='red'>(*)</font>:
										</label>
										<div class="col-md-5">
											<textarea class="form-control" name="intro"
												rows="3" id="appIntro" placeholder="应用描述"></textarea>
											<span class="help-block">
												不超过128个字符，可以包含中文
											</span>
										</div>
									</div>

									<div class="form-group">
										<label class="control-label col-md-3">
											存储种类:
										</label>
										<div class="col-md-5">
											<select name="type" class="form-control select2_category">
												<option value="2">
													Redis-cluster
												</option>
												<option value="5">
													Redis-Sentinel
												</option>
												<%--<option value="7">--%>
													<%--Redis+Twemproxy--%>
												<%--</option>--%>
												<%--<option value="8">--%>
													<%--Pika+Sentine--%>
												<%--</option>--%>
												<%--<option value="9">--%>
													<%--Pika+Twemproxy--%>
												<%--</option>--%>
												<option value="6">
													Redis-standalone
												</option>
											</select>
										</div>
									</div>

									<div class="form-group">
										<label class="control-label col-md-3">
											内存总量<font color='red'>(*)</font>:
										</label>
										<div class="col-md-5">
											<input type="text" name="memSize" id="memSize" placeholder="如：1/2/4/.../32" class="form-control"/>
											<span class="help-block">
												填写整数，单位GB
											</span>
										</div>

									</div>

									<div class="form-group">
										<label class="control-label col-md-3">
											项目负责人<font color='red'>(*)</font>:
										</label>
										<div class="col-md-5">
											<select id="officer" name="officer" class="form-control selectpicker bla bla bli" multiple data-live-search="true" data-width="31%">
												<c:forEach items="${userList}" var="user">
													<option data-icon="glyphicon-user" value="${user.id}">${user.chName}【${user.email}】</option>
												</c:forEach>
											</select>
										</div>
									</div>

									<div class="form-group">
										<label class="control-label col-md-3">
											Redis部署版本:
										</label>
										<div class="col-md-5">
											<select id="versionId" name="versionId" class="form-control">
												<c:forEach items="${versionList}" var="version">
													<%--<c:if test="${version.ispush == 1}">--%>
														<option value="${version.id}">${version.name}</option>
													<%--</c:if>--%>
												</c:forEach>
											</select>
										</div>
									</div>

									<div class="form-group">
										<label class="control-label col-md-3">
											测试:
										</label>
										<div class="col-md-5">
											<select id="isTest" name="isTest" class="form-control">
												<option value="0">
													否
												</option>
												<option value="1">
													是
												</option>
											</select>
										</div>
									</div>

									<div class="form-group">
										<label class="control-label col-md-3">
											是否有数据备份:
										</label>
										<div class="col-md-5">
											<select id="hasBackStore" name="hasBackStore" class="form-control">
												<option value="1">
													是
												</option>
												<option value="0">
													否
												</option>
											</select>
											<span class="help-block">
												即是否用作数据缓存，有其他数据备份策略
											</span>
										</div>
									</div>

									<div class="form-group">
										<label class="control-label col-md-3">
											是否需要持久化:
										</label>
										<div class="col-md-5">
											<select id="needPersistence" name="needPersistence" class="form-control">
												<option value="1">
													是
												</option>
												<option value="0">
													否
												</option>
											</select>
										</div>
									</div>

									<div class="form-group">
										<label class="control-label col-md-3">
											是否需要slave节点:
										</label>
										<div class="col-md-5">
											<select id="needHotBackUp" name="needHotBackUp" class="form-control">
												<option value="1">
													是
												</option>
												<option value="0">
													否
												</option>
											</select>
										</div>
									</div>

									<div class="form-group">
										<label class="control-label col-md-3">
											预估QPS<font color='red'>(*)</font>:
										</label>
										<div class="col-md-5">
											<input type="text" name="forecaseQps" id="forecaseQps" value="800" class="form-control" onchange="testisNum(this.id)"/>
											<span class="help-block">
												预估QPS，如：800
											</span>
										</div>
									</div>

									<div class="form-group">
										<label class="control-label col-md-3">
											预估条目数量:<font color='red'>(*)</font>:
										</label>
										<div class="col-md-5">
											<input type="text" name="forecastObjNum" id="forecastObjNum" value="100000" class="form-control" onchange="testisNum(this.id)"/>
											<span class="help-block">
												预估键数量，如：100000
											</span>
										</div>
									</div>

									<div class="form-group">
										<label class="control-label col-md-3">
											客户端机房:<font color='red'>(*)</font>:
										</label>
										<div class="col-md-5">
											<select name="clientMachineRoom" id="clientMachineRoom" class="form-control select2_category">
												<c:forEach items="${roomList}" var="room">
													<option value="${room.name}">${room.name} (${room.ipNetwork})</option>
												</c:forEach>
											</select>
										</div>
									</div>

									<div class="form-group">
										<label class="control-label col-md-3">
											内存报警阀值<font color='red'>(*)</font>:
										</label>
										<div class="col-md-5">
											<input type="text" name="memAlertValue" id="memAlertValue" value="90" class="form-control" onchange="testisNum(this.id)"/>
											<span class="help-block">
												例如：内存使用率超过90%报警，请填写90（<font color="red">大于100以上则不报警</font>）
											</span>
										</div>
									</div>

									<div class="form-group">
										<label class="control-label col-md-3">
											客户端连接数报警阀值<font color='red'>(*)</font>:
										</label>
										<div class="col-md-5">
											<input type="text" name="clientConnAlertValue" id="clientConnAlertValue" value="2000" class="form-control" onchange="testisNum(this.id)"/>
											<span class="help-block">
												例如：如客户端连接数超过2000报警，填写2000
											</span>
										</div>
									</div>

									<input name="userId" id="userId" value="${userInfo.id}" type="hidden" />
									<input id="appExist" value="0" type="hidden" />

									<div class="form-actions fluid">
										<div class="row">
											<div class="col-md-12">
												<div class="col-md-offset-3 col-md-9">
													<button type="submit" class="btn btn-info">
														<i class="fa fa-check"></i>
														提交申请
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
		</div>
	</div>
</div>


