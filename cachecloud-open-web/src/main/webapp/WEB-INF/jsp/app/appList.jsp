<%@ page contentType="text/html;charset=UTF-8" language="java" import="com.sohu.cache.web.enums.AppOrderByEnum" %>
<%@ include file="/WEB-INF/jsp/manage/commons/appConstants.jsp"%>

<!DOCTYPE html>
<html lang="en">
<head>
    <title>CacheCloud应用列表</title>
    <jsp:include page="/WEB-INF/include/head.jsp"/>
    <script type="text/javascript">
  	//改变内存阀值
    function cleanAppData(appId){
    	var appCleanDataBtn = document.getElementById("appCleanDataBtn" + appId);
    	appCleanDataBtn.disabled = true;
    	
    	$.post(
    		'/admin/app/cleanAppData.do',
    		{
    			appId: appId
    		},
            function(data){
                if(data==1){
                    alert("appId:" + appId + "清除数据成功！");
                }else{
                    alert("appId:" + appId + "清除数据失败,请联系管理员查看原因！");
                }
            	appCleanDataBtn.disabled = false;
            }
         );
    }
    </script>
</head>
<body role="document">
<div class="container">
    <jsp:include page="/WEB-INF/include/headMenu.jsp"/>
    <div id="systemAlert">
    </div>
    <div class="row">
        <div class="col-md-8">
            <div class="page-header">
                <h4>全局信息</h4>
            </div>
            <table class="table table-striped table-hover">
                <tbody>
                <tr>
                    <td>总应用数</td>
                    <td>${totalApps}个 </td>
                    <td>总申请内存</td>
                    <td>
                    	<fmt:formatNumber value="${totalApplyMem / 1024.0}" pattern="0.00"/>G
                    </td>
                </tr>
                <tr>
                    <td>已使用内存</td>
                    <td>
                    	<fmt:formatNumber value="${totalUsedMem / 1024.0}" pattern="0.00"/>G
                    </td>
                    <td></td>
                    <td></td>
                </tr>
                </tbody>
            </table>
        </div>
        <div class="col-md-4">
            <div class="page-header">
                <h4>联系我们: </h4>
            </div>
            <div id="contact" class="page-body">
            	<jsp:include page="/WEB-INF/include/contact.jsp"/>
            </div>
        </div>
    </div>
    
    
    
    <div class="row">
        <div class="col-md-12">
            <div class="page-header">
                <h4>应用列表</h4>
            </div>
            <c:if test="${currentUser.type == 0}">
				<div style="float:right">
					<form class="form-inline" method="post" action="/admin/app/list.do" id="appList" name="ec">
					  <div class="form-group">
					    <input type="text" class="form-control" id="appId" name="appId" value="${appSearch.appId}" placeholder="应用ID" onchange="testisNum(this.id)">
					  </div>
					  <div class="form-group">
					    <input type="text" class="form-control" id="appName" name="appName" value="${appSearch.appName}" placeholder="应用名">
					  </div>
					  <div class="form-group">
						<select name="appType" class="form-control">
							<option value="">
								全部类型
							</option>
							<option value="2" <c:if test="${appSearch.appType == 2}">selected</c:if>>
								Redis-cluster
							</option>
							<option value="5" <c:if test="${appSearch.appType == 5}">selected</c:if>>
								redis-sentinel
							</option>
							<option value="6" <c:if test="${appSearch.appType == 6}">selected</c:if>>
								redis-standalone
							</option>
						</select>
					 </div>
					 <div class="form-group">
						<select name="appStatus" class="form-control">
							<option value="">
								全部状态
							</option>
							<option value="0" <c:if test="${appSearch.appStatus == 0}">selected</c:if>>
								未分配
							</option>
							<option value="1" <c:if test="${appSearch.appStatus == 1}">selected</c:if>>
								申请中
							</option>
							<option value="2" <c:if test="${appSearch.appStatus == 2}">selected</c:if>>
								运行中
							</option>
		                    <option value="3" <c:if test="${appSearch.appStatus == 3}">selected</c:if>>
		                       	 已下线
		                    </option>
		                    <option value="4" <c:if test="${appSearch.appStatus == 4}">selected</c:if>>
		                       	 驳回
		                    </option>
						</select>
					 </div>
					 <div class="form-group">
						<select name="importantLevel" class="form-control">
							<option value="">
								全部级别
							</option>
							<option value="1" <c:if test="${appSearch.importantLevel == 1}">selected</c:if>>
								S级
							</option>
							<option value="2" <c:if test="${appSearch.importantLevel == 2}">selected</c:if>>
								A级
							</option>
							<option value="3" <c:if test="${appSearch.importantLevel == 3}">selected</c:if>>
								B级
							</option>
		                    <option value="4" <c:if test="${appSearch.importantLevel == 4}">selected</c:if>>
		                       	C级
		                    </option>
						</select>
					 </div>
					 
					 <div class="form-group">
						<select name="pageSize" class="form-control">
							<option value="10" <c:if test="${page.pageSize == 10}">selected</c:if>>
								10行
							</option>
		                    <option value="20" <c:if test="${page.pageSize == 20}">selected</c:if>>
		                       	 20行
		                    </option>
		                    <option value="50" <c:if test="${page.pageSize == 50}">selected</c:if>>
		                       	 50行
		                    </option>
		                    <option value="100" <c:if test="${page.pageSize == 100}">selected</c:if>>
		                       	 100行
		                    </option>
						</select>
					 </div>
					 
					 
					  <input type="hidden" name="pageNo" id="pageNo">
					  <button type="submit" class="btn btn-default">查询</button>
					</form>
				</div>
		    </c:if>
		</div>
	</div>
	<div class="row">
			<br/>
			<div class="col-md-12">
	            <table class="table table-striped table-hover" style="margin-top: 0px">
	                <thead>
		                <tr>
		                    <td>应用ID</td>
		                    <td>应用名</td>
		                    <td>应用类型</td>
		                    <td>内存详情</td>
		                    <td>命中率</td>
		                    <td>已运行时间</td>
		                    <td>申请状态</td>
		                    <td>操作</td>
		                </tr>
	                </thead>
	                <tbody>
	                	<c:forEach items="${appDetailList}" var="appDetail">
	                		 <tr>
			                    <td>
			                    	<c:choose>
			                    		<c:when test="${appDetail.appDesc.status == 0 or appDetail.appDesc.status == 1}">
			                   				${appDetail.appDesc.appId}
			                    		</c:when>
			                    		<c:when test="${appDetail.appDesc.status == 2 or appDetail.appDesc.status == 3 or appDetail.appDesc.status == 4}">
			                    			<a title="${appDetail.appDesc.intro}" target="_blank" href="/admin/app/index.do?appId=${appDetail.appDesc.appId}">${appDetail.appDesc.appId}</a>
			                    		</c:when>
			                    	</c:choose>
			                    </td>
			                    <td>
			                    	<c:choose>
			                    		<c:when test="${appDetail.appDesc.status == 0 or appDetail.appDesc.status == 1}">
			                    			${appDetail.appDesc.name}
			                    		</c:when>
			                    		<c:when test="${appDetail.appDesc.status == 2 or appDetail.appDesc.status == 3 or appDetail.appDesc.status == 4}">
			                    			<a target="_blank" href="/admin/app/index.do?appId=${appDetail.appDesc.appId}">${appDetail.appDesc.name}</a>
			                    		</c:when>
			                    	</c:choose>
			                    </td>
			                    <td>
			                    	<c:choose>
			        		            <c:when test="${appDetail.appDesc.type == 2}">redis-cluster</c:when>
	    		        		        <c:when test="${appDetail.appDesc.type == 5}">redis-sentinel</c:when>
	    		        		        <c:when test="${appDetail.appDesc.type == 6}">redis-standalone</c:when>
			                    	</c:choose>
			                    </td>
			                    <td>
			                        <div class="progress margin-custom-bottom0">
			                        	<c:choose>
			                        		<c:when test="${appDetail.memUsePercent >= 80}">
												<c:set var="progressBarStatus" value="progress-bar-danger"/>
			                        		</c:when>
			                        		<c:otherwise>
												<c:set var="progressBarStatus" value="progress-bar-success"/>
			                        		</c:otherwise>
			                        	</c:choose>
										<div class="progress-bar ${progressBarStatus}"
											role="progressbar" aria-valuenow="${appDetail.memUsePercent}" aria-valuemax="100"
											aria-valuemin="0" style="width: ${appDetail.memUsePercent}%">
											<label style="color: #000000">
												<fmt:formatNumber value="${appDetail.mem * appDetail.memUsePercent / 100 / 1024}" pattern="0.00"/>G&nbsp;&nbsp;Used/<fmt:formatNumber value="${appDetail.mem / 1024 * 1.0}" pattern="0.00"/>G&nbsp;&nbsp;Total
											</label>
										</div>
									</div>
			                    </td>
			                    <td>
			                    	<c:choose>
			                    		<c:when test="${appDetail.hitPercent <= 0}">
			                    			无
			                    		</c:when>
			                    		<c:when test="${appDetail.hitPercent <= 30}">
			                    			<label class="label label-danger">${appDetail.hitPercent}%</label>
			                    		</c:when>
			                    		<c:when test="${appDetail.hitPercent >= 30 && appDetail.hitPercent < 50}">
			                    			<label class="label label-warning">${appDetail.hitPercent}%</label>
			                    		</c:when>
			                    		<c:when test="${appDetail.hitPercent >= 50 && appDetail.hitPercent < 90}">
			                    			<label class="label label-info">${appDetail.hitPercent}%</label>
			                    		</c:when>
			                    		<c:otherwise>
			                    			<label class="label label-success">${appDetail.hitPercent}%</label>
			                    		</c:otherwise>
			                    	</c:choose>
			                    	
			                    </td>
			                    <td>${appDetail.appDesc.appRunDays}天</td>
			                    <td>
			                    	<c:choose>
			                    		<c:when test="${appDetail.appDesc.status == 0}">
			                    			<font color="red">未申请</font>
			                    		</c:when>
			                    		<c:when test="${appDetail.appDesc.status == 1}">
			                    			<font color="red">申请中</font>
			                    		</c:when>
			                    		<c:when test="${appDetail.appDesc.status == 2}">
			                    			运行中
			                    		</c:when>
	                                    <c:when test="${appDetail.appDesc.status == 3}">
	                                        <font color="red">已下线</font>
	                                    </c:when>
	                                    <c:when test="${appDetail.appDesc.status == 4}">
	                                        <font color="red">驳回</font>
	                                    </c:when>
			                    	</c:choose>
			                    </td>
			                    <td>
			                    	<c:choose>
			                    		<c:when test="${appDetail.appDesc.status == 2 && appDetail.appDesc.isTest == 1}">
			                    			<button type="button" id="appCleanDataBtn${appDetail.appDesc.appId}" onclick="if(window.confirm('确认要清除应用appid=${appDetail.appDesc.appId}的数据?!')){cleanAppData('${appDetail.appDesc.appId}');return true;}else{return false;}">清空数据</button>
			                    		</c:when>
			                    	</c:choose>
			                    </td>
			                </tr>
	                	</c:forEach>
	                </tbody>
	            </table>
            </div>
            <div style="margin-bottom: 10px;float: right;margin-right: 15px">
				<span>
					<ul id='ccPagenitor' style="margin-bottom: 0px;margin-top: 0px"></ul>
					<div id="pageDetail" style="float:right;padding-top:7px;padding-left:8px;color:#4A64A4;display: none">共${page.totalPages}页,${page.totalCount}条</div>		
				</span>
			</div>
        </div>
    </div>

<jsp:include page="/WEB-INF/include/foot.jsp"/>

<script type="text/javascript" src="/resources/js/mem-cloud.js"></script>

<script src="/resources/bootstrap/paginator/bootstrap-paginator.js"></script>
<script src="/resources/bootstrap/paginator/custom-pagenitor.js"></script>
<script type="text/javascript">
    $(function(){
    	var userType = '${currentUser.type}';
    	if (userType == 0) {
    		//分页点击函数
        	var pageClickedFunc = function (e, originalEvent, type, page){
        		//form传参用pageSize
        		document.getElementById("pageNo").value=page;
        		document.getElementById("appList").submit();
        	};
        	//分页组件
            var element = $('#ccPagenitor');
            //当前page号码
            var pageNo = '${page.pageNo}';
            //总页数
            var totalPages = '${page.totalPages}';
            //显示总页数
            var numberOfPages = '${page.numberOfPages}';
    		var options = generatePagenitorOption(pageNo, numberOfPages, totalPages, pageClickedFunc);
    		if(totalPages > 0){
    			element.bootstrapPaginator(options);
    			document.getElementById("pageDetail").style.display = "";
    		}else{
    			element.html("未查询到相关记录！");
    		}
    	}
    	
    });
</script>


</body>
</html>
