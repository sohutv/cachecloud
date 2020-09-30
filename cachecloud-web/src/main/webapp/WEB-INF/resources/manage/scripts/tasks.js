var Tasks = function () {


    return {

        //main function to initiate the module
        initDashboardWidget: function () {
			$('input.liChild').change(function() {
				if ($(this).is(':checked')) { 
					$(this).parents('li').addClass("task-done"); 
				} else { 
					$(this).parents('li').removeClass("task-done"); 
				}
			}); 
        }

    };

}();