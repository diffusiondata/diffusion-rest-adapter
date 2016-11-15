import { Component } from '@angular/core';
import { ModelService } from './model.service';
import { Router } from '@angular/router';

import { Service } from './model';

@Component({
  selector: 'create-service',
  template: `<services-list></services-list>
<div class="row">
    <div class="col-md-8">
        <form *ngIf="active" #createServiceForm="ngForm" (ngSubmit)="onCreateService($event)" class="form-horizontal">
            <div class="form-group">
                <h3>Create service</h3>
            </div>
            <div class="form-group" [class.has-error]="!name.valid && !name.pristine">
                <label for="name" class="col-sm-2 control-label">Name</label>
                <div class="col-sm-4">
                    <input id="name" required [(ngModel)]="service.name" name="name" #name="ngModel" class="form-control">
                </div>
                <span class="help-block col-sm-4">The name is required to uniquely identify the service</span>
            </div>
            <div class="form-group" [class.has-error]="!host.valid && !host.pristine">
                <label for="host" class="col-sm-2 control-label">Host</label>
                <div class="col-sm-4">
                    <input id="host" required [(ngModel)]="service.host" name="host" #host="ngModel" class="form-control">
                </div>
                <span class="help-block col-sm-4">The host is required to to describe the location of the service</span>
            </div>
            <div class="form-group" [class.has-error]="!port.valid && !port.pristine">
                <label for="port" class="col-sm-2 control-label">Port</label>
                <div class="col-sm-4">
                    <input id="port" required [(ngModel)]="service.port" name="port" #port="ngModel" class="form-control">
                </div>
                <span class="help-block col-sm-4">The port is required to to describe the location of the service</span>
            </div>
            <div class="form-group">
                <label for="secure" class="col-sm-2 control-label">Secure</label>
                <div class="col-sm-4">
                    <select id="secure" required [(ngModel)]="service.secure" name="secure" class="form-control">
                        <option selected="selected" value="true">Secure</option>
                        <option value="false">Insecure</option>
                    </select>
                </div>
                <span class="help-block col-sm-4">Indicates how to communicate with the service</span>
            </div>
            <div class="form-group" [class.has-error]="!pollPeriod.valid && !pollPeriod.pristine">
                <label for="pollPeriod" class="col-sm-2 control-label">Poll period</label>
                <div class="col-sm-4">
                    <input id="pollPeriod" required [(ngModel)]="service.pollPeriod" name="pollPeriod" #pollPeriod="ngModel" class="form-control">
                </div>
                <span class="help-block col-sm-4">Indicates how frequently the service endpoints should be polled</span>
            </div>
            <div class="form-group" [class.has-error]="!topicPathRoot.valid && !topicPathRoot.pristine">
                <label for="topicPathRoot" class="col-sm-2 control-label">Topic path root</label>
                <div class="col-sm-4">
                    <input id="topicPathRoot" required [(ngModel)]="service.topicPathRoot" name="topicPathRoot" #topicPathRoot="ngModel" class="form-control">
                </div>
                <span class="help-block col-sm-4">Indicates the Diffusion topic that the service is published under</span>
            </div>
            <div class="form-group">
                <div class="col-sm-offset-2 col-sm-8">
                    <button class="btn btn-default" [disabled]="!createServiceForm.form.valid" type="submit">Create service</button>
                </div>
            </div>
        </form>
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
        topicPathRoot: null,
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
            topicPathRoot: null,
            security: null
        };
        setTimeout(() => this.active = true, 0);
    }
}
