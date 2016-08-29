import { Component } from '@angular/core';
import { ModelService } from './model.service';
import { Router } from '@angular/router';

import { Service } from './model';

@Component({
  selector: 'create-service',
  template: `<h3>Create service</h3>
<form #createServiceForm="ngForm" (ngSubmit)="onCreateService()">
    <div class="form-group">
        <label for="name">Name</label>
        <input id="name" required [(ngModel)]="service.name" name="name" #name="ngModel">
        <div [hidden]="name.valid || name.pristine"
             class="alert alert-danger">
          Name is required
        </div>
    </div>
    <div class="form-group">
        <label for="host">Host</label>
        <input id="host" required [(ngModel)]="service.host" name="host" #host="ngModel">
        <div [hidden]="host.valid || host.pristine"
             class="alert alert-danger">
          Host is required
        </div>
    </div>
    <div class="form-group">
        <label for="port">Port</label>
        <input id="port" required [(ngModel)]="service.port" name="port" #port="ngModel">
        <div [hidden]="port.valid || port.pristine"
             class="alert alert-danger">
          Port is required
        </div>
    </div>
    <div class="form-group">
        <label for="secure">Secure</label>
        <select id="secure" required [(ngModel)]="service.secure" name="secure">
            <option value="true">Secure</option>
            <option value="false">Insecure</option>
        </select>
    </div>
    <div class="form-group">
        <label for="pollPeriod">Poll period</label>
        <input id="pollPeriod" required [(ngModel)]="service.pollPeriod" name="pollPeriod" #pollPeriod="ngModel">
        <div [hidden]="pollPeriod.valid || pollPeriod.pristine"
             class="alert alert-danger">
          Poll period is required
        </div>
    </div>
    <div class="form-group">
        <label for="topicRoot">Topic root</label>
        <input id="topicRoot" required [(ngModel)]="service.topicRoot" name="topicRoot" #topicRoot="ngModel">
        <div [hidden]="name.valid || name.pristine"
             class="alert alert-danger">
          Topic root is required
        </div>
    </div>
    <div>
        <button class="btn btn-default" [disabled]="!createServiceForm.form.valid" type="submit">Create service</button>
    </div>
</form>`
})
export class CreateServiceComponent {
    service: Service = {
        name: null,
        host: null,
        port: null,
        secure: false,
        endpoints: [],
        pollPeriod: null,
        topicRoot: null,
        security: null
    };

    constructor(private router: Router, private modelService: ModelService) {}

    onCreateService(): boolean {
        this.modelService.createService(this.service);
        return false;
    }
}
