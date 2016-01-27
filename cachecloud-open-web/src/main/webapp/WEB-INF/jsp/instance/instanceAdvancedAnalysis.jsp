<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<script>
    var instance_advancedAnalysis_c_cu = {
    <c:forEach items="${appCommandStats}" var="appCommand" varStatus="status">${appCommand.commandName}:
    '/admin/instance/getCommandStatsV2.json?instanceId=${instanceInfo.id}&commandName=${appCommand.commandName}&startDate=${startDate}&endDate=${endDate}'<c:if test="${!status.last}">,
    </c:if></c:forEach>
    }
    var instance_advancedAnalysis_c_o = {
    <c:forEach items="${appCommandStats}" var="appCommand" varStatus="status">${appCommand.commandName}:
    {
        plotOptions: {
            area:{
                marker: {
                    enabled: false,
                    symbol: 'circle',
                    radius: 0,
                    states:{
                        hover: {
                            enabled: true
                        }
                    }
                }
            }
        }
//    ,
//        scrollbar: {
//            enabled: true/*,
//             barBackgroundColor: 'gray',
//             barBorderRadius: 7,
//             barBorderWidth: 0,
//             buttonBackgroundColor: 'gray',
//             buttonBorderWidth: 0,
//             buttonArrowColor: 'yellow',
//             buttonBorderRadius: 7,
//             rifleColor: 'yellow',
//             trackBackgroundColor: 'white',
//             trackBorderWidth: 1,
//             trackBorderColor: 'silver',
//             trackBorderRadius: 7
//             */
//        }
    }
    <c:if test="${!status.last}">,</c:if></c:forEach>
    }
</script>
<div class="container">
    <br/>
    <div class="row">
        <div style="float:right">
            <form method="get" action="/admin/instance/index.do" id="ec" name="ec">
                <label style="font-weight:bold;text-align:left;">
                    开始日期:&nbsp;&nbsp;
                </label>
                <input type="text" size="21" name="startDate" id="startDate" value="${startDate}"
                       onFocus="WdatePicker({startDate:'%y%M%d',dateFmt:'yyyyMMdd',alwaysUseStartDate:true})"/>
                <label style="font-weight:bold;text-align:left;">
                    结束日期:
                </label>
                <input type="text" size="20" name="endDate" id="endDate" value="${endDate}"
                       onFocus="WdatePicker({startDate:'%y%M%d',dateFmt:'yyyyMMdd',alwaysUseStartDate:true})"/>
                <input type="hidden" name="instanceId" value="${instanceInfo.id}">
                <input type="hidden" name="tabTag" value="instance_advancedAnalysis">
                <label>&nbsp;<input type="submit" class="btn-4" value="查询"/></label>
            </form>
        </div>
    </div>

    <div class="row">
        <div class="col-md-12">
            <div class="page-header">
                <h4>TOP的命令执行趋势比较</h4>
            </div>
            <c:forEach items="${appCommandStats}" var="appCommand">
                <div id="${appCommand.commandName}" class="page-body">
                </div>
                <br/>
            </c:forEach>
        </div>
    </div>
</div>
