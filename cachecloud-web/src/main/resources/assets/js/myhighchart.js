$(function(){
    Highcharts.setOptions({
        time: {
            useUTC: false
        }
    });
});

function generateDataPieChart(container, title, dataArr, legendName) {
    var chartType = "pie";
    var options = {
        chart: {
            renderTo: container,
            animation: Highcharts.svg,
            borderColor: '#E6E6E6',
            borderWidth: 2,
            // backgroundColor: '#E6F1F5',
            plotBackgroundColor: '#FFFFFF',
            type: chartType,
            marginRight: 10
        },
        title: {
            useHTML: true,
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
            formatter: function () {
                return '<b>' + this.point.name + '</b><br/>'
            }
        },
        legend: {
            enabled: true
        },
        credits: {
            enabled: false
        },
        exporting: {
            enabled: true
        },
        series: []
    };
    var length = dataArr.length;
    var arr = [];
    for (var i = 0; i < length; i++) {
        var data = dataArr[i];
        var point = {
            name: data.param + ": " + data.count,
            y: data.count
        };
        arr.push(point);
    }
    var series = {
        name: legendName,
        data: arr
    };
    options.series.push(series);
    new Highcharts.Chart(options);
}


function getClientCostSeriesPoints(dataType, dataArr, tags, unit) {
    var length = dataArr.length;
    var finalPoints = [];
    for (var k = 0; k < tags.length; k += 2) {
        var tag = tags[k];
        var tagName = tags[k + 1];
        var dataSeries = [];
        for (var i = 0; i < length; i++) {
            var data = dataArr[i];
            var count = data[tag];
            var pointName = tagName + ":" + count + unit;
            pointName = pointName + "<br/>调用量: " + data.count;
            if (dataType == 1 && tag == "max100") {
                pointName = pointName
                    + "<br/>实例: " + data.maxInst
                    + "<br/>客户端: " + data.maxClient;
            }
            var dataPoint = {
                name: pointName,
                x: data.timeStamp,
                y: count,
            };
            dataSeries.push(dataPoint);
        }
        var seriesPoints = {
            name: tagName,
            data: dataSeries,
            marker: {
                radius: 1, // 曲线点半径，默认是4
            }
        };
        finalPoints.push(seriesPoints);
    }
    return finalPoints;
}

function pushOptionSeries(options, data, dates, nameLegendPrefix, unit, defaultCount) {
    if (typeof (unit) == "undefined") {
        unit = "次";
    }
    var dataObject = eval("(" + data.data + ")");
    for (var t = 0; t < dates.length; t++) {
        date = dates[t];
        var dataArr = dataObject[date];
        var length = 0;
        if(dataArr == undefined){
        }else{
            length = dataArr.length;
        }
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
                name: pointName,
                x: data.x,
                y: count,
            };
            dataSeries.push(dataPoint);
        }

        var seriesPoints = {
            name: nameLegendPrefix + "(" + date + ")",
            data: dataSeries,
            marker: {
                radius: 1,  //曲线点半径，默认是4
            }
        };
        options.series.push(seriesPoints);
    }

}

function pushMemFragRatioOptionSeries(options, data, dates, nameLegendPrefix, unit, defaultCount) {
    if (typeof (unit) == "undefined") {
        unit = "";
    }
    var dataObject = eval("(" + data.data + ")");
    for (var t = 0; t < dates.length; t++) {
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
                name: pointName,
                x: data.x,
                y: count,
            };
            dataSeries.push(dataPoint);
        }

        var seriesPoints = {
            name: nameLegendPrefix + "(" + date + ")",
            data: dataSeries,
            marker: {
                radius: 1,  //曲线点半径，默认是4
            }
        };
        options.series.push(seriesPoints);
    }

}

function getSeriesPoints(data, nameLegend, unit, defaultCount) {
    if (typeof (unit) == "undefined") {
        unit = "次";
    }
    var dataArr = eval("(" + data + ")");
    var length = 0;
    if(dataArr != undefined && dataArr != null){
        length = dataArr.length;
    }
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
            name: pointName,
            x: data.x,
            y: count,
        };
        dataSeries.push(dataPoint);
    }

    var seriesPoints = {
        name: nameLegend,
        data: dataSeries,
        marker: {
            radius: 1,  //曲线点半径，默认是4
        }
    };
    return seriesPoints;
}

function getMemFragRatioSeriesPoints(data, nameLegend, unit, defaultCount) {
    if (typeof (unit) == "undefined") {
        unit = "";
    }
    var dataArr = eval("(" + data + ")");
    var length = 0;
    if(dataArr != undefined && dataArr != null){
        length = dataArr.length;
    }
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
            name: pointName,
            x: data.x,
            y: count,
        };
        dataSeries.push(dataPoint);
    }

    var seriesPoints = {
        name: nameLegend,
        data: dataSeries,
        marker: {
            radius: 1,  //曲线点半径，默认是4
        }
    };
    return seriesPoints;
}

function getInstanceNetPoints(instanceNetData, nameLegend, command, unit) {
    var dataArr = instanceNetData.instanceNetStatMapList;
    var length = 0;
    if(dataArr != undefined && dataArr != null){
        length = dataArr.length;
    }
    //i,o,t
    var dataSeries = [];

    var unitTxt = "";
    if (typeof (unit) == "undefined") {
        var byteCounter = 0;
        var kbCounter = 0;
        var mbCounter = 0;
        for (var i = 0; i < length - 1; i++) {
            var data = dataArr[i];
            var count = data[command];
            if (count < 1024) {
                byteCounter = byteCounter + 1;
            } else if (count >= 1024 && count < 1024 * 1024) {
                kbCounter = kbCounter + 1;
            } else {
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
        } else {
            if (kbCounter > mbCounter) {
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
        } else if (unit == 1024) {
            unitTxt = "Kb";
        } else if (unit == 1024 * 1024) {
            unitTxt = "Mb";
        }
    }
    for (var i = 0; i < length - 1; i++) {
        var data = dataArr[i];
        var count = Math.round(data[command] / unit);
        var pointName = count + unitTxt;
        var dataPoint = {
            name: pointName,
            x: data.t,
            y: count,
        };
        dataSeries.push(dataPoint);
    }

    var seriesPoints = {
        name: nameLegend,
        data: dataSeries,
        unit: unit,
        unitTxt: unitTxt
    };
    return seriesPoints;
}

function getInstanceCpuPoints(instance, nameLegend, command) {
    var dataArr = instance.instanceCpuStatMapList;
    var length = 0;
    if(dataArr != undefined && dataArr != null){
        length = dataArr.length;
    }
    //i,o,t
    var dataSeries = [];
    var unit = 1;
    var unitTxt = "秒"

    for (var i = 0; i < length - 1; i++) {
        var data = dataArr[i];
        var count = Math.round(data[command] / unit);
        var pointName = formatDate(data.t) + ":  " + count + unitTxt;
        var dataPoint = {
            name: pointName,
            x: data.t,
            y: count,
        };
        dataSeries.push(dataPoint);
    }

    var seriesPoints = {
        name: nameLegend,
        data: dataSeries,
        unit: unit,
        unitTxt: unitTxt
    };
    return seriesPoints;
}

function getInstanceKeyPoints(instance, nameLegend, command) {
    var dataArr = instance.instanceExpiredEvictedKeysStatMapList;
    var length = 0;
    if(dataArr != undefined && dataArr != null){
        length = dataArr.length;
    }
    //i,o,t
    var dataSeries = [];
    var unit = 1;
    var unitTxt = "个"

    for (var i = 0; i < length - 1; i++) {
        var data = dataArr[i];
        var count = Math.round(data[command] / unit);
        var pointName = formatDate(data.t) + ":  " + count + unitTxt;
        var dataPoint = {
            name: pointName,
            x: data.t,
            y: count,
        };
        dataSeries.push(dataPoint);
    }

    var seriesPoints = {
        name: nameLegend,
        data: dataSeries,
        unit: unit,
        unitTxt: unitTxt
    };
    return seriesPoints;
}

function getInstanceBaseCpuPoints(instance, nameLegend, ratio) {
    var dataArr = instance.instanceCpuStatMapList;
    var length = 0;
    if(dataArr != undefined && dataArr != null){
        length = dataArr.length;
    }
    //i,o,t
    var dataSeries = [];
    var unit = 1;
    var unitTxt = "秒"

    for (var i = 0; i < length - 1; i++) {
        var data = dataArr[i];
        var count = 60 * ratio;
        var pointName = formatDate(data.t) + ":  " + count + unitTxt;
        var dataPoint = {
            name: pointName,
            x: data.t,
            y: count,
        };
        dataSeries.push(dataPoint);
    }

    var seriesPoints = {
        name: nameLegend,
        data: dataSeries,
        unit: unit,
        unitTxt: unitTxt
    };
    return seriesPoints;
}

function getInstanceMemFragRatioPoints(instance, nameLegend, command) {
    var dataArr = instance.instanceMemFragRatioStatMapList;
    var length = 0;
    if(dataArr != undefined && dataArr != null){
        length = dataArr.length;
    }
    //i,o,t
    var dataSeries = [];
    var unit = 1;
    var unitTxt = ""

    for (var i = 0; i < length - 1; i++) {
        var data = dataArr[i];
        var count = data[command] / unit;
        var pointName = count + unitTxt;
        var dataPoint = {
            name: pointName,
            x: data.t,
            y: count,
        };
        dataSeries.push(dataPoint);
    }

    var seriesPoints = {
        name: nameLegend,
        data: dataSeries,
        unit: unit,
        unitTxt: unitTxt
    };
    return seriesPoints;
}

function getCpuPoints(dataArr, nameLegend, unit) {
    var length = 0;
    if(dataArr != undefined && dataArr != null){
        length = dataArr.length;
    }
    var dataSeries = [];

    var unitTxt = "";

    if (unit == 1) {
        unit = 1;
        unitTxt = "秒"
    }

    for (var i = 0; i < length - 1; i++) {
        var data = dataArr[i];
        var count = Math.round(data.y / unit);
        var pointName = data.date + ":  " + count + unitTxt;
        var dataPoint = {
            name: pointName,
            x: data.x,
            y: count,
        };
        dataSeries.push(dataPoint);
    }

    var seriesPoints = {
        name: nameLegend,
        data: dataSeries,
        unit: unit,
        unitTxt: unitTxt
    };
    return seriesPoints;
}

function getBaseCpuPoints(dataArr, nameLegend, unit) {
    var length = 0;
    if(dataArr != undefined && dataArr != null){
        length = dataArr.length;
    }
    var dataSeries = [];

    var unitTxt = "";

    if (unit == 1) {
        unit = 1;
        unitTxt = "秒"
    }

    for (var i = 0; i < length - 1; i++) {
        var data = dataArr[i];
        var count = masterNum * 1;
        var pointName = data.date + ":  " + count + unitTxt;
        var dataPoint = {
            name: pointName,
            x: data.x,
            y: count,
        };
        dataSeries.push(dataPoint);
    }

    var seriesPoints = {
        name: nameLegend,
        data: dataSeries,
        unit: unit,
        unitTxt: unitTxt
    };
    return seriesPoints;
}

function getMemoryPoints(dataArr, nameLegend, unit, defaultValue) {
    var length = 0;
    if(dataArr != undefined && dataArr != null){
        length = dataArr.length;
    }
    var dataSeries = [];

    var unitTxt = "";

    if (unit == 1) {
        unit = 1;
        unitTxt = "M"
    }
    var count;
    for (var i = 0; i < length - 1; i++) {
        var data = dataArr[i];
        count = data.y;
        if (defaultValue > 0) {
            count = defaultValue;
        }
        var pointName = data.date + ":  " + count + unitTxt;
        var dataPoint = {
            name: pointName,
            x: data.x,
            y: count,
        };
        dataSeries.push(dataPoint);
    }

    var seriesPoints = {
        name: nameLegend,
        data: dataSeries,
        unit: unit,
        unitTxt: unitTxt
    };
    return seriesPoints;
}

function getKeyPoints(dataArr, nameLegend, unit, defaultValue) {
    var length = 0;
    if(dataArr != undefined && dataArr != null){
        length = dataArr.length;
    }
    var dataSeries = [];

    var unitTxt = "";

    if (unit == 1) {
        unit = 1;
        unitTxt = "个"
    }
    var count;
    for (var i = 0; i < length - 1; i++) {
        var data = dataArr[i];
        count = data.y;
        if (defaultValue > 0) {
            count = defaultValue;
        }
        var pointName = data.date + ":  " + count + unitTxt;
        var dataPoint = {
            name: pointName,
            x: data.x,
            y: count,
        };
        dataSeries.push(dataPoint);
    }

    var seriesPoints = {
        name: nameLegend,
        data: dataSeries,
        unit: unit,
        unitTxt: unitTxt
    };
    return seriesPoints;
}

function getNetPoints(dataArr, nameLegend, unit, timeUnit) {
    var length = 0;
    if(dataArr != undefined && dataArr != null){
        length = dataArr.length;
    }
    var dataSeries = [];

    var unitTxt = "";
    if (typeof (unit) == "undefined") {
        var byteCounter = 0;
        var kbCounter = 0;
        var mbCounter = 0;
        for (var i = 0; i < length - 1; i++) {
            var data = dataArr[i];
            var count = data.y;
            if (count < 1024) {
                byteCounter = byteCounter + 1;
            } else if (count >= 1024 && count < 1024 * 1024) {
                kbCounter = kbCounter + 1;
            } else {
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
        } else {
            if (kbCounter > mbCounter) {
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
        } else if (unit == 1024) {
            unitTxt = "Kb";
        } else if (unit == 1024 * 1024) {
            unitTxt = "Mb";
        }
    }

    if (timeUnit == 1) {
        unit = 1;
        unitTxt = "秒"
    }

    for (var i = 0; i < length - 1; i++) {
        var data = dataArr[i];
        var count = Math.round(data.y / unit);
        var pointName = data.date + ":  " + count + unitTxt;
        var dataPoint = {
            name: pointName,
            x: data.x,
            y: count,
        };
        dataSeries.push(dataPoint);
    }

    var seriesPoints = {
        name: nameLegend,
        data: dataSeries,
        unit: unit,
        unitTxt: unitTxt
    };
    return seriesPoints;
}


function getClientStatisticsByType(dataMap, type, unit, searchDate, exceptionType) {
    var date = new Date(searchDate.replace('-', '/'));
    var timestamp = date.getTime();//毫秒级
    var finalPoints = [];
    for (var client in dataMap) {
        console.log("client:" + client);
        var tagName = client;
        var statList = dataMap[client];
        var statMap = setStatMap(statList, type);
        var dataSeries = [];
        for (var i = 0; i < 24 * 60; i++) {
            var xValue = getTimestampAdd(timestamp, i);
            var yValue = statMap.has(xValue) ? statMap.get(xValue) : 0;
            var pointName = tagName
                + "<br/>" + type + ": " + +yValue + unit
                + "<br/>时间: " + formatDate(xValue);
            var dataPoint = {
                name: pointName,
                x: xValue,
                y: yValue,
            };
            dataSeries.push(dataPoint);
        }
        console.log("dataSeries length:" + dataSeries.length);
        var seriesPoints = {
            name: tagName,
            data: dataSeries,
            marker: {
                radius: 1, // 曲线点半径，默认是4
            },
            events: {
                click: function (e) {
                    if (exceptionType != undefined) {
                        var app_id = '&appId=' + appId;
                        var search_time = '&searchTime=' + e.point.category;
                        window.open('/client/show/latencyCommandDetails?' + app_id + search_time);
                    }
                }
            }
        };
        finalPoints.push(seriesPoints);

    }
    return finalPoints;

}


function getAppLatencyInfo(dataMap, type, unit, searchDate, contextPath) {
    var date = new Date(searchDate.replace('-', '/'));
    var timestamp = date.getTime();//毫秒级
    var finalPoints = [];
    for (var event in dataMap) {
        console.log("event:" + event);
        var tagName = event;
        var statList = dataMap[event];
        var statMap = setStatMap(statList, type);
        var dataSeries = [];
        for (var i = 0; i < 24 * 60; i++) {
            var xValue = getTimestampAdd(timestamp, i);
            var yValue = statMap.has(xValue) ? statMap.get(xValue) : 0;
            if (xValue == 1588995285000) {
                console.log("xValue: " + xValue);
                console.log("yValue: " + yValue);
            }
            var pointName = tagName
                + "<br/>" + type + ": " + +yValue + unit
                + "<br/>时间: " + formatDate(xValue);
            var dataPoint = {
                name: pointName,
                x: xValue,
                y: yValue,
            };
            dataSeries.push(dataPoint);
        }
        console.log("dataSeries length:" + dataSeries.length);
        var seriesPoints = {
            name: tagName,
            data: dataSeries,
            marker: {
                radius: 1, // 曲线点半径，默认是4
            },
            events: {
                click: function (e) {
                    var app_id = '&appId=' + appId;
                    var search_time = '&searchTime=' + e.point.category;
                    window.open(contextPath + '/admin/app/latencyInfoDetails?' + app_id + search_time);
                }
            }
        };
        finalPoints.push(seriesPoints);

    }
    return finalPoints;
}

function setStatMap(statList, type) {
    var map = new Map();
    for (var i = 0; i < statList.length; i++) {
        var commandStat = statList[i];
        var xValue = commandStat.timestamp * 1000;
        var yValue;
        if (type == 'count') {
            yValue = commandStat.count;
        } else if (type == 'cost') {
            yValue = commandStat.cost;
        } else if (type == 'bytesIn') {
            yValue = commandStat.bytes_in / 1024 / 1024;
        } else if (type == 'bytesOut') {
            yValue = commandStat.bytes_out / 1024 / 1024;
        }
        map.set(xValue, yValue);
        //console.log("x:y : "+xValue+":"+yValue);
    }
    console.log("map size:" + map.size);
    return map;
}

/**
 * @param container 图表加载的位置，是页面上的一个DOM对象
 * @param title 图标标题
 * @param titleText 纵坐标标题
 */
function getOption(container, title, titleText) {
    var chartOption = {
        chart: {
            renderTo: container,
            animation: Highcharts.svg,
            borderColor: '#E6E6E6',
            borderWidth: 2,
            // backgroundColor: '#E6F1F5',
            plotBackgroundColor: '#FFFFFF',
            zoomType: 'x',
            type: 'line',
            marginRight: 10
        },
        title: {
            useHTML: true,
            text: title
        },
        xAxis: {
            type: 'datetime',
        },
        yAxis: {
            title: {
                text: titleText
            },
            min: 0
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
        tooltip: {
            formatter: function () {
                return '<b>' + this.point.name + '</b><br/>';
            }
        },
        legend: {
            enabled: true
        },
        credits: {
            enabled: false
        },
        exporting: {
            enabled: true
        },
        series: []
    };
    return chartOption;
}

function getTimestampAdd(timestamp, min) {
    var date = new Date(timestamp);
    date.setMinutes(date.getMinutes() + min);
    var timestampNew = date.getTime();//毫秒级
    return timestampNew;
}

function formatDate(timestamp) {
    var date = new Date(timestamp);
    var YY = date.getFullYear() + '-';
    var MM = (date.getMonth() + 1 < 10 ? '0' + (date.getMonth() + 1) : date.getMonth() + 1) + '-';
    var DD = (date.getDate() < 10 ? '0' + (date.getDate()) : date.getDate());
    var hh = (date.getHours() < 10 ? '0' + date.getHours() : date.getHours()) + ':';
    var mm = (date.getMinutes() < 10 ? '0' + date.getMinutes() : date.getMinutes());
    return YY + MM + DD + " " + hh + mm;
}