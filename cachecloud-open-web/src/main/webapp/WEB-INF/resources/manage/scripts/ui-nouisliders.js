var UINoUiSliders = function () {

    return {
        //main function to initiate the module
        init: function () {

            // slider 1
            $("#slider_1").noUiSlider({
                 start: [20, 80]
                ,range: [0, 100]
                ,connect: true
                ,handles: 2
            });

            // slider 2
            $('#slider_2').noUiSlider({
                     range: [-20,40]
                    ,start: [10,30]
                    ,handles: 2
                    ,connect: true
                    ,step: 1
                    ,serialization: {
                         to: [$('#slider_2_input_start'), $('#slider_2_input_end')]
                        ,resolution: 1
                }
            });

            // slider 3
            $("#slider_3").noUiSlider({
                 start: [20, 80]
                ,range: [0, 100]
                ,connect: true
                ,handles: 2
            });

            $("#slider_3_checkbox").change(function(){
                // If the checkbox is checked
                if ($(this).is(":checked")) {
                    // Disable the slider
                    $("#slider_3").attr("disabled", "disabled");
                } else {
                    // Enabled the slider
                    $("#slider_3").removeAttr("disabled");
                }
            });

            // slider 4
            $("#slider_4").noUiSlider({
                 start: [20, 80]
                ,range: [0, 100]
                ,connect: true
                ,handles: 2
            });

            $("#slider_4_btn").click(function(){
                alert($("#slider_4").val());
            });
        }

    };

}();