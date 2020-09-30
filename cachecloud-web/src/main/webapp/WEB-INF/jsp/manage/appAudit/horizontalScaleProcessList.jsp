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
                        <td>id</td>
                        <td>slot迁移进度</td>
                        <td>目标源实例</td>
                        <td>开始结束slot</td>
                        <td>正在迁移的slot</td>
                        <td>状态</td>
                        <td>开始时间</td>
                        <td>结束时间</td>
                        <td>操作</td>
                    </tr>
                    </thead>
                    <tbody>
	                    <c:forEach var="instanceReshardProcess" items="${instanceReshardProcessList}">
	                        <tr>
	                            <td>${instanceReshardProcess.id}</td>
	                            <td>
		                          <div class="progress margin-custom-bottom0">
							      	  <div id="reshardSlotProgress${instanceReshardProcess.id}" class="progress-bar progress-bar-success"
							           role="progressbar" aria-valuenow="${instanceReshardProcess.finishSlotNum}" aria-valuemax="${instanceReshardProcess.totalSlot}"
							           aria-valuemin="0" style="width: ${instanceReshardProcess.finishSlotNum / instanceReshardProcess.totalSlot}">
							             <label style="color: #000000">
							                 <span id="finishSlotNum${instanceReshardProcess.id}">${instanceReshardProcess.finishSlotNum}</span>&nbsp;&nbsp;Finish/<span id="totalSlot${instanceReshardProcess.id}">${instanceReshardProcess.totalSlot}</span>&nbsp;&nbsp;Total
							             </label>
							           </div>
							       </div>
	                            </td>
	                            <td id="sourceTargetInstance${instanceReshardProcess.id}">
	                                ${instanceReshardProcess.sourceInstanceId}(${instanceInfoMap[instanceReshardProcess.sourceInstanceId].ip}:${instanceInfoMap[instanceReshardProcess.sourceInstanceId].port})
	                            		-->
	                            		${instanceReshardProcess.targetInstanceId}(${instanceInfoMap[instanceReshardProcess.targetInstanceId].ip}:${instanceInfoMap[instanceReshardProcess.targetInstanceId].port})
	                            </td>
	                            <td id="startEndSlot${instanceReshardProcess.id}">
	                            		${instanceReshardProcess.startSlot} 
	                            		-->
	                            		${instanceReshardProcess.endSlot} 
	                            </td>
	                            <td id="migratingSlot${instanceReshardProcess.id}">
	                            		${instanceReshardProcess.migratingSlot} 
	                            </td>
	                            <td id="statusDesc${instanceReshardProcess.id}">
		                            	<c:choose>
		                            		<c:when test="${instanceReshardProcess.status == 0}">运行中</c:when>
		                            		<c:when test="${instanceReshardProcess.status == 1}">完成</c:when>
		                            		<c:when test="${instanceReshardProcess.status == 2}">出错</c:when>
		                            	</c:choose>
	                            </td>
	                            <td>
	                            		<fmt:formatDate value="${instanceReshardProcess.startTime}" type="time" timeStyle="full" pattern="yyyy-MM-dd HH:mm:ss"/>
	                            </td>
	                            <td id="endTime${instanceReshardProcess.id}">
	                            		<c:if test="${instanceReshardProcess.status == 1}">
	                            			<fmt:formatDate value="${instanceReshardProcess.endTime}" type="time" timeStyle="full" pattern="yyyy-MM-dd HH:mm:ss"/>
	                            		</c:if>
	                            </td>
	                            <td>
	                            	   <c:if test="${instanceReshardProcess.status == 2}">
		                            	   <button id="retryBtn${instanceReshardProcess.id}"  type="button" class="btn btn-small" onclick="retryHorizontalScale('${instanceReshardProcess.id}')">
	                                        	重试
	                                    </button>
	                            	   </c:if>
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
		   var auditId = document.getElementById("appAuditId").value
		   var url = "/manage/app/showReshardProcess.json?auditId=" + auditId;
		   $.get(url, function(data) {
  				var dataArr = eval("(" + data + ")");
  				var length = dataArr.length;

  				for (var i = 0; i < length; i++) {
  					var data = dataArr[i];
  					var id = data.id;
  					var appId = data.appId;
  					var finishSlotNum = data.finishSlotNum;
  					var totalSlot = data.totalSlot;
  					var status = data.status;
  					var statusDesc = data.statusDesc;
  					var migratingSlot = data.migratingSlot;
  					var endTimeFormat = data.endTimeFormat;
  					document.getElementById("finishSlotNum" + id).innerHTML = finishSlotNum;
  					document.getElementById("totalSlot" + id).innerHTML = totalSlot;
  					document.getElementById("reshardSlotProgress" + id).style.width = (finishSlotNum * 100 / totalSlot ) + "%";
  					document.getElementById("statusDesc" + id).innerHTML = statusDesc;
  					document.getElementById("migratingSlot" + id).innerHTML = migratingSlot;
  					//如果完成显示结束时间
  					if (status == 1) {
  	  					document.getElementById("endTime" + id).innerHTML = endTimeFormat;
  					}
  					//非出错不显示
  					if (status != 2) {
  	  					var retryBtn = document.getElementById("retryBtn" + id);
  	  					if (retryBtn != null) {
  	  						retryBtn.style.display = "none";
  	  					}
  					}
  				}
  			});
		}
		setInterval(show,2000);// 注意函数名没有引号和括弧！
		// 使用setInterval("show()",3000);会报“缺少对象”
	});
</script>
