import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

import { Model, Service } from './model';
import { ModelService } from './model.service';
import { ErrorService } from "./error.service";
import {MetricsService} from "./metrics.service";

@Component({
    selector: 'services-list',
    template: `<div class="col-md-3">
    <div class="panel panel-default">
        <div class="panel-heading">
            <h3 class="panel-title">Manage Services</h3>
        </div>
        <ul class="list-group">
            <li *ngFor="let service of model.services" (click)="onSelect(service)" class="list-group-item"><span class="list-group-item-text">{{service.name}}</span></li>
            <li routerLink="/createService" class="list-group-item"><span class="list-group-item-text">Create new service</span></li>
        </ul>
    </div>
    <div *ngIf="model.services.length > 0" class="panel panel-default">
        <div class="panel-heading">
            <h3 class="panel-title">Explore Services</h3>
        </div>
        <ul class="list-group">
            <li *ngFor="let service of model.services" (click)="onExploreSelect(service)" class="list-group-item"><span class="list-group-item-text">{{service.name}}</span></li>
        </ul>
    </div>
    <div *ngIf="hasMetrics" class="panel panel-default">
        <div class="panel-heading">
            <h3 class="panel-title">Metrics</h3>
        </div>
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
    hasMetrics = false;

    constructor(
            private router: Router,
            private modelService: ModelService,
            private errorService: ErrorService,
            private metricsService: MetricsService) {
        this.model = this.modelService.model;
        metricsService
            .metricsReady()
            .then(() => {
                this.hasMetrics = true;
            }, (err) => {
                errorService.onError('Failed to detect if metrics are available, ' + err);
            });
    }

    onSelect(service: Service): void {
        let link = ['/service', service.name];
        this.router.navigate(link);
    }

    onExploreSelect(service: Service): void {
        let link = ['/explore', service.name];
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
