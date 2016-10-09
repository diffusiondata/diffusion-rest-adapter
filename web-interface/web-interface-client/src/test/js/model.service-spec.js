
require('reflect-metadata');

var ms = require('../../../target/js/model.service'),
    when = require('saywhen'),
    diffusion = require('diffusion');

var jsonDataType = diffusion.datatypes.json();

describe('Model service', function() {
    var diffusionService;
    var session;
    var onMessage;
    var modelService;

    beforeEach(function() {
        diffusionService = jasmine.createSpyObj('diffusionService', ['get']);
        onConnectSuccess = when.captor();
        onConnectFailure = when.captor();

        session = {
            messages : jasmine.createSpyObj('messages', ['listen', 'send'])
        };
        onMessage = when.captor();

        when(session.messages.listen).isCalledWith('adapter/rest/model/store', onMessage);
        when(diffusionService.get).isCalled.thenReturn(Promise.resolve(session));

        modelService = new ms.ModelService(diffusionService);
    });

    function respondWithSuccesfulModelLookup() {
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
    }

    function respondWithSuccess() {
        when(session.messages.send).isCalled.then(function() {
            onMessage.latest({
                content: jsonDataType.writeValue({
                    id: 0,
                    response: {}
                })
            });
        });
    }

    it('can be created', function() {
        expect(modelService).toBeDefined();
    });

    it('can lookup model', function(done) {
        respondWithSuccesfulModelLookup();

        var modelPromise = modelService.getModel();
        expect(diffusionService.get).toHaveBeenCalled();
        expect(modelPromise).toBeDefined();

        modelPromise.then(function(model) {
            expect(session.messages.send).toHaveBeenCalledWith('adapter/rest/model/store', jasmine.anything());
            expect(model.services[0].name).toBe('service0');
            done();
        }, done.fail);
    });

    it('can lookup a service', function(done) {
        respondWithSuccesfulModelLookup();

        var servicePromise = modelService.getService('service0');
        expect(diffusionService.get).toHaveBeenCalled();
        expect(servicePromise).toBeDefined();

        servicePromise.then(function(service) {
            expect(session.messages.send).toHaveBeenCalledWith('adapter/rest/model/store', jasmine.anything());
            expect(service.name).toBe('service0');
            done();
        }, done.fail);
    });

    it('can create a service', function(done) {
        respondWithSuccess();

        var createPromise = modelService.createService({
            name: 'service0'
        });
        expect(diffusionService.get).toHaveBeenCalled();
        expect(createPromise).toBeDefined();

        createPromise.then(function() {
            expect(session.messages.send).toHaveBeenCalledWith('adapter/rest/model/store', jasmine.anything());
            expect(modelService.model.services[0].name).toBe('service0');
            done();
        }, done.fail);
    });

    it('can create an endpoint', function(done) {
        respondWithSuccess();
        modelService.model.services[0] = {
            name: 'service0',
            endpoints: []
        };

        var createPromise = modelService.createEndpoint('service0', {
            name: 'endpoint0'
        });
        expect(diffusionService.get).toHaveBeenCalled();
        expect(createPromise).toBeDefined();

        createPromise.then(function() {
            expect(session.messages.send).toHaveBeenCalledWith('adapter/rest/model/store', jasmine.anything());
            expect(modelService.model.services[0].name).toBe('service0');
            done();
        }, done.fail);
    });

    it('can delete a service', function(done) {
        respondWithSuccess();
        modelService.model.services[0] = {
            name: 'service0',
            endpoints: []
        };

        var deletePromise = modelService.deleteService('service0');
        expect(diffusionService.get).toHaveBeenCalled();
        expect(deletePromise).toBeDefined();

        deletePromise.then(function() {
            expect(session.messages.send).toHaveBeenCalledWith('adapter/rest/model/store', jasmine.anything());
            expect(modelService.model.services.length).toBe(0);
            done();
        }, done.fail);
    });

    it('can delete an deleteEndpoint', function(done) {
        respondWithSuccess();
        modelService.model.services[0] = {
            name: 'service0',
            endpoints: [{
                name: 'endpoint0'
            }]
        };

        var deletePromise = modelService.deleteEndpoint('service0', 'endpoint0');
        expect(diffusionService.get).toHaveBeenCalled();
        expect(deletePromise).toBeDefined();

        deletePromise.then(function() {
            expect(session.messages.send).toHaveBeenCalledWith('adapter/rest/model/store', jasmine.anything());
            expect(modelService.model.services[0].endpoints.length).toBe(0);
            done();
        }, done.fail);
    });
});
