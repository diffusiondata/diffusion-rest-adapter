
require('reflect-metadata');

var ms = require('../../../target/js/model.service'),
    when = require('saywhen');

describe('Model service', function() {
    var diffusionService;
    var onConnectSuccess;
    var onConnectFailure;
    var session;
    var onMessage;
    var onSendSuccess;
    var onSendFailure;
    var modelService;


    beforeEach(function() {
        diffusionService = jasmine.createSpyObj('diffusionService', ['get']);
        onConnectSuccess = when.captor();
        onConnectFailure = when.captor();

        var sessionPromise = jasmine.createSpyObj('promise', ['then']);
        when(sessionPromise.then).isCalledWith(onConnectSuccess).thenReturn(sessionPromise);
        when(sessionPromise.then).isCalledWith(onConnectSuccess, onConnectFailure).thenReturn(sessionPromise);
        when(diffusionService.get).isCalled.thenReturn(sessionPromise);

        session = {
            messages : jasmine.createSpyObj('messages', ['listen', 'send'])
        };
        onMessage = when.captor();
        onSendSuccess = when.captor();
        onSendFailure = when.captor();

        var sendPromise = jasmine.createSpyObj('promise', ['then']);
        when(sendPromise.then).isCalledWith(onSendSuccess).thenReturn(sendPromise);
        when(sendPromise.then).isCalledWith(onSendSuccess, onSendFailure).thenReturn(sendPromise);
        when(session.messages.send).isCalled.thenReturn(sendPromise);
        when(session.messages.listen).isCalledWith('adapter/rest/model/store', onMessage);

        modelService = new ms.ModelService(diffusionService);
    });

    it('can be created', function() {
        expect(modelService).toBeDefined();
    });

    it('can lookup model', function(done) {
        var modelPromise = modelService.getModel();
        expect(diffusionService.get).toHaveBeenCalled();
        expect(modelPromise).toBeDefined();

        console.log(onConnectSuccess.values());

        onConnectSuccess.values()[0](session); // init
        onConnectSuccess.values()[1](); // make request

        expect(session.messages.send).toHaveBeenCalledWith('adapter/rest/model/store', jasmine.anything());
        expect(onSendSuccess.latest).toBeDefined();
        expect(onSendFailure.latest).toBeDefined();

        onConnectSuccess.latest([ 'service0' ]);

        modelPromise.then(function(result) {
            done();
        }, done.fail);

        onConnectSuccess.latest({});

    });
});
