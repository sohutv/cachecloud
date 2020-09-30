<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.sohu.cache.util.ConstUtils"%>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp"%>
<div class="navbar navbar-inverse navbar-fixed-top" role="navigation">
    <div class="container">
        <div class="navbar-header">
            <a class="navbar-brand" href="/admin/app/list">CacheCloud</a>
        </div>
        <div class="navbar-collapse collapse">
            <ul class="nav navbar-nav navbar-right">
                <li class="dropdown">
                    <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                        <span class="glyphicon glyphicon-cog"></span>
                        <font color='white'> ${userInfo.chName}</font>
                        <b class="caret"></b>
<%--					<i class="fa fa-angle-down"></i>--%>
					</a>
					<ul class="dropdown-menu">
						<c:if test="${userInfo.type == 0}">
							<li><a target="_blank" href="/manage/total/statlist"><span class="glyphicon glyphicon-th-large"></span> 管理后台</a></li>
							<li><a target="_blank" href="/import/app/init"><span class="glyphicon glyphicon-import"></span> 导入应用</a></li>
                            <li><a target="_blank" href="/data/migrate/list"><span class="glyphicon glyphicon-retweet"></span> 数据迁移工具</a></li>
                            <li class="divider"></li>
						</c:if>
						<li><a href="/admin/app/list"><span class="glyphicon glyphicon-th-list"></span> 应用列表</a></li>
                        <li><a target="_blank" href="/admin/app/jobs"><span class="glyphicon glyphicon-list-alt"></span> 我的申请</a></li>
						<li><a href="javascript:alert('CacheCloud <%=ConstUtils.CACHECLOUD_VERSION%>版本')" ><span class="glyphicon glyphicon-info-sign"></span> 关于</a></li>
						<li><a href="/manage/logout"><span class="glyphicon glyphicon-log-out"></span> 注销</a></li>
					</ul>
                </li>
            </ul>
            
            <ul class="nav navbar-nav navbar-left">
                <li>
                    <a href="/wiki/intro/index"><font color='white'>CacheCloud文档</font>
					</a>
                </li>
            </ul>
            
            
            <ul class="nav navbar-nav navbar-left">
                <li>
                    <a href="/admin/app/initBecomeContributor"><font color='white'>成为CacheCloud贡献者</font>
					</a>
                </li>
            </ul>
            
            <ul class="nav navbar-nav navbar-left">
                <li>
                    <a href="/daily.jsp"><font color='white'>值班电话</font>
					</a>
                </li>
            </ul>
            
            
        </div>
    </div>
</div>
