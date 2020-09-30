//重新加载appDetail页面
function reloadAppDetailPage(appId) {
    location.href = "/admin/app/index?appId=" + appId + "&tabTag=app_detail";
}

//appDetail页面删除用户
function deleteAppUser(userId, appId) {
    if (window.confirm('确认要删除该用户吗?!')) {
        var url = "/admin/app/deleteAppToUser?userId=" + userId + "&appId=" + appId;
        $.ajax({
            type: "get",
            url: url,
            async: false,
            success: function (data) {
                alert("删除成功");
                reloadAppDetailPage(appId);
            }
        });
    }
    return false;
}


//改变应用信息
function updateAppDetailChange(appId) {
    var appDescName = document.getElementById("appDescName");
    if (appDescName.value == "") {
        alert("应用名不能为空");
        appDescName.focus();
        return false;
    }
    var appDescIntro = document.getElementById("appDescIntro");
    if (appDescIntro.value == "") {
        alert("应用描述不能为空");
        appDescIntro.focus();
        return false;
    }
    var officer =  $('#officer_select').selectpicker('val');
    if (officer == null || officer.length == 0) {
        alert("负责人不能为空");
        officer.focus();
        return false;
    }
    var updateAppDetailBtn = document.getElementById("updateAppDetailBtn");
    updateAppDetailBtn.disabled = true;
    $.post(
        '/admin/app/updateAppDetail',
        {
            appId: appId,
            appDescName: appDescName.value,
            appDescIntro: appDescIntro.value,
            officer: officer.toString()
        },
        function (data) {
            if (data == 1) {
                alert("修改成功！");
                $("#updateAppDetailInfo").html("<div class='alert alert-error' ><button class='close' data-dismiss='alert'>×</button><strong>Success!</strong>更新成功，窗口会自动关闭</div>");
                setTimeout("reloadAppDetailPage(" + appId + ");", 1000);
            } else {
                updateAppDetailBtn.disabled = false;
                $("#updateAppDetailInfo").html("<div class='alert alert-error' ><button class='close' data-dismiss='alert'>×</button><strong>Error!</strong>更新失败！</div>");
            }
        }
    );
}


//改变内存阀值
function appAlertConfigChange(appId) {
    var memAlertValue = document.getElementById("memAlertValue");
    if (memAlertValue.value == "") {
        alert("内存报警阀值不能为空");
        memAlertValue.focus();
        return false;
    }
    var clientConnAlertValue = document.getElementById("clientConnAlertValue");
    if (clientConnAlertValue.value == "") {
        alert("客户端连接数报警阀值不能为空");
        clientConnAlertValue.focus();
        return false;
    }
    var hitPrecentAlertValue = document.getElementById("hitPrecentAlertValue");
    if (hitPrecentAlertValue.value == "") {
        alert("应用平均命中率报警阀值不能为空");
        hitPrecentAlertValue.focus();
        return false;
    }
    var isAccessMonitor = jQuery("#isAccessMonitor option:selected");
    if (isAccessMonitor.attr("value") == "") {
        alert("应用全局报警不能为空");
        isAccessMonitor.focus();
        return false;
    }
    var appConfigChangeBtn = document.getElementById("appConfigChangeBtn");
    appConfigChangeBtn.disabled = true;
    $.post(
        '/admin/app/changeAppAlertConfig',
        {
            appId: appId,
            memAlertValue: memAlertValue.value,
            clientConnAlertValue: clientConnAlertValue.value,
            hitPrecentAlertValue: hitPrecentAlertValue.value,
            isAccessMonitor: isAccessMonitor.attr("value")
        },
        function (data) {
            if (data == 1) {
                alert("修改成功！");
                $("#appConfigChangeInfo").html("<div class='alert alert-error' ><button class='close' data-dismiss='alert'>×</button><strong>Success!</strong>更新成功，窗口会自动关闭</div>");
                setTimeout("reloadAppDetailPage(" + appId + ");", 1000);
            } else {
                appConfigChangeBtn.disabled = false;
                $("#appConfigChangeInfo").html("<div class='alert alert-error' ><button class='close' data-dismiss='alert'>×</button><strong>Error!</strong>更新失败！</div>");
            }
        }
    );
}

function updateWholeAlertConfigChange(appId) {
    var appConfigKey = document.getElementById("appMonitorConfigKey");
    if (appConfigKey.value == "") {
        alert("配置项不能为空");
        appConfigKey.focus();
        return false;
    }

    var appConfigValue = document.getElementById("appMonitorConfigValue");
    if (appConfigValue.value == "") {
        alert("配置值不能为空");
        appConfigValue.focus();
        return false;
    }

    var appConfigReason = document.getElementById("appMonitorConfigReason");
    if (appConfigReason.value == "") {
        alert("配置原因不能为空");
        appConfigReason.focus();
        return false;
    }

    var updateConfigChangeBtn = document.getElementById("updateConfigChangeBtn");
    updateConfigChangeBtn.disabled = true;

    $.post(
        '/admin/app/changeAppMonitorConfig',
        {
            appId: appId,
            instanceId: "",
            appConfigKey: appConfigKey.value,
            appConfigValue: appConfigValue.value,
            appConfigReason: appConfigReason.value
        },
        function (data) {
            if (data == 1) {
                alert("申请成功，请在邮件中关注申请状况.");
                $("#updateConfigChangeInfo").html("<div class='alert alert-error' ><button class='close' data-dismiss='alert'>×</button><strong>Success!</strong>更新成功，窗口会自动关闭</div>");
                setTimeout("reloadAppStatPage(" + appId + ");", 1000);

            } else {
                appConfigChangeBtn.disabled = false;
                $("#updateConfigChangeInfo").html("<div class='alert alert-error' ><button class='close' data-dismiss='alert'>×</button><strong>Error!</strong>更新失败！</div>");
            }
        }
    );
}

//添加应用用户
function appAddUser(appId) {
    var addAppToUser = $('#addAppToUser').selectpicker('val');
    if (addAppToUser == null || addAppToUser.length == 0) {
        alert("用户名不能为空");
        return false;
    }
    var appAddUserBtn = document.getElementById("appAddUserBtn");
    appAddUserBtn.disabled = true;

    $.post(
        '/admin/app/addAppToUser',
        {
            appId: appId,
            users: addAppToUser.toString()
        },
        function (data) {
            if (data == 1) {
                alert("用户添加成功!");
                $("#appAddUserInfo").html("<div class='alert alert-error' ><button class='close' data-dismiss='alert'>×</button><strong>Success!</strong>更新成功，窗口会自动关闭</div>");
                setTimeout("reloadAppDetailPage(" + appId + ");", 1000);
            } else {
                appAddUserBtn.disabled = false;
                alert("cachecloud中不存在该用户，只能添加有cachecloud权限的用户");
                $("#appAddUserInfo").html("<div class='alert alert-error' ><button class='close' data-dismiss='alert'>×</button><strong>Error!</strong>添加失败,cachecloud中不存在该用户，只能添加有cachecloud权限的用户！</div>");
            }
        }
    );
}

//验证手机号格式
var valPhones = /^((13[0-9])|(14[5,7])|(15[0-3,5-9])|(17[0,3,5-8])|(18[0-9])|166|198|199|(147))\d{8}$/;
//验证邮箱格式
var valEmails = /^(([a-zA-Z0-9]+[_|\_|\.]?)*[a-zA-Z0-9]+@([a-zA-Z0-9]+[_|\_|\.|\-]?)*[a-zA-Z0-9]+\.[a-zA-Z]{2,3};){0,6}([a-zA-Z0-9]+[_|\_|\.]?)*[a-zA-Z0-9]+@([a-zA-Z0-9]+[_|\_|\.|\-]?)*[a-zA-Z0-9]+\.[a-zA-Z]{2,3}$/;

function saveOrUpdateUser(userId, appId) {
    var name = document.getElementById("name" + userId);
    var chName = document.getElementById("chName" + userId);
    var email = document.getElementById("email" + userId);
    var mobile = document.getElementById("mobile" + userId);
    var weChat = document.getElementById("weChat" + userId);
    var type = document.getElementById("type" + userId);
    var isAlert = document.getElementById("isAlert" + userId);
    if (name.value == "") {
        alert("域账户名不能为空!");
        name.focus();
        return false;
    }
    if (chName.value == "") {
        alert("中文名不能为空!");
        chName.focus();
        return false;
    }
    if (email.value == "") {
        alert("邮箱不能为空!");
        email.focus();
        return false;
    }
    if (!valEmails.test(email.value)) {
        alert("邮箱格式错误!");
        email.focus();
        return false;
    }
    if (mobile.value == "") {
        alert("手机号不能为空!");
        mobile.focus();
        return false;
    }
    if (!valPhones.test(mobile.value)) {
        alert("手机号格式错误!");
        mobile.focus();
        return false;
    }
    if (weChat.value == "") {
        alert("微信不能为空!");
        weChat.focus();
        return false;
    }
    $.post(
        '/admin/app/changeAppUserInfo',
        {
            name: name.value,
            chName: chName.value,
            email: email.value,
            mobile: mobile.value,
            weChat: weChat.value,
            type: type.value,
            isAlert: isAlert.value,
            userId: userId
        },
        function (data) {
            if (data == 1) {
                $("#info" + userId).html("<div class='alert alert-error' ><button class='close' data-dismiss='alert'>×</button><strong>Success!</strong>更新成功，窗口会自动关闭</div>");
                var targetId = "#addUserModal" + userId;
                setTimeout("reloadAppDetailPage(" + appId + ")", 1000);
            } else {
                $("#info" + userId).html("<div class='alert alert-error' ><button class='close' data-dismiss='alert'>×</button><strong>Error!</strong>更新失败！</div>");
            }
        }
    );
}




