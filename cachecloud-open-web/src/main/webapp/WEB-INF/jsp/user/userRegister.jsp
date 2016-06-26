<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <title>CacheCloud用户申请</title>
    <script type="text/javascript">
    //验证邮箱格式
    var valEmails=/^(([a-zA-Z0-9]+[_|\_|\.]?)*[a-zA-Z0-9]+@([a-zA-Z0-9]+[_|\_|\.|\-]?)*[a-zA-Z0-9]+\.[a-zA-Z]{2,3};){0,6}([a-zA-Z0-9]+[_|\_|\.]?)*[a-zA-Z0-9]+@([a-zA-Z0-9]+[_|\_|\.|\-]?)*[a-zA-Z0-9]+\.[a-zA-Z]{2,3}$/;
    function checkUser(){
    	var name = document.getElementById("name");
    	var chName = document.getElementById("chName");
    	var email = document.getElementById("email");
    	var mobile = document.getElementById("mobile");
    	if(name.value == ""){
        	alert("域账户名不能为空!");
    		name.focus();
    		return false;
        }
    	if(chName.value == ""){
        	alert("中文名不能为空!");
    		chName.focus();
    		return false;
        }
    	if(email.value == ""){
    		alert("邮箱不能为空!");
    		email.focus();
    		return false;
    	}
    	if(!valEmails.test(email.value)){
    		alert("邮箱格式错误!");
    		email.focus();
    		return false;
    	}
    	if(mobile.value == ""){
    		alert("手机号不能为空!");
    		mobile.focus();
    		return false;
    	}
    	return true;
    }
    function checkUserNameExist(id) {
    	var userName = document.getElementById(id).value;
    	if(userName != ''){
    		$.post(
    			'/user/checkUserNameExist',
    			{
    				userName: userName,
    			},
    	        function(data){
    	            if(data==1){
    	            	alert("用户名已经存在，请修改或者联系管理员");
    	            	document.getElementById(id).focus();
    	            	document.getElementById(id).value="";
    	            }
    	        }
    	     );
    	}
    }
    
    
    </script>
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
						CacheCloud用户申请
						<font color='red' size="4">
							<c:choose>
								<c:when test="${success == 1}">(申请成功，请关注邮件中审批进度)</c:when>
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
									<form action="/user/apply.do" method="post"
										class="form-horizontal form-bordered form-row-stripped" onsubmit="return checkUser()">
										<div class="form-body">
											<div class="form-group">
												<label class="control-label col-md-3">
													域账户名:
												</label>
												<div class="col-md-5">
													<input type="text" name="name" id="name" placeholder="域账户名(邮箱前缀)" class="form-control" onchange="checkUserNameExist(this.id)"/>
												</div>
											</div>
										
											<div class="form-group">
												<label class="control-label col-md-3">
													中文名:
												</label>
												<div class="col-md-5">
													<input type="text" name="chName" id="chName" placeholder="中文名"
														class="form-control" />
												</div>
											</div>
										
											<div class="form-group">
												<label class="control-label col-md-3">
													邮箱:
												</label>
												<div class="col-md-5">
													<input type="text" name="email" id="email" placeholder="邮箱" class="form-control" />
												</div>
											</div>
										
											<div class="form-group">
												<label class="control-label col-md-3">
													手机:
												</label>
												<div class="col-md-5">
													<input type="text" id="mobile" name="mobile" placeholder="手机"
														class="form-control" />
												</div>
											</div>
											<input type="hidden" name="type" value="-1">
											
											<div class="form-actions fluid">
												<div class="row">
													<div class="col-md-12">
														<div class="col-md-offset-4 col-md-9">
															<button type="submit" class="btn green" onclick=""  <c:if test="${success == 1}">disabled="disabled"</c:if>>
																<i class="fa fa-check"></i>
																提交申请
															</button>
															&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
															<button type="reset" class="btn green">
																<i class="fa fa-check"></i>
																&nbsp;&nbsp;重&nbsp;&nbsp;&nbsp;置&nbsp;&nbsp;
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

