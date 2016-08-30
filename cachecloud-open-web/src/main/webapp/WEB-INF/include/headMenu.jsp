<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@page import="com.sohu.cache.util.ConstUtils"%>   
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp"%>
<div class="navbar navbar-inverse navbar-fixed-top" role="navigation">
    <div class="container">
        <div class="navbar-header">
            <a class="navbar-brand" href="/admin/app/list.do">CacheCloud</a>
        </div>
        <div class="navbar-collapse collapse">
            <ul class="nav navbar-nav navbar-right">
                <li class="dropdown">
                    <a href="#" class="dropdown-toggle" data-toggle="dropdown"><font color='white'>${userInfo.chName}</font><b class="caret"></b>
					<i class="fa fa-angle-down"></i>
					</a>
					<ul class="dropdown-menu">
						<!-- 
						<li><a href="javascript;" data-target="#addUserModal${userInfo.id}" data-toggle="modal"><i class="fa fa-user"></i>修改资料</a></li>
						-->
						<c:if test="${userInfo.type == 0}">
							<li><a target="_blank" href="/manage/total/list.do"><i class="fa fa-user"></i>管理后台</a></li>
							<li><a target="_blank" href="/import/app/init"><i class="fa fa-user"></i>导入应用</a></li>
							<li><a target="_blank" href="/data/migrate/list"><i class="fa fa-user"></i>迁移数据工具</a></li>
						</c:if>
						<li><a href="/admin/app/list.do"><i class="fa fa-user"></i>应用列表</a></li>
						<li><a href="/admin/app/init.do"><i class="fa fa-user"></i>应用申请</a></li>
						<li><a href="javascript:alert('CacheCloud <%=ConstUtils.CACHECLOUD_VERSION%>版本')" ><i class="fa fa-user"></i>关于</a></li>
						<li><a href="/manage/logout.do"><i class="fa fa-user"></i>注销</a></li>
					</ul>
                </li>
            </ul>
            
            <ul class="nav navbar-nav navbar-left">
                <li>
                    <a href="<%=ConstUtils.DOCUMENT_URL%>"><font color='white'>CacheCloud文档</font>
					</a>
                </li>
            </ul>
            
            
            <ul class="nav navbar-nav navbar-left">
                <li>
                    <a href="/admin/app/initBecomeContributor.do"><font color='white'>成为CacheCloud贡献者</font>
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
