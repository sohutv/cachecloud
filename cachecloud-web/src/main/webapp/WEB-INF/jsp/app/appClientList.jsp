<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp" %>

<div class="container">
    <br/>
    <div class="row">
        <div class="col-md-12">
            <div class="page-header">
                <h4>应用连接信息</h4>
            </div>
            <div class="page-header">
                <h5>
                    <form method="post" action="/admin/app/index?appId=${appDesc.appId}&tabTag=app_clientList" id="conditionFrom">
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
                    <td>总连接数</td>
                    <td>客户端类型</td>
                    <td>实例详细</td>
                </tr>
                </thead>
                <tbody>
                <c:forEach items="${addrInstanceList}" var="addrInstance" varStatus="status">
                    <tr>
                        <td>${status.index + 1}</td>
                        <td>${addrInstance['addr']}</td>
                        <td>${addrInstance['size']}</td>
                        <td>
                            <c:forEach items="${addrInstance['flags']}" var="clientType">
                                ${clientType}
                                <br/>
                            </c:forEach>
                        </td>
                        <td>
                            <button type="button" class="btn btn-success" data-target="#modal-${status.index}"
                                    data-toggle="modal">
                                查看实例连接统计
                            </button>

                            <div id="modal-${status.index}" class="modal fade" tabindex="-1">
                                <div class="modal-dialog" style="width: max-content">
                                    <div class="modal-content">
                                        <div class="modal-header">
                                            <button type="button" class="close" data-dismiss="modal"
                                                    aria-hidden="true"></button>
                                            <h4 class="modal-title">客户端 ${addrInstance['addr']} 连接信息</h4>
                                        </div>

                                        <form class="form-horizontal form-bordered form-row-stripped">
                                            <div class="modal-body">
                                                <div class="row">
                                                    <!-- 控件开始 -->
                                                    <div class="col-md-12">
                                                        <table class="table table-bordered table-striped table-hover">
                                                            <thead>
                                                            <tr>
                                                                <td>实例id</td>
                                                                <td>实例</td>
                                                                <td>连接数</td>
                                                            </tr>
                                                            </thead>
                                                            <tbody>
                                                            <c:forEach items="${addrInstance['instanceClientStats']}" var="instance">
                                                                <c:set var="instanceId" value="${instance.key}"></c:set>
                                                                <tr>
                                                                    <td>${instanceId}</td>
                                                                    <td><a href="/admin/instance/index?instanceId=${instanceId}&tabTag=instance_clientList" target="_blank">${instanceMap[instanceId]}</a></td>
                                                                    <td>${instance.value['count']}</td>
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
</div>

<script type="text/javascript">
    function changeCondition(value) {
        console.log('radio:'+value);
        $('#condition').val(value);
        $('#conditionFrom').submit();
    }
</script>