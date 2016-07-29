$(function () 
 { $("[data-toggle='popover']").popover(
	{
		trigger : 'manual',
		placement : 'right',
		html : 'true',
		content : "<div id='popOverBox'>正在加载，请稍候...</div>",
		animation : false
	}).on(
	"mouseenter",
	function() {
		var _this = this;
		$(this).popover("show");
		$(this).siblings(".popover").on("mouseleave",
				function() {
					$(_this).popover('hide');
				});
	}).on("mouseleave", function() {
		var _this = this;
		setTimeout(function() {
			if (!$(".popover:hover").length) {
				$(_this).popover("hide");
			}
		}, 100);
	});
 });