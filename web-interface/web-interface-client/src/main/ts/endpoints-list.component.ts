import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Params } from '@angular/router';

import { Model, Service } from './model';
import { ModelService } from './model.service';

@Component({
    selector: 'endpoints-list',
    template: `<div>
    <div class="panel panel-default">
        <div class="panel-body scroll-panel">
            <div *ngIf="service" class="list-group">
                <endpoint-detail *ngFor="let endpoint of service.endpoints" [endpoint]="endpoint"></endpoint-detail>
                <create-endpoint></create-endpoint>
            </div>
        </div>
    <div>
</div>`
})
export class EndpointsListComponent implements OnInit {
    service: Service;
    showCreateComponent = false;

    constructor(private modelService: ModelService, private route: ActivatedRoute) {
    }

    ngOnInit(): void {
        this.route.params.forEach((params: Params) => {
            let name: string = params['name'];
            this.modelService.getService(name).then(service => this.service = service);
        });
    }
}
