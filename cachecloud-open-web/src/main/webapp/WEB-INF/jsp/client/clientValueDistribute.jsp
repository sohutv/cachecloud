<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp"%>

<script type="text/javascript">
function search() {
    var valueDistriStartDate = document.getElementById("valueDistriStartDate").value;
    var valueDistriEndDate = document.getElementById("valueDistriEndDate").value;
	var difTime = calDateWidgetDifMs(valueDistriStartDate, valueDistriEndDate);
	var oneDayTime = 86400000;
	if (difTime > oneDayTime) {
		alert("由于数据量较大,值区间查询暂不支持跨天查询!");
	} else {
		document.getElementById("clientValueForm").submit();
	}
}
</script>

<div class="container">
	<div class="row">
		<div class="col-md-12"></div>
	</div>
    <div class="row">
        <div class="col-md-12">
        	<form method="get" action="/client/show/index.do" id="clientValueForm">
				<div style="float:right">
						<label style="font-weight:bold;text-align:left;">
						 	&nbsp;开始日期:&nbsp;&nbsp;
						</label>
						<input type="text" size="21" name="valueDistriStartDate" id="valueDistriStartDate" value="${valueDistriStartDate}" onFocus="WdatePicker({startDate:'%y-%M-01',dateFmt:'yyyy-MM-dd',alwaysUseStartDate:true})"/>
						
						<label style="font-weight:bold;text-align:left;">
						 	结束日期:
						</label>
						<input type="text" size="20" name="valueDistriEndDate" id="valueDistriEndDate" value="${valueDistriEndDate}" onFocus="WdatePicker({startDate:'%y-%M-01',dateFmt:'yyyy-MM-dd',alwaysUseStartDate:true})"/>
						
						<input type="hidden" name="appId" value="${appDesc.appId}">
						<input type="hidden" name="tabTag" value="app_client_value_distribute">
						<label>&nbsp;<input type="button" class="btn-4" value="查询" onclick="search()"/></label>
				</div>
			</form>
        </div>
    </div>
    
    <div class="row">
        <div class="col-md-12">
        	<div class="page-header">
				<h4>
					redis值分布统计图表
				</h4>
			</div>
			<script type="text/javascript">
				$(document).ready(
					function() {
					    var title =  "<b>值分布</b>";
					    var chartType = "pie";
					   	var options = {
							chart: {
								renderTo:'valueDistriContainer',
								animation: Highcharts.svg,
								backgroundColor: '#E6F1F5',
								plotBackgroundColor:'#FFFFFF',
								type: chartType,
								marginRight: 10
							},
				            title: {
				            	useHTML:true,
				                text: title
				            },
				            xAxis: {
				                type: 'category'
				            },
				            yAxis: {
				            	title: {
		                    		text: ''
		                		},
				                plotLines: [{
				                    value: 0,
				                    width: 1,
				                    color: '#808080'
				                }]
				            },
				            plotOptions: {
				                line: {
				                    dataLabels: {
				                        enabled: true
				                    }
				                },
				                series: {
					                cursor: 'pointer'
					            }
				            },
				            tooltip: {
				                formatter: function() {
				                        return '<b>'+ this.point.name +'</b><br/>'
				                }
				            },
				            legend: {
				                enabled: true
				            },
				            credits:{
				            	enabled: false
				            },
				            exporting: {
				                enabled: true
				            },
				            series: []
				        };
					   	var data = '${appClientValueDistriSimpleListJson}';
			          	var dataArr = eval("("+data+")");
		    			var length = dataArr.length;
		    			var legendName = "值分布统计";
		    			var arr = [];
		                
		                for (var i = 0; i <length; i++) {
		                    var data = dataArr[i];
	                  		var pointName = data.distributeDesc + ":" + data.count + "次";
		                    var point = {
		                    	name:pointName,
		                        y: data.count
		                    };
		                    arr.push(point);
		                }
	             	    var series={
		    				name:legendName,
		    				data:arr
		    			};
		    			options.series.push(series);
	     		        new Highcharts.Chart(options); 
				    });
			</script>
			
			<div id="valueDistriContainer" style="min-width: 310px; height: 350px; margin: 0 auto"></div>
        </div>
    </div>
    
    <br/>
    
    <div class="row">
        <div class="col-md-12">
        	<div class="page-header">
				<h4>
					redis值分布统计列表
				</h4>
			</div>
        	<table class="table table-striped table-hover table-bordered" style="margin-top: 0px">
	   			<thead>
					<tr>
						<td style="text-align:center">值区间</td>
						<td style="text-align:center">总次数</td>
				</thead>
				<tbody>
					<c:forEach items="${appClientValueDistriSimpleList}" var="appClientValueDistriSimple">
						<tr>
							<td style="text-align:center">${appClientValueDistriSimple.distributeDesc}</td>
							<td style="text-align:center">${appClientValueDistriSimple.count}</td>
						</tr>
					</c:forEach>
				</tbody>
			</table>
        </div>
    </div>
    
    <br/><br/><br/><br/>
     
</div>
