<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp" %>
<script type="text/javascript" src="/resources/bootstrap/jquery/jquery-1.11.0.js"></script>
<script type="text/javascript" src="/resources/select/bootstrap-select.js"></script>
<link rel="stylesheet" type="text/css" href="/resources/select/bootstrap-select.css"/>
<script type="text/javascript" src="/resources/js/selectpicker.js?<%=System.currentTimeMillis()%>"></script>
<script type="text/javascript">
    function checkAppImportFormat() {
        var res = saveAppDesc();
        if (res == false) {
            return
        }
        var password = document.getElementById("password");
        var type = document.getElementById("type")
        $.post(
            '/import/app/check.json',
            {
                type: type.value,
                password: password.value,
                appInstanceInfo: appInstanceInfo.value
            },
            function (data) {
                var status = data.status;
                alert(data.message);
                if (status == 1) {
                    var submitButton = document.getElementById("submitButton");
                    submitButton.disabled = false;

                    var checkButton = document.getElementById("checkButton");
                    checkButton.disabled = true;
                }
            }
        );
    }
</script>
<div class="col-md-9">
    <div class="row">
        <div class="col-md-12">
            <h3 class="page-header">
                应用导入申请
                <font color='red' size="4">
                    <c:choose>
                        <c:when test="${success == 1}">(更新成功)</c:when>
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
                        <form action="/admin/app/import/submit" method="post"
                              class="form-horizontal form-bordered form-row-stripped">
                            <div class="form-body">
                                <h4 class="page-header">源：Redis实例信息</h4>

                                <div class="form-group">
                                    <label class="control-label col-md-2">
                                        存储类型:
                                    </label>
                                    <div class="col-md-5">
                                        <select id="sourceType" name="sourceType" class="form-control select2_category">
                                            <option value="5">
                                                Redis-standalone
                                            </option>
                                            <option value="6">
                                                Redis-sentinel
                                            </option>
                                            <option value="7" selected="selected">
                                                Redis-cluster
                                            </option>
                                        </select>
                                    </div>
                                </div>

                                <div class="form-group">
                                    <label class="control-label col-md-2">
                                        实例详情:<font color='red'>(*)</font>:
                                    </label>
                                    <div class="col-md-5">
                                        <textarea rows="13" name="appInstanceInfo" id="appInstanceInfo"
                                                  placeholder="节点详情，样例：&#10;1. standalone类型：&#10;127.0.0.1:6379&#10;2. sentinel类型：&#10;mymastername:master&#10;127.0.0.1:6379&#10;127.0.0.1:6380&#10;127.0.0.1:6381&#10;3. cluster类型：&#10;127.0.0.1:6379&#10;127.0.0.1:6380&#10;127.0.0.1:6381
                                            " class="form-control"></textarea>

                                    </div>
                                    <div class="col-md-5">
										<span class="help-block">
											<strong>实例格式说明</strong><br/>
                                            每行格式都是:&nbsp;&nbsp;ip:port(例如：10.10.xx.xx:6379)<br/>
                                            1. standalone类型：<span style="color: #00BE67" title="standalone模式下，需要填写单个redis节点的地址" class="glyphicon glyphicon-question-sign"></span><br/>
                                            &nbsp;&nbsp;&nbsp;&nbsp;masterIp:masterPort<br/>
                                            2. sentinel类型：<span style="color: #00BE67" title="sentinel模式下，需要填写sentinel_master_name:master_or_slave&#10;和多个sentinel_cluster_address。sentinel_master_name表示sentinel配置下master的名字，master_or_slave表示从sentinel中选择的redis是master还是slave，sentinel_cluster_address表示sentinel的单节点或者集群地址。例如：&#10;mymaster:master&#10;127.0.0.1:26379&#10;127.0.0.1:26380" class="glyphicon glyphicon-question-sign"></span><br/>
                                            &nbsp;&nbsp;&nbsp;master_name:master<br/>
                                            &nbsp;&nbsp;&nbsp;sentinelIp1:sentinelPort1<br/>
                                            &nbsp;&nbsp;&nbsp;sentinelIp2:sentinelPort2<br/>
                                            &nbsp;&nbsp;&nbsp;sentinelIp3:sentinelPort3<br/>
                                            3. cluster类型：<span style="color: #00BE67" title="cluster模式下，需要填写集群地址" class="glyphicon glyphicon-question-sign"></span><br/>
                                            &nbsp;&nbsp;&nbsp;&nbsp;masterIp1:masterPort1<br/>
                                            &nbsp;&nbsp;&nbsp;&nbsp;masterIp2:masterPort2<br/>
                                            &nbsp;&nbsp;&nbsp;&nbsp;masterIp3:masterPort3<br/>
                                        </span>
                                    </div>
                                </div>

                                <div class="form-group">
                                    <label class="control-label col-md-2">
                                        redis密码:
                                    </label>
                                    <div class="col-md-5">
                                        <input type="text" name="password" id="password" placeholder="redis密码"
                                               class="form-control"/>
                                        <span class="help-block">
											redis密码，如果没有则为空
										</span>
                                    </div>
                                </div>


                                <h4 class="page-header">目标：应用信息</h4>
                                <div class="form-group">
                                    <label class="control-label col-md-3">
                                        应用名称<font color='red'>(*)</font>:
                                    </label>
                                    <div class="col-md-5">
                                        <input type="text" name="name" id="appName"
                                               placeholder="\${服务名}-\${机房:js/tc}-\${环境:online/test}"
                                               class="form-control" onchange="checkAppNameExist()"/>
                                        <span class="help-block">
												如：cachecloud-js-online，全局唯一
											</span>
                                    </div>
                                </div>

                                <div class="form-group">
                                    <label class="control-label col-md-3">
                                        应用描述<font color='red'>(*)</font>:
                                    </label>
                                    <div class="col-md-5">
											<textarea class="form-control" name="intro"
                                                      rows="3" id="appIntro" placeholder="应用描述"></textarea>
                                        <span class="help-block">
												不超过128个字符，可以包含中文
											</span>
                                    </div>
                                </div>

                                <div class="form-group">
                                    <label class="control-label col-md-3">
                                        项目负责人<font color='red'>(*)</font>:
                                    </label>
                                    <div class="col-md-5">
                                        <select id="officer" name="officer"
                                                class="form-control selectpicker bla bla bli" multiple
                                                data-live-search="true" data-width="31%">
                                            <c:forEach items="${userList}" var="user">
                                                <option data-icon="glyphicon-user"
                                                        value="${user.id}">${user.chName}【${user.email}】
                                                </option>
                                            </c:forEach>
                                        </select>
                                    </div>
                                </div>

                                <div class="form-group">
                                    <label class="control-label col-md-3">
                                        存储种类:
                                    </label>
                                    <div class="col-md-2">
                                        <select id="type" name="type" class="form-control select2_category">
                                            <option value="2">
                                                Redis-cluster
                                            </option>
                                            <option value="5">
                                                Redis-Sentinel
                                            </option>
                                            <option value="6">
                                                Redis-standalone
                                            </option>
                                        </select>
                                    </div>

                                    <label class="control-label col-md-2">
                                        内存总量<font color='red'>(*)</font>:
                                    </label>
                                    <div class="col-md-2">
                                        <input type="text" name="memSize" id="memSize" placeholder="填整数,单位G"
                                               class="form-control"/>
                                    </div>
                                </div>

                                <div class="form-group">
                                    <label class="control-label col-md-3">
                                        Redis部署版本:
                                    </label>
                                    <div class="col-md-2">
                                        <select id="versionId" name="versionId" class="form-control">
                                            <option value="-1">自定义</option>
                                            <c:forEach items="${versionList}" var="version">
                                                <option value="${version.id}">${version.name}</option>
                                            </c:forEach>
                                        </select>
                                    </div>
                                    <label class="control-label col-md-1">
                                        其他:
                                    </label>
                                    <div class="col-md-3">
                                        <input type="text" id="versionName" name="versionName"
                                               placeholder="格式：redis-x.x.x" class="form-control"/>
                                    </div>
                                </div>

                                <div class="form-group">
                                    <label class="control-label col-md-3">
                                        测试:
                                    </label>
                                    <div class="col-md-2">
                                        <select id="isTest" name="isTest" class="form-control">
                                            <option value="0">
                                                否
                                            </option>
                                            <option value="1">
                                                是
                                            </option>
                                        </select>
                                    </div>
                                    <label class="control-label col-md-2">
                                        是否有数据备份:
                                    </label>
                                    <div class="col-md-2">
                                        <select id="hasBackStore" name="hasBackStore" class="form-control">
                                            <option value="1">
                                                是
                                            </option>
                                            <option value="0">
                                                否
                                            </option>
                                        </select>
                                    </div>
                                </div>

                                <div class="form-group">
                                    <label class="control-label col-md-3">
                                        是否需要持久化:
                                    </label>
                                    <div class="col-md-2">
                                        <select id="needPersistence" name="needPersistence" class="form-control">
                                            <option value="1">
                                                是
                                            </option>
                                            <option value="0">
                                                否
                                            </option>
                                        </select>
                                    </div>
                                    <label class="control-label col-md-2">
                                        是否需要slave:
                                    </label>
                                    <div class="col-md-2">
                                        <select id="needHotBackUp" name="needHotBackUp" class="form-control">
                                            <option value="1">
                                                是
                                            </option>
                                            <option value="0">
                                                否
                                            </option>
                                        </select>
                                    </div>
                                </div>

                                <div class="form-group">
                                    <label class="control-label col-md-3">
                                        预估QPS<font color='red'>(*)</font>:
                                    </label>
                                    <div class="col-md-2">
                                        <input type="text" name="forecaseQps" id="forecaseQps" value="800"
                                               class="form-control" onchange="testisNum(this.id)"/>
                                    </div>
                                    <label class="control-label col-md-2">
                                        预估条目数量:<font color='red'>(*)</font>:
                                    </label>
                                    <div class="col-md-2">
                                        <input type="text" name="forecastObjNum" id="forecastObjNum" value="100000"
                                               class="form-control" onchange="testisNum(this.id)"/>
                                    </div>
                                </div>

                                <div class="form-group">
                                    <label class="control-label col-md-3">
                                        客户端机房:<font color='red'>(*)</font>:
                                    </label>
                                    <div class="col-md-5">
                                        <select name="clientMachineRoom" id="clientMachineRoom"
                                                class="form-control select2_category">
                                            <c:forEach items="${roomList}" var="room">
                                                <option value="${room.name}">${room.name} (${room.ipNetwork})</option>
                                            </c:forEach>
                                        </select>
                                    </div>
                                </div>

                                <div class="form-group">
                                    <label class="control-label col-md-3">
                                        内存报警阀值<font color='red'>(*)</font>:
                                    </label>
                                    <div class="col-md-5">
                                        <input type="text" name="memAlertValue" id="memAlertValue" value="90"
                                               class="form-control" onchange="testisNum(this.id)"/>
                                        <span class="help-block">
												例如：内存使用率超过90%报警，请填写90（<font color="red">大于100以上则不报警</font>）
											</span>
                                    </div>
                                </div>

                                <div class="form-group">
                                    <label class="control-label col-md-3">
                                        客户端连接数报警阀值<font color='red'>(*)</font>:
                                    </label>
                                    <div class="col-md-5">
                                        <input type="text" name="clientConnAlertValue" id="clientConnAlertValue"
                                               value="2000" class="form-control" onchange="testisNum(this.id)"/>
                                        <span class="help-block">
												例如：如客户端连接数超过2000报警，填写2000
											</span>
                                    </div>
                                </div>

                                <input name="userId" id="userId" value="${userInfo.id}" type="hidden"/>
                                <input id="appExist" value="0" type="hidden"/>

                                <div class="form-actions fluid">
                                    <div class="row">
                                        <div class="col-md-12">
                                            <div class="col-md-offset-3 col-md-9">
                                                <button id="checkButton" type="button" class="btn btn-info"
                                                        onclick="checkAppImportFormat()">
                                                    <i class="fa fa-check"></i>
                                                    检查格式
                                                </button>
                                                <button id="submitButton" type="submit" class="btn btn-info"
                                                        disabled="disabled">
                                                    <i class="fa fa-check"></i>
                                                    提交申请
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
        </div>
    </div>
</div>


