import { ModuleWithProviders }  from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import { CreateServiceComponent } from './create-service.component';
import { ServiceDetailComponent } from './service-detail.component';
import { UnselectedComponent } from './unselected.component';

const appRoutes: Routes = [
    {
        path: '',
        component: UnselectedComponent
    },
    {
        path: 'createService',
        component: CreateServiceComponent
    },
    {
        path: 'service/:name',
        component: ServiceDetailComponent
    }
];

export const appRoutingProviders: any[] = [
];
export const routing: ModuleWithProviders = RouterModule.forRoot(appRoutes);
