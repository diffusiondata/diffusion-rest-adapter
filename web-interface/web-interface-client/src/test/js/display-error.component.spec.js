
require('reflect-metadata');

var dec = require('../../../target/js/display-error.component');

describe('Endpoints list component', function() {
    var errorService;
    var component;

    beforeEach(function() {
        errorService = jasmine.createSpyObj('errorService', ['addErrorListener', 'removeErrorListener']);
        global.scrollTo = jasmine.createSpy();

        component = new dec.DisplayErrorComponent(errorService);
    });

    it('can be created', function() {
        expect(component).toBeDefined();
        expect(component.hasError).toBe(false);
    });

    it('can be initialised', function() {
        component.ngOnInit();
        expect(errorService.addErrorListener).toHaveBeenCalled();
    });

    it('can be destroyed', function() {
        component.ngOnDestroy();
        expect(errorService.removeErrorListener).toHaveBeenCalled();
    });

    it('can be notified of errors', function () {
        component.setError('message');
        expect(component.hasError).toBe(true);
        expect(component.errorMessage).toBe('message');
        expect(global.scrollTo).toHaveBeenCalledWith(0, 0);
    });
});
