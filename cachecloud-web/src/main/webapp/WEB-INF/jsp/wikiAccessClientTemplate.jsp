<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
  <head>
	<jsp:include page="../include/headMenu.jsp" />
    <jsp:include page="../include/head.jsp"/>
    <script src="/resources/manage/plugins/jquery-1.10.2.min.js"></script>
  	<link href="/resources/prettify/prettify.css" rel="stylesheet">
  	<link href="/resources/css/githubmd.css" rel="stylesheet">
	<link href="/resources/css/common.css" rel="stylesheet" type="text/css"/>
  	<!-- 滚动插件 -->
	<script src="${request.contextPath}/resources/nicescroll/jquery.nicescroll.min.js"></script>
	<script type="text/javascript" src="${request.contextPath}/resources/prettify/prettify.js"></script>
	<meta name="referrer" content="no-referrer">
	<style>
		th, td{padding:.5em;border:1px solid #000;}
	</style>
  	<title>CacheCloud</title>
  </head>
  <body role="document">
    <div class="container">
		<div class="row">
			<div class="col-md-10 markdown-body">
				${response}
			</div>
		</div>
    </div>
  </body>


  <jsp:include page="../include/foot.jsp"/>
  <script>
	$(function(){
		$("body").niceScroll({
			cursorcolor:"#ddd",
		  	cursorwidth:"12px",
			cursorminheight: 64,
			horizrailenabled : false
		});
		$('pre').each(function(i, pre) {
            $(pre).addClass("prettyprint");
        });
		PR.prettyPrint();
		if(window.location.hash && $(window.location.hash).offset()){
			$("html,body").animate({scrollTop: $(window.location.hash).offset().top - 52}, 500);
		}
		$("#introLi").addClass("active");
	});
  </script>
</html>
