<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp"%>

<div class="container">

	<br/>
    <form method="get" action="/admin/app/index.do">
		<div class="row">
			<div style="float:right">
				<label style="font-weight:bold;text-align:left;">
				 	&nbsp;日期:&nbsp;&nbsp;
				</label>
				<input type="text" size="21" name="dailyDate" id="dailyDate" value="${dailyDate}" onFocus="WdatePicker({startDate:'%y-%M-01',dateFmt:'yyyy-MM-dd',alwaysUseStartDate:true})"/>
				
				<input type="hidden" name="appId" value="${appDesc.appId}">
				<input type="hidden" name="tabTag" value="app_daily">
				<label>&nbsp;<input type="submit" class="btn-4" value="查询"/></label>
			</div>
		</div>
	</form>


    <div class="row">
        <div class="page-header">
            <h4>客户端相关</h4> 
        </div>
        <table class="table table-striped table-hover">
            <tbody>
             <tr>
                 <td>客户端值分布(全天)</td>
                 <td>${appDailyData.valueSizeDistributeCountDescHtml}</td>
                 <td>客户端异常个数(全天)</td>
                 <td>${appDailyData.clientExceptionCount}</td>
                 <td>客户端连接数(每分钟)</td>
                 <td>
                  最大值:${appDailyData.maxMinuteClientCount} <br/>
                     平均值:${appDailyData.avgMinuteClientCount}
                 </td>
             </tr>
            </tbody>
        </table>
    </div>
    
    <div class="row">
        <div class="page-header">
            <h4>服务端相关</h4> 
        </div>
        <table class="table table-striped table-hover">
            <tbody>
	             <tr>
	                 <td>慢查询个数(全天)</td>
	                 <td>${appDailyData.slowLogCount}</td>
	                 <td>命令次数(每分钟)</td>
	                 <td>
	                 	最大值:${appDailyData.maxMinuteCommandCount} <br/>
	                    平均值:${appDailyData.avgMinuteCommandCount}
	                 </td>
	                 <td>命中率(每分钟)</td>
	                 <td>
	                  	最大值:${appDailyData.maxMinuteHitRatio}% <br/>
	                    最小值:${appDailyData.minMinuteHitRatio}% <br/>
	                    平均值:${appDailyData.avgHitRatio}%
	                 </td>
	             </tr>
	             
	             <tr>
	                 <td>内存使用量(全天)</td>
	                 <td>
	                 	平均使用量:${appDailyData.avgUsedMemory} M<br/>
                        最大使用量:${appDailyData.maxUsedMemory} M
	                 </td>
	                 <td>过期键数(全天)</td>
	                 <td>
	                 	${appDailyData.expiredKeysCount}
	                 </td>
	                 <td>剔除键数(全天)</td>
	                 <td>
	                  	${appDailyData.evictedKeysCount}
	                 </td>
	             </tr>
	             
	             
	             <tr>
	                 <td>键个数(全天)</td>
	                 <td>
	                 	平均值:${appDailyData.avgObjectSize}<br/>
                        最大值:${appDailyData.maxObjectSize}
	                 </td>
	                 <td>input流量(每分钟)</td>
	                 <td>
	                 	平均值:${appDailyData.avgMinuteNetInputByte} M<br/>
                       	 最大值:${appDailyData.maxMinuteNetInputByte} M
	                 </td>
	                 <td>output流量(每分钟)</td>
	                 <td>
	                  	 平均:${appDailyData.avgMinuteNetOutputByte} M<br/>
                       	 最大:${appDailyData.maxMinuteNetOutputByte} M<br/>
	                 </td>
	             </tr>
	             
            </tbody>
        </table>
    </div>
    
    
    <br/><br/><br/>
</div>



