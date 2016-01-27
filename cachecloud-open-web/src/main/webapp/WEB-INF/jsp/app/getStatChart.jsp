<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>

<script type="text/javascript">
    var title = "<b>get命令</b>";
    var chartType = 'line';
    var appId = '${appId}';
    var value = "${requestScope.container}";
    $(document).ready(function() {
        Highcharts.setOptions({
            global: {
                useUTC: false
            }
        });
    Highcharts.setOptions({ colors: ['#2f7ed8', '#E3170D', '#0d233a', '#8bbc21', '#1aadce', '#492970','#804000','#f28f43', '#77a1e5', '#c42525', '#a6c96a']});
   	var options = {
			chart: {
				renderTo: 'container3',
				animation: Highcharts.svg,
				backgroundColor: '#F1F0FE',
				plotBackgroundColor:'#FFFFFF',
				zoomType:'x',
				type: chartType,
				marginRight: 10
			},
            title: {
            	useHTML:true,
                text: title
            },
            xAxis: {
                type: 'datetime',
            },
            yAxis: {
            	title: {
                  		text: '次数'
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
                spline: {
                    dataLabels: {
                        enabled: true
                    }
                },
                column: {
                    dataLabels: {
                        enabled: true
                    }
                },
                area: {
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
       	var tvUrl = "/admin/app/testChartJson.do?appId="+appId+"&commandName=get";
    	$.ajax({  
          type : "get",  
          url : tvUrl,  
          async : false,  
          success : function(data){
          	var dataArr = eval("("+data+")");
   			var length = dataArr.length;
            var time = (new Date()).getTime();
            var nameSynStatus = "趋势图";
   			var dataSynStatus = [];
               
               for (var i = 0; i <length - 1; i++) {
                   var data = dataArr[i];
                   var marker = new Object();
		        marker.fillColor = '#FF0000';
		        marker.lineWidth = 2;
                		marker.radius = 8;
                		
                		var pointName = data.date + "<br/>" + "次数:" + data.y;
                   
                   var synStatusPoint = {
                   	name:pointName,
                       x: data.x,
                       y: data.y,
                   };
                   dataSynStatus.push(synStatusPoint);
               }
               
           	    var seriesSynStatus={
   				name:nameSynStatus,
   				data:dataSynStatus
   			}
   			options.series.push(seriesSynStatus);
          }
    	});

       var chart = new Highcharts.Chart(options); 
    });
</script>
<div id="container3" style="min-width: 310px; height: 350px; margin: 0 auto">
</div>
