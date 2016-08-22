
(function() {
    var setup = false;
    function ready() {
        if (setup) {
            return;
        }

        // TODO: Init
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
})();
