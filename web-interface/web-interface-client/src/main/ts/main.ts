
import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';
import { AppModule } from './app.module';
import { enableProdMode } from '@angular/core';
const diffusionConfig: diffusion.Options = require('diffusionConfig');

enableProdMode();
platformBrowserDynamic().bootstrapModule(AppModule);
