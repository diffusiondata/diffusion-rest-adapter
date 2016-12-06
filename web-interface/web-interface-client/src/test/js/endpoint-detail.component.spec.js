
require('reflect-metadata');

var edc = require('../../../target/js/endpoint-detail.component');

describe('Endpoint detail component', function() {
    var modelService;
    var component;

    beforeEach(function() {
        modelService = jasmine.createSpyObj('modelService', ['deleteEndpoint']);

        component = new edc.EndpointDetailComponent(modelService);
        // Injected by Angular
        component.service = {
            name: 'service0'
        };
        component.endpoint = {
            name: 'endpoint0'
        };
    });

    it('can be created', function() {
        expect(component).toBeDefined();
    });

    it('can remove the endpoint', function() {
        component.onRemove('service0', 'endpoint0');

        expect(modelService.deleteEndpoint).toHaveBeenCalledWith('service0', 'endpoint0');
    });
});
