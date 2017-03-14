
require('reflect-metadata');

var sdc = require('../../../target/js/service-detail.component'),
    when = require('saywhen'),
    p = require('when');

describe('Service detail component', function() {
    var router;
    var modelService;
    var activeRoute;
    var component;
    var model;
    var activeService;

    beforeEach(function() {
        router = jasmine.createSpyObj('router', ['navigate']);
        modelService = jasmine.createSpyObj('modelService', ['getModel', 'getService', 'deleteService']);
        model = {
            services: []
        };
        modelService.model = model;
        activeService = {
            name: 'service0'
        };
        activeRoute = {
            params: [activeService]
        };
        when(modelService.getService).isCalledWith('service0').thenReturn(p.Promise.resolve(activeService));
        when(modelService.deleteService).isCalled.thenReturn(p.Promise.resolve(null));

        component = new sdc.ServiceDetailComponent(router, modelService, activeRoute);
    });

    it('can be created', function() {
        expect(component).toBeDefined();
    });

    it('can be initialised', function(done) {
        component.ngOnInit();
        expect(modelService.getService).toHaveBeenCalledWith('service0');

        // Timeout needed to allow promise to invoke resolve handler
        setTimeout(function() {
            expect(component.service).toBe(activeService);
            done();
        }, 0);
    });

    it('can remove a service', function(done) {
        component.ngOnInit();

        // Timeout needed to allow promise to invoke resolve handler
        setTimeout(function() {
            component.onRemove('service0');

            // Timeout needed to allow promise to invoke resolve handler
            setTimeout(function() {
                expect(modelService.deleteService).toHaveBeenCalledWith('service0');
                expect(router.navigate).toHaveBeenCalledWith(['/home']);
                done();
            });
        }, 0);
    });
});
