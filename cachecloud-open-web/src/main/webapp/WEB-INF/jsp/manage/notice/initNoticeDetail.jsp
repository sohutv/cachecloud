<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp"%>
<script type="text/javascript">
	function checkNoticeText(){
		var notice = document.getElementById("notice");
		if(notice.value == ""){
			alert("系统通知不能为空!");
			notice.focus();
			return false;
		}
		$.post(
			'/manage/notice/add.json',
			{
				notice: notice.value,
			},
	        function(data){
	            if(data.success==1){
	            	alert("更新成功!");
	            }else{
	            	alert("更新失败!");
	            }
	            window.location.reload();
	        }
	     );
		
	}
</script>
<div class="page-container">
	<div class="page-content">
		<div class="row">
			<div class="col-md-12">
				<h3 class="page-title">
					系统通知管理
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
							填写系统通知:
							&nbsp;
						</div>
						<div class="tools">
							<a href="javascript:;" class="collapse"></a>
						</div>
					</div>
					<div class="portlet-body">
						<div class="form">
								<!-- BEGIN FORM-->
								<form action="/manage/notice/add.do" method="post"
									class="form-horizontal form-bordered form-row-stripped">
									<div class="form-body">
										<div class="form-group">
											<label class="control-label col-md-3">
												系统通知:<font color='red'>(*)</font>:
											</label>
											<div class="col-md-5">
												<textarea rows="10" name="notice" id="notice" placeholder="系统通知" class="form-control">${notice}</textarea>
												<span class="help-block">
												例如:<br/>
												1.CacheCloud相关文档可以在菜单栏查询(2014-12-16)<br/>
												2.接入代码模块加入了Protostuff序列化的演示。(2014-12-19)
												</span>
											</div>
										</div>
										
										<div class="form-actions fluid">
											<div class="row">
												<div class="col-md-12">
													<div class="col-md-offset-5">
														<button type="button" class="btn green" onclick="checkNoticeText()">
															<i class="fa fa-check"></i>
															提交
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
				<!-- END EXAMPLE TABLE PORTLET-->
			</div>
		
		</div>
	</div>
</div>
