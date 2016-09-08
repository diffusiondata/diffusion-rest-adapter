import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Params } from '@angular/router';

import { ModelService } from './model.service';
import { Endpoint } from './model';

@Component({
  selector: 'create-endpoint',
  template: `<div class="list-group-item">
    <form *ngIf="active" #createEndpointForm="ngForm" (ngSubmit)="onCreateEndpoint($event)" class="form-horizontal">
        <div>
            <div class="form-group">
                <label for="name" class="col-sm-2 control-label">Name</label>
                <div class="col-sm-10">
                    <input id="name" required [(ngModel)]="endpoint.name" name="name" #name="ngModel">
                    <div [hidden]="name.valid || name.pristine"
                         class="alert alert-danger">
                      Name is required
                    </div>
                </div>
            </div>
            <div class="form-group">
                <label for="url" class="col-sm-2 control-label">URL</label>
                <div class="col-sm-10">
                    <input id="url" required [(ngModel)]="endpoint.url" name="url" #url="ngModel">
                    <div [hidden]="url.valid || url.pristine"
                         class="alert alert-danger">
                      URL is required
                    </div>
                </div>
            </div>
            <div class="form-group">
                <label for="topic" class="col-sm-2 control-label">Topic</label>
                <div class="col-sm-10">
                    <input id="topic" required [(ngModel)]="endpoint.topic" name="topic" #topic="ngModel">
                    <div [hidden]="topic.valid || topic.pristine"
                         class="alert alert-danger">
                      Topic is required
                    </div>
                </div>
            </div>
            <div class="form-group">
                <label for="produces" class="col-sm-2 control-label">Produces</label>
                <div class="col-sm-10">
                    <select id="produces" required [(ngModel)]="endpoint.produces" name="produces">
                        <option value="json">JSON</option>
                        <option value="binary">Binary</option>
                        <option value="string">String</option>
                    </select>
                </div>
            </div>
            <button class="btn btn-default" [disabled]="!createEndpointForm.form.valid" type="submit">Create endpoint</button>
        </div>
    </form>
</div>`
})
export class CreateEndpointComponent implements OnInit {
    active = true;
    serviceName: string;
    endpoint: Endpoint = {
        name: '',
        url: '',
        topic: '',
        produces: 'binary'
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
            topic: '',
            produces: 'binary'
        };
        setTimeout(() => this.active = true, 0);
    }

    ngOnInit(): void {
        this.route.params.forEach((params: Params) => {
            this.serviceName = params['name'];
        });
    }
}
