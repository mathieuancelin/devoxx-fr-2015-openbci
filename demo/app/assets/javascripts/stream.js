(function() {

    var sparkLines = [
        createSparkline('line01'),
        createSparkline('line02'),
        createSparkline('line03'),
        createSparkline('line04'),
        createSparkline('line05'),
        createSparkline('line06'),
        createSparkline('line07'),
        createSparkline('line08'),
        createSparkline('line09'),
        createSparkline('line10'),
        createSparkline('line11')
    ];


    function createSparkline(name) {
        var serieLength = 500;
        var options = { height: '20px', width: '200px', fillColor: '#B9B9B9', lineColor: '#FFFFFF' };
        var dataSeries = _.map(_.range(serieLength), function() { return 0; });
        function updateSparkle(array) {
            _.each(array, function(blob) {
                dataSeries.push(blob[name]);
                if (dataSeries.length > serieLength) {
                    dataSeries.splice(0, 1);
                }
            });
            $('[data-sparknametitle="' + name + '"]').html(name);
            $('[data-sparkname="' + name + '"]').sparkline(dataSeries, options);
        }
        $('#sparklines').append('<span class="spklinelabel" data-sparknametitle="' + name + '"></span> ');
        $('#sparklines').append('<div class="spkline" data-sparkname="' + name + '"></div><span data-sparknamepeak="' + name + '" class="label-default">peak</span><br/>');
        return updateSparkle;
    }

    var feed = new EventSource('/feed');
    var wait = false;
    var pause = true;

    feed.onmessage = function ( e ) {
        var data = JSON.parse ( e.data );
        if (data.peak) {
            if (data.peak === 'on') {
                $('[data-sparknamepeak="' + data.name + '"]').attr('class', 'label-danger');
                if (data.name === "line03" && !wait) {
                    if (!pause) Reveal.next();
                    wait = true;
                    setTimeout(function() {
                        wait = false;
                    }, 2000);
                }
            } else {
                $('[data-sparknamepeak="' + data.name + '"]').attr('class', 'label-default');
                if (data.name === "line05" && !wait) {
                    if (!pause) Reveal.prev();
                    wait = true;
                    setTimeout(function() {
                        wait = false;
                    }, 2000);
                }
            }
        } else {
            _.each(sparkLines, function (line) {
                line(data);
            });
        }
    };

    $('#start').click(function(e) {
        e.preventDefault();
        pause = false;
        $.post('/start', function() {});
    });

    $('#live').click(function(e) {
        e.preventDefault();
        pause = false;
        $.post('/live', function() {});
    });

    $('#stop').click(function(e) {
        e.preventDefault();
        pause = true;
        $.post('/stop', function() {});
    });
    $('#pause').click(function(e) {
        e.preventDefault();
        pause = !pause;
        if (pause) {
            $('#pause').html('Unpause');
        } else {
            $('#pause').html('Pause');
        }
    });

    $('body').on('keydown', function(e) {
        console.log(e.keyCode);
        if (e.keyCode === 37) {
            Reveal.prev();
        } else if (e.keyCode === 33) {
            Reveal.prev();
        } else if (e.keyCode === 39) {
            Reveal.next();
        } else if (e.keyCode === 34) {
            Reveal.next();
        } else if (e.keyCode === 32) {
            Reveal.next();
        }
    });
})();


