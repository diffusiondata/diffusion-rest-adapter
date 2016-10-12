
require('reflect-metadata');

var cec = require('../../../target/js/create-endpoint.component'),
    when = require('saywhen');

describe('Create endpoint component', function() {
    var activeService;
    var activeRoute;
    var modelService;
    var router;
    var component;

    beforeEach(function() {
        activeService = {
            name: 'service0'
        };
        activeRoute = {
            params: [activeService]
        };
        modelService = jasmine.createSpyObj('modelService', ['createEndpoint']);

        component = new cec.CreateEndpointComponent(modelService, activeRoute);
    });

    it('can be created', function() {
        expect(component).toBeDefined();
        expect(component.active).toBe(true);
        expect(component.route).toBe(activeRoute);
        expect(component.modelService).toBe(modelService);
        expect(component.endpoint.name).toBe('');
    });

    it('can be initialised', function() {
        component.ngOnInit();

        expect(component.serviceName).toBe('service0');
    });

    it('can create an endpoint', function() {
        component.ngOnInit();

        component.onCreateEndpoint();

        expect(modelService.createEndpoint).toHaveBeenCalled;
    });

    it('can be reset', function(done) {
        component.reset();

        expect(component.active).toBe(false);
        expect(component.endpoint.name).toBe('');

        setTimeout(function() {
            expect(component.active).toBe(true);
            done();
        }, 0);
    });
});
