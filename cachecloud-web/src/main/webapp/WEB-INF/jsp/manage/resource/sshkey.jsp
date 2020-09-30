<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<script type="text/javascript" src="/resources/bootstrap/jquery/jquery-1.11.0.js"></script>
<%@include file="/WEB-INF/jsp/manage/commons/taglibs.jsp" %>

<script type="text/javascript">

    function generate(resourceId){

        $.post(
            '/manage/app/resource/ssh.json',
            {
                command: $('#ssh-keygen').val(),
                resourceId: resourceId
            },
            function (data) {
                var status = data.status;
                if (status == 1) {
                    alert("推送成功");
                } else {
                    alert("推送失败！" + data.message);
                }
                window.location.reload();
            }
        );
    }

</script>

<div class="row">
    <div class="col-md-12">
        <h4 class="glyphicon glyphicon-lock" >
            sshkey管理
        </h4>
    </div>
</div>

<div class="row">
    <div id="respo-div" class="col-md-12">
        <form class="form-inline" role="form" name="ec">
            <div class="form-group col-md-2">
                <label id="respo">sshkey script:</label>
            </div>
            <div class="col-md-8">
            <input id="ssh-keygen" style="width: 80%" type="text" class="form-control" name="pattern"
            value="ssh-keygen -t rsa -f /opt/ssh2/id_rsa -P '' -C cachecloud" placeholder="ssh key script"></div>

            <div class="form-group col-md-2" style="float:right; width: max-content">
                <button type="button" class="form-control btn green" onclick="generate('${resource.id}')">生成&推送</button>
            </div>

            <div class="publickey-div">
                <div class="form-group col-md-2">
                    <label id="publickey">公钥信息:</label>
                </div>
                <div class="col-md-7">
                    <textarea rows="8" name="notice" id="notice" readonly="readonly" placeholder="公钥" class="form-control"></textarea>
                </div>
            </div>
        </form>
    </div>
</div>
    <div class="row">9
    <div class="col-md-12">
        <div class="portlet box light-grey" id="clientIndex">

            <table class="table table-striped table-bordered table-hover" id="tableDataList">
                <thead>
                <tr>
                    <td>序号</td>
                    <th>脚本名称</th>
                    <th>脚本说明</th>
                    <th>目录</th>
                    <th>最后更新时间</th>
                    <th>状态</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach items="${resourceList}" var="resource">
                    <tr>
                        <td>${resource.id}</td>
                        <td>
                            <c:if test="${resource.ispush==1 || resource.ispush==3 }"><a target="_blank" href="${repository.url}${resource.dir}">${resource.name}</a></c:if>
                            <c:if test="${resource.ispush==0 || resource.ispush==2 || resource.ispush==4}">${resource.name}</c:if>
                        </td>
                        <td>
                            ${resource.intro}
                        </td>
                        <td>
                            ${resource.dir}
                        </td>
                        <td>
                            <fmt:formatDate value="${resource.lastmodify}" pattern="yyyy-MM-dd HH:mm:ss"/>
                        </td>
                       <td>
                            <c:if test="${resource.ispush == 0}">未推送</c:if>
                            <c:if test="${resource.ispush == 1}"><span style="color:green">已推送</span></c:if>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </div>
</div>






