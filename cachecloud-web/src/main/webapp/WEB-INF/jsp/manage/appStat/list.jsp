<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<title>CacheCloud管理后台</title>
	<meta http-equiv="X-UA-Compatible" content="IE=edge">
	
	<%@include file="/WEB-INF/jsp/manage/include/cache_cloud_main_css.jsp" %>

</head>

<body class="page-header-fixed">
	<%@include file="/WEB-INF/jsp/manage/include/head.jsp" %>

	<%@include file="/WEB-INF/jsp/manage/include/left.jsp" %>

	<%@include file="appStatList.jsp" %>
	
	<%@include file="/WEB-INF/jsp/manage/include/foot.jsp" %>

	<%@include file="/WEB-INF/jsp/manage/include/cache_cloud_main_js_include.jsp" %>

	<script>
        var TableManaged = function () {
            return {
                //main function to initiate the module
                init: function () {

                    if (!jQuery().dataTable) {
                        return;
                    }

					$('#tableDataList').dataTable({
						"searching": true,
						"bLengthChange": false,
						"iDisplayLength": 20,
						"sPaginationType": "bootstrap",
						"oLanguage": {
							"sLengthMenu": "_MENU_ records",
							"oPaginate": {
								"sPrevious": "Prev",
								"sNext": "Next"
							}
						},
						"aoColumnDefs": [
							 { "sType": "numeric-comma", "aTargets": [3,4,5,6,7,8,9,10] },
						],
					});

                    // jQuery('#tableDataList_wrapper>div:first-child').css("display","none");
                }
            };
        }();

    	jQuery(document).ready(function() {
    	   App.init(); // initlayout and core plugins
    	   TableManaged.init();
    	});
    </script>
	
</body>
</html>