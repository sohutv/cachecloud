<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp"%>
<div class="container">
	<div class="row">
		<div class="col-md-12">
			<h3 class="page-header">
				redis密码修改
			</h3>
		</div>
	</div>
	<div class="row">
		<div class="col-md-12">
			<div class="portlet box light-grey">
				<div class="portlet-title">
					<div class="tools">
						<a href="javascript:;" class="collapse"></a>
					</div>
				</div>
				<div class="portlet-body">
					<div class="form">
						<!-- BEGIN FORM-->
						<form class="form-horizontal form-bordered form-row-stripped">
							<div class="form-body">
								<c:if test="${customPassword != null && customPassword != ''}">
									<input type="hidden" id="oldPassword" name="oldPassword" value="${customPassword}">
								</c:if>
								<c:if test="${customPassword == null || customPassword == ''}">
									<input type="hidden" id="oldPassword" name="oldPassword" value="${pkey}">
								</c:if>
								<input type="hidden" id="appId" name="appId" value="${appId}">
								<div class="form-group">
									<label class="control-label col-md-3">
										redis密码
									</label>
									<div class="col-md-4">
										<c:if test="${customPassword != null && customPassword != ''}">
											<input type="text" name="password" id="password" value="${customPassword}" class="form-control"/>
										</c:if>
										<c:if test="${customPassword == null || customPassword == ''}">
											<input type="text" name="password" id="password" value="${pkey}" class="form-control"/>
										</c:if>
									</div>
									<div class="col-md-2">
										<c:if test="${customPassword != null && customPassword != ''}">
											<input type="checkbox" id="isSetPasswd" name="isSetPasswd" checked="checked"/>设置自定义密码
										</c:if>
										<c:if test="${customPassword == null || customPassword == ''}">
											<input type="checkbox" id="isSetPasswd" name="isSetPasswd" />设置自定义密码
										</c:if>
									</div>
									<div class="col-md-2">
										<button type="button" class="btn btn-small btn-primary" onclick="updateAppPassword('${customPassword}')">
											更新
										</button>
										<button type="button" class="btn btn-small btn-primary" onclick="checkAppPassword()">
											校验
										</button>
									</div>
								</div>
								<div class="row">
									<label class="control-label col-md-offset-3 col-md-6" style="color: orangered; text-align: left; ">
										自定义密码：用户定义的密码，设置的值即为密码；<br>
										默认密码：设置的值仅为基础值，对该值经过系统默认加密处理从而生成密码。<br>
										自定义密码优先级高于默认密码，如需清除密码，请先置空自定义密码，然后置空默认密码。
									</label>
								</div>
								<div class="row">
									<label class="control-label col-md-offset-2 col-md-8" style="color: orange; margin-left: auto">
										请注意，redis6.0.0——6.0.8版本由于源码bug(#7899)，不支持清除密码 &nbsp;&nbsp;<a href="https://raw.githubusercontent.com/redis/redis/6.0/00-RELEASENOTES">点击查看</a>
									</label>
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

	<script>

        function updateAppPassword(customPwd) {
            var oldPassword = document.getElementById("oldPassword");
            var password = document.getElementById("password");
            var appId = document.getElementById("appId");
			var isSetPasswd = document.getElementById("isSetPasswd").checked;
			if(customPwd != null && customPwd != ''){
            	if(isSetPasswd == true){
					if(oldPassword.value.trim() == password.value.trim()){
						alert("密码未变更,不更新!");
						return false;
					}
				}
			}
			if(customPwd == null || customPwd == ''){
            	if(isSetPasswd == false){
					if(oldPassword.value.trim() == password.value.trim()){
						alert("密码未变更,不更新!");
						return false;
					}
				}
			}

			var originType = (customPwd != null && customPwd != '') ? "自定义密码" : "默认密码";
			var newType = isSetPasswd == true ? "自定义密码" : "默认密码";

            $.get(
                '/manage/app/updateAppPassword.json',
                {
                    password: password.value.trim(),
                    appId: appId.value,
					isSetPasswd: isSetPasswd
                },
                function(data){
                    var status = data.status;
                    if (status == 1) {
                        alert("设置成功! 原有:" + originType + "【"+oldPassword.value+"】已更新成新密码：" + newType + "【"+password.value+"】");
                        window.location.reload();
                        // $("#oldPassword").attr("value",password.value);
                    } else {
                        alert("设置失败!");
                    }
                }
            );
        }

        // 检验密码一致性
        function checkAppPassword() {
            //var password = document.getElementById("password");
            var appId = document.getElementById("appId");

            $.get(
                '/manage/app/checkAppPassword.json',
                {
                    //password: password.value,
                    appId: appId.value
                },
                function(data){
                    var status = data.status;
                    if (status == 1) {
                        alert("应用密码是有效且一致!");
                    } else {
                        alert("应用密码是有效不一致!");
                    }
                }
            );
        }
	</script>

</div>





