import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

import { Model, Service } from './model';
import { ModelService } from './model.service';

@Component({
    selector: 'services-list',
    template: `<div class="serviceList">
    <div>
        <button *ngFor="let service of model.services" (click)="onSelect(service)">{{service.name}}</button>
        <button routerLink="/createService">Create new service</button>
    </div>
</div><router-outlet></router-outlet>`,
    providers: [ModelService]
})
export class ServicesListComponent implements OnInit {
    model: Model = {
        services: []
    };
    showCreateComponent = false;
    selectedService: Service = null;

    constructor(private router: Router, private modelService: ModelService) {}

    onSelect(service: Service): void {
        let link = ['/service', service.name];
        this.router.navigate(link);
    }

    createService(): void {
        this.selectedService = null;
        this.showCreateComponent = true;
    }

    updateMode(): void {
        this.modelService.getModel().then(model => this.model = model)
    }

    ngOnInit(): void {
        this.updateMode();
    }
}
