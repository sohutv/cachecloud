var UIExtendedModals = function () {

    
    return {
        //main function to initiate the module
        init: function () {
        
            // general settings
            $.fn.modal.defaults.spinner = $.fn.modalmanager.defaults.spinner = 
              '<div class="loading-spinner" style="width: 200px; margin-left: -100px;">' +
                '<div class="progress progress-striped active">' +
                  '<div class="progress-bar" style="width: 100%;"></div>' +
                '</div>' +
              '</div>';

            $.fn.modalmanager.defaults.resize = true;

            //dynamic demo:
            $('.dynamic .demo').click(function(){
              var tmpl = [
                // tabindex is required for focus
                '<div class="modal hide fade" tabindex="-1">',
                  '<div class="modal-header">',
                    '<button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>',
                    '<h4 class="modal-title">Modal header</h4>', 
                  '</div>',
                  '<div class="modal-body">',
                    '<p>Test</p>',
                  '</div>',
                  '<div class="modal-footer">',
                    '<a href="#" data-dismiss="modal" class="btn btn-default">Close</a>',
                    '<a href="#" class="btn btn-primary">Save changes</a>',
                  '</div>',
                '</div>'
              ].join('');
              
              $(tmpl).modal();
            });

            //ajax demo:
            var $modal = $('#ajax-modal');

            $('#ajax-demo').on('click', function(){
              // create the backdrop and wait for next modal to be triggered
              $('body').modalmanager('loading');

              setTimeout(function(){
                  $modal.load('ui_extended_modals_ajax_sample.html', '', function(){
                  $modal.modal();
                });
              }, 1000);
            });

            $modal.on('click', '.update', function(){
              $modal.modal('loading');
              setTimeout(function(){
                $modal
                  .modal('loading')
                  .find('.modal-body')
                    .prepend('<div class="alert alert-info fade in">' +
                      'Updated!<button type="button" class="close" data-dismiss="alert">&times;</button>' +
                    '</div>');
              }, 1000);
            });
        }

    };

}();