
require('reflect-metadata');

var dcs = require('../../../target/js/diffusion-config.service');

describe('Diffusion configuration service', function() {
    var diffusionConfigService;
    var diffusionConfig = {
    };

    beforeEach(function() {
        diffusionConfigService = new dcs.DiffusionConfigService(diffusionConfig);
    });

    it('can be created', function() {
        expect(diffusionConfigService).toBeDefined();
    });

    it('can return the initial config', function(done) {
        diffusionConfigService.get().then(function (config) {
            expect(config).toBe(diffusionConfig);
            done();
        }, done.fail);
    })
});
