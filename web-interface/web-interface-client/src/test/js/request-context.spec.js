
var rc = require('../../../target/js/request-context'),
    when = require('saywhen'),
    diffusion = require('diffusion');

var jsonDataType = diffusion.datatypes.json();

describe('Request context', function() {
    var session;
    var requestContext;
    var onMessage;
    var onSendSuccess;
    var onSendFailure;

    beforeEach(function() {
        session = {
            messages : jasmine.createSpyObj('messages', ['listen', 'send'])
        };
        onMessage = when.captor();
        onSendSuccess = when.captor();
        onSendFailure = when.captor();

        var promise = jasmine.createSpyObj('promise', ['then']);
        when(promise.then).isCalledWith(onSendSuccess, onSendFailure);
        when(session.messages.send).isCalled.thenReturn(promise);
        when(session.messages.listen).isCalledWith('path', onMessage);

        requestContext = new rc.RequestContext(session, 'path');
    });

    it('can be created', function() {
        expect(session.messages.listen).toHaveBeenCalledWith('path', jasmine.anything());
        expect(onMessage.latest).toBeDefined();
    });

    it('can send a message', function() {
        var promise = requestContext.request({});

        expect(promise).toBeDefined();
        expect(session.messages.send).toHaveBeenCalledWith('path', jasmine.anything());
        expect(onSendSuccess.latest).toBeDefined();
        expect(onSendFailure.latest).toBeDefined();
    });

    it('can handle failing to send a message', function(done) {
        var promise = requestContext.request({});

        promise.then(done.fail, function() {
            done();
        });

        onSendFailure.latest();
    });

    it('can receive a response', function(done) {
        var promise = requestContext.request({});

        promise.then(function(result) {
            expect(result).toBe('hello');
            done();
        }, done.fail);

        onMessage.latest({
            content: jsonDataType.writeValue({
                id: 0,
                response: 'hello'
            })
        });
    });

    it('can handle a rejected request', function(done) {
        var promise = requestContext.request({});

        promise.then(done.fail, function(result) {
            expect(result.message).toBe('hello');
            done();
        });

        onMessage.latest({
            content: jsonDataType.writeValue({
                id: 0,
                error: 'hello'
            })
        });
    });

    it('can handle a bad response', function(done) {
        var promise = requestContext.request({});

        promise.then(done.fail, function(result) {
            expect(result.message).toBe('Badly formatted response');
            done();
        });

        onMessage.latest({
            content: jsonDataType.writeValue({
                id: 0
            })
        });
    });
});
