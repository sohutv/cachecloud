<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp" %>
<%@ page import="com.sohu.cache.utils.EnvCustomUtil"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <title>CacheCloud用户申请</title>
    <script type="text/javascript">
        //验证邮箱格式
        var valEmails = /^(([a-zA-Z0-9]+[_|\_|\.]?)*[a-zA-Z0-9]+@([a-zA-Z0-9]+[_|\_|\.|\-]?)*[a-zA-Z0-9]+\.[a-zA-Z]{2,3};){0,6}([a-zA-Z0-9]+[_|\_|\.]?)*[a-zA-Z0-9]+@([a-zA-Z0-9]+[_|\_|\.|\-]?)*[a-zA-Z0-9]+\.[a-zA-Z]{2,3}$/;
        //验证手机号格式
        var valPhones = /^((13[0-9])|(14[5,7])|(15[0-3,5-9])|(17[0,3,5-8])|(18[0-9])|166|198|199|(147))\d{8}$/;
        //验证密码强度
        var pwdRegex = new RegExp('(?=.*[0-9])(?=.*[a-zA-Z]).{8,30}');

        function encrypt(data) {
            var key  = CryptoJS.enc.Latin1.parse('CacheCloud123456'); //16位，自定义
            var iv   = CryptoJS.enc.Latin1.parse('CacheCloud123456'); //16位，自定义
            return CryptoJS.AES.encrypt(data, key, {iv:iv,mode:CryptoJS.mode.CBC,padding:CryptoJS.pad.ZeroPadding}).toString();
        }

        function checkUser() {
            var name = document.getElementById("name");
            var chName = document.getElementById("chName");
            var email = document.getElementById("email");
            var mobile = document.getElementById("mobile");
            var weChat = document.getElementById("weChat");
            var isAlert = document.getElementById("isAlert");
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
                alert("微信号不能为空!");
                weChat.focus();
                return false;
            }
            return true;
        }

        function checkUserNameExist(id) {
            var userName = document.getElementById(id).value;
            if (userName != '') {
                $.post(
                    '/user/checkUserNameExist',
                    {
                        userName: userName,
                    },
                    function (data) {
                        if (data == 1) {
                            alert("用户名已经存在，请修改或者联系管理员");
                            document.getElementById(id).focus();
                            document.getElementById(id).value = "";
                        }
                    }
                );
            }
        }

        function checkPassword() {

            var password = $('#password').val();
            if (!pwdRegex.test(password)) {
                alert("您的密码复杂度太低，密码中必须包含字母、数字、特殊字符至少8个字符，请修改密码！");
                $('#password').focus();
                return false;
            }
            return true;
        }

        function checkConfirmPassword() {
            var password = $('#password').val();
            var password1 = $('#password1').val();
            if (password != password1) {
                alert("两次密码输入不一致，请确认密码！")
                $('#password1').focus();
                return false;
            }
            return true;
        }

        function checkUserExtraInfo() {
            var company = document.getElementById("company");
            var purpose = document.getElementById("purpose");
            if (company.value == "") {
                alert("公司名称不能为空!");
                company.focus();
                return false;
            }
            if (purpose.value == "") {
                alert("使用目的不能为空!");
                purpose.focus();
                return false;
            }
            return true;
        }

        function registerUser(pwdFlag, openFlag) {
            var company = "";
            var purpose = "";
            var encryPwd = "";
            if(pwdFlag){
                if (!checkPassword()) {
                    return;
                }
                if (!checkConfirmPassword()) {
                    return;
                }
                var password = document.getElementById("password");
                encryPwd = $.md5(password.value);
            }
            if(openFlag){
                if(!checkUserExtraInfo()){
                    return;
                }
                company = document.getElementById("company").value;
                purpose = document.getElementById("purpose").value;
            }
            if(!checkUser()){
                return;
            }

            var name = document.getElementById("name");
            var chName = document.getElementById("chName");
            var email = document.getElementById("email");
            var mobile = document.getElementById("mobile");
            var weChat = document.getElementById("weChat");
            var type = document.getElementById("type");
            var isAlert = document.getElementById("isAlert");

            var userBtn = document.getElementById("registerUserBtn");
            userBtn.disabled = true;

            $.post(
                '/user/apply',
                {
                    name: name.value,
                    chName: chName.value,
                    email: email.value,
                    mobile: mobile.value,
                    weChat: weChat.value,
                    type: type.value,
                    isAlert: isAlert.value,
                    password: encryPwd,
                    company: company,
                    purpose: purpose
                },
                function (data) {
                    $("#allBody").html(data);//刷新整个body页面的html
                }
            );
        }

    </script>
    <jsp:include page="/WEB-INF/include/head.jsp"/>
    <script src="/resources/manage/plugins/jquery.md5.js" type="text/javascript"></script>
</head>
<body role="document" id="allBody">
<div class="container">
    <jsp:include page="/WEB-INF/include/headMenu.jsp"/>
    <div id="systemAlert">
    </div>
    <div class="page-content">
        <div class="row">
            <div class="col-md-12">
                <h3 class="page-header">
                    CacheCloud用户申请
                    <font color='red' size="4">
                        <c:choose>
                            <c:when test="${success == 1}">(申请成功，请关注邮件中审批进度)</c:when>
                            <c:when test="${success == 0}">(申请失败，请确认填写信息)</c:when>
                        </c:choose>
                    </font>
                </h3>
            </div>
        </div>
        <div class="row">
            <div class="col-md-12">
                <div class="portlet box light-grey">
                    <div class="portlet-body">
                        <div class="form">
                            <!-- BEGIN FORM-->
                            <form method="post"
                                  class="form-horizontal form-bordered form-row-stripped">
                                <div class="form-body">

                                    <c:if test="<%=EnvCustomUtil.openswitch%>">
                                        <div class="control-label col-md-8">
                                    </c:if>

                                    <div class="form-group">
                                        <label class="control-label col-md-4">
                                            账户名:
                                        </label>
                                        <div class="col-md-6">
                                            <input type="text" name="name" id="name" placeholder="域账户名(邮箱前缀)"
                                                   class="form-control" onchange="checkUserNameExist(this.id)"/>
                                        </div>
                                    </div>

                                    <c:if test="<%=EnvCustomUtil.pwdswitch%>">
                                        <div class="form-group">
                                            <label class="control-label col-md-4">
                                                密码:
                                            </label>
                                            <div class="col-md-6">
                                                <input type="password" name="password" id="password" placeholder="输入密码"
                                                       class="form-control" onchange="checkPassword()"/>
                                                <span class="help-block">密码中必须包含字母、数字，至少8个字符</span>
                                            </div>
                                        </div>

                                        <div class="form-group">
                                            <label class="control-label col-md-4">
                                                确认密码:
                                            </label>
                                            <div class="col-md-6">
                                                <input type="password" name="password1" id="password1" placeholder="再次输入密码"
                                                       class="form-control" onchange="checkConfirmPassword()"/>
                                            </div>
                                        </div>
                                    </c:if>

                                    <c:if test="<%=EnvCustomUtil.openswitch%>">
                                        </div>
                                        <div class="control-label col-md-8">
                                    </c:if>

                                    <div class="form-group">
                                        <label class="control-label col-md-4">
                                            中文名:
                                        </label>
                                        <div class="col-md-6">
                                            <input type="text" name="chName" id="chName" placeholder="中文名"
                                                   class="form-control"/>
                                        </div>
                                    </div>

                                    <div class="form-group">
                                        <label class="control-label col-md-4">
                                            邮箱:
                                        </label>
                                        <div class="col-md-6">
                                            <input type="text" name="email" id="email" placeholder="邮箱"
                                                   class="form-control"/>
                                        </div>
                                    </div>

                                    <div class="form-group">
                                        <label class="control-label col-md-4">
                                            手机:
                                        </label>
                                        <div class="col-md-6">
                                            <input type="text" id="mobile" name="mobile" placeholder="手机"
                                                   class="form-control"/>
                                        </div>
                                    </div>

                                    <div class="form-group">
                                        <label class="control-label col-md-4">
                                            微信:
                                        </label>
                                        <div class="col-md-6">
                                            <input type="text" id="weChat" name="weChat" placeholder="微信"
                                                   class="form-control"/>
                                        </div>
                                    </div>

                                    <div class="form-group">
                                        <label class="control-label col-md-4">
                                            是否接收报警:
                                        </label>
                                        <div class="col-md-6">
                                            <select name="isAlert" id="isAlert" class="form-control select2_category">
                                                <option value="1" selected>
                                                    是
                                                </option>
                                                <option value="0">
                                                    否
                                                </option>
                                            </select>
                                        </div>
                                    </div>

                                    <c:if test="<%=EnvCustomUtil.openswitch%>">
                                        <div class="form-group">
                                            <label class="control-label col-md-4">
                                                公司名称:
                                            </label>
                                            <div class="col-md-6">
                                                <input type="text" id="company" name="company" placeholder="公司名称"
                                                       class="form-control"/>
                                            </div>
                                        </div>

                                        <div class="form-group">
                                            <label class="control-label col-md-4">
                                                使用目的:
                                            </label>
                                            <div class="col-md-6">
                                                <textarea rows="5" cols="5" id="purpose" name="purpose" class="form-control" maxlength="120"></textarea>
                                            </div>
                                        </div>
                                    </c:if>

                                    <c:if test="<%=EnvCustomUtil.openswitch%>">
                                        </div>
                                        <div id="contact" class="col-md-3">
                                            <div>
                                                <h5>QQ群：</h5>
                                                CacheCloud开发运维 已满<br/>
                                                CacheCloud开发运维2群:894022242<br/>
                                                CacheCloud开发运维3群:908821300
                                            </div>
                                            <div>
                                                 <h5>微信群：</h5>
                                                <img width="200px" height="200px" src="http://photocdn.tv.sohu.com/img/cachecloud/weixin.jpg?time=<%=System.currentTimeMillis()%>"/>
                                            </div>
                                        </div>
                                    </c:if>

                                    <input type="hidden" id="type" name="type" value="-1">
                                    <br/><br/>
                                    <div class="form-actions fluid">
                                        <div class="row">
                                            <div class="col-md-12">
                                                <div class="col-md-offset-4 col-md-11">
                                                    <button id="registerUserBtn" type="submit" class="btn green" onclick="registerUser(<%=EnvCustomUtil.pwdswitch%>, <%=EnvCustomUtil.openswitch%>)"
                                                            <c:if test="${success == 1}">disabled="disabled"</c:if>>
                                                        <i class="fa fa-check"></i>
                                                        提交申请
                                                    </button>
                                                    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                                                    <button type="reset" class="btn green">
                                                        <i class="fa fa-check"></i>
                                                        &nbsp;&nbsp;重&nbsp;&nbsp;&nbsp;置&nbsp;&nbsp;
                                                    </button>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>

                            </form>
                            <!-- END FORM-->
                        </div>
                    </div>
                </div>
                <!-- END TABLE PORTLET-->
            </div>
        </div>
    </div>
</div>
<br/><br/><br/><br/><br/><br/><br/>
<jsp:include page="/WEB-INF/include/foot.jsp"/>
</body>
</html>

