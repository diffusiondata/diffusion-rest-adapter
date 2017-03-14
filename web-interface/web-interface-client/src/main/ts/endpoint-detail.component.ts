import { Component, Input } from '@angular/core';

import { ModelService } from './model.service';
import { ErrorService } from './error.service';
import { Service, Endpoint } from './model';

@Component({
    selector: 'endpoint-detail',
    template: `<div class="panel panel-default">
    <div class="panel-heading">
        <a role="button" href="#endpoint-{{endpointIndex}}" data-toggle="collapse"><h3 class="panel-title">{{endpoint.name}}<span class="pull-right clickable fa fa-chevron-down"></span></h3></a>
    </div>
    <div class="panel-body collapse" id="endpoint-{{endpointIndex}}">
        <div class="form-horizontal">
            <div class="form-group">
                <label for="url" class="col-sm-2 control-label">URL</label>
                <p id="url" class="form-control-static col-sm-4">{{endpoint.url}}</p>
            </div>
            <div class="form-group">
                <label for="topicPath" class="col-sm-2 control-label">Topic path</label>
                <p id="topicPath" class="form-control-static col-sm-4">{{endpoint.topicPath}}</p>
            </div>
            <div class="form-group">
                <label for="produces" class="col-sm-2 control-label">Produces</label>
                <p id="produces" class="form-control-static col-sm-4">{{endpoint.produces | asEndpointType}}</p>
            </div>
            <div class="form-group">
                <div class="col-sm-offset-2 col-sm-8">
                    <button class="btn btn-default" (click)="onRemove()">Remove endpoint</button>
                </div>
            </div>
        </div>
    </div>
</div>`
})
export class EndpointDetailComponent {
    @Input() service: Service;
    @Input() endpoint: Endpoint;
    @Input() endpointIndex: Number;

    constructor(private modelService: ModelService, private errorService: ErrorService) {
    }

    onRemove() {
        this.modelService
            .deleteEndpoint(this.service.name, this.endpoint.name)
            .catch(() => this.errorService.onError('Failed to remove endpoint from service'));
    }
}
