
require('reflect-metadata');

var es = require('../../../target/js/error.service');

describe('Error service', function() {
    var listener;
    var errorService;

    beforeEach(function() {
        listener = jasmine.createSpy();
        errorService = new es.ErrorService();
    });

    it('can be created', function() {
        expect(errorService).toBeDefined();
    });

    it('can have listeners added to it', function() {
        errorService.addErrorListener(listener);
        errorService.onError('Error');
        expect(listener).toHaveBeenCalledWith('Error');
    });

    it('can have listeners removed from it', function() {
        errorService.addErrorListener(listener);
        errorService.removeErrorListener(listener);
        errorService.onError('Error');
        expect(listener).not.toHaveBeenCalled();
    });
});
