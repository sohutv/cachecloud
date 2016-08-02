function getClientCostSeriesPoints(dataType, dataArr, tags, unit){
	var length = dataArr.length;
	var finalPoints = [];
	for (var k = 0; k < tags.length; k+=2) {
		var tag = tags[k];
		var tagName = tags[k+1];
		var dataSeries = [];
		for (var i = 0; i < length; i++) {
			var data = dataArr[i];
			var count = data[tag];
			var pointName =  tagName + ":"  + count + unit;
			pointName = pointName + "<br/>调用量: " + data.count;
			if (dataType == 1 && tag == "max100") {
				pointName = pointName 
				            + "<br/>实例: " + data.maxInst
				            + "<br/>客户端: " + data.maxClient;
			}
			var dataPoint = {
				name : pointName,
				x : data.timeStamp,
				y : count,
			};
			dataSeries.push(dataPoint);
		}
		var seriesPoints = {
			name : tagName,
			data : dataSeries,
			marker : {
				radius : 1, // 曲线点半径，默认是4
			}
		};
		finalPoints.push(seriesPoints);
	}
	return finalPoints;
}

function pushOptionSeries(options, data, dates, nameLegendPrefix, unit, defaultCount) {
	if (typeof(unit) == "undefined") { 
		unit = "次";
	}  
	var dataObject = eval("(" + data.data + ")");
	for(var t=0;t<dates.length;t++){
		date = dates[t];
		var dataArr = dataObject[date];
		var length = dataArr.length;
		var dataSeries = [];
		var count;
		for (var i = 0; i < length - 1; i++) {
			var data = dataArr[i];
			count = data.y;
			if (defaultCount > 0) { 
				count = defaultCount;
			}
	  		var pointName = data.date + ":  " + count + unit;
			var dataPoint = {
	            name : pointName,
				x : data.x,
				y : count,
			};
			dataSeries.push(dataPoint);
		}

		var seriesPoints = {
			name : nameLegendPrefix + "(" + date + ")",
			data : dataSeries,
			marker: {
	            radius: 1,  //曲线点半径，默认是4
	        }
		};
		options.series.push(seriesPoints);
	}
	
}

function getSeriesPoints(data,nameLegend, unit, defaultCount){
	if (typeof(unit) == "undefined") { 
		unit = "次";
	}  
	var dataArr = eval("(" + data + ")");
	var length = dataArr.length;
	var dataSeries = [];

	var count;
	for (var i = 0; i < length - 1; i++) {
		var data = dataArr[i];
		count = data.y;
		if (defaultCount > 0) { 
			count = defaultCount;
		} 
  		var pointName = data.date + ":  " + count + unit;
		var dataPoint = {
            name : pointName,
			x : data.x,
			y : count,
		};
		dataSeries.push(dataPoint);
	}

	var seriesPoints = {
		name : nameLegend,
		data : dataSeries,
		marker: {
            radius: 1,  //曲线点半径，默认是4
        }
	};
	return seriesPoints;
}

function getInstanceNetPoints(instanceNetData,nameLegend, command, unit){
	var dataArr = instanceNetData.instanceNetStatMapList;
	var length = dataArr.length;
	//i,o,t
	var dataSeries = [];

	var unitTxt = "";
	if (typeof(unit) == "undefined") { 
		var byteCounter = 0;
		var kbCounter = 0;
		var mbCounter = 0;
		for (var i = 0; i < length - 1; i++) {
			var data = dataArr[i];
			var count = data[command];
			if (count < 1024){
				byteCounter = byteCounter + 1;
			}else if (count >= 1024 && count < 1024 * 1024) {
				kbCounter = kbCounter + 1;
			}else {
				mbCounter = mbCounter + 1;
			}
		}
		if (byteCounter > kbCounter) {
			if (byteCounter > mbCounter) {
				unit = 1;
				unitTxt = "byte";
			} else {
				unit = 1024 * 1024;
				unitTxt = "Mb";
			}
		}else {
			if (kbCounter > mbCounter){
				unit = 1024;
				unitTxt = "Kb";
			} else {
				unit = 1024 * 1024;
				unitTxt = "Mb";
			}
		}
	} else {
		if (unit == 1) {
			unitTxt = "byte";
		}else if (unit == 1024) {
			unitTxt = "Kb";
		}else if (unit == 1024 * 1024){
			unitTxt = "Mb";
		}
	}
	for (var i = 0; i < length - 1; i++) {
		var data = dataArr[i];
		var count = Math.round(data[command] / unit);
  		var pointName = count + unitTxt;
		var dataPoint = {
            name : pointName,
			x : data.t,
			y : count,
		};
		dataSeries.push(dataPoint);
	}

	var seriesPoints = {
		name : nameLegend,
		data : dataSeries,
		unit: unit,
		unitTxt: unitTxt
	};
	return seriesPoints;
}

function getNetPoints(dataArr,nameLegend, unit){
	var length = dataArr.length;
	var dataSeries = [];

	var unitTxt = "";
	if (typeof(unit) == "undefined") { 
		var byteCounter = 0;
		var kbCounter = 0;
		var mbCounter = 0;
		for (var i = 0; i < length - 1; i++) {
			var data = dataArr[i];
			var count = data.y;
			if (count < 1024){
				byteCounter = byteCounter + 1;
			}else if (count >= 1024 && count < 1024 * 1024) {
				kbCounter = kbCounter + 1;
			}else {
				mbCounter = mbCounter + 1;
			}
		}
		if (byteCounter > kbCounter) {
			if (byteCounter > mbCounter) {
				unit = 1;
				unitTxt = "byte";
			} else {
				unit = 1024 * 1024;
				unitTxt = "Mb";
			}
		}else {
			if (kbCounter > mbCounter){
				unit = 1024;
				unitTxt = "Kb";
			} else {
				unit = 1024 * 1024;
				unitTxt = "Mb";
			}
		}
	} else {
		if (unit == 1) {
			unitTxt = "byte";
		}else if (unit == 1024) {
			unitTxt = "Kb";
		}else if (unit == 1024 * 1024){
			unitTxt = "Mb";
		}
	}
	for (var i = 0; i < length - 1; i++) {
		var data = dataArr[i];
		var count = Math.round(data.y / unit);
  		var pointName = data.date + ":  " + count + unitTxt;
		var dataPoint = {
            name : pointName,
			x : data.x,
			y : count,
		};
		dataSeries.push(dataPoint);
	}

	var seriesPoints = {
		name : nameLegend,
		data : dataSeries,
		unit: unit,
		unitTxt: unitTxt
	};
	return seriesPoints;
}


function getOption(container, title, titleText){
	var chartOption = {
			chart : {
				renderTo : container,
				animation : Highcharts.svg,
				backgroundColor : '#E6F1F5',
				plotBackgroundColor : '#FFFFFF',
				zoomType : 'x',
				type : 'line',
				marginRight : 10
			},
			title : {
				useHTML : true,
				text : title
			},
			xAxis : {
				type : 'datetime',
			},
			yAxis : {
				title : {
					text : titleText
				},
				min:0
			},
			plotOptions: {
	            line: {
	                dataLabels: {
	                    enabled: false
	                },
	                enableMouseTracking: true
	            },
				series: {
					//默认只能显示1000个点,如果为0就没有这个限制
					turboThreshold: 0,
	                marker: {
	                    enabled: false
	                }				
	            }
	        },
			tooltip : {
				formatter: function() {
                    return '<b>'+ this.point.name +'</b><br/>';
				}
			},
			legend : {
				enabled : true
			},
			credits:{
            	enabled: false
            },
			exporting : {
				enabled : true
			},
			series : []
		};
	return chartOption;
}