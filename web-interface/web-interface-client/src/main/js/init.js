
(function() {
    var diffusion = require('diffusion');
    var model = require('model');
    var reflect = require('reflect-metadata');
    var zone = require('zone.js');
    var view;

    function start() {
        console.log('Ready');

        var main = require('main');

        session
            .stream('?adapter/rest/model/store/')
            .asType(diffusion.datatypes.json())
            .on('value', function(path, specification, newValue, oldValue) {
                console.log(path, newValue);
            });
    }

    var started = false;
    function tryStart() {
        if (!started && setup && connected) {
            start();

            started = true;
        }
    }

    var connected = false;
    var session;
    function connect() {
        diffusion.connect({
            host : 'localhost',
            port : 8080
        }).then(function (newSession) {
            connected = true;
            session = newSession;
            session.subscribe('?adapter/rest/model/store/');

            tryStart();
        }, function() {
            setTimeout(connect, 5000);
        });
    }

    var setup = false;
    function ready() {
        if (setup) {
            return;
        }

        tryStart();

        setup = true;
    }

    if (document.readyState === 'complete') {
        setTimeout(ready, 0);
    }
    else if (document.addEventListener) {
        document.addEventListener('DOMContentLoaded', ready, false);
        window.addEventListener('load', ready, false);
    }
    else {
        document.attachEvent('onreadystatechange', readyStateChange);
        window.attachEvent('onload', ready);
    }

    connect();
})();
