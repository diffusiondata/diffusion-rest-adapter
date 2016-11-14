import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

import { Model, Service } from './model';
import { ModelService } from './model.service';

@Component({
    selector: 'services-list',
    template: `<div class="col-md-3">
    <div class="panel panel-default">
        <div class="panel-body scroll-panel">
            <div class="list-group">
                <a *ngFor="let service of model.services" (click)="onSelect(service)" class="list-group-item"><span class="list-group-item-text">{{service.name}}</span></a>
                <a routerLink="/createService" class="list-group-item"><span class="list-group-item-text">Create new service</span></a>
            </div>
        </div>
    <div>
</div>`
})
export class ServicesListComponent implements OnInit {
    model: Model = {
        services: []
    };
    showCreateComponent = false;

    constructor(private router: Router, private modelService: ModelService) {
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
        });
    }
}
