var TableManaged = function () {
    return {
        //main function to initiate the module
        init: function () {
            
            if (!jQuery().dataTable) {
                return;
            }
            // begin first table
            $('#tableDataList').dataTable({
                "iDisplayLength": 100,
                "sPaginationType": "bootstrap",
                "oLanguage": {
                    "sLengthMenu": "_MENU_ records",
                    "oPaginate": {
                        "sPrevious": "Prev",
                        "sNext": "Next"
                    }
                },
            });
            //jQuery('.pagination>li:first-child').css("display","none");
            //jQuery('.pagination>li:last-child').css("display","none");
            jQuery('#tableDataList_wrapper>div:first-child').css("display","none");
            
        }

    };

}();