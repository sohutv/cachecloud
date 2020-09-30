<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp"%>

<div class="row">
	<div class="col-md-12">
		<h3 class="page-title">
			配置信息
		</h3>
	</div>
</div>
<div class="row">
	<div class="col-md-12">
		<div class="portlet box light-grey">
			<div class="portlet-title">
				<div class="caption"><i class="fa fa-globe"></i>配置信息列表</div>
				<div class="tools">
					<a href="javascript:;" class="collapse"></a>
				</div>
			</div>
			<div class="portlet-body">
				<table class="table table-striped table-bordered table-hover" id="tableDataList">
					<thead>
						<tr>
			                <td>配置项</td>
			                <td>配置值</td>
						</tr>
					</thead>
					<tbody>
            			<c:forEach items="${redisConfigList}" var="redisConfig" varStatus="status">
			                <tr>
			                    <td>${redisConfig.key}</td>
			                    <td>${redisConfig.value}</td>
			                </tr>
			            </c:forEach>
					</tbody>
				</table>
			</div>
		</div>
	</div>
</div>