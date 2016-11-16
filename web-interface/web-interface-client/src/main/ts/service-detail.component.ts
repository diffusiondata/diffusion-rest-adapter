import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Params, Router } from '@angular/router';

import { Service } from './model';
import { ModelService } from './model.service';

@Component({
  selector: 'service-detail',
  template: `<services-list></services-list>
<div class="row">
    <div class="col-md-8">
        <div *ngIf="service" class="form-horizontal">
            <div class="form-group">
                <h3>{{service.name}}</h3>
            </div>
            <div class="form-group">
                <label for="host" class="col-sm-2 control-label">Host</label>
                <p id="host" class="form-control-static col-sm-4">{{service.host}}</p>
            </div>
            <div class="form-group">
                <label for="port" class="col-sm-2 control-label">Port</label>
                <p id="port" class="form-control-static col-sm-4">{{service.port}}</p>
            </div>
            <div class="form-group">
                <label for="secure" class="col-sm-2 control-label">Secure</label>
                <p id="secure" class="form-control-static col-sm-4">{{service.secure}}</p>
            </div>
            <div class="form-group">
                <label for="pollPeriod" class="col-sm-2 control-label">Poll period</label>
                <p id="pollPeriod" class="form-control-static col-sm-4">{{service.pollPeriod}}</p>
            </div>
            <div class="form-group">
                <label for="topicPathRoot" class="col-sm-2 control-label">Topic path root</label>
                <p id="topicPathRoot" class="form-control-static col-sm-4">{{service.topicPathRoot}}</p>
            </div>
            <div class="form-group">
                <div class="col-sm-offset-2 col-sm-8">
                    <button class="btn btn-default" (click)="onRemove()">Remove service</button>
                </div>
            </div>
            <div class="form-group">
                <endpoints-list></endpoints-list>
            </div>
        </div>
    </div>
</div>`
})
export class ServiceDetailComponent implements OnInit {
    private service: Service;

    constructor(private router: Router, private modelService: ModelService, private route: ActivatedRoute) {}

    ngOnInit(): void {
        this.route.params.forEach((params: Params) => {
            let name: string = params['name'];
            this.modelService.getService(name).then(service => this.service = service);
        });
    }

    onRemove() {
        this.modelService.deleteService(this.service.name);
        this.router.navigate(['/home']);
    }
}
