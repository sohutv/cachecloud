var MapsVector = function () {

    var setMap = function (name) {
        var data = {
            map: 'world_en',
            backgroundColor: null,
            borderColor: '#333333',
            borderOpacity: 0.5,
            borderWidth: 1,
            color: '#c6c6c6',
            enableZoom: true,
            hoverColor: '#c9dfaf',
            hoverOpacity: null,
            values: sample_data,
            normalizeFunction: 'linear',
            scaleColors: ['#b6da93', '#427d1a'],
            selectedColor: '#c9dfaf',
            selectedRegion: null,
            showTooltip: true,
            onRegionOver: function (event, code) {
                //sample to interact with map
                if (code == 'ca') {
                    event.preventDefault();
                }
            },
            onRegionClick: function (element, code, region) {
                //sample to interact with map
                var message = 'You clicked "' + region + '" which has the code: ' + code.toUpperCase();
                alert(message);
            }
        };

        data.map = name + '_en';
        var map = jQuery('#vmap_' + name);
        if (!map) {
            return;
        }
        map.width(map.parent().width());
        map.vectorMap(data);
    }


    return {
        //main function to initiate map samples
        init: function () {
            setMap("world");
            setMap("usa");
            setMap("europe");
            setMap("russia");
            setMap("germany");

            // redraw maps on window or content resized 
            App.addResponsiveHandler(function(){
                setMap("world");
                setMap("usa");
                setMap("europe");
                setMap("russia");
                setMap("germany");
            });
        }

    };

}();