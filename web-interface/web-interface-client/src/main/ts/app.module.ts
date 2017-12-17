
import 'reflect-metadata';
import { NgModule, provide } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule } from '@angular/forms';

import { ServicesListComponent } from './nav-bar.component';
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
import { MetricsService } from './metrics.service';
import { StackService } from './stack.service';
import { ErrorService } from './error.service';
import { SessionEstablishmentGuard } from './session-establishment.guard';
import { EndpointTypePipe } from './endpoint-type.pipe';
import { DisplayErrorComponent } from "./display-error.component";
import { MetricsComponent } from "./metrics.component";

import { routing } from './app.routing';
import * as diffusion from 'diffusion';
import {ExploreServiceComponent} from "./explore-service.component";
import {ExploreEndpointComponent} from "./explore-endpoint.component";
const diffusionConfig: diffusion.SessionOptions = require('diffusionConfig');

@NgModule({
    imports: [ BrowserModule, FormsModule, routing ],
    providers: [
        provide('diffusion.config', {
            useValue: diffusionConfig
        }),
        DiffusionService,
        MetricsService,
        ModelService,
        StackService,
        ErrorService,
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
        DisplayErrorComponent,
        RootComponent,
        EndpointTypePipe,
        MetricsComponent,
        ExploreServiceComponent,
        ExploreEndpointComponent
    ],
    bootstrap: [ RootComponent ]
})
export class AppModule { }
