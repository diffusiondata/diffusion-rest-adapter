import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Params } from '@angular/router';

import { Service } from './model';
import { ModelService } from './model.service';

@Component({
  selector: 'service-detail',
  template: `<h3 *ngIf="service">{{service.name}}</h3>`
})
export class ServiceDetailComponent implements OnInit {
    service: Service;

    constructor(private modelService: ModelService, private route: ActivatedRoute) {}

    ngOnInit(): void {
        this.route.params.forEach((params: Params) => {
            let name: string = params['name'];
            this.modelService.getService(name).then(service => this.service = service);
        });
    }
}
