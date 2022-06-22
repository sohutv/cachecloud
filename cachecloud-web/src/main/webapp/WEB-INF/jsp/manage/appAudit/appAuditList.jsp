<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp" %>
<script type="text/javascript" src="/resources/select/bootstrap-select.js"></script>
<link rel="stylesheet" type="text/css" href="/resources/select/bootstrap-select.css"/>
<script type="text/javascript" src="/resources/js/selectpicker.js?<%=System.currentTimeMillis()%>"></script>
<div class="page-container">
    <div class="page-content">
        <div class="row">
            <div class="col-md-12">
                <h3 class="page-title">
                    应用审批列表
                </h3>
            </div>
        </div>
        <br/><br/>

        <div class="row">
            <div class="col-md-12">
                <div class="portlet light-grey">
                    <div class="portlet-title">
                        <div class="caption"><i class="glyphicon glyphicon-tags"></i>工单处理汇总</div>
                        <div class="tools">
                            <a href="javascript:;" class="collapse"></a>
                        </div>
                    </div>
                    <div class="portlet-body">
                        <div class="table-toolbar">
                            <div class="btn-group">
                                <form class="form-inline" role="form" method="post" action="/manage/app/auditList"
                                      id="search_form">
                                    <input name="tabId" id="tabId" value="${tabId}" type="hidden"/>
                                    <div class="form-group">
                                        <label>&nbsp;开始日期:&nbsp;&nbsp;</label>
                                        <input type="date" size="15" name="startDate" id="startDate"
                                               value="${startDate}"/>
                                        <label>&nbsp;结束日期:&nbsp;&nbsp;</label>
                                        <input type="date" size="15" name="endDate" id="endDate" value="${endDate}"/>
                                        <label>&nbsp;处理人:&nbsp;&nbsp;</label>
                                        <select name="adminId" style="width: 150px">
                                            <option value="" <c:if test="${adminId == null}">selected="selected"</c:if>>
                                                所有
                                            </option>
                                            <c:forEach items="${userMap}" var="entry">
                                                <c:set var="admin" value="${entry.value}"></c:set>
                                                <c:if test="${admin.type==0}">
                                                    <option value="${admin.id}"
                                                            <c:if test="${adminId == admin.id}">selected</c:if>>
                                                        【${admin.id}】${admin.name}&nbsp;${admin.chName}
                                                    </option>
                                                </c:if>
                                            </c:forEach>
                                        </select>
                                    </div>
                                    &nbsp;&nbsp;
                                    <button type="submit" class="btn blue btn-sm">查询</button>
                                </form>
                            </div>
                        </div>
                        <br/>
                        <table class="table table-striped table-bordered table-hover">
                            <tr>
                                <td><span style="font-weight:bold">工单总数</span></td>
                                <td>
                                    ${(statusStatisMap['0']+statusStatisMap['1']+statusStatisMap['2']+statusStatisMap['-1'])==null?0:
                                            (statusStatisMap['0']+statusStatisMap['1']+statusStatisMap['2']+statusStatisMap['-1'])}
                                </td>
                                <td><span style="font-weight:bold">完成工单数</span></td>
                                <td>${statusStatisMap['1']==null?0:statusStatisMap['1']}</td>
                                <td style="color:orange;"><span style="font-weight:bold">待处理工单数</span></td>
                                <td style="color:orange;">${(statusStatisMap['0']+statusStatisMap['2'])==null?0:(statusStatisMap['0']+statusStatisMap['2'])}</td>
                                <td><span style="font-weight:bold">被驳回工单数</span></td>
                                <td>${statusStatisMap['-1']==null?0:statusStatisMap['-1']}</td>
                            </tr>
                            <tr>
                                <td><span style="font-weight:bold">申请应用</span></td>
                                <td>${typeStatisMap['0']==null?0:typeStatisMap['0']}</td>
                                <td><span style="font-weight:bold">下线应用</span></td>
                                <td>${typeStatisMap['10']==null?0:typeStatisMap['10']}</td>
                                <td><span style="font-weight:bold">键值分析</span></td>
                                <td>${typeStatisMap['6']==null?0:typeStatisMap['6']}</td>
                                <td><span style="font-weight:bold">诊断应用</span></td>
                                <td>${typeStatisMap['8']==null?0:typeStatisMap['8']}</td>
                            </tr>
                            <tr>
                                <td><span style="font-weight:bold">容量变更</span></td>
                                <td>${typeStatisMap['1']==null?0:typeStatisMap['1']}</td>
                                <td><span style="font-weight:bold">配置修改</span></td>
                                <td>${(typeStatisMap['4']+typeStatisMap['2'])==null?0:(typeStatisMap['4']+typeStatisMap['2'])}</td>
                                <td><span style="font-weight:bold">用户注册</span></td>
                                <td>${typeStatisMap['3']==null?0:typeStatisMap['3']}</td>
                                <td><span style="font-weight:bold">应用数据迁移</span></td>
                                <td>${typeStatisMap['11']==null?0:typeStatisMap['11']}</td>

                            </tr>

                        </table>
                        <br/>
                    </div>
                </div>
            </div>
        </div>
        <div class="row">
            <div class="col-md-12">
                <div class="portlet">
                    <div class="portlet-title">
                        <div class="caption"><i class="glyphicon glyphicon-list"></i>审批列表</div>
                        <div class="tools">
                            <a href="javascript:;" class="collapse"></a>
                        </div>
                    </div>
                    <div class="portlet-body">
                        <div class="table-toolbar">
                            <div class="col-md-12">
                                <form action="/manage/app/auditList" method="post" class="form-inline" role="form">
                                    &nbsp;&nbsp;
                                    <div class="form-group">
                                        <label class="control-label">
                                            &nbsp;申请人:&nbsp;&nbsp;
                                        </label>
                                        <select name="userId" data-width="50px" class="selectpicker form-control"
                                                data-live-search="true">
                                            <option value="" <c:if test="${userId == null}">selected="selected"</c:if>>
                                                所有
                                            </option>
                                            <c:forEach items="${userMap}" var="entry">
                                                <c:set var="user" value="${entry.value}"></c:set>
                                                <option value="${user.id}"
                                                        <c:if test="${userId == user.id}">selected</c:if>>
                                                    【${user.id}】${user.name}&nbsp;${user.chName}
                                                </option>
                                            </c:forEach>
                                        </select>
                                    </div>
                                    <div class="form-group col-md-offset-1">
                                        <label class="control-label">
                                            处理人:&nbsp;&nbsp;
                                        </label>
                                        <select class="form-control" name="operateId" style="width: 150px">
                                            <option value=""
                                                    <c:if test="${operateId == null}">selected="selected"</c:if>>
                                                所有
                                            </option>
                                            <c:forEach items="${userMap}" var="entry">
                                                <c:set var="admin" value="${entry.value}"></c:set>
                                                <c:if test="${admin.type==0}">
                                                    <option value="${admin.id}"
                                                            <c:if test="${operateId == admin.id}">selected</c:if>>
                                                        【${admin.id}】${admin.name}&nbsp;${admin.chName}
                                                    </option>
                                                </c:if>
                                            </c:forEach>
                                        </select>
                                    </div>
                                    <div class="form-group">
                                        <label class="control-label">
                                            &nbsp;&nbsp;申请类型:&nbsp;&nbsp;
                                        </label>
                                        <select class="form-control" name="type" style="width: 150px">
                                            <option value="" <c:if test="${type == null}">selected="selected"</c:if>>
                                                所有类型
                                            </option>
                                            <c:forEach items="${appAuditTypeMap}" var="entry">
                                                <c:set var="auditType" value="${entry.value}"></c:set>
                                                <option value="${auditType.value}"
                                                        <c:if test="${type == auditType.value}">selected="selected"</c:if>>
                                                        ${auditType.info}
                                                </option>
                                            </c:forEach>
                                        </select>
                                    </div>
                                    <div class="form-group">
                                        <label class="control-label">
                                            &nbsp;审核状态:&nbsp;&nbsp;
                                        </label>
                                        <select class="form-control" name="status" style="width: 150px">
                                            <option value="0" <c:if test="${status == 0}">selected="selected"</c:if>>
                                                待处理列表
                                            </option>
                                            <option value="2" <c:if test="${status == 2}">selected="selected"</c:if>>
                                                已受理任务
                                            </option>
                                            <option value="1" <c:if test="${status == 1}">selected="selected"</c:if>>
                                                通过列表
                                            </option>
                                            <option value="-1" <c:if test="${status == -1}">selected="selected"</c:if>>
                                                驳回列表
                                            </option>
                                            <option value="4" <c:if test="${status == 4}">selected="selected"</c:if>>
                                                所有状态
                                            </option>
                                        </select>
                                    </div>
                                    &nbsp;&nbsp;
                                    <button type="submit" class="btn blue btn-sm">查询</button>
                                </form>
                            </div>
                            <br/><br/><br/>
                        </div>
                        <table class="table table-striped table-bordered table-hover" id="tableDataList">
                            <thead>
                            <tr>
                                <th>审批id</th>
                                <th>appID</th>
                                <th>应用名</th>
                                <th>申请人</th>
                                <th>申请类型</th>
                                <th>申请描述</th>
                                <th>申请时间</th>
                                <th>审核状态</th>
                                <th>处理人</th>
                                <th>操作</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach items="${list}" var="item">
                                <tr class="odd gradeX">
                                    <td>
                                            ${item.id}
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${item.type == 3}">
                                                无
                                            </c:when>
                                            <c:otherwise>
                                                <a target="_blank"
                                                   href="/admin/app/index?appId=${item.appId}">${item.appId}</a>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${item.type == 3}">
                                                无
                                            </c:when>
                                            <c:otherwise>
                                                ${item.appDesc.name}
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>${item.userName}</td>
                                    <td>${appAuditTypeMap[item.type].info}</td>
                                    <td>${item.info}</td>
                                    <td><fmt:formatDate pattern="yyyy-MM-dd HH:mm:ss" value="${item.createTime}"/></td>

                                    <td>
                                        <c:choose>
                                        <c:when test="${item.status == 0}"><font
                                                style="color: orange;">待审</font></c:when>
                                        <c:when test="${item.status == 1}"><font
                                                style="color: green;">通过</font></c:when>
                                        <c:when test="${item.status == 2}"><font
                                                style="color: cornflowerblue;">处理中</font></c:when>
                                        <c:when test="${item.status == -1}"><font style="color: darkred;">驳回</c:when>
                                        </c:choose>
                                    </td>
                                    <td>${userMap[item.operateId].chName}</td>
                                    <td>
                                            <%-- 任务--%>
                                        <c:if test="${item.taskId > 0}">
                                            <a target="_blank" href="/manage/task/flow?taskId=${item.taskId}">[查看任务]</a>
                                        </c:if>
                                            <%--驳回--%>
                                        <c:if test="${item.status == 0}">
                                            <a href="javascript:void(0);" data-target="#appRefuseModal${item.id}"
                                               data-toggle="modal">[驳回]</a>
                                        </c:if>

                                            <%--处理--%>
                                        <c:if test="${item.status == 2}">
                                            <c:set var="auditUrl"
                                                   value="/manage/app/addAuditStatus?status=1&appAuditId=${item.id}"/>
                                            <a onclick="if(window.confirm('确认要通过该申请请求吗?')){return true;}else{return false;}"
                                               href="${auditUrl}">[通过]</a>
                                            <c:if test="${item.type == 12}">
                                                <a target="_blank"
                                                   href="/import/app/init?importId=${item.param1}">[迁移进度]</a>
                                            </c:if>
                                        </c:if>
                                        &nbsp;
                                        <c:if test="${item.status == 0}">
                                            <c:choose>
                                                <c:when test="${item.type == 0}">
                                                    <c:set var="auditDealUrl"
                                                           value="/manage/app/initAppDeploy?appAuditId=${item.id}"/>
                                                    <a target="_blank" href="${auditDealUrl}">[审批处理]</a>
                                                </c:when>
                                                <c:when test="${item.type == 1}">
                                                    <c:set var="auditDealUrl"
                                                           value="/manage/app/initAppScaleApply?appAuditId=${item.id}"/>
                                                    <a target="_blank" href="${auditDealUrl}">[审批处理]</a>
                                                </c:when>
                                                <c:when test="${item.type == 2}">
                                                    <c:set var="auditDealUrl"
                                                           value="/manage/app/initAppConfigChange?appAuditId=${item.id}"/>
                                                    <a target="_blank" href="${auditDealUrl}">[审批处理]</a>
                                                </c:when>
                                                <c:when test="${item.type == 3}">
                                                    <a onclick="if(window.confirm('确认要通过该申请请求吗?')){return true;}else{return false;}"
                                                       href="/manage/user/addAuditStatus?status=1&appAuditId=${item.id}">[通过]</a>
                                                </c:when>
                                                <c:when test="${item.type == 4}">
                                                    <c:set var="auditDealUrl"
                                                           value="/manage/instance/initInstanceConfigChange?appAuditId=${item.id}"/>
                                                    <a target="_blank" href="${auditDealUrl}">[审批处理]</a>
                                                </c:when>
                                                <c:when test="${item.type == 5}">
                                                    <c:set var="auditUrl"
                                                           value="/manage/app/addAuditStatus?status=1&appAuditId=${item.id}"/>
                                                    <a onclick="if(window.confirm('确认要通过该申请请求吗?')){return true;}else{return false;}"
                                                       href="${auditUrl}">[通过]</a>
                                                </c:when>
                                                <c:when test="${item.type == 6}">
                                                    <a onclick="if(window.confirm('确认要处理该申请吗?')){return true;}else{return false;}"
                                                       target="_blank"
                                                       href="/manage/app/startKeyAnalysis?status=1&appId=${item.appDesc.appId}&appAuditId=${item.id}">[分析]</a>
                                                </c:when>
                                                <c:when test="${item.type == 7}">
                                                    <c:set var="auditDealUrl"
                                                           value="/manage/app/addAuditStatus?status=2&appAuditId=${item.id}&type=7&appId=${item.appId}"/>
                                                    <a target="_blank" href="${auditDealUrl}">[审批处理]</a>
                                                </c:when>
                                                <c:when test="${item.type == 8}">
                                                    <a onclick="if(window.confirm('确认要处理该申请吗?')){return true;}else{return false;}"
                                                       target="_blank"
                                                       href="/manage/app/addAuditStatus?status=2&appAuditId=${item.id}&type=8">[诊断]</a>
                                                </c:when>
                                                <c:when test="${item.type == 10}">
                                                    <a target="_blank"
                                                       href="/manage/app/addAuditStatus?status=2&appAuditId=${item.id}&type=10&appId=${item.appId}">[下线]</a>
                                                </c:when>
                                                <c:when test="${item.type == 11}">
                                                    <a target="_blank"
                                                       href="/manage/app/addAuditStatus?status=2&appAuditId=${item.id}&type=11&appId=${item.appId}">[审批处理]</a>
                                                </c:when>
                                                <c:when test="${item.type == 12}">
                                                    <a target="_blank"
                                                       href="/manage/app/addAuditStatus?status=2&appAuditId=${item.id}&type=12&appId=${item.appId}">[审批处理]</a>
                                                </c:when>
                                                <c:when test="${item.type == 13}">
                                                    <a target="_blank"
                                                       href="/manage/app/addAuditStatus?status=2&appAuditId=${item.id}&type=13&appId=${item.appId}">[审批处理]</a>
                                                </c:when>
                                            </c:choose>
                                        </c:if>


                                    </td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </div>
                <!-- END EXAMPLE TABLE PORTLET-->
            </div>
        </div>
    </div>

</div>

<c:forEach items="${list}" var="item">
    <%@include file="addAudit.jsp" %>
</c:forEach>







