import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Params } from '@angular/router';

import { ModelService } from './model.service';
import { Endpoint } from './model';

@Component({
  selector: 'create-endpoint',
  template: `<div class="panel panel-default">
    <div class="panel-heading">
        <a role="button" href="#create-endpoint" data-toggle="collapse"><h3 class="panel-title">Create endpoint<span class="pull-right clickable fa fa-chevron-down"></span></h3></a>
    </div>
    <div class="panel-body collapse" id="create-endpoint">
        <form *ngIf="active" #createEndpointForm="ngForm" (ngSubmit)="onCreateEndpoint($event)" class="form-horizontal">
            <div class="form-group" [class.has-error]="!name.valid && !name.pristine">
                <label for="name" class="col-sm-2 control-label">Name</label>
                <div class="col-md-4">
                    <input id="name" required [(ngModel)]="endpoint.name" name="name" #name="ngModel" class="form-control">
                </div>
                <span class="help-block col-sm-4">The name is required to uniquely identify the endpoint</span>
            </div>
            <div class="form-group" [class.has-error]="!url.valid && !url.pristine">
                <label for="url" class="col-sm-2 control-label">URL</label>
                <div class="col-sm-4">
                    <input id="url" required [(ngModel)]="endpoint.url" name="url" #url="ngModel" class="form-control">
                </div>
                <span class="help-block col-sm-4">The host is required to to describe the location of the endpoint</span>
            </div>
            <div class="form-group" [class.has-error]="!topicPath.valid && !topicPath.pristine">
                <label for="topicPath" class="col-sm-2 control-label">Topic path</label>
                <div class="col-sm-4">
                    <input id="topicPath" required [(ngModel)]="endpoint.topicPath" name="topicPath" #topicPath="ngModel" class="form-control">
                </div>
                <span class="help-block col-sm-4">Indicates the Diffusion topic that the endpoint is published to</span>
            </div>
            <div class="form-group">
                <label for="produces" class="col-sm-2 control-label">Produces</label>
                <div class="col-sm-4">
                    <select id="produces" required [(ngModel)]="endpoint.produces" name="produces" class="form-control">
                        <option value="auto">Auto</option>
                        <option value="binary">Binary</option>
                        <option value="json">JSON</option>
                        <option value="string">String</option>
                    </select>
                </div>
                <span class="help-block col-sm-4">Indicates the type of response returned by the endpoint and the type of Diffusion topic it is published to</span>
            </div>
            <div class="form-group">
                <div class="col-sm-offset-2 col-sm-8">
                    <button class="btn btn-default" [disabled]="!createEndpointForm.form.valid" type="submit">Create endpoint</button>
                </div>
            </div>
        </form>
    </div>
</div>`
})
export class CreateEndpointComponent implements OnInit {
    active = true;
    serviceName: string;
    endpoint: Endpoint = {
        name: '',
        url: '',
        topicPath: '',
        produces: 'auto'
    };

    constructor(private modelService: ModelService, private route: ActivatedRoute) {}

    onCreateEndpoint(): void {
        try {
            this.modelService.createEndpoint(this.serviceName, this.endpoint);
            this.reset();
        }
        catch (e) {
            console.error(e);
        }
    }

    private reset(): void {
        this.active = false;
        this.endpoint = {
            name: '',
            url: '',
            topicPath: '',
            produces: 'auto'
        };
        setTimeout(() => this.active = true, 0);
    }

    ngOnInit(): void {
        this.route.params.forEach((params: Params) => {
            this.serviceName = params['name'];
        });
    }
}
