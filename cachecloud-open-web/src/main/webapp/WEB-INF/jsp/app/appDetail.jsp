<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp"%>

<div class="container">
    <div class="row">
        <div class="col-md-8">
            <div class="page-header">
                                        应用详情&nbsp;&nbsp;&nbsp;
                <button type="button" class="btn default" data-target="#updateAppDetailModal" data-toggle="modal">修改应用信息</button>
            </div>
            <table class="table table-striped table-hover">
                <tbody>
                <tr>
                    <td>应用id</td>
                    <td>${appDetail.appDesc.appId}</td>
                    <td>应用名称</td>
                    <td>${appDetail.appDesc.name}</td>
                </tr>
                <tr>
                    <td>应用申请人</td>
                    <td>${appDetail.appDesc.officer}</td>
                    <td>应用类型</td>
                    <td>
                    	<c:choose>
        		            <c:when test="${appDetail.appDesc.type == 2}">redis-cluster</c:when>
		        		    <c:when test="${appDetail.appDesc.type == 5}">redis-sentinel</c:when>
		        		    <c:when test="${appDetail.appDesc.type == 6}">redis-standalone</c:when>
                    	</c:choose>
                    </td>
                </tr>
                <tr>
                    <td>报警用户</td>
                    <td>
                    	<c:forEach items="${appDetail.appUsers}" var="appUser" varStatus="stat">
                    		<c:if test="${stat.index != 0}">
                    			;
                    		</c:if>
                    		${appUser.chName}(${appUser.name})
                    	</c:forEach>
                    </td>
                    <td>负责人</td>
                    <td>${appDetail.appDesc.officer}</td>
                </tr>
                <tr>
                    <td>内存空间</td>
                    <td><fmt:formatNumber value="${appDetail.mem / 1024 * 1.0}" pattern="0.00"/>G</td>
                    <td>分布机器数</td>
                    <td>${appDetail.machineNum}</td>
                </tr>
                <tr>
                    <td>主节点数</td>
                    <td>${appDetail.masterNum}</td>
                    <td>从节点数</td>
                    <td>${appDetail.slaveNum}</td>
                </tr>
                <tr>
                    <td>appKey</td>
                    <td>
                    <c:choose>
                    	<c:when test="${appDetail.appDesc.appKey == null || appDetail.appDesc.appKey == ''}">
                    		暂无
                    	</c:when>
                    	<c:otherwise>
                    		${appDetail.appDesc.appKey}
                    	</c:otherwise>
                    </c:choose>
                    </td>
                </tr>
                </tbody>
            </table>
        </div>
        
        <div class="col-md-4">
            <div class="page-header">
                <h4>报警指标</h4>
            </div>
            <table class="table table-striped table-hover">
                <thead>
	                <tr>
	                    <td>id</td>
	                    <td>报警key</td>
	                    <td>阀值</td>
	                    <td>周期</td>
	                </tr>
                </thead>
                <tbody>
	                <tr>
	                    <td>1</td>
	                    <td>内存使用率大于</td>
	                    <td>${appDetail.appDesc.memAlertValue}%</td>
	                    <td>每20分钟</td>
	                </tr>
	                 <tr>
	                    <td>2</td>
	                    <td>客户端连接数大于</td>
	                    <td>${appDetail.appDesc.clientConnAlertValue}</td>
	                    <td>每20分钟</td>
	                </tr>
                </tbody>
            </table>
        </div>
    </div>
    
    <div class="row">
   	 	<div class="col-md-12 page-header">
            <h4>
            	用户管理&nbsp;&nbsp;&nbsp;
            	<button type="button" class="btn default" data-target="#appAddUserModal" data-toggle="modal">添加用户</button>
            </h4>
        </div>
   		<div class="col-md-12">
   		  	<table class="table table-striped table-hover">
	   			<thead>
					<tr>
						<td>id</td>
						<td>域账户</td>
						<td>中文名</td>
						<td>邮箱</td>
						<td>手机</td>
						<td>操作</td>
					</tr>
				</thead>
				<tbody>
					<c:forEach items="${appDetail.appUsers}" var="user">
						<tr>
							<td>${user.id}</td>
							<td>${user.name}</td>
							<td>${user.chName}</td>
							<td>${user.email}</td>
							<td>${user.mobile}</td>
							<td>
							<a href="javascript;" data-target="#addUserModal${user.id}" data-toggle="modal">[修改]</a>
							&nbsp;
							<a href="javascript:void(0);" onclick="deleteAppUser('${user.id}','${appDetail.appDesc.appId}')">[删除]</a>
							</td>
						</tr>
					</c:forEach>
				</tbody>
			</table>
   		</div>
    </div>

 	<div class="row">
   	 	<div class="col-md-12 page-header">
            <h4>管理操作</h4>
        </div>
   		<div class="col-md-12">
   		   				<div class="col-md-2"></div>
   		
   			<button class="col-md-3" type="button" class="btn default" data-target="#appAddUserModal" data-toggle="modal">添加报警接收用户</button>
   				<div class="col-md-2"></div>
   			<button class="col-md-3" type="button" class="btn default" data-target="#appAlertConfigModal" data-toggle="modal">应用报警配置</button>
   		</div>
    </div>
    <br/><br/><br/>
    <br/><br/><br/>
	
</div>

<div id="appAddUserModal" class="modal fade" tabindex="-1" data-width="400">
	<div class="modal-dialog">
		<div class="modal-content">
		
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal" aria-hidden="true"></button>
				<h4 class="modal-title">添加用户</h4>
			</div>
			
			<form class="form-horizontal form-bordered form-row-stripped">
				<div class="modal-body">
					<div class="row">
						<!-- 控件开始 -->
						<div class="col-md-12">
							<!-- form-body开始 -->
							<div class="form-body">
								<div class="form-group">
									<label class="control-label col-md-4">
										用户名(邮箱前缀):
									</label>
									<div class="col-md-6">
										<input type="text" name="userName" id="userName" placeholder="用户名" class="form-control" />
										<span class="help-block">请确保用户已经申请cachecloud权限</span>
									</div>
								</div>
							</div>
							<!-- form-body 结束 -->
						</div>
						<div id="appAddUserInfo"></div>
						<!-- 控件结束 -->
					</div>
				</div>
				
				<div class="modal-footer">
					<button type="button" data-dismiss="modal" class="btn" >Close</button>
					<button type="button" id="appAddUserBtn" class="btn red" onclick="appAddUser('${appDetail.appDesc.appId}')">Ok</button>
				</div>
			
			</form>
		</div>
	</div>
</div>


<div id="appAlertConfigModal" class="modal fade" tabindex="-1" data-width="400">
	<div class="modal-dialog">
		<div class="modal-content">
		
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal" aria-hidden="true"></button>
				<h4 class="modal-title">应用报警修改</h4>
			</div>
			
			<form class="form-horizontal form-bordered form-row-stripped">
				<div class="modal-body">
					<div class="row">
						<!-- 控件开始 -->
						<div class="col-md-12">
							<!-- form-body开始 -->
							<div class="form-body">
								<div class="form-group">
									<label class="control-label col-md-3">内存报警阀值:</label>
									<div class="col-md-7">
										<input type="text" name="memAlertValue" value="${appDetail.appDesc.memAlertValue}" id="memAlertValue" placeholder="内存报警阀值" class="form-control" onchange="testisNum(this.id)">
										<span class="help-block">例如:如果想内存使用率超过90%报警，填写90<br/><font color="red">(如果不需要报警请填写100以上的数字)</font></span>
									</div>
								</div>
								
								<div class="form-group">
									<label class="control-label col-md-3">客户端连接数报警阀值:</label>
									<div class="col-md-7">
										<input type="text" name="clientConnAlertValue" value="${appDetail.appDesc.clientConnAlertValue}" id="clientConnAlertValue" placeholder="客户端连接数报警阀值" class="form-control" onchange="testisNum(this.id)">
										<span class="help-block">例如:如果想客户端连接数率超过2000报警，填写2000</span>
									</div>
								</div>
								
							</div>
							<!-- form-body 结束 -->
						</div>
						<div id="appConfigChangeInfo"></div>
						<!-- 控件结束 -->
					</div>
				</div>
				
				<div class="modal-footer">
					<button type="button" data-dismiss="modal" class="btn" >Close</button>
					<button type="button" id="appConfigChangeBtn" class="btn red" onclick="appAlertConfigChange('${appDetail.appDesc.appId}')">Ok</button>
				</div>
			
			</form>
		</div>
	</div>
</div>


<div id="updateAppDetailModal" class="modal fade" tabindex="-1" data-width="400">
	<div class="modal-dialog">
		<div class="modal-content">
		
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal" aria-hidden="true"></button>
				<h4 class="modal-title">应用信息修改</h4>
			</div>
			
			<form class="form-horizontal form-bordered form-row-stripped">
				<div class="modal-body">
					<div class="row">
						<!-- 控件开始 -->
						<div class="col-md-12">
							<!-- form-body开始 -->
							<div class="form-body">
								<div class="form-group">
									<label class="control-label col-md-3">应用名:</label>
									<div class="col-md-7">
										<input type="text" name="appDescName" value="${appDetail.appDesc.name}" id="appDescName" class="form-control">
									</div>
								</div>
								
								<div class="form-group">
									<label class="control-label col-md-3">应用描述:</label>
									<div class="col-md-7">
										<textarea class="form-control" name="appDescIntro" rows="3" id="appDescIntro" placeholder="应用描述">${appDetail.appDesc.intro}</textarea>
									</div>
								</div>
								
								<div class="form-group">
									<label class="control-label col-md-3">负责人:</label>
									<div class="col-md-7">
										<input type="text" name="officer" value="${appDetail.appDesc.officer}" id="officer" class="form-control">
									</div>
								</div>
								
							</div>
							<!-- form-body 结束 -->
						</div>
						<div id="updateAppDetailInfo"></div>
						<!-- 控件结束 -->
					</div>
				</div>
				
				<div class="modal-footer">
					<button type="button" data-dismiss="modal" class="btn" >Close</button>
					<button type="button" id="updateAppDetailBtn" class="btn red" onclick="updateAppDetailChange('${appDetail.appDesc.appId}')">Ok</button>
				</div>
			
			</form>
		</div>
	</div>
</div>


<c:forEach items="${appDetail.appUsers}" var="user">
	<div id="addUserModal${user.id}" class="modal fade" tabindex="-1" data-width="400">
		<div class="modal-dialog">
			<div class="modal-content">
			
				<div class="modal-header">
					<button type="button" class="close" data-dismiss="modal" aria-hidden="true"></button>
					<h4 class="modal-title">管理用户</h4>
				</div>
				
				<form class="form-horizontal form-bordered form-row-stripped">
					<div class="modal-body">
						<div class="row">
							<!-- 控件开始 -->
							<div class="col-md-12">
								<!-- form-body开始 -->
								<div class="form-body">
									<div class="form-group">
										<label class="control-label col-md-3">
											域账户名:
										</label>
										<div class="col-md-5">
											<input type="text" name="name" id="name${user.id}"
												value="${user.name}" placeholder="域账户名(邮箱前缀)"
												class="form-control" />
										</div>
									</div>
									
									<div class="form-group">
										<label class="control-label col-md-3">
											中文名:
										</label>
										<div class="col-md-5">
											<input type="text" name="chName" id="chName${user.id}"
												value="${user.chName}" placeholder="中文名"
												class="form-control" />
										</div>
									</div>
									
									<div class="form-group">
										<label class="control-label col-md-3">
											邮箱:
										</label>
										<div class="col-md-5">
											<input type="text" name="email" id="email${user.id}"
												value="${user.email}" placeholder="邮箱"
												class="form-control" />
										</div>
									</div>
									
									<div class="form-group">
										<label class="control-label col-md-3">
											手机:
										</label>
										<div class="col-md-5">
											<input type="text" name="mobile" id="mobile${user.id}"
												value="${user.mobile}" placeholder="手机"
												class="form-control" />
										</div>
									</div>
									
									<input type="hidden" id="type${user.id}" value="${user.type}">
									<input type="hidden" id="userId${user.id}" name="userId" value="${user.id}"/>
								</div>
								<!-- form-body 结束 -->
							</div>
							<div id="info${user.id}"></div>
							<!-- 控件结束 -->
						</div>
					</div>
					
					<div class="modal-footer">
						<button type="button" data-dismiss="modal" class="btn" >Close</button>
						<button type="button" class="btn red" onclick="saveOrUpdateUser('${user.id}')">Ok</button>
					</div>
				</form>
			</div>
		</div>
	</div>
</c:forEach>


