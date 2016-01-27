var chart_options = {
    t: "t"
};

var refreshChartByTool = function (url, param, option_user) {
    $.get(url, param, function (data) {
        var options = data.chart;
        if (typeof options.tooltip != undefined && typeof options.tooltip.formatter != null) {
            options.tooltip.formatter = eval('(' + options.tooltip.formatter + ')');
        }

        for (var o in options) {
            console.log(o);
            if (options.hasOwnProperty(o)) {
                $.extend(options[o], options[o]);
            } else {
                options[o] = options[o];
            }

        }

        for (var o in option_user) {
            console.log(o);
            if (options.hasOwnProperty(o)) {
                $.extend(options[o], option_user[o]);
            } else {
                options[o] = option_user[o];
            }
        }
        new Highcharts.Chart(options);
    }, 'json');
}

var initChart = function (pre) {
    var _c_cu = eval(pre + '_c_cu');
    var _c_o = eval(pre + '_c_o');
//    console.log(instance_stat_c_cu);
    if (typeof (_c_cu) != 'undefined' && typeof (_c_o) != 'undefined') {
        for (var index in _c_cu) {
            var container = index;
            var url = _c_cu[index];
            var param = {};
            param['container'] = container;
//            console.log(container);
            refreshChartByTool(url, param, _c_o[index]);
        }
    }
};
