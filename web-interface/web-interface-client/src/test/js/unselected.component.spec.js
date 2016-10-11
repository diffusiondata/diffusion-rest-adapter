
require('reflect-metadata');

var uc = require('../../../target/js/unselected.component');

describe('Unselected component', function() {
    it('can be created', function() {
        var component = new uc.UnselectedComponent();
        expect(component).toBeDefined();
    });
});
