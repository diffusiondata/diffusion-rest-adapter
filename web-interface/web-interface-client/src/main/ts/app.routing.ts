import { ModuleWithProviders }  from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import { CreateServiceComponent } from './create-service.component';
import { ServiceDetailComponent } from './service-detail.component';
import { UnselectedComponent } from './unselected.component';
import { LoginComponent } from './login.component';
import { MetricsComponent } from "./metrics.component";

import { SessionEstablishmentGuard } from './session-establishment.guard';

const appRoutes: Routes = [
    {
        path: '',
        component: LoginComponent
    },
    {
        path: 'login',
        component: LoginComponent
    },
    {
        path: 'home',
        component: UnselectedComponent,
        canActivate: [SessionEstablishmentGuard]
    },
    {
        path: 'createService',
        component: CreateServiceComponent,
        canActivate: [SessionEstablishmentGuard]
    },
    {
        path: 'service/:name',
        component: ServiceDetailComponent,
        canActivate: [SessionEstablishmentGuard]
    },
    {
        path: 'metrics',
        component: MetricsComponent,
        canActivate: [SessionEstablishmentGuard]
    }
];

export const appRoutingProviders: any[] = [
];
export const routing: ModuleWithProviders = RouterModule.forRoot(appRoutes);
