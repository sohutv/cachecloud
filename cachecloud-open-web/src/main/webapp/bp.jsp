<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<title>cachecloud bootstrap pagenitor</title>
	<link rel="stylesheet" href="/resources/bootstrap/bootstrap3/css/bootstrap.css">
    <script src="/resources/bootstrap/jquery/jquery-1.11.0.min.js" type="text/javascript"></script>
    <script src="/resources/bootstrap/bootstrap3/js/bootstrap.js" type="text/javascript"></script>
</head>
<body>
<div>
    <ul id='carlosfu'></ul>
</div>
<script src="/resources/bootstrap/paginator/bootstrap-paginator.js"></script>
<script type="text/javascript">
    $(function(){
        var element = $('#carlosfu');
		var options = {
			//small mini normal large
			size:"small",
			bootstrapMajorVersion:3,
			currentPage: 3,
			numberOfPages: 10,
			totalPages:100,
			pageUrl: function(type, page, current){
				//提交form
				//alert("page: " + page + ",current: " + current);
            },
			itemContainerClass: function (type, page, current) {
                return (page === current) ? "active" : "pointer-cursor";
            },
			itemTexts: function (type, page, current) {
				switch (type) {
					case "first":
						return "<<";
					case "prev":
						return "prev";
					case "next":
						return "next";
					case "last":
						return ">>";
					case "page":
						return page;
				}
			},
			onPageClicked: function (e, originalEvent, type, page) {  
                alert("type:" + type + ",Page:" + page);  
            }
		}
		element.bootstrapPaginator(options);
    });
</script>
</body>
</html>