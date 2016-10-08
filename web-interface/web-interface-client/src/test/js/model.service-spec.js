
require('reflect-metadata');

var ms = require('../../../target/js/model.service'),
    when = require('saywhen'),
    diffusion = require('diffusion');

var jsonDataType = diffusion.datatypes.json();

describe('Model service', function() {
    var diffusionService;
    var session;
    var onMessage;
    var onSendSuccess;
    var onSendFailure;
    var modelService;

    beforeEach(function() {
        diffusionService = jasmine.createSpyObj('diffusionService', ['get']);
        onConnectSuccess = when.captor();
        onConnectFailure = when.captor();

        session = {
            messages : jasmine.createSpyObj('messages', ['listen', 'send'])
        };
        onMessage = when.captor();
        onSendSuccess = when.captor();
        onSendFailure = when.captor();

        when(session.messages.send).isCalled.then(function() {
            onMessage.latest({
                content: jsonDataType.writeValue({
                    id: 0,
                    response: [{
                        name: 'service0'
                    }]
                })
            });
        });
        when(session.messages.listen).isCalledWith('adapter/rest/model/store', onMessage);

        when(diffusionService.get).isCalled.thenReturn(Promise.resolve(session));

        modelService = new ms.ModelService(diffusionService);
    });

    it('can be created', function() {
        expect(modelService).toBeDefined();
    });

    it('can lookup model', function(done) {
        var modelPromise = modelService.getModel();
        expect(diffusionService.get).toHaveBeenCalled();
        expect(modelPromise).toBeDefined();

        modelPromise.then(function(model) {
            expect(session.messages.send).toHaveBeenCalledWith('adapter/rest/model/store', jasmine.anything());
            expect(model.services[0].name).toBe('service0');
            done();
        }, done.fail);
    });
});
