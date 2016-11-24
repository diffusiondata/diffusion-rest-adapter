import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Params } from '@angular/router';

import { Model, Service } from './model';
import { ModelService } from './model.service';

@Component({
    selector: 'endpoints-list',
    template: `<div class="panel panel-default">
    <div class="panel-heading">Endpoints</div>
    <div class="panel-body">
        <div *ngIf="service" class="list-group">
            <div *ngFor="let endpoint of service.endpoints" class="list-group-item">
                <endpoint-detail [service]="service" [endpoint]="endpoint"></endpoint-detail>
            </div>
            <div class="list-group-item">
                <create-endpoint></create-endpoint>
            </div>
        </div>
    </div>
</div>`
})
export class EndpointsListComponent implements OnInit {
    service: Service;

    constructor(private modelService: ModelService, private route: ActivatedRoute) {
    }

    ngOnInit(): void {
        this.route.params.forEach((params: Params) => {
            let name: string = params['name'];
            this.modelService.getService(name).then(service => this.service = service);
        });
    }
}
