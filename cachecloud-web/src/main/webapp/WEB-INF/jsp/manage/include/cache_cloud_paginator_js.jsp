<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<script src="/resources/manage/plugins/jquery-1.10.2.min.js" type="text/javascript"></script>
<script src="/resources/manage/plugins/bootstrap/js/bootstrap.min.js" type="text/javascript"></script>
<script src="/resources/manage/plugins/data-tables/DT_bootstrap.js" type="text/javascript"></script>
<script src="/resources/manage/scripts/table-managed.js" type="text/javascript"></script>

<link href="/resources/bootstrap/bootstrap3/css/bootstrap-theme.min.css" rel="stylesheet"/>
<script src="/resources/bootstrap/paginator/bootstrap-paginator.js"></script>
<script src="/resources/bootstrap/paginator/custom-pagenitor.js"></script>

<script type="text/javascript">
    $(function(){
        var userType = '${currentUser.type}';
        if (userType == 0) {
            //分页点击函数
            var pageClickedFunc = function (e, originalEvent, type, page){
                //form传参用pageSize
                document.getElementById("pageNo").value=page;
                document.getElementById("appList").submit();
            };
            //分页组件
            var element = $('#ccPagenitor');
            //当前page号码
            var pageNo = '${page.pageNo}';
            //总页数
            var totalPages = '${page.totalPages}';
            //显示总页数
            var numberOfPages = '${page.numberOfPages}';
            var options = generatePagenitorOption(pageNo, numberOfPages, totalPages, pageClickedFunc);
            if(totalPages > 0){
                element.bootstrapPaginator(options);
                document.getElementById("pageDetail").style.display = "";
            }else{
                element.html("未查询到相关记录！");
            }
        }

    });
</script>


