import { NgModule }      from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule }   from '@angular/forms';

import { ServicesListComponent }  from './services-list.component';
import { ServiceModalComponent }  from './service-modal.component';
import { CreateServiceComponent }  from './create-service.component';
import { ServiceDetailComponent }  from './service-detail.component';
import { UnselectedComponent }  from './unselected.component';
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
