
import 'reflect-metadata';
import { NgModule, provide } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule } from '@angular/forms';

import { ServicesListComponent } from './services-list.component';
import { CreateServiceComponent } from './create-service.component';
import { ServiceDetailComponent } from './service-detail.component';
import { EndpointsListComponent } from './endpoints-list.component';
import { CreateEndpointComponent } from './create-endpoint.component';
import { EndpointDetailComponent } from './endpoint-detail.component';
import { LoginComponent } from './login.component';
import { UnselectedComponent } from './unselected.component';
import { RootComponent } from './root.component';
import { DiffusionService } from './diffusion.service';
import { ModelService } from './model.service';
import { StackService } from './stack.service';
import { SessionEstablishmentGuard } from './session-establishment.guard';
import { EndpointTypePipe } from './endpoint-type.pipe';

import { routing } from './app.routing';
import * as diffusion from 'diffusion';
const diffusionConfig: diffusion.Options = require('diffusionConfig');

@NgModule({
    imports: [ BrowserModule, FormsModule, routing ],
    providers: [
        provide('diffusion.config', {
            useValue: diffusionConfig
        }),
        DiffusionService,
        ModelService,
        StackService,
        SessionEstablishmentGuard ],
    declarations: [
        ServicesListComponent,
        CreateServiceComponent,
        UnselectedComponent,
        ServiceDetailComponent,
        EndpointsListComponent,
        CreateEndpointComponent,
        EndpointDetailComponent,
        LoginComponent,
        RootComponent,
        EndpointTypePipe
    ],
    bootstrap: [ RootComponent ]
})
export class AppModule { }
