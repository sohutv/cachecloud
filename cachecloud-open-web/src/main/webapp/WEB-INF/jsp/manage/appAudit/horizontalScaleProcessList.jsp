<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp"%>

<div class="row">
    <div class="col-md-12">
        <h3 class="page-title">
        	迁移进度
        </h3>
    </div>
</div>
<div class="row">
    <div class="col-md-12">
        <div class="portlet box light-grey">
            <div class="portlet-title">
                <div class="caption"><i class="fa fa-globe"></i>扩容进度</div>
                <div class="tools">
                    <a href="javascript:;" class="collapse"></a>
                </div>
            </div>
            <div class="portlet-body">
                <table class="table table-striped table-bordered table-hover" id="tableDataList">
                    <thead>
                    <tr>
                        <td>应用id</td>
                        <td>slot迁移进度</td>
                        <td>节点状态</td>
                        <td>状态</td>
                        <td>开始时间</td>
                        <td>结束时间 </td>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="processMap" items="${appScaleProcessMap}">
                        <tr>
                            <td>${processMap.key}</td>
                            <td>
	                          <div class="progress margin-custom-bottom0">
						      	  <div id="reshardSlotProgress${processMap.key}" class="progress-bar progress-bar-success"
						           role="progressbar" aria-valuenow="${processMap.value.reshardSlot}" aria-valuemax="${processMap.value.totalSlot}"
						           aria-valuemin="0" style="width: ${processMap.value.reshardSlot / processMap.value.totalSlot}">
						             <label style="color: #000000">
						                 <span id="reshardSlot${processMap.key}">${processMap.value.reshardSlot}</span>&nbsp;&nbsp;Finish/<span id="totalSlot${processMap.key}">${processMap.value.totalSlot}</span>&nbsp;&nbsp;Total
						             </label>
						           </div>
						       </div>
                            </td>
                            <td>
                            	<c:choose>
                            		<c:when test="${processMap.value.type == 0}">上线节点</c:when>
                            		<c:when test="${processMap.value.type == 1}">下线节点</c:when>
                            	</c:choose>
                            </td>
                            <td id="statusDesc${processMap.key}">
                            	<c:choose>
                            		<c:when test="${processMap.value.status == 0}">运行中</c:when>
                            		<c:when test="${processMap.value.status == 1}">完成</c:when>
                            		<c:when test="${processMap.value.status == 2}">出错</c:when>
                            	</c:choose>
                            </td>
                            <td>
                            	<fmt:formatDate value="${processMap.value.beginTime}" type="time" timeStyle="full" pattern="yyyy-MM-dd HH:mm:ss"/>
                            </td>
                            <td>
                            	<fmt:formatDate value="${processMap.value.endTime}" type="time" timeStyle="full" pattern="yyyy-MM-dd HH:mm:ss"/>
                            </td>
                            
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>
<script type="text/javascript">
	$(function(){
		function show(){
		   var url = "/manage/app/showReshardProcess.json";
		   $.get(url, function(data) {
  				var dataArr = eval("(" + data + ")");
  				var length = dataArr.length;

  				for (var i = 0; i < length; i++) {
  					var data = dataArr[i];
  					var appId = data.appId;
  					var reshardSlot = data.reshardSlot;
  					var totalSlot = data.totalSlot;
  					var status = data.status;
  					var statusDesc = "";
  					if (status == 0) {
  						statusDesc = "运行中";
  			        } else if (status == 1) {
  			        	statusDesc = "完成";
  			        } else {
  			        	statusDesc = "出错";
  			        }
  					document.getElementById("reshardSlot" + appId).innerHTML = reshardSlot;
  					document.getElementById("totalSlot" + appId).innerHTML = totalSlot;
  					document.getElementById("reshardSlotProgress" + appId).style.width = (reshardSlot * 100 / totalSlot ) + "%";
  					document.getElementById("statusDesc" + appId).innerHTML = statusDesc;
  				}
  			});
		}
		setInterval(show,2000);// 注意函数名没有引号和括弧！
		// 使用setInterval("show()",3000);会报“缺少对象”
	});
</script>
