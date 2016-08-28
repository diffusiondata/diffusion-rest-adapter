import { ModuleWithProviders }  from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import { CreateServiceComponent } from './create-service';
import { ServiceDetailComponent } from './service-detail';
import { UnselectedComponent } from './unselected';

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
