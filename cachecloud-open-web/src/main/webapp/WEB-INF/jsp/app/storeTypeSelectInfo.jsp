<%@ page contentType="text/html;charset=UTF-8" language="java" import="com.sohu.cache.web.enums.AppOrderByEnum" %>
<%@ include file="/WEB-INF/jsp/manage/commons/appConstants.jsp"%>

<!DOCTYPE html>
<html lang="en">
<head>
    <title>CacheCloud应用列表</title>
    <jsp:include page="/WEB-INF/include/head.jsp"/>
</head>
<body>
	<div class="container">
	    <jsp:include page="/WEB-INF/include/headMenu.jsp"/>
	    <div id="systemAlert">
	    </div>
		<div class="row">
			<div class="page-header">
				<h1>Redis存储方案选型说明</h1>
			</div>
			<div class="bs-docs-example">
				<p>&nbsp;&nbsp;&nbsp;&nbsp;CacheCloud针对redis提供了三种存储方案, 分别是:
				<p>&nbsp;&nbsp;&nbsp;&nbsp;一、Redis-Standalone:</p>
				
				<ol>
					<li>
						&nbsp;&nbsp;&nbsp;&nbsp;特点:
					</li>
					<li>
						&nbsp;&nbsp;&nbsp;&nbsp;示意图:
					</li>
					<li>
						&nbsp;&nbsp;&nbsp;&nbsp;不足:
						<p>1. 非高可用，出现故障时不能自动迁移</p>
						<p>2. 非分布式结构，不能实现水平扩容</p>
					</li>
				</ol>
				
				<p>&nbsp;&nbsp;&nbsp;&nbsp;二、Redis-Sentinel:</p>
				<ol>
					<li>
						&nbsp;&nbsp;&nbsp;&nbsp;特点:
					</li>
					<li>
						&nbsp;&nbsp;&nbsp;&nbsp;示意图:<br/>
					    <img src="/resources/img/sentinel.jpg"><br/>
					    
					</li>
					<li>
						&nbsp;&nbsp;&nbsp;&nbsp;不足:
						<p>2. 非分布式结构，不能实现水平扩容</p>
					</li>
				</ol>
				
				
				<p>&nbsp;&nbsp;&nbsp;&nbsp;三、Redis-Cluster:</p>
				<ol>
					<li>
						&nbsp;&nbsp;&nbsp;&nbsp;特点:
						同时满足高可用，水平扩容
					</li>
					<li>
						&nbsp;&nbsp;&nbsp;&nbsp;示意图:
					</li>
					<li>
						&nbsp;&nbsp;&nbsp;&nbsp;不足:
					</li>
				</ol>			
			
			
			</div>
			
		</div>
	</div>
	<jsp:include page="/WEB-INF/include/foot.jsp"/>
	<script type="text/javascript" src="/resources/js/mem-cloud.js"></script>

</body>
</html>
