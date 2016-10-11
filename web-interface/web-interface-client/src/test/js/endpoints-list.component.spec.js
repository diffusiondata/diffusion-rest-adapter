
require('reflect-metadata');

var elc = require('../../../target/js/endpoints-list.component'),
    when = require('saywhen');

describe('Endpoints list component', function() {
    var activeService;
    var activeRoute;
    var modelService;
    var component;

    beforeEach(function() {
        modelService = jasmine.createSpyObj('modelService', ['getService']);
        activeService = {
            name: 'service0'
        };
        when(modelService.getService).isCalled.thenReturn(Promise.resolve(activeService));
        activeRoute = {
            params: [activeService]
        };

        component = new elc.EndpointsListComponent(modelService, activeRoute);
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
});
