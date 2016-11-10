
require('reflect-metadata');

var lc = require('../../../target/js/login.component'),
    when = require('saywhen');

describe('Login component', function() {
    var reappt = {
        host: 'example.reappt.io',
        port: 443,
        secure: true
    };
    var session = {};
    var router;
    var diffusionService;
    var component;

    beforeEach(function() {
        router = jasmine.createSpyObj('router', ['navigate']);
        diffusionService = jasmine.createSpyObj('diffusionService', ['createSession']);

        component = new lc.LoginComponent(router, diffusionService, reappt);
    });

    it('can be created', function() {
        expect(component).toBeDefined();
    });

    it('sends people to home when they get a session', function(done) {
        var promise = Promise.resolve(session);
        when(diffusionService.createSession).isCalled.thenReturn(promise);

        component.doLogin();
        expect(diffusionService.createSession).toHaveBeenCalled();

        promise.then(() => {
            expect(router.navigate).toHaveBeenCalledWith(['/home']);
            done();
        }, done.fail);
    });
});
