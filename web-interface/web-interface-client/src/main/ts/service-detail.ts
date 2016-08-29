import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Params } from '@angular/router';

import { Service } from './model';
import { ModelService } from './model.service';

@Component({
  selector: 'service-detail',
  template: `<div *ngIf="service">
    <h3>{{service.name}}</h3>
    <div><span>Host</span><span>{{service.host}}</span></div>
    <div><span>Port</span><span>{{service.port}}</span></div>
    <div><span>Secure</span><span>{{service.secure}}</span></div>
    <div><span>Poll period</span><span>{{service.pollPeriod}}</span></div>
    <div><span>Topic root</span><span>{{service.topicRoot}}</span></div>
</div>`
})
export class ServiceDetailComponent implements OnInit {
    private service: Service;

    constructor(private modelService: ModelService, private route: ActivatedRoute) {}

    ngOnInit(): void {
        this.route.params.forEach((params: Params) => {
            let name: string = params['name'];
            this.modelService.getService(name).then(service => this.service = service);
        });
    }
}
