import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

import { Model, Service } from './model';
import { ModelService } from './model.service';
import { ErrorService } from "./error.service";

@Component({
    selector: 'services-list',
    template: `<div class="col-md-3">
    <div class="panel panel-default">
        <div class="panel-heading">
            <h3 class="panel-title">Services</h3>
        </div>
        <ul class="list-group">
            <li *ngFor="let service of model.services" (click)="onSelect(service)" class="list-group-item"><span class="list-group-item-text">{{service.name}}</span></li>
            <li routerLink="/createService" class="list-group-item"><span class="list-group-item-text">Create new service</span></li>
        </ul>
        <ul class="list-group">
            <li routerLink="/metrics" class="list-group-item"><span class="list-group-item-text">Metrics</span></li>
        </ul>
    </div>
</div>`
})
export class ServicesListComponent implements OnInit {
    model: Model = {
        services: []
    };
    showCreateComponent = false;

    constructor(private router: Router, private modelService: ModelService, private errorService: ErrorService) {
        this.model = this.modelService.model;
    }

    onSelect(service: Service): void {
        let link = ['/service', service.name];
        this.router.navigate(link);
    }

    createService(): void {
        this.showCreateComponent = true;
    }

    ngOnInit(): void {
        this.modelService.getModel().then(model => {}, error => {
            console.error(error);
            this.errorService.onError('Failed to load services');
        });
    }
}
