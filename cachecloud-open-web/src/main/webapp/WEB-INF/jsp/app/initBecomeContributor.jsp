<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp"%>

<!DOCTYPE html>
<html lang="en">
<head>
    <title>成为cachecloud贡献者</title>
    <jsp:include page="/WEB-INF/include/head.jsp"/>
    <script type="text/javascript">
    	function applyBecomeContributor(){
    		var groupName = document.getElementById("groupName");
    		if(groupName.value == ""){
    			alert("请填写所在项目组!");
    			groupName.focus();
    			return false;
    		}
    		
    		var checked = false;
    		var reason = "";
    		var arr = document.getElementsByName("applyReason");
    		var i; 
    		for(i=0;i<arr.length;i++) { 
    			if(arr[i].type=='checkbox' && arr[i].checked==true) {
    				checked = true;
    				reason = reason + arr[i].value + "<br/>";
    			} 
    		}
    		if(checked == false){
    			alert("请填写申请原因!");
    			return false;
    		}
    		 
    		$.post(
    			'/admin/app/addBecomeContributor.json',
    			{
    				groupName: groupName.value,
    				applyReason: reason
    			},
    	        function(data){
    	            if(data.success==1){
    	                alert("申请成功,请在邮件中关注申请状况.");
    	            }
    	        }
    	     );
    	}
    </script>
</head>
<body>
	<div class="container">
	    <jsp:include page="/WEB-INF/include/headMenu.jsp"/>
	    <div id="systemAlert">
	    </div>
		<div class="page-content">
			<div class="row">
				<div class="col-md-12">
					<h3 class="page-header">
						成为Cachecloud贡献者
					</h3>
				</div>
			</div>
			<div class="row">
				<div class="col-md-12">
					<div class="portlet box light-grey">
						<div class="portlet-body">
							<div class="form">
									<!-- BEGIN FORM-->
									<form action="/admin/app/applyBecomeContributor.do" method="post"
										class="form-horizontal form-bordered form-row-stripped">
										<div class="form-body">
											<div class="form-group">
												<label class="control-label col-md-3">
													项目组<font color='red'>(*)</font>:
												</label>
												<div class="col-md-5">
													<input type="text" name="groupName" id="groupName" placeholder="项目组" class="form-control"/>
													<span class="help-block">
														(必填，所在项目组)
													</span>
												</div>
											</div>
											
											<div class="form-group">
												<label class="control-label col-md-3">
												 	希望得到<font color='red'>(*)</font>:
												</label>
												<div class="col-md-5">
													<input type="checkbox" name="applyReason" value="提升DevOps的能力"/>提升DevOps的能力<br/>
													<input type="checkbox" name="applyReason" value="云产品的开发经验和思维方式"/>云产品的开发经验和思维方式<br/>
													<input type="checkbox" name="applyReason" value="体验到自动化带来的好处,体现到自己的系统中"/>体验到自动化带来的好处,体现到自己的系统中<br/>
													<input type="checkbox" name="applyReason" value="Redis深入掌握"/>Redis深入掌握 <br/>
												</div>
											</div>
											
											<input name="userId" id="userId" value="${userInfo.id}" type="hidden" />
											<div class="form-actions fluid">
												<div class="row">
													<div class="col-md-12">
														<div class="col-md-offset-4 col-md-9">
															<button type="button" id="applyBecomeContributorBtn" class="btn green" onclick="applyBecomeContributor()">
																<i class="fa fa-check"></i>
																提交申请
															</button>
															&nbsp;&nbsp;
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

