<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp"%>

<!DOCTYPE html>
<html lang="en">
<head>
    <title>申请应用</title>
    <jsp:include page="/WEB-INF/include/head.jsp"/>
</head>
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
									<form action="/admin/app/add.do" method="post"
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
															Redis-sentinel
														</option>
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
													<input type="text" name="memSize" id="memSize" placeholder="内存总量" class="form-control"/>
													<span class="help-block">
														例如填写：512M,1G,2G..32G等
													</span>
												</div>
												
											</div>
											
											<div class="form-group">
												<label class="control-label col-md-3">
													项目负责人<font color='red'>(*)</font>:
												</label>
												<div class="col-md-5">
													<input type="text" name="officer" id="officer" placeholder="项目负责人(中文必填)"
														class="form-control" />
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
													<input type="text" name="clientMachineRoom" id="clientMachineRoom" placeholder="例如北显、兆维、或者北显和兆维" class="form-control"/>
												</div>
											</div>
											
											<div class="form-group">
												<label class="control-label col-md-3">
													内存报警阀值<font color='red'>(*)</font>:
												</label>
												<div class="col-md-5">
													<input type="text" name="memAlertValue" id="memAlertValue" placeholder="内存报警阀值" class="form-control" onchange="testisNum(this.id)"/>
													<span class="help-block">
														例如内存使用率超过90%就报警，请填写90(<font color="red">如果不需要报警请填写100以上的数字</font>)
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
															<button type="submit" class="btn green">
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

