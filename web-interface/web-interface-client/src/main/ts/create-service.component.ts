import { Component } from '@angular/core';
import { ModelService } from './model.service';
import { Router } from '@angular/router';

import { Service } from './model';

@Component({
  selector: 'create-service',
  template: `<services-list></services-list>
<div class="col-md-8">
    <div class="row">
        <div class="col-md-12">
            <h3>Create service</h3>
            <form *ngIf="active" #createServiceForm="ngForm" (ngSubmit)="onCreateService($event)" class="form-horizontal">
                <div class="form-group">
                    <label for="name" class="col-sm-2 control-label">Name</label>
                    <div class="col-sm-10">
                        <input id="name" required [(ngModel)]="service.name" name="name" #name="ngModel">
                        <div [hidden]="name.valid || name.pristine"
                             class="alert alert-danger">
                          Name is required
                        </div>
                    </div>
                </div>
                <div class="form-group">
                    <label for="host" class="col-sm-2 control-label">Host</label>
                    <div class="col-sm-10">
                    <input id="host" required [(ngModel)]="service.host" name="host" #host="ngModel">
                    <div [hidden]="host.valid || host.pristine"
                         class="alert alert-danger">
                      Host is required
                    </div>
                    </div>
                </div>
                <div class="form-group">
                    <label for="port" class="col-sm-2 control-label">Port</label>
                    <div class="col-sm-10">
                        <input id="port" required [(ngModel)]="service.port" name="port" #port="ngModel">
                        <div [hidden]="port.valid || port.pristine"
                             class="alert alert-danger">
                          Port is required
                        </div>
                    </div>
                </div>
                <div class="form-group">
                    <label for="secure" class="col-sm-2 control-label">Secure</label>
                    <div class="col-sm-10">
                        <select id="secure" required [(ngModel)]="service.secure" name="secure">
                            <option value="true">Secure</option>
                            <option value="false">Insecure</option>
                        </select>
                    </div>
                </div>
                <div class="form-group">
                    <label for="pollPeriod" class="col-sm-2 control-label">Poll period</label>
                    <div class="col-sm-10">
                        <input id="pollPeriod" required [(ngModel)]="service.pollPeriod" name="pollPeriod" #pollPeriod="ngModel">
                        <div [hidden]="pollPeriod.valid || pollPeriod.pristine"
                             class="alert alert-danger">
                          Poll period is required
                        </div>
                    </div>
                </div>
                <div class="form-group">
                    <label for="topicRoot" class="col-sm-2 control-label">Topic root</label>
                    <div class="col-sm-10">
                        <input id="topicRoot" required [(ngModel)]="service.topicRoot" name="topicRoot" #topicRoot="ngModel">
                        <div [hidden]="name.valid || name.pristine"
                             class="alert alert-danger">
                          Topic root is required
                        </div>
                    </div>
                </div>
                <div>
                    <button class="btn btn-default" [disabled]="!createServiceForm.form.valid" type="submit">Create service</button>
                </div>
            </form>
        </div>
    </div>
</div>`
})
export class CreateServiceComponent {
    active = true;
    service: any = {
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

    onCreateService(): void {
        // Fix types from data entry
        this.service.port = parseInt(this.service.port);
        this.service.pollPeriod = parseInt(this.service.pollPeriod);
        this.service.secure = this.service.secure === true || this.service.secure === "true";

        try {
            this.modelService.createService(this.service);
            this.reset();
        }
        catch (e) {
            console.error(e);
        }
    }

    private reset(): void {
        this.active = false;
        this.service = {
            name: null,
            host: null,
            port: null,
            secure: false,
            endpoints: [],
            pollPeriod: null,
            topicRoot: null,
            security: null
        };
        setTimeout(() => this.active = true, 0);
    }
}
