import { Injectable } from '@angular/core';
import { Model, Service } from './model';
import * as d from './diffusion.d.ts';
import { RequestContext } from './request-context';
const diffusion: d.Diffusion = require('diffusion');

@Injectable()
export class ModelService {
    private session: Promise<any>;
    private context: RequestContext;
    model: Model = {
        services: []
    };

    private init(): Promise<any> {
        if (!this.session) {
            this.session = new Promise((resolve, reject) => {
                diffusion.connect({
                    host: 'localhost',
                    port: 8080,
                    secure: false
                }).then((session) => {
                    console.log('Connected');
                    this.context = new RequestContext(session, 'adapter/rest/model/store');
                    resolve(session);
                }, (error) => {
                    console.log(error);
                    reject(error);
                });
            });
        }

        return this.session;
    }

    getModel(): Promise<Model> {
        return this.init()
            .then(() => this.context.request({
                type: 'list-services'
            }))
            .then((response) => {
                this.model.services = response
                return this.model;
            });
    }

    getService(name: string): Promise<Service> {
        return this.init()
            .then(() => this.getModel())
            .then((model) => Promise.resolve(model.services.find((element) => element.name === name)));
    }

    createService(service: Service): Promise<void> {
        return this.init()
            .then(() => this.context.request({
                type: 'create-service',
                service: service
            }))
            .then(() => {
                this.model.services.push(JSON.parse(JSON.stringify(service)));
            });
    }
}
