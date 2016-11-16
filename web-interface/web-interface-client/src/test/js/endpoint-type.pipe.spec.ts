
require('reflect-metadata');

var etp = require('../../../target/js/endpoints-type.pipe'),
    when = require('saywhen');

describe('Endpoint type pipe', function() {
    var pipe;

    beforeEach(function() {
        pipe = new etp.EndpointTypePipe();
    });

    it('can be created', function() {
        expect(pipe).toBeDefined();
    });

    it('transforms json', function() {
        expect(pipe.transform('json')).toBe('JSON');
    });

    it('transforms binary', function() {
        expect(pipe.transform('binary')).toBe('Binary');
    });

    it('transforms string', function() {
        expect(pipe.transform('string')).toBe('String');
    });
});
