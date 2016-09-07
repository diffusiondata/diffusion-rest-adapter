import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Params } from '@angular/router';

import { Service } from './model';
import { ModelService } from './model.service';

@Component({
  selector: 'service-detail',
  template: `<form *ngIf="service" class="form-horizontal">
    <h3>{{service.name}}</h3>
    <div class="form-group">
        <label for="host" class="col-sm-2 control-label">Host</label>
        <p id="host" class="form-control-static">{{service.host}}</p>
    </div>
    <div class="form-group">
        <label for="port" class="col-sm-2 control-label">Port</label>
        <p id="port" class="form-control-static">{{service.port}}</p>
    </div>
    <div class="form-group">
        <label for="secure" class="col-sm-2 control-label">Secure</label>
        <p id="secure" class="form-control-static">{{service.secure}}</p>
    </div>
    <div class="form-group">
        <label for="pollPeriod" class="col-sm-2 control-label">Poll period</label>
        <p id="pollPeriod" class="form-control-static">{{service.pollPeriod}}</p>
    </div>
    <div class="form-group">
        <label for="topicRoot" class="col-sm-2 control-label">Topic root</label>
        <p id="topicRoot" class="form-control-static">{{service.topicRoot}}</p>
    </div>
</form>`
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
