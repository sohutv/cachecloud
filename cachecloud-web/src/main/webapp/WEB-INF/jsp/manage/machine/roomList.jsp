<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<script type="text/javascript" src="/resources/bootstrap/jquery/jquery-1.11.0.js"></script>
<%@include file="/WEB-INF/jsp/manage/commons/taglibs.jsp" %>
<%@include file="/WEB-INF/jsp/manage/include/cache_cloud_main_js_include.jsp" %>
<%@include file="/WEB-INF/jsp/manage/include/cache_cloud_main_css.jsp" %>

<script type="text/javascript">
    var TableManaged = function () {
        return {
            //main function to initiate the module
            init: function () {

                if (!jQuery().dataTable) {
                    return;
                }

                $('#tableDataList').dataTable({
                    "searching": true,
                    "bLengthChange": false,
                    "iDisplayLength": 15,
                    "sPaginationType": "bootstrap",
                    "oLanguage": {
                        "sLengthMenu": "_MENU_ records",
                        "oPaginate": {
                            "sPrevious": "Prev",
                            "sNext": "Next"
                        }
                    }
                });

                jQuery('#tableDataList_wrapper>div:first-child').css("display", "none");
            }
        };
    }();
</script>


<div class="row">
    <div class="col-md-12">
        <button class="btn green" data-target="#addRoomModal" data-toggle="modal">
            添加机房 <i class="fa fa-plus"></i>
        </button>
    </div>
</div>

<br/>
<div class="row">
    <div class="col-md-12">
        <div class="portlet box light-grey" id="clientIndex">

            <table class="table table-striped table-bordered table-hover" id="tableDataList">
                <thead>
                <tr>
                    <td>序号</td>
                    <th>机房名称</th>
                    <th>可用状态</th>
                    <th>描述</th>
                    <th>网段</th>
                    <th>运营商</th>
                    <th>操作</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach items="${roomList}" var="record" varStatus="status">
                    <tr>
                        <td>${status.index + 1}</td>
                        <td>${record.name}</td>
                        <td>
                            <c:if test="${record.status==0}">无效</c:if>
                            <c:if test="${record.status==1}">有效</c:if>
                        </td>
                        <td>${record.desc}</td>
                        <td>${record.ipNetwork}</td>
                        <td>${record.operator}</td>
                        <td>
                            <%@include file="addRoom.jsp" %>
                            <a href="javascript:void(0);" data-target="#addRoomModal${record.id}"
                               class="btn btn-sm btn-info" data-toggle="modal">修改</a>
                            <button id="removeBtn${record.id}" type="button" class="btn btn-sm btn-info"
                                    onclick="removeRoom(this.id,'${record.id}')">删除
                            </button>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </div>
</div>

<%@include file="addRoom.jsp" %>

