import {Component, Input, OnDestroy, OnInit} from '@angular/core';

import {Endpoint, Service} from './model';
import {DiffusionService} from "./diffusion.service";
import * as diffusion from 'diffusion';

@Component({
    selector: 'explore-endpoint',
    template: `<div class="form-group">
    <label class="col-sm-2 control-label">URL</label>
    <p id="url-{{endpointIndex}}" class="form-control-static col-sm-4">{{endpoint.url}}</p>
</div>
<div class="form-group">
    <label class="col-sm-2 control-label">Topic</label>
    <p id="topic-{{endpointIndex}}" class="form-control-static col-sm-4">{{service.topicPathRoot}}/{{endpoint.topicPath}}</p>
</div>
<div class="form-group">
    <label class="col-sm-2 control-label">Value</label>
    <textarea id="value-{{endpointIndex}}" class="form-control-static col-sm-4" readonly="readonly">{{value}}</textarea>
</div>`
})
export class ExploreEndpointComponent implements OnInit, OnDestroy {
    @Input() service: Service;
    @Input() endpoint: Endpoint;
    @Input() endpointIndex: Number;

    private stream;
    private value: String;

    constructor(private diffusionService: DiffusionService) {}

    ngOnInit(): void {
        this
            .diffusionService
            .get()
            .then(session => {
                this.stream = session
                    .stream(this.service.topicPathRoot + '/' + this.endpoint.topicPath)
                    .on('subscribe', spec => {
                        if (spec.type.id === diffusion.topics.TopicType.JSON.id) {
                            session
                                .stream(this.service.topicPathRoot + '/' + this.endpoint.topicPath)
                                .asType(diffusion.datatypes.json())
                                .on('value', (path, spec, newValue) => {
                                    this.value = JSON.stringify(newValue.get());
                                });
                        }
                        else if (spec.type.id === diffusion.topics.TopicType.STRING.id) {
                            session
                                .stream(this.service.topicPathRoot + '/' + this.endpoint.topicPath)
                                .asType(diffusion.datatypes.string())
                                .on('value', (path, spec, newValue) => {
                                    this.value = newValue;
                                });
                        }
                        else if (spec.type.id === diffusion.topics.TopicType.BINARY.id) {
                            session
                                .stream(this.service.topicPathRoot + '/' + this.endpoint.topicPath)
                                .asType(diffusion.datatypes.binary())
                                .on('value', (path, spec, newValue) => {
                                    this.value = newValue.get().toString('hex');
                                });
                        }
                    });
                session.subscribe(this.service.topicPathRoot + '/' + this.endpoint.topicPath);
            });
    }

    ngOnDestroy(): void {
        this.stream.close();
        this.diffusionService.get().then(session => session.unsubscribe(this.service.topicPathRoot + '/' + this.endpoint.topicPath));
    }
}
