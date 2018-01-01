import {Component, Input, OnDestroy, OnInit} from '@angular/core';

import {Endpoint, Service} from './model';
import {DiffusionService} from "./diffusion.service";
import * as diffusion from 'diffusion';
import {Stream} from "diffusion";

@Component({
    selector: 'explore-endpoint',
    template: `<div class="panel panel-default">
    <div class="panel-heading">
        <h3 class="panel-title">{{endpoint.name}}</h3>
    </div>
    <div class="panel-body">
        <div class="form-horizontal">
            <div class="form-group">
                <label class="col-sm-2 control-label">URL</label>
                <p id="url-{{endpointIndex}}" class="form-control-static col-sm-4">{{url}}</p>
            </div>
            <div class="form-group">
                <label class="col-sm-2 control-label">Topic</label>
                <p id="topic-{{endpointIndex}}" class="form-control-static col-sm-4">{{service.topicPathRoot}}/{{endpoint.topicPath}}</p>
            </div>
            <div class="form-group">
                <label class="col-sm-2 control-label">Value</label>
                <textarea id="value-{{endpointIndex}}" class="form-control-static col-sm-4" readonly="readonly">{{value}}</textarea>
            </div>
        </div>
    </div>
</div>`
})
export class ExploreEndpointComponent implements OnInit, OnDestroy {
    @Input() service: Service;
    @Input() endpoint: Endpoint;
    @Input() endpointIndex: Number;

    private value: String;
    private url: String = '';
    private streams: Stream[] = [];

    constructor(private diffusionService: DiffusionService) {}

    ngOnInit(): void {
        this.setUrl();

        this
            .diffusionService
            .get()
            .then(session => {
                this.streams.push(session
                    .stream(this.service.topicPathRoot + '/' + this.endpoint.topicPath)
                    .on('subscribe', spec => {
                        if (spec.type.id === diffusion.topics.TopicType.JSON.id) {
                            this.streams.push(session
                                .stream(this.service.topicPathRoot + '/' + this.endpoint.topicPath)
                                .asType(diffusion.datatypes.json())
                                .on('value', (path, spec, newValue, oldValue) => {
                                    this.value = JSON.stringify(newValue.get());
                                }));
                        }
                        else if (spec.type.id === diffusion.topics.TopicType.STRING.id) {
                            this.streams.push(session
                                .stream(this.service.topicPathRoot + '/' + this.endpoint.topicPath)
                                .asType(diffusion.datatypes.string())
                                .on('value', (path, spec, newValue) => {
                                    this.value = newValue;
                                }));
                        }
                        else if (spec.type.id === diffusion.topics.TopicType.BINARY.id) {
                            this.streams.push(session
                                .stream(this.service.topicPathRoot + '/' + this.endpoint.topicPath)
                                .asType(diffusion.datatypes.binary())
                                .on('value', (path, spec, newValue) => {
                                    this.value = newValue.get().toString('hex');
                                }));
                        }
                    }));
                session.subscribe(this.service.topicPathRoot + '/' + this.endpoint.topicPath);
            });
    }

    ngOnDestroy(): void {
        this.diffusionService.get().then(session => session.unsubscribe(this.service.topicPathRoot + '/' + this.endpoint.topicPath));
        this.streams.forEach(stream => stream.close());
    }

    private setUrl() {
        this.url = this.service.secure ? 'https://' : 'http://';
        this.url += this.service.host;
        this.url += this.service.secure && this.service.port !== 443 ? ':' + this.service.port : '';
        this.url += !this.service.secure && this.service.port !== 80 ? ':' + this.service.port : '';
        this.url += this.endpoint.url;
    }
}
