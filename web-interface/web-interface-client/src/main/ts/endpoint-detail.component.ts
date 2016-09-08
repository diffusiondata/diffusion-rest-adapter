import { Component, Input } from '@angular/core';

import { Endpoint } from './model';

@Component({
    selector: 'endpoint-detail',
    template: `<div class="list-group-item">
    <div class="form-group">
        <label for="name" class="col-sm-2 control-label">Name</label>
        <p id="name" class="form-control-static">{{endpoint.name}}</p>
    </div>
    <div class="form-group">
        <label for="url" class="col-sm-2 control-label">URL</label>
        <p id="url" class="form-control-static">{{endpoint.url}}</p>
    </div>
    <div class="form-group">
        <label for="topic" class="col-sm-2 control-label">Topic</label>
        <p id="topic" class="form-control-static">{{endpoint.topic}}</p>
    </div>
    <div class="form-group">
        <label for="produces" class="col-sm-2 control-label">Produces</label>
        <p id="produces" class="form-control-static">{{endpoint.produces}}</p>
    </div>
</div>`
})
export class EndpointDetailComponent {
    @Input() endpoint: Endpoint;

    constructor() {
    }
}
