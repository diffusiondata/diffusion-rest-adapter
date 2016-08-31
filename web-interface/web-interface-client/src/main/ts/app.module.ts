import { NgModule }      from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule }   from '@angular/forms';

import { ServicesListComponent }  from './services-list';
import { ServiceModalComponent }  from './service.modal';
import { CreateServiceComponent }  from './create-service';
import { ServiceDetailComponent }  from './service-detail';
import { UnselectedComponent }  from './unselected';
import { ModelService } from './model.service';

import { routing } from './app.routing';

@NgModule({
    imports: [ BrowserModule, FormsModule, routing ],
    providers: [ModelService],
    declarations: [
        ServicesListComponent,
        ServiceModalComponent,
        CreateServiceComponent,
        UnselectedComponent,
        ServiceDetailComponent
    ],
    bootstrap: [ ServicesListComponent, ServiceModalComponent ]
})
export class AppModule { }
