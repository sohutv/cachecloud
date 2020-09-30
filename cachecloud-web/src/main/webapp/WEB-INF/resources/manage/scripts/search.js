var Search = function () {

    return {
        //main function to initiate the module
        init: function () {
            if (jQuery().datepicker) {
                $('.date-picker').datepicker();
            }

            App.initFancybox();
        }

    };

}();