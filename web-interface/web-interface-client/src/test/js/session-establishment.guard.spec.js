
require('reflect-metadata');

var seg = require('../../../target/js/session-establishment.guard'),
    when = require('saywhen');

describe('Session establishment guard', function() {
    var diffusionService;
    var router;
    var guard;

    beforeEach(function() {
        diffusionService = jasmine.createSpyObj('diffusionService', ['get']);
        router = jasmine.createSpyObj('router', ['navigate']);

        guard = new seg.SessionEstablishmentGuard(router, diffusionService);
    });

    it('can be created', function() {
        expect(guard).toBeDefined();
    });

    it('present login page', function() {
        expect(guard.canActivate()).toBe(false);
        expect(router.navigate).toHaveBeenCalled();
    });
});
