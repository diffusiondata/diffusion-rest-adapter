
require('reflect-metadata');

var dec = require('../../../target/js/display-error.component');

describe('Endpoints list component', function() {
    var errorService;
    var component;

    beforeEach(function() {
        errorService = jasmine.createSpyObj('errorService', ['addErrorListener', 'removeErrorListener']);

        component = new dec.DisplayErrorComponent(errorService);
    });

    it('can be created', function() {
        expect(component).toBeDefined();
    });

    it('can be initialised', function() {
        component.ngOnInit();
        expect(errorService.addErrorListener).toHaveBeenCalled();
    });

    it('can be destroyed', function() {
        component.ngOnDestroy();
        expect(errorService.removeErrorListener).toHaveBeenCalled();
    });
});
