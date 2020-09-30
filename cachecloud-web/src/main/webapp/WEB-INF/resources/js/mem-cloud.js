$(document).ready(function () {
    if (typeof (alert) != 'undefined') {
        if (alert.isAlert == 0) {
            return;
        }
        $.get("/manage/notice/get.json", {}, function (data) {
            console.log(data);
            if (data.status == 1) {
                var alertDiv = $('<div class="alert alert-warning alert-dismissable"></div>');
                $('<button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button>').appendTo(alertDiv);
                $('<p><strong>系统提醒：</strong></p>').appendTo(alertDiv);
                var i = 0;
                list = data.data;
                for (var value in list) {
                    i++;
                    $('<p>' + list[value] + '</p>').appendTo(alertDiv);
                }
                alertDiv.appendTo($('#systemAlert'));
            }
        });
    }




});