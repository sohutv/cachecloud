function generatePagenitorOption(pageNo, numberOfPages, totalPages, pageClickedFunc){
	var options = {
		//small mini normal large
		size:"normal",
		bootstrapMajorVersion:3,
		currentPage: pageNo,
		numberOfPages: numberOfPages,
		totalPages: totalPages,
		itemContainerClass: function (type, page, current) {
            return (page === current) ? "active" : "pointer-cursor";
        },
		shouldShowPage:function(type, page, current){
			return true;
        },
		onPageClicked: pageClickedFunc
	};
	return options;
}