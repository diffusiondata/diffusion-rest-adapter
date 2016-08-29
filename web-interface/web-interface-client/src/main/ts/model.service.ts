import { Injectable } from '@angular/core';
import { Model, Service } from './model';
import * as diffusion from 'diffusion';

@Injectable()
export class ModelService {
    private session: any;
    model: Model = {
        services: []
    };

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
        let model = this.model;
        return this.init().then(function(session) {
            // TODO: Get the current model
            return Promise.resolve(model);
        });
    }

    getService(name: string): Promise<Service> {
        let model = this.model;
        return this.init().then(function(session) {
            return Promise.resolve(model.services.find(function(element) {
                return element.name === name;
            }));
        });
    }

    createService(service: Service): Promise<void> {
        let model = this.model;
        return this.init().then(function(session) {
            // TODO: Handle failure to add service
            model.services.push(service);
            return session.messages.send(
                'adapter/rest/model/store',
                diffusion.datatypes.json().from({
                    type: 'create-service',
                    service: service
                }));
        });
    }
}
