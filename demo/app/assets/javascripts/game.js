(function() {

    var feed = new EventSource('/feed');
    var wait = false;
    var pause = true;
    var x = 0;
    var y = 4;

    feed.onmessage = function ( e ) {
        var data = JSON.parse ( e.data );
        if (data.peak) {
            if (data.peak === 'on') {
                if (data.name === "line03" && !wait) {
                    console.log(data);
                    if (!pause) next();
                    wait = true;
                    setTimeout(function() {
                        wait = false;
                    }, 1000);
                }
                if (data.name === "line05" && !wait) {
                    console.log(data);
                    if (!pause) up();
                    wait = true;
                    setTimeout(function() {
                        wait = false;
                    }, 1000);
                }
            }
        }
    };

    function drawPoint() {
        if ($('[data-position="' + x + '-' + y + '"]').hasClass('end')) {
            $('.title').html('You win !!!!');
            $('.cell').removeClass('end');
        }
        $('.cell').removeClass('player');
        $('[data-position="' + x + '-' + y + '"]').addClass('player');
    }

    function up() {
        if (y > 0) {
            y--;
        }
        drawPoint();
    }

    function next() {
        if (x < 4) {
            x++;
        }
        drawPoint();
    }

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
        if (e.keyCode === 38) {
            up();
        } else if (e.keyCode === 33) {
            up();
        } else if (e.keyCode === 39) {
            next(); // space
        } else if (e.keyCode === 34) {
            next(); // command
        } else if (e.keyCode === 32) {
            next(); // command
        }
    });

    drawPoint();
})();


