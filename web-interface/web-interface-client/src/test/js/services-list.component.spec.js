
require('reflect-metadata');

var slc = require('../../../target/js/services-list.component'),
    when = require('saywhen');

describe('Services list component', function() {
    var router;
    var modelService;
    var component;
    var model;
    var errorService;
    var metricsService;

    beforeEach(function() {
        router = jasmine.createSpyObj('router', ['navigate']);
        modelService = jasmine.createSpyObj('modelService', ['getModel']);
        errorService = jasmine.createSpyObj('errorService', ['onError']);
        metricsService = jasmine.createSpyObj('metricsService', ['metricsReady']);
        model = {
            services: []
        };
        modelService.model = model;
        when(modelService.getModel).isCalled.thenReturn(Promise.resolve({}));
        when(metricsService.metricsReady).isCalled.thenReturn(Promise.resolve({}));

        component = new slc.ServicesListComponent(router, modelService, errorService, metricsService);
    });

    it('can be created', function() {
        expect(component).toBeDefined();
        expect(component.model).toBe(model);
        expect(component.showCreateComponent).toBe(false);
    });

    it('can be initialised', function() {
        component.ngOnInit();
        expect(modelService.getModel).toHaveBeenCalled();
    });

    it('can select a service', function() {
        var service = {
            name: 'service0'
        };

        component.onSelect(service);

        expect(router.navigate).toHaveBeenCalledWith(['/service', service.name]);
    });

    it('can display service creator', function() {
        var service = {
            name: 'service0'
        };

        component.createService();

        expect(component.showCreateComponent).toBe(true);
    });
});
