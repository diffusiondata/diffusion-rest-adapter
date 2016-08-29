import { Injectable } from '@angular/core';
import { Model, Service } from './model';
import * as diffusion from 'diffusion';

@Injectable()
export class ModelService {
    private session: any;

    private init(): Promise<any> {
        if (this.session) {
            return Promise.resolve(this.session);
        }

        console.log('Initialising');
        return diffusion.connect({
            host: 'localhost',
            port: 8080,
            secure: false
        }).then((session) => {
            console.log('Connected');
            this.session = session;
            return Promise.resolve(this.session);
        }, (error) => {
            console.log(error);
        });
    }

    getModel(): Promise<Model> {
        return this.init().then(function(session) {
            return Promise.resolve({
                services: [{
                    name: 'string',
                    host: 'string',
                    port: 80,
                    secure: false,
                    endpoints: [],
                    pollPeriod: 5000,
                    topicRoot: 'string',
                    security: null
                }]
            });
        });
    }

    getService(name: string): Promise<Service> {
        return this.init().then(function(session) {
            return Promise.resolve({
                name: 'string',
                host: 'string',
                port: 80,
                secure: false,
                endpoints: [],
                pollPeriod: 5000,
                topicRoot: 'string',
                security: null
            });
        });
    }

    createService(service: Service): Promise<void> {
        console.log(service);
        return Promise.resolve();
    }
}
