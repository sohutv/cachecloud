<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<div class="container">
    <br/>

    <div class="row">
        <div class="col-md-8">
            <div class="page-header">
                <h4>实例信息-所属应用<a href="/admin/app/index.do?appId=${appDetail.appDesc.appId}" target="_blank">【${appDetail.appDesc.name}】</a></h4>
            </div>
            <table class="table table-striped table-hover">
                <tbody>
                <tr>
                    <td>内存使用率</td>
                    <td>
                        <div class="progress margin-custom-bottom0">
                            <c:choose>
                            <c:when test="${instanceStats.memUsePercent >= 80}">
                            <div class="progress-bar progress-bar-danger"
                                 role="progressbar" aria-valuenow="${instanceStats.memUsePercent}" aria-valuemax="100"
                                 aria-valuemin="0" style="width: ${instanceStats.memUsePercent}%">
                                </c:when>
                                <c:otherwise>
                                <div class="progress-bar progress-bar-success"
                                     role="progressbar" aria-valuenow="${instanceStats.memUsePercent}" aria-valuemax="100"
                                     aria-valuemin="0" style="width: ${instanceStats.memUsePercent}%">				                    		</c:otherwise>
                                    </c:choose>
                                    <label style="color: #000000">
                                        <fmt:formatNumber value="${instanceInfo.mem  * instanceStats.memUsePercent / 100 / 1024}" pattern="0.00"/>G&nbsp;&nbsp;Used/${instanceInfo.mem / 1024 * 1.0}G&nbsp;&nbsp;Total
                                    </label>
                                </div>
                            </div>
                    </td>
                    <!--
                     <td>${instanceInfo.mem}M</td>
                     <td>已用内存TODO</td>
                     <td><fmt:formatNumber value="${instanceStats.usedMemory/1024/1024}" pattern="#,#00"/>M</td>
                     -->
                    <td>命中率</td>
                    <c:choose>
                        <c:when test="${instanceStats.misses+instanceStats.hits==0}">
                            <td>无操作数据</td>
                        </c:when>
                        <c:otherwise>
                            <td><fmt:formatNumber value="${instanceStats.hits/(instanceStats.misses+instanceStats.hits)}" type="percent" maxFractionDigits="2"></fmt:formatNumber></td>
                        </c:otherwise>
                    </c:choose>

                    <td>实例角色</td>
                    <td>
                        <c:if test="${instanceStats.role == 1}">master</c:if>
                        <c:if test="${instanceStats.role == 2}">slave</c:if>
                    </td>
                </tr>
                <tr>
                    <td>当前对象数</td>
                    <td><fmt:formatNumber value="${instanceStats.currItems}" pattern="#,#00"/></td>
                    <td>实例类型</td>
                    <td>${instanceInfo.typeDesc}</td>
                    <td>当前连接数</td>
                    <td>${instanceStats.currConnections}</td>
                </tr>
                <tr>
                    <td>实例地址</td>
                    <td>${instanceInfo.ip}:${instanceInfo.port}</td>
                    <td>运行状态</td>
                    <td>${instanceInfo.statusDesc}</td>
                    <td>运行天数</td>
                    <td>
                    <c:choose>
                        <c:when test="${instanceInfo.type == 1}">
                            <fmt:formatNumber value="${instanceStats.infoMap['stats'].uptime/60/60/24}" pattern="0.0"/>
                        </c:when>
                        <c:otherwise>${instanceStats.infoMap['Server'].uptime_in_days}</c:otherwise>
                    </c:choose>
                    </td>
                </tr>
                </tbody>
            </table>
        </div>
        <div class="col-md-4">
            <div class="page-header">
                <h4>联系我们:</h4>
            </div>
            <div id="contact" class="page-body">
                 <jsp:include page="/WEB-INF/include/contact.jsp"/>
            </div>
        </div>
    </div>
    <div class="row">
        <c:choose>
            <c:when test="${instanceInfo.type == 1}">
                <div class="page-header">
                    <h4>实时状态</h4>
                </div>
                <table class="table table-bordered table-striped table-hover">
                    <thead>
                    <tr>
                        <td>分组</td>
                        <td>键值</td>
                        <td>值</td>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach items="${instanceStats.infoMap['stats']}" var="info" varStatus="status">
                        <c:if test="${status.last}">
                            <c:set var="rows" value="${status.count}" scope="page"></c:set>
                        </c:if>
                    </c:forEach>
                    <c:forEach items="${instanceStats.infoMap['stats']}" var="info" varStatus="status">
                        <tr>
                            <c:if test="${status.first}">
                                <td rowspan="${rows}">stats</td>
                            </c:if>
                            <td>${info.key}</td>
                            <td>${info.value}</td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>

                <div class="page-header">
                    <h4>实例的item状态</h4>
                </div>
                <table class="table table-bordered table-striped table-hover">
                    <thead>
                    <tr>
                        <td>item号</td>
                        <td>evicted_nonzero</td>
                        <td>outofmemory</td>
                        <td>reclaimed</td>
                        <td>age</td>
                        <td>evicted_time</td>
                        <td>number</td>
                        <td>tailrepairs</td>
                        <td>evicted</td>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach items="${instanceStats.infoMap['itemsVo']}" var="item" varStatus="status">
                        <tr>
                            <td>${item.key}</td>
                            <td>${item.value.evictedNonzero}</td>
                            <td>${item.value.outOfMemory}</td>
                            <td>${item.value.reclaimed}</td>
                            <td>${item.value.age}</td>
                            <td>${item.value.evictedTime}</td>
                            <td>${item.value.number}</td>
                            <td>${item.value.tailRepairs}</td>
                            <td>${item.value.evicted}</td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>

                <div class="page-header">
                    <h4>实例的slab状态</h4>
                </div>
                <table class="table table-bordered table-striped table-hover">
                    <thead>
                    <tr>
                        <td>slab号</td>
                        <td>free_<br/>chunks</td>
                        <td>incr_hits</td>
                        <td>delete_hits</td>
                        <td>cmd_set</td>
                        <td>get_hits</td>
                        <td>chunk_size</td>
                        <td>decr_hits</td>
                        <td>chunks_<br/>per_page</td>
                        <td>free_chunks_end</td>
                        <td>used_<br/>chunks</td>
                        <td>total_pages</td>
                        <%--<td>cas_hits</td>--%>
                        <td>mem_requested</td>
                        <%--<td>cas_badval</td>--%>
                        <td>total_<br/>chunks</td>

                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach items="${instanceStats.infoMap['slabsVo']}" var="slab" varStatus="status">
                        <tr>
                            <td>${slab.key}</td>
                            <td>${slab.value.freeChunks}</td>
                            <td>${slab.value.incrHits}</td>
                            <td>${slab.value.deleteHits}</td>
                            <td>${slab.value.cmdSet}</td>
                            <td>${slab.value.getHits}</td>
                            <td>${slab.value.chunkSize}</td>
                            <td>${slab.value.decrHits}</td>
                            <td>${slab.value.chunksPerPage}</td>
                            <td>${slab.value.freeChunksEnd}</td>
                            <td>${slab.value.usedChunks}</td>
                            <td>${slab.value.totalPages}</td>
                            <%--<td>${slab.value.casHits}</td>--%>
                            <td>${slab.value.memRequested}</td>
                            <%--<td>${slab.value.casBadval}</td>--%>
                            <td>${slab.value.totalChunks}</td>

                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </c:when>
            <c:when test="${instanceInfo.type == 2 or instanceInfo.type == 5 or instanceInfo.type == 6}">
                <div class="page-header">
                    <h4>实时状态</h4>
                </div>
                <table class="table table-bordered table-striped table-hover">
                    <thead>
                    <tr>
                        <td>分组</td>
                        <td>键值</td>
                        <td>值</td>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach items="${instanceStats.infoMap}" var="infoMap">
                        <c:forEach items="${infoMap.value}" var="info" varStatus="status">
                            <c:if test="${status.last}">
                                <c:set var="rows" value="${status.count}" scope="page"></c:set>
                            </c:if>
                        </c:forEach>
                        <c:forEach items="${infoMap.value}" var="info" varStatus="status">
                            <tr>
                                <c:if test="${status.first}">
                                    <td rowspan="${rows}">${infoMap.key}</td>
                                </c:if>
                                <td>${info.key}</td>
                                <td>${info.value}</td>
                            </tr>
                        </c:forEach>
                    </c:forEach>
                    </tbody>
                </table>
            </c:when>
        </c:choose>
    </div>
</div>
