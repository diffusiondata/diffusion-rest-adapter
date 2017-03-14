import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Params } from '@angular/router';

import { Service } from './model';
import { ModelService } from './model.service';
import { ErrorService } from './error.service';

@Component({
    selector: 'endpoints-list',
    template: `<div *ngIf="service">
    <endpoint-detail *ngFor="let endpoint of service.endpoints; let endpointIndex = index;" [service]="service" [endpoint]="endpoint" [endpointIndex]="endpointIndex"></endpoint-detail>
    <create-endpoint></create-endpoint>
</div>`
})
export class EndpointsListComponent implements OnInit {
    service: Service;

    constructor(private modelService: ModelService, private route: ActivatedRoute, private errorService: ErrorService) {
    }

    ngOnInit(): void {
        this.route.params.forEach((params: Params) => {
            let name: string = params['name'];
            this.modelService.getService(name)
                .then(service => this.service = service)
                .catch(() => this.errorService.onError('Failed to load endpoints for service'));
        });
    }
}
