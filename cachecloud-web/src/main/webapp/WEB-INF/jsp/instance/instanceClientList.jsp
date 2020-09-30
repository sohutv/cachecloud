<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<div class="container">
    <br/>
    <div class="row">
        <div class="col-md-12">
            <div class="page-header">
                <h4>客户端连接统计</h4>
            </div>
            <div class="page-header">
                <h5>
                    <form method="post" action="/admin/instance/index?instanceId=${instanceId}&tabTag=instance_clientList" id="conditionFrom">
                        <input type="radio" value="0" <c:if test="${condition == 0}">checked="checked"</c:if>
                               onchange="changeCondition(this.value)"/> 应用客户端
                        <input type="radio" value="1" <c:if test="${condition == 1}">checked="checked"</c:if>
                               onchange="changeCondition(this.value)"/> cc客户端
                        <input type="radio" value="2" <c:if test="${condition == 2}">checked="checked"</c:if>
                               onchange="changeCondition(this.value)"/> redis客户端
                        <input type="radio" value="3" <c:if test="${condition == 3}">checked="checked"</c:if>
                               onchange="changeCondition(this.value)"/> 所有客户端
                        <input type="hidden" id="condition" name="condition" value="${condition}">
                    </form>
                </h5>
            </div>
            <table class="table table-striped table-hover">
                <thead>
                <tr>
                    <td>序号</td>
                    <td>客户端ip</td>
                    <td>客户端类型</td>
                    <td>连接数</td>
                    <td>连接信息</td>
                </tr>
                </thead>
                <tbody>
                <c:forEach items="${clientMapList}" var="clientMap" varStatus="status">
                    <tr>
                        <td>${status.index + 1}</td>
                        <td>${clientMap['addr']}</td>
                        <td>
                            <c:forEach items="${clientMap['clientTypeSet']}" var="clientType">
                                ${clientType}
                                <br/>
                            </c:forEach>
                        </td>
                        <td>${clientMap['count']}</td>
                        <td>
                            <button type="button" class="btn btn-success" data-target="#modal-${status.index}"
                                    data-toggle="modal">
                                查看连接信息
                            </button>

                            <div id="modal-${status.index}" class="modal fade" tabindex="-1">
                                <div class="modal-dialog" style="width: max-content">
                                    <div class="modal-content">
                                        <div class="modal-header">
                                            <button type="button" class="close" data-dismiss="modal"
                                                    aria-hidden="true"></button>
                                            <h4 class="modal-title">连接信息</h4>
                                        </div>

                                        <form class="form-horizontal form-bordered form-row-stripped">
                                            <div class="modal-body">
                                                <div class="row">
                                                    <!-- 控件开始 -->
                                                    <div class="col-md-12">
                                                        <table class="table table-bordered table-striped table-hover">
                                                            <thead>
                                                            <tr>
                                                                <td>客户端类型</td>
                                                                <td>执行命令</td>
                                                                <td>连接</td>
                                                            </tr>
                                                            </thead>
                                                            <tbody>
                                                            <c:forEach items="${clientMap['clientInfoList']}"
                                                                       var="client">
                                                                <tr>
                                                                    <td>${client['flags']}</td>
                                                                    <td>${client['cmd']}</td>
                                                                    <td>${client}</td>
                                                                </tr>
                                                            </c:forEach>
                                                            </tbody>
                                                        </table>
                                                    </div>
                                                </div>
                                            </div>

                                            <div class="modal-footer">
                                                <button type="button" data-dismiss="modal" class="btn">Close</button>
                                            </div>
                                        </form>
                                    </div>
                                </div>
                            </div>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </div>
    <div class="row">
        <div class="col-md-12">
            <div class="page-header">
                <h4>客户端连接信息</h4>
            </div>
            <table class="table table-striped table-hover">
                <tbody>
                <c:forEach items="${clientList}" var="client" varStatus="status">
                    <tr>
                        <td>${status.index + 1}</td>
                        <td>${client}</td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </div>
</div>

<script type="text/javascript">
    function changeCondition(value) {
        console.log('radio:'+value);
        $('#condition').val(value);
        $('#conditionFrom').submit();
    }
</script>