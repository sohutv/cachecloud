<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp"%>

<!DOCTYPE html>
<html lang="en">
<head>
    <title>申请应用</title>
    <jsp:include page="/WEB-INF/include/head.jsp"/>
</head>
<script src="/resources/manage/plugins/jquery-1.10.2.min.js"></script>
<script type="text/javascript">
	var jQuery_1_10_2 = $;
</script>
<script type="text/javascript" src="/resources/select/bootstrap-select.js"></script>
<link rel="stylesheet" type="text/css" href="/resources/select/bootstrap-select.css"/>
<script type="text/javascript">
	$(window).on('load', function () {
		jQuery_1_10_2('.selectpicker').selectpicker({'selectedText': 'cat'});
		jQuery_1_10_2('.selectpicker').selectpicker('refresh');
	});
	$(function () {
		jQuery_1_10_2('.selectpicker').selectpicker({noneSelectedText: '选择负责人'});
	})
</script>
<body role="document">
	<div class="container">
	    <jsp:include page="/WEB-INF/include/headMenu.jsp"/>
	    <div id="systemAlert">
	    </div>
		<div class="page-content">
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
													<input type="text" name="name" id="appName"
														class="form-control" onchange="checkAppNameExist()"/>
													<span class="help-block">
														应用名称（必填，全局唯一，以字母开头的4~32个字符序列，不能含中文，可以是大小写字符，数字，下划线，连接线，点）
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
														应用描述（必填，不超过128个字符，可以包含中文）
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
													<input type="text" name="memSize" id="memSize" placeholder="内存总量,填写数值" class="form-control"/>
													<span class="help-block">
														例如填写：1,2,4..32,单位GB等
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
																<option value="${version.id}">${version.name}</option>
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
													后端是否有数据源:
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
													是否需要热备:
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
													<input type="text" name="forecaseQps" id="forecaseQps" placeholder="预估QPS(如填写:800)" class="form-control" onchange="testisNum(this.id)"/>
												</div>
											</div>

											<div class="form-group">
												<label class="control-label col-md-3">
													预估条目数量:<font color='red'>(*)</font>:
												</label>
												<div class="col-md-5">
													<input type="text" name="forecastObjNum" id="forecastObjNum" placeholder="预估条目数量(如填写:100000)" class="form-control" onchange="testisNum(this.id)"/>
												</div>
											</div>

											<div class="form-group">
												<label class="control-label col-md-3">
													客户端机房:<font color='red'>(*)</font>:
												</label>
												<div class="col-md-5">
													<option value="" >
														不作限制
													</option>
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
													<input type="text" name="memAlertValue" id="memAlertValue" placeholder="内存报警阀值" class="form-control" onchange="testisNum(this.id)"/>
													<span class="help-block">
														例如:内存使用率超过90%报警，请填写90(<font color="red">填写大于100以上则不报警</font>)
													</span>
												</div>
											</div>

											<div class="form-group">
												<label class="control-label col-md-3">
													客户端连接数报警阀值<font color='red'>(*)</font>:
												</label>
												<div class="col-md-5">
													<input type="text" name="clientConnAlertValue" id="clientConnAlertValue" placeholder="客户端连接数报警阀值" class="form-control" onchange="testisNum(this.id)"/>
													<span class="help-block">
														例如:如果想客户端连接数率超过2000报警，填写2000
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
					<!-- END TABLE PORTLET-->
				</div>
			</div>
		</div>
	</div>
	<br/><br/><br/><br/><br/><br/><br/>
	<jsp:include page="/WEB-INF/include/foot.jsp"/>
</body>
</html>

