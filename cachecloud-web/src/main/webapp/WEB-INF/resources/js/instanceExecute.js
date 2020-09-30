var execute = function () {
    var command = $('#command').value;
    $.ajax({
        url: "/admin/instance/commandExecute.json",
        data: {instanceId: $('#instanceId').val(), command: $('#command').val()},
        dataType: "json",
        success: function (result) {
            $('#result').append('<p>' + $('#command').val() + '</p>');
            $('#result').append('<p>' + result.result + '</p>');
        }
    });
};