<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp" %>


<div class="page-container">
    <div class="page-content">
    
        <%@include file="appIntanceReferList.jsp" %>
    	<%@include file="horizontalScaleProcessList.jsp" %>

		<div class="row">
		    <div class="col-md-12">
		        <h3 class="page-title">
		        	迁移计划
		        </h3>
		    </div>
		</div>

        <div class="row">
            <div class="col-md-12">
                <div class="portlet box light-grey">
                    <div class="portlet-title">
                        <div class="caption">
                            <i class="fa fa-globe"></i>
                            	填写迁移计划
                            &nbsp;
                        </div>
                        <div class="tools">
                            <a href="javascript:;" class="collapse"></a>
                            <a href="javascript:;" class="remove"></a>
                        </div>
                    </div>
                    <div class="portlet-body">
                        <div class="form">
                            <!-- BEGIN FORM-->
                            <form class="form-horizontal form-bordered form-row-stripped">
                                <div class="form-body">
                                    <div class="form-group">
                                        <label class="control-label col-md-3">
                                            	源实例ID:<font color='red'>(*)</font>:
                                        </label>
                                        <div class="col-md-5">
                                            <input type="text" name="sourceId" id="sourceId" class="form-control" onchange="testisNum(this.id)"/>
                                        </div>
                                    </div>
                                    
                                    <div class="form-group">
                                        <label class="control-label col-md-3">
                                            	目标实例ID:<font color='red'>(*)</font>:
                                        </label>
                                        <div class="col-md-5">
                                            <input type="text" name="targetId" id="targetId" class="form-control" onchange="testisNum(this.id)"/>
                                        </div>
                                    </div>
                                    
                                    <div class="form-group">
                                        <label class="control-label col-md-3">
                                            	开始slot:<font color='red'>(*)</font>:
                                        </label>
                                        <div class="col-md-5">
                                            <input type="text" name="startSlot" id="startSlot" class="form-control" onchange="testisNum(this.id)"/>
                                        </div>
                                    </div>
                                    
                                    <div class="form-group">
                                        <label class="control-label col-md-3">
                                            	结束slot:<font color='red'>(*)</font>:
                                        </label>
                                        <div class="col-md-5">
                                            <input type="text" name="endSlot" id="endSlot" class="form-control" onchange="testisNum(this.id)"/>
                                        </div>
                                    </div>
                                    
                                    <div class="form-group">
                                        <label class="control-label col-md-3">
                                             	批量migrate<font color='red'>(*)</font>:
                                        </label>
                                        <div class="col-md-5">
											<select id="migrateType" name="migrateType" class="form-control select2_category">
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
                                        <label class="control-label col-md-offset-2 col-md-8" style="text-align: left">
                                            redis版本低于<font color='red'>4.0.7</font>时，如待迁移集群设置有密码，由于redis migrate命令问题，不能正常执行，该情况下不支持水平扩容。
                                        </label>
                                    </div>
                                    
                                    <input type="hidden" name="appId" id="appId" value="${appAudit.appId}">
                                    <input type="hidden" name="appAuditId" id="appAuditId" value="${appAudit.id}">

                                    <div class="form-actions fluid">
                                        <div class="row">
                                            <div class="col-md-12">
                                                <div class="col-md-offset-3 col-md-3">
                                                    <button id="submitButton" disabled="disabled" type="button" class="btn green" onclick="startHorizontalScale()">
                                                        <i class="fa fa-check"></i>
                                                        	开始迁移	
                                                    </button>
                                                    <button id="checkButton" type="button" class="btn green" onclick="checkHorizontalScale()">
                                                        <i class="fa fa-check"></i>
                                                        	验证格式
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

