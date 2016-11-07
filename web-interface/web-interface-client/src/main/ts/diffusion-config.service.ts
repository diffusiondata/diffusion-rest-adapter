import { Injectable, Inject } from '@angular/core';
import * as diffusion from 'diffusion';

@Injectable()
export class DiffusionConfigService {
    private options: Promise<any>;

    constructor(@Inject('diffusion.config') private diffusionConfig: diffusion.Options) {
        this.options = Promise.resolve(diffusionConfig);
    }

    get(): Promise<diffusion.Options> {
        return this.options;
    }
}
