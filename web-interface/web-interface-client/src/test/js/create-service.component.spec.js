
require('reflect-metadata');

var csc = require('../../../target/js/create-service.component'),
    when = require('saywhen');

describe('Create service component', function() {
    var modelService;
    var router;
    var component;

    beforeEach(function() {
        router = jasmine.createSpyObj('router', ['navigate']);
        modelService = jasmine.createSpyObj('modelService', ['createService']);

        component = new csc.CreateServiceComponent(router, modelService);
    });

    it('can be created', function() {
        expect(component).toBeDefined();
        expect(component.active).toBe(true);
        expect(component.router).toBe(router);
        expect(component.modelService).toBe(modelService);
        expect(component.service.name).toBe(null);
    });

    it('can create a service', function() {
        component.onCreateService();

        expect(modelService.createService).toHaveBeenCalled;
    });

    it('can be reset', function(done) {
        component.reset();

        expect(component.active).toBe(false);
        expect(component.service.name).toBe(null);

        setTimeout(function() {
            expect(component.active).toBe(true);
            done();
        }, 0);
    });
});
