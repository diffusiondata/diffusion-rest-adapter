import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Params, Router } from '@angular/router';

import { Service } from './model';
import { ModelService } from './model.service';
import { ErrorService } from './error.service';

@Component({
    selector: 'explore-service',
    template: `<nav-bar></nav-bar>
<div class="col-md-8">
    <div *ngIf="service" class="panel panel-default">
        <div class="panel-heading">
            <h3 class="panel-title">{{service.name}}</h3>
        </div>
        <div class="panel-body">
            <div class="form-horizontal">
                <explore-endpoint *ngFor="let endpoint of service.endpoints; let endpointIndex = index;" [service]="service" [endpoint]="endpoint" [endpointIndex]="endpointIndex"></explore-endpoint>
            </div>
        </div>
    </div>
</div>`
})
export class ExploreServiceComponent implements OnInit {
    private service: Service;

    constructor(
        private router: Router,
        private modelService: ModelService,
        private route: ActivatedRoute,
        private errorService: ErrorService) {}

    ngOnInit(): void {
        this.route.params.forEach((params: Params) => {
            let name: string = params['name'];
            this.modelService
                .getService(name)
                .then(service => this.service = service)
                .catch(() => this.errorService.onError('Failed to load service detail'));
        });
    }
}
