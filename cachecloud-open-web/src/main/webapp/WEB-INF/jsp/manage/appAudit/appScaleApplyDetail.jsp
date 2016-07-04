<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp" %>


<div class="page-container">
    <div class="page-content">
        <%@include file="appIntanceReferList.jsp" %>

        <div class="row">
            <div class="col-md-12">
                <div class="portlet box light-grey">
                    <div class="portlet-title">
                        <div class="caption">
                            <i class="fa fa-globe"></i>
                            	填写扩容配置
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
                            <form action="/manage/app/addAppScaleApply.do" method="post"
                                  class="form-horizontal form-bordered form-row-stripped"
                                  onsubmit="return checkAppScaleText();">
                                <div class="form-body">
                                    <div class="form-group">
                                        <label class="control-label col-md-3">
                                            	扩容配置:<font color='red'>(*)</font>:
                                        </label>

                                        <div class="col-md-5">
                                            <textarea rows="1" name="appScaleText" id="appScaleText"
                                                      placeholder="请输入扩容后单实例最大内存（填写数字即可，单位MB）"
                                                      class="form-control"></textarea>
                                        </div>
                                    </div>
                                    <input type="hidden" name="appId" value="${appId}">
                                    <input type="hidden" name="appAuditId" value="${appAuditId}">

                                    <div class="form-actions fluid">
                                        <div class="row">
                                            <div class="col-md-12">
                                                <div class="col-md-offset-3 col-md-3">
                                                    <button type="submit" class="btn green">
                                                        <i class="fa fa-check"></i>
                                                       	 确认
                                                    </button>

                                                </div>
                                                <c:if test="${appDesc.type == 2}">
                                                	<div class="col-md-6 ">
	                                                    <a class="btn btn-info" href="/manage/app/initHorizontalScaleApply?appAuditId=${appAuditId}">水平扩容</a>
	                                                </div>
                                                </c:if>
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

